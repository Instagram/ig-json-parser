// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.app.ommodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * List of models for iterations > 1
 */
public class OmListOfModels {
  @JsonProperty("list")
  List<OmModelRequest> list;
}
