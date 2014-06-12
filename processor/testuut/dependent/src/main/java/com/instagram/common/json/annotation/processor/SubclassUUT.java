// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.dependent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.ParentUUT;

/**
 * Subclasses {@link ParentUUT}.
 */
@JsonType
public class SubclassUUT extends ParentUUT {
  public static final String SUBCLASS_INT_KEY = "subclass_int";

  @JsonField(fieldName = SUBCLASS_INT_KEY)
  public int subclassInt;
}
