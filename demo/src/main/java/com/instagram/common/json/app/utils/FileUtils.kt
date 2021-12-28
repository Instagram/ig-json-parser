/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.utils

import android.content.Context

fun Context.rawResourceAsString(resourceId: Int): String {
  return resources.openRawResource(resourceId).use { inputStream ->
    inputStream.readBytes().toString(Charsets.UTF_8)
  }
}
