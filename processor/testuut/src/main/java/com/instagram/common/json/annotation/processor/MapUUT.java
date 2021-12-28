/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import static com.instagram.common.json.annotation.JsonField.TypeMapping.EXACT;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.HashMap;

/** UUT with different map types */
@JsonType
public class MapUUT {

  public static final String STRING_INTEGER_MAP_FIELD_NAME = "StringIntegerMap";
  public static final String STRING_STRING_MAP_FIELD_NAME = "StringStringMap";
  public static final String STRING_LONG_MAP_FIELD_NAME = "StringLongMap";
  public static final String STRING_DOUBLE_MAP_FIELD_NAME = "StringDoubleMap";
  public static final String STRING_FLOAT_MAP_FIELD_NAME = "StringFloatMap";
  public static final String STRING_BOOLEAN_MAP_FIELD_NAME = "StringBooleanMap";
  public static final String STRING_OBJECT_MAP_FIELD_NAME = "StringObjectMap";

  @JsonField(fieldName = STRING_INTEGER_MAP_FIELD_NAME, mapping = EXACT)
  public HashMap<String, Integer> stringIntegerMapField;

  @JsonField(fieldName = STRING_STRING_MAP_FIELD_NAME)
  public HashMap<String, String> stringStringMapField;

  @JsonField(fieldName = STRING_LONG_MAP_FIELD_NAME)
  public HashMap<String, Long> stringLongMapField;

  @JsonField(fieldName = STRING_DOUBLE_MAP_FIELD_NAME)
  public HashMap<String, Double> stringDoubleMapField;

  @JsonField(fieldName = STRING_FLOAT_MAP_FIELD_NAME)
  public HashMap<String, Float> stringFloatMapField;

  @JsonField(fieldName = STRING_BOOLEAN_MAP_FIELD_NAME)
  public HashMap<String, Boolean> stringBooleanMapField;

  @JsonField(fieldName = STRING_OBJECT_MAP_FIELD_NAME)
  public HashMap<String, MapObject> stringObjectMapField;

  @JsonType
  public static class MapObject {

    public static final String INT_KEY = "int_key";

    @JsonField(fieldName = INT_KEY)
    public int subclassInt;

    @Override
    public int hashCode() {
      return subclassInt;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MapObject mapObject = (MapObject) o;

      if (subclassInt != mapObject.subclassInt) return false;

      return true;
    }
  }
}
