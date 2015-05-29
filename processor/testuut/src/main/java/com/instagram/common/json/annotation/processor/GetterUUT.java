// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * UUT for embedding a subobject with getter.
 */
@JsonType(useGetters = true)
public class GetterUUT {
  public static final String INT_FIELD_NAME = "int";

  @JsonField(fieldName = INT_FIELD_NAME)
  public int intField;

  public int getIntField() {
    return intField + 5;
  }
}