/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/** Simple UUT for testing the most basic functionality. */
@JsonType()
public class StringInterfaceUUT {
  public static final String INT_FIELD_NAME = "int";

  @JsonField(fieldName = INT_FIELD_NAME)
  public int intField;
}
