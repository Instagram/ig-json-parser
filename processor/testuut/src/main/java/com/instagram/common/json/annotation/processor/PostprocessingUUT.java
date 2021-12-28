/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/** UUT to test the optional post-processing code. */
@JsonType(postprocessingEnabled = true)
public class PostprocessingUUT {

  public static final String FIELD_NAME = "abcabc";

  @JsonField(fieldName = FIELD_NAME)
  int value;

  PostprocessingUUT postprocess() {
    value = value + 1;
    return this;
  }

  public int getValue() {
    return value;
  }
}
