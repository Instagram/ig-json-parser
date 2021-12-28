/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class AlternateFieldUUT {

  public static final String FIELD_NAME = "tyler";
  public static final String ALTERNATE_FIELD_NAME_1 = "josh";
  public static final String ALTERNATE_FIELD_NAME_2 = "kang";

  @JsonField(
      fieldName = FIELD_NAME,
      alternateFieldNames = {ALTERNATE_FIELD_NAME_1, ALTERNATE_FIELD_NAME_2})
  String nameField;

  public String getNameField() {
    return nameField;
  }
}
