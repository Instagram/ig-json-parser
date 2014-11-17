// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import java.util.List;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * Object with lists that request an {@link JsonField.TypeMapping#EXACT} mapping.
 */
@JsonType
public class StrictListParseUUT {
  public static final String INTEGER_LIST_FIELD_NAME = "IntegerList";
  public static final String SUBOBJECT_LIST_FIELD_NAME = "SubobjectList";

  @JsonField(fieldName = INTEGER_LIST_FIELD_NAME, mapping = JsonField.TypeMapping.EXACT)
  public List<Integer> integerListField;

  @JsonField(fieldName = SUBOBJECT_LIST_FIELD_NAME, mapping = JsonField.TypeMapping.EXACT)
  public List<SubobjectParseUUT> subobjectListField;

  /**
   * UUT for embedding a subobject.
   */
  @JsonType
  public static class SubobjectParseUUT {
    public static final String INT_FIELD_NAME = "int";

    @JsonField(fieldName = INT_FIELD_NAME)
    public int intField;
  }
}
