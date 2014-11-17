// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import static com.instagram.common.json.annotation.JsonField.TypeMapping.*;

/**
 * UUT for testing the {@link JsonField.TypeMapping#EXACT} mapping.
 */
@JsonType
public class ExactMappingUUT {
  public static final String BOOLEAN_FIELD_NAME = "boolean";
  public static final String BOOLEAN_OBJ_FIELD_NAME = "Boolean";
  public static final String INT_FIELD_NAME = "int";
  public static final String INTEGER_FIELD_NAME = "Integer";
  public static final String LONG_FIELD_NAME = "long";
  public static final String LONG_OBJ_FIELD_NAME = "Long";
  public static final String FLOAT_FIELD_NAME = "float";
  public static final String FLOAT_OBJ_FIELD_NAME = "Float";
  public static final String DOUBLE_FIELD_NAME = "double";
  public static final String DOUBLE_OBJ_FIELD_NAME = "Double";
  public static final String STRING_FIELD_NAME = "String";

  @JsonField(fieldName = BOOLEAN_FIELD_NAME, mapping = EXACT)
  public boolean booleanField;

  @JsonField(fieldName = BOOLEAN_OBJ_FIELD_NAME, mapping = EXACT)
  public Boolean BooleanField;

  @JsonField(fieldName = INT_FIELD_NAME, mapping = EXACT)
  public int intField;

  @JsonField(fieldName = INTEGER_FIELD_NAME, mapping = EXACT)
  public Integer IntegerField;

  @JsonField(fieldName = LONG_FIELD_NAME, mapping = EXACT)
  public long longField;

  @JsonField(fieldName = LONG_OBJ_FIELD_NAME, mapping = EXACT)
  public Long LongField;

  @JsonField(fieldName = FLOAT_FIELD_NAME, mapping = EXACT)
  public float floatField;

  @JsonField(fieldName = FLOAT_OBJ_FIELD_NAME, mapping = EXACT)
  public Float FloatField;

  @JsonField(fieldName = DOUBLE_FIELD_NAME, mapping = EXACT)
  public double doubleField;

  @JsonField(fieldName = DOUBLE_OBJ_FIELD_NAME, mapping = EXACT)
  public Double DoubleField;

  @JsonField(fieldName = STRING_FIELD_NAME, mapping = EXACT)
  public String StringField;
}
