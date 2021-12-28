/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/** UUT for embedding a subobject with getter. */
@JsonType(useGetters = true)
public class GetterUUT {
  public static final String INT_FIELD_NAME = "int";

  @JsonField(fieldName = INT_FIELD_NAME)
  public int intField;

  public int getIntField() {
    return intField + 5;
  }
}
