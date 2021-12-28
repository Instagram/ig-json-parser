/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.instagram.common.json.annotation.JsonField
import com.instagram.common.json.annotation.JsonType
import com.instagram.common.json.annotation.processor.parent.DynamicDispatchAdapter

/** Used for testing collection of generics */
@JsonType
class WrapperAnimal {
  @JsonField(fieldName = "animals") lateinit var animals: List<Animal<*>>
}

@JsonType(
    valueExtractFormatter =
        "com.instagram.common.json.annotation.processor.WrapperWildcardHelper.Companion.getDISPATCHER().parseFromJson(\${parser_object})",
    serializeCodeFormatter =
        "com.instagram.common.json.annotation.processor.WrapperWildcardHelper.Companion.getDISPATCHER().serializeToJson(\${generator_object}, \${subobject})")
interface Animal<T> : DynamicDispatchAdapter.TypeNameProvider {
  var name: String

  fun getId(): String

  fun buildParams(name: String): T
}

interface DogParams {
  fun nameLength(): Int
}

@JsonType
class Dog : Animal<DogParams> {
  companion object {
    const val TYPE_NAME: String = "Dog"
  }

  @JsonField(fieldName = "name") override var name: String = "dog"

  override fun getId(): String {
    return Dog::class.simpleName + name
  }

  override fun buildParams(name: String): DogParams {
    return object : DogParams {
      override fun nameLength(): Int {
        return name.length
      }
    }
  }

  override fun getTypeName(): String {
    return TYPE_NAME
  }
}
