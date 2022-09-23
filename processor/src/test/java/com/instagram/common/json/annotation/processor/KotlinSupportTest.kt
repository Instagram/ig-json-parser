/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testing whether the [JsonAnnotationProcessor] generates valid Java code when reading annotations
 * of Kotlin classes.
 */
class KotlinSupportTest {

  private val stuff =
      SomeKotlinStuff(
          data = "my data",
          isFun = Nested(foo = 1, bar = "my bar"),
          someMap = hashMapOf("first" to 1, "second" to 2))

  private val stuffJson =
      """
    {"data":"my data","fun":{"foo":1,"bar":"my bar"},"some_map":{"first":1,"second":2}}
    """
          .trimIndent()

  @Test
  fun handlesKotlinDataClassToJson() {
    val json = SomeKotlinStuff__JsonHelper.serializeToJson(stuff)

    assertEquals(stuffJson, json)
  }

  @Test
  fun handlesKotlinDataClassFromJson() {
    val actual = SomeKotlinStuff__JsonHelper.parseFromJson(stuffJson)

    assertEquals(stuff, actual)
  }

  @Test
  fun serializeThenParseBackIsSymmetric() {
    val json = SomeKotlinStuff__JsonHelper.serializeToJson(stuff)
    val obj = SomeKotlinStuff__JsonHelper.parseFromJson(json)

    assertEquals(stuff, obj)
  }
}
