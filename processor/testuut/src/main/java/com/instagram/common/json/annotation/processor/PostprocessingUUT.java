// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * UUT to test the optional post-processing code.
 */
@JsonType(postprocessingEnabled = true)
public class PostprocessingUUT {
  static final String FIELD_NAME = "abcabc";

  @JsonField(fieldName = FIELD_NAME)
  int value;

  PostprocessingUUT postprocess() {
    value = value + 1;
    return this;
  }
}
