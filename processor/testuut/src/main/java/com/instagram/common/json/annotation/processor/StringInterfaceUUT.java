// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * Simple UUT for testing the most basic functionality.
 */
@JsonType()
public class StringInterfaceUUT {
  public static final String INT_FIELD_NAME = "int";

  @JsonField(fieldName = INT_FIELD_NAME)
  public int intField;
}
