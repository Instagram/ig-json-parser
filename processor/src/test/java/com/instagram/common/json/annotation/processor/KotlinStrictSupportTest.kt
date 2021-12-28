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
 * of strict classes.
 */
class KotlinStrictSupportTest {

  private val model = KotlinImmutableObject("my data", 1)

  private val json = """{"data3":"bar","data1":"my data","data2":1}"""

  init {
    model.data3 = "bar"
  }

  @Test
  fun handlesKotlinImmutableClassToJson() {
    val actual = KotlinImmutableObject__JsonHelper.serializeToJson(model)

    assertEquals(json, actual)
  }

  @Test
  fun handlesKotlinImmutableClassFromJson() {
    val actual = KotlinImmutableObject__JsonHelper.parseFromJson(json)
    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.data3, actual.data3)
  }

  @Test
  fun serializeThenParseBackIsSymmetric() {
    val actualJson = KotlinImmutableObject__JsonHelper.serializeToJson(model)
    val actual = KotlinImmutableObject__JsonHelper.parseFromJson(actualJson)

    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.data3, actual.data3)
  }

  @Test
  fun serializeThenParseBackWithNullIsSymmetric() {
    model.data3 = null
    val actualJson = KotlinImmutableObject__JsonHelper.serializeToJson(model)
    val actual = KotlinImmutableObject__JsonHelper.parseFromJson(actualJson)

    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.data3, actual.data3)
  }
}
