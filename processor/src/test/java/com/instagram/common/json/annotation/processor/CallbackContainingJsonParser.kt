/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.util.JsonParserDelegate
import com.instagram.common.json.JsonCallback

class CallbackContainingJsonParser(jsonParser: JsonParser, val callback: JsonCallback) :
    JsonParserDelegate(jsonParser), JsonCallback.Provider {
  override fun getJsonCallback(): JsonCallback = callback
}
