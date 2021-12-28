/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.instagram.common.json.annotation.JsonField
import com.instagram.common.json.annotation.JsonType

@JsonType(strict = true)
data class KotlinImmutableObject(
    @JsonField(fieldName = "data1") val data1: String,
    @JsonField(fieldName = "data2") val data2: Int
) {
  @JsonField(fieldName = "data3") var data3: String? = null
}
