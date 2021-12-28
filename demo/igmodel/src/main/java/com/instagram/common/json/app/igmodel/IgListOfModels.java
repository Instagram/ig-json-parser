/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.igmodel;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.List;

/** List of models for iterations > 1 */
@JsonType
public class IgListOfModels {
  @JsonField(fieldName = "list")
  List<IgModelRequest> list;
}
