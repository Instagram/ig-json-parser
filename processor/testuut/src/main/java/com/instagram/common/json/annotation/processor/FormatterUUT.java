// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * UUT for testing the overridden formatters.  See {@link JsonField#valueExtractFormatter()} and
 * {@link JsonField#fieldAssignmentFormatter()}.
 */
@JsonType
public class FormatterUUT {

  public static final String VALUE_FORMATTER_FIELD_NAME = "value_formatter";
  public static final String FIELD_ASSIGNMENT_FIELD_NAME = "field_assignment";

  @JsonField(fieldName = VALUE_FORMATTER_FIELD_NAME,
      valueExtractFormatter = "${parser_object}.getIntValue() + 15")
  int valueFormatter;

  @JsonField(fieldName = FIELD_ASSIGNMENT_FIELD_NAME,
      fieldAssignmentFormatter = "${object_varname}.${field_varname} = -${extracted_value}")
  int fieldAssignmentFormatter;

  public int getValueFormatter() {
    return valueFormatter;
  }

  public int getFieldAssignmentFormatter() {
    return fieldAssignmentFormatter;
  }
}
