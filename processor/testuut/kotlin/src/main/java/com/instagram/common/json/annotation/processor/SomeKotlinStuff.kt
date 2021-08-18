/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.instagram.common.json.annotation.JsonField
import com.instagram.common.json.annotation.JsonType

@JsonType
data class SomeKotlinStuff(
    @JsonField(fieldName = "data") var data: String = "",
    // 'is' prefix is a special case in kotlin, getters become isFun() instead of getIsFun() and
    // similar with setters
    @JsonField(fieldName = "fun") var isFun: Nested = Nested(),
    @JsonField(fieldName = "some_map") var someMap: HashMap<String, Int> = hashMapOf()
)

@JsonType
data class Nested(
    @JsonField(fieldName = "foo") var foo: Int? = null,
    @JsonField(fieldName = "bar") var bar: String? = null
)
