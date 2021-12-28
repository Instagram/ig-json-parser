/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Simple UUT for testing the most basic functionality. The fields are serialized in the same order
 * that they are appearing in the class declaration. SerializeTest.FIELD_DECLARATION_ORDER should be
 * updated when adding any new field to this class.
 */
@JsonType
public class SimpleParseUUT {
  public static final String INT_FIELD_NAME = "int";
  public static final String INTEGER_FIELD_NAME = "Integer";
  public static final String FLOAT_FIELD_NAME = "float";
  public static final String FLOAT_OBJ_FIELD_NAME = "Float";
  public static final String STRING_FIELD_NAME = "String";
  public static final String INTEGER_LIST_FIELD_NAME = "IntegerList";
  public static final String INTEGER_ARRAY_LIST_FIELD_NAME = "IntegerArrayList";
  public static final String INTEGER_QUEUE_FIELD_NAME = "IntegerQueue";
  public static final String INTEGER_SET_FIELD_NAME = "IntegerSet";
  public static final String SUBOBJECT_FIELD_NAME = "Subobject";
  public static final String SUBENUM_FIELD_NAME = "Subenum";
  public static final String SUBENUM_LIST_FIELD_NAME = "SubenumList";

  @JsonField(fieldName = INT_FIELD_NAME)
  public int intField;

  @JsonField(fieldName = INTEGER_FIELD_NAME)
  public Integer integerField;

  @JsonField(fieldName = FLOAT_FIELD_NAME)
  public float floatField;

  @JsonField(fieldName = FLOAT_OBJ_FIELD_NAME)
  public Float FloatField;

  @JsonField(fieldName = STRING_FIELD_NAME, mapping = JsonField.TypeMapping.EXACT)
  public String stringField;

  @JsonField(fieldName = INTEGER_LIST_FIELD_NAME)
  public List<Integer> integerListField;

  @JsonField(fieldName = INTEGER_ARRAY_LIST_FIELD_NAME)
  public ArrayList<Integer> integerArrayListField;

  @JsonField(fieldName = INTEGER_QUEUE_FIELD_NAME)
  public Queue<Integer> integerQueueField;

  @JsonField(fieldName = INTEGER_SET_FIELD_NAME)
  public Set<Integer> integerSetField;

  @JsonField(fieldName = SUBOBJECT_FIELD_NAME)
  public SubobjectParseUUT subobjectField;

  @JsonField(
      fieldName = SUBENUM_FIELD_NAME,
      valueExtractFormatter = "SimpleParseUUT.SubenumUUT.valueOf(${parser_object}.getText())",
      serializeCodeFormatter =
          "${generator_object}.writeStringField(\"${json_fieldname}\", "
              + "${object_varname}.${field_varname}.toString())")
  public SubenumUUT subenumField;

  @JsonField(
      fieldName = SUBENUM_LIST_FIELD_NAME,
      valueExtractFormatter = "SimpleParseUUT.SubenumUUT.valueOf(${parser_object}.getText())",
      serializeCodeFormatter = "${generator_object}.writeString(element.toString())",
      fieldAssignmentFormatter =
          "${object_varname}.${field_varname} = "
              + "new ArrayList<SimpleParseUUT.SubenumUUT>(${extracted_value})")
  public List<SubenumUUT> subenumFieldList;

  /** UUT for embedding a subobject. */
  @JsonType
  public static class SubobjectParseUUT {
    public static final String INT_FIELD_NAME = "int";

    @JsonField(fieldName = INT_FIELD_NAME)
    public int intField;
  }

  /** UUT for embedding a subenum. */
  public enum SubenumUUT {
    A,
    B
  }
}
