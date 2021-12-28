/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.dependent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.TypeFormatterImportsUUT;

/** A container to exercise the callee imports tools */
@JsonType
public class TypeFormatterImportsContainerUUT {
  @JsonField(fieldName = "callee_ref")
  public TypeFormatterImportsUUT mTypeFormatterImports;
}
