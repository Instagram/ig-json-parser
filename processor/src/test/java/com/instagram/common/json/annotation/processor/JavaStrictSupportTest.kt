/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.instagram.common.json.JsonCallback
import com.instagram.common.json.JsonFactoryHolder
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * Testing whether the [JsonAnnotationProcessor] generates valid Java code when reading annotations
 * of strict classes.
 */
class JavaStrictSupportTest {

  class TestCallback : JsonCallback {
    var counter = 0
    var fieldName: String? = null
    var className: String? = null
    override fun onUnexpectedNull(fld: String, cls: String) {
      counter += 1
      fieldName = fld
      className = cls
    }
  }

  @JvmField @Rule var expectedException = ExpectedException.none()

  private val model = ImmutableObject("my data", 2)

  private val callback = TestCallback()

  init {
    val data3 = mutableListOf<Int>()
    data3.add(3)
    model.mData3 = data3
  }

  @Test
  fun handlesJavaImmutableClassToJson() {
    val actual = ImmutableObject__JsonHelper.serializeToJson(model)
    val json = """{"data3":[3],"data4":1234,"data1":"my data","data2":2}"""
    assertEquals(json, actual)
  }

  @Test
  fun handlesJavaImmutableClassFromJson() {
    val json = """{"data3":[3],"data1":"my data","data2":2}"""
    val actual = ImmutableObject__JsonHelper.parseFromJson(json)
    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.mData3, actual.mData3)
    assertEquals(ImmutableObject.DATA_4_DEFAULT, actual.mData4)
  }

  @Test
  fun serializeThenParseBackIsSymmetric() {
    val actualJson = ImmutableObject__JsonHelper.serializeToJson(model)
    val actual = ImmutableObject__JsonHelper.parseFromJson(actualJson)

    assertEquals(model.data1, actual.data1)
    assertEquals(model.data2, actual.data2)
    assertEquals(model.mData3, actual.mData3)
  }

  @Test
  fun deserializeNullableFieldHasNoLogs() {
    // data2 is the only non null required field
    val json = """{"data2":2}"""
    val jp =
        CallbackContainingJsonParser(JsonFactoryHolder.APP_FACTORY.createParser(json), callback)
    // jackson needs to be advanced to the first token before parsing
    jp.nextToken()
    ImmutableObject__JsonHelper.parseFromJson(jp)

    assertEquals(callback.counter, 0)
  }

  @Test
  fun deserializeNonNullableFieldHasLogs() {
    val json = """{"data3":[3],"data1":"my data"}"""
    val jp =
        CallbackContainingJsonParser(JsonFactoryHolder.APP_FACTORY.createParser(json), callback)
    // jackson needs to be advanced to the first token before parsing
    jp.nextToken()
    ImmutableObject__JsonHelper.parseFromJson(jp)

    assertEquals(1, callback.counter)
    assertEquals("data2", callback.fieldName)
    assertEquals("ImmutableObject", callback.className)
  }

  @Test
  fun deserializeNonNullableFieldThrowsException() {
    val json = """{"data3":[3],"data1":"my data"}"""
    expectedException.expect(JsonCallback.JsonDeserializationException::class.java)
    val jp =
        CallbackContainingJsonParser(
            JsonFactoryHolder.APP_FACTORY.createParser(json),
            object : JsonCallback {
              override fun onUnexpectedNull(fld: String, cls: String) {
                throw JsonCallback.JsonDeserializationException()
              }
            })
    // jackson needs to be advanced to the first token before parsing
    jp.nextToken()
    ImmutableObject__JsonHelper.parseFromJson(jp)
  }
}
