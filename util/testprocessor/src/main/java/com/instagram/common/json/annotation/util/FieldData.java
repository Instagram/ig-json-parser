/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

/** Records the data we gathered about each field. */
class FieldData {

  boolean mIsList;
  TypeUtils.ParseType mParseType;
  String mParsableType;
  String mParsableTypeGeneratedClass;
}
