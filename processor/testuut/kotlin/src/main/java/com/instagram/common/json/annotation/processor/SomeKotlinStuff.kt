/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.instagram.common.json.annotation.JsonField
import com.instagram.common.json.annotation.JsonType

@JsonType
data class SomeKotlinStuff(
    @JsonField(fieldName = "data") var data: String = "",
    // 'is' prefix is a special case in kotlin, getters become isFun() instead of getIsFun() and
    // similar with setters
    @JsonField(fieldName = "fun") var isFun: Nested = Nested(),
    @JsonField(fieldName = "some_map") var someMap: HashMap<String, Int> = hashMapOf(),
    @JsonField(
        fieldName = "not_parseable",
        valueExtractFormatter =
            "com.instagram.common.json.annotation.processor.NotParseable.deserializeNotParseable(\${parser_object})",
        serializeCodeFormatter =
            "com.instagram.common.json.annotation.processor.NotParseable.serializeNotParseable(" +
                "\${generator_object}, \"\${json_fieldname}\", \${object_varname}.\${field_varname})")
    var notParseable: NotParseable = NotParseable(0),
)

@JsonType
data class Nested(
    @JsonField(fieldName = "foo") var foo: Int? = null,
    @JsonField(fieldName = "bar") var bar: String? = null
)

data class NotParseable(var foo: Int) {
  companion object {
    @JvmStatic
    fun deserializeNotParseable(jp: JsonParser): NotParseable {
      val foo: Int = jp.getIntValue()
      jp.nextToken()
      return NotParseable(foo)
    }

    @JvmStatic
    fun serializeNotParseable(jgen: JsonGenerator, fieldName: String, value: NotParseable) {
      jgen.writeNumberField(fieldName, value.foo)
    }
  }
}
