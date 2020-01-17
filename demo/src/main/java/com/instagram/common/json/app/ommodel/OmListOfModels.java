// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.app.ommodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** List of models for iterations > 1 */
public class OmListOfModels {
  @JsonProperty("list")
  List<OmModelRequest> list;
}
