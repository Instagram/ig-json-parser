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

/** Wraps {@link ParentUUT}. */
@JsonType
public class WrapperClassUUT {
  public static final String PARENT_KEY = "parent";

  @JsonField(fieldName = PARENT_KEY)
  public ParentUUT parent;
}
