/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import org.junit.Assert
import org.junit.Test

/**
 * Testing whether the [JsonAnnotationProcessor] generates valid Java code when using a
 * [JsonAdapter].
 */
class JsonAdapterTest {

  private val myEnumHolder = MyEnumHolder(myEnum = MyEnum.BAR)

  private val myEnumHolderJson =
      """
    {"my_enum":"bar"}
    """
          .trimIndent()

  @Test
  fun serializeEnumWithJsonAdapter() {
    val json = MyEnumHolder__JsonHelper.serializeToJson(myEnumHolder)

    Assert.assertEquals(myEnumHolderJson, json)
  }

  @Test
  fun parseEnumWithJsonAdapter() {
    val parsedMyEnumHolder = MyEnumHolder__JsonHelper.parseFromJson(myEnumHolderJson)

    Assert.assertEquals(myEnumHolder, parsedMyEnumHolder)
  }
}
