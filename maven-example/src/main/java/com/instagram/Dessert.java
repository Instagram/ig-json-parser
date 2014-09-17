// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
class Dessert {
  @JsonField(fieldName="type")
  String type;

  @JsonField(fieldName="rating")
  float rating;
}
