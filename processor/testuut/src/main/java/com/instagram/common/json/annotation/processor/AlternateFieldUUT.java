// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class AlternateFieldUUT {

  public static final String FIELD_NAME = "tyler";
  public static final String ALTERNATE_FIELD_NAME_1 = "josh";
  public static final String ALTERNATE_FIELD_NAME_2 = "kang";

  @JsonField(fieldName = FIELD_NAME,
      alternateFieldNames = {ALTERNATE_FIELD_NAME_1, ALTERNATE_FIELD_NAME_2})
  String nameField;

  public String getNameField() {
    return nameField;
  }
}
