/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.instagram.common.json.annotation.processor.parent.DynamicDispatchAdapter
import java.io.IOException

/** Helper for [WrapperAnimal] */
class WrapperWildcardHelper {
  companion object {
    val DISPATCHER = DynamicDispatchAdapter<Animal<*>>()
    fun registerJsonTypes() {
      DISPATCHER.register(
          Dog.TYPE_NAME,
          object : DynamicDispatchAdapter.TypeAdapter<Animal<*>?> {
            @Throws(IOException::class)
            override fun parseFromJson(parser: JsonParser): Animal<*> {
              return Dog__JsonHelper.parseFromJson(parser)
            }

            @Throws(IOException::class)
            override fun serializeToJson(generator: JsonGenerator?, obj: Animal<*>?) {
              Dog__JsonHelper.serializeToJson(generator, obj as Dog, true)
            }
          })
    }
  }
}
