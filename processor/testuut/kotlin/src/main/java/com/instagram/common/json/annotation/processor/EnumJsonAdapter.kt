/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.instagram.common.json.annotation.FromJson
import com.instagram.common.json.annotation.JsonAdapter
import com.instagram.common.json.annotation.JsonField
import com.instagram.common.json.annotation.JsonType
import com.instagram.common.json.annotation.ToJson

@JsonType
data class MyEnumHolder(@JsonField(fieldName = "my_enum") var myEnum: MyEnum = MyEnum.NONE)

@JsonAdapter(adapterClass = MyEnumAdapter::class)
enum class MyEnum(val serverValue: String) {
  FOO("foo"),
  BAR("bar"),
  NONE("")
}

object MyEnumAdapter {

  private val reverseMap = MyEnum.values().associateBy(MyEnum::serverValue)

  @JvmStatic
  @FromJson
  fun fromJson(serverValue: String?): MyEnum = reverseMap[serverValue] ?: MyEnum.NONE

  @JvmStatic @ToJson fun toJson(value: MyEnum): String? = value.serverValue
}
