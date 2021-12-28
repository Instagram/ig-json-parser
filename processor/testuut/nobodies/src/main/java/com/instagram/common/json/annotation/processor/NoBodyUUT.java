/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.nobodies;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/** This class has method body generation turned off. */
@JsonType
public class NoBodyUUT {

  @JsonField(fieldName = "value")
  public String mValue;
}
