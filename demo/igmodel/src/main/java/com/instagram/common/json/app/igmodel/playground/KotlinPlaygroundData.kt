/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.igmodel.playground

import com.instagram.common.json.annotation.JsonField
import com.instagram.common.json.annotation.JsonType

@JsonType
data class KotlinIsFun(
    @JsonField(fieldName = "data") var data: String = "",
    @JsonField(fieldName = "fun") var isFun: Nested = Nested()
)

@JsonType
data class Nested(
    @JsonField(fieldName = "foo") var foo: String? = null,
    @JsonField(fieldName = "bar") var bar: String? = null
)
