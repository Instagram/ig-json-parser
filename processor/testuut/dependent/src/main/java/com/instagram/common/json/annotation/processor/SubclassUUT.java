/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.dependent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.ParentUUT;

/** Subclasses {@link ParentUUT}. */
@JsonType
public class SubclassUUT extends ParentUUT {
  public static final String SUBCLASS_INT_KEY = "subclass_int";

  @JsonField(fieldName = SUBCLASS_INT_KEY)
  public int subclassInt;
}
