// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.util.TypeUtils;

/**
 * Represents the data needed to serialize and deserialize a field.
 */
class TypeData {
  private String mFieldName;
  private JsonField.TypeMapping mMapping;
  private String mValueExtractFormatter;
  private String mAssignmentFormatter;
  private String mSerializeCodeFormatter;
  private boolean mInCollection;
  private TypeUtils.ParseType mParseType;
  private String mParsableType;
  private String mParsableTypeParserClass;

  String getFieldName() {
    return mFieldName;
  }

  void setFieldName(String fieldName) {
    this.mFieldName = fieldName;
  }

  JsonField.TypeMapping getMapping() {
    return mMapping;
  }

  void setMapping(JsonField.TypeMapping mapping) {
    this.mMapping = mapping;
  }

  public String getValueExtractFormatter() {
    return mValueExtractFormatter;
  }

  public void setValueExtractFormatter(String valueExtractFormatter) {
    mValueExtractFormatter = valueExtractFormatter;
  }

  public String getAssignmentFormatter() {
    return mAssignmentFormatter;
  }

  public void setAssignmentFormatter(String assignmentFormatter) {
    mAssignmentFormatter = assignmentFormatter;
  }

  public String getSerializeCodeFormatter() {
    return mSerializeCodeFormatter;
  }

  public void setSerializeCodeFormatter(String serializeCodeFormatter) {
    mSerializeCodeFormatter = serializeCodeFormatter;
  }

  boolean isInCollection() {
    return mInCollection;
  }

  void setInCollection(boolean inCollection) {
    mInCollection = inCollection;
  }

  TypeUtils.ParseType getParseType() {
    return mParseType;
  }

  void setParseType(TypeUtils.ParseType parseType) {
    mParseType = parseType;
  }

  String getParsableType() {
    return mParsableType;
  }

  void setParsableType(String parsableType) {
    mParsableType = parsableType;
  }

  String getParsableTypeParserClass() {
    return mParsableTypeParserClass;
  }

  void setParsableTypeParserClass(String parsableTypeParserClass) {
    mParsableTypeParserClass = parsableTypeParserClass;
  }
}
