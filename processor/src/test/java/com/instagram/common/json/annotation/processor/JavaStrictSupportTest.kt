/*
 * Copyright (c) Facebook, Inc. and its affiliates.
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
class JavaStrictSupportTest {

  private val model = ImmutableObject("my data", 2)

  private val json = """{"data3":[3],"data1":"my data","data2":2}"""

  init {
    val data3 = mutableListOf<Int>()
    data3.add(3)
    model.mData3 = data3
  }

  @Test
  fun handlesJavaImmutableClassToJson() {
    val actual = ImmutableObject__JsonHelper.serializeToJson(model)

    assertEquals(json, actual)
  }

  @Test
  fun handlesJavaImmutableClassFromJson() {
    val actual = ImmutableObject__JsonHelper.parseFromJson(json)
    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.mData3, actual.mData3)
  }

  @Test
  fun serializeThenParseBackIsSymmetric() {
    val actualJson = ImmutableObject__JsonHelper.serializeToJson(model)
    val actual = ImmutableObject__JsonHelper.parseFromJson(actualJson)

    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.mData3, actual.mData3)
  }
}
