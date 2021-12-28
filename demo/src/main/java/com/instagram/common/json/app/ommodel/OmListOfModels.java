/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.ommodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** List of models for iterations > 1 */
public class OmListOfModels {
  @JsonProperty("list")
  List<OmModelRequest> list;
}
