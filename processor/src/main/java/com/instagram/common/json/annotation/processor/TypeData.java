/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.util.TypeUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Represents the data needed to serialize and deserialize a field. These roughly correspond to the
 * attributes of the JsonField annotation.
 */
class TypeData {

  enum DeserializeType {
    NONE,
    FIELD,
    PARAM,
    SETTER,
  }

  enum SerializeType {
    NONE,
    FIELD,
    GETTER,
  }

  /** {@link JsonField#fieldName()} */
  private String mFieldName;

  /** How to populate the data of a deserialized field. */
  private DeserializeType mDeserializeType = DeserializeType.NONE;

  private SerializeType mSerializeType = SerializeType.NONE;

  /** {@link JsonField#alternateFieldNames()} */
  private String[] mAlternateFieldNames;

  /** {@link JsonField#mapping()} */
  private JsonField.TypeMapping mMapping;

  /** {@link JsonField#valueExtractFormatter()} */
  private CodeFormatter mValueExtractFormatter;

  /** {@link JsonField#fieldAssignmentFormatter()} */
  private CodeFormatter mAssignmentFormatter;

  /** {@link JsonField#serializeCodeFormatter()} */
  private CodeFormatter mSerializeCodeFormatter;

  /**
   * The collection type of the field, if the field is a collection, otherwise it is set to {@link
   * TypeUtils.CollectionType#NOT_A_COLLECTION}
   */
  private TypeUtils.CollectionType mCollectionType;

  /** {@link JsonType#typeFormatterImports()} (for parseable object types) */
  @Nullable private List<String> mFormatterImports;

  /**
   * The parse type of the field. This is either the
   *
   * <p>1) the generic type if the field is a collection<br>
   * 2) an internal type that we know how to parse<br>
   * 3) parsable object, if it refers to an object that is annotated with {@link JsonType}
   */
  private TypeUtils.ParseType mParseType;

  /** If this is a parsable object, the package name for this field's class. */
  private String mPackageName;

  /** If this is a parsable object, the name of this field's class. */
  private String mParsableType;

  /** If this is an enum, the name of this field's enum. */
  private String mEnumType;

  /** If this is a parsable object, the name of this field's parser class. */
  private String mParsableTypeParserClass;

  private boolean mIsInterface;

  private boolean mIsWildcard;

  /** The parse type of the json adapter. */
  private TypeUtils.ParseType mJsonAdapterParseType;

  /** The name of the method which converts from json to the adapter type. */
  private String mJsonAdapterFromJsonMethod;

  /** The name of the method which converts to json from the adapter type. */
  private String mJsonAdapterToJsonMethod;

  private String mMemberVariableName;
  private Integer mConstructorArgIndex;
  private String mSetterName;
  private String mGetterName;

  private boolean mIsNullable;

  private int mFieldIndex;

  DeserializeType getDeserializeType() {
    return mDeserializeType;
  }

  void setDeserializeType(DeserializeType deserializeType) {
    this.mDeserializeType = deserializeType;
  }

  SerializeType getSerializeType() {
    return mSerializeType;
  }

  void setSerializeType(SerializeType serializeType) {
    this.mSerializeType = serializeType;
  }

  String getFieldName() {
    return mFieldName;
  }

  void setFieldName(String fieldName) {
    this.mFieldName = fieldName;
  }

  int getFieldIndex() {
    return mFieldIndex;
  }

  void setFieldIndex(int fieldIndex) {
    this.mFieldIndex = fieldIndex;
  }

  String getMemberVariableName() {
    return mMemberVariableName;
  }

  void setMemberVariableName(String memberVariableName) {
    this.mMemberVariableName = memberVariableName;
  }

  String getSetterName() {
    return mSetterName;
  }

  void setSetterName(String setterName) {
    this.mSetterName = setterName;
  }

  String getGetterName() {
    return mGetterName;
  }

  void setGetterName(String getterName) {
    this.mGetterName = getterName;
  }

  Integer getConstructorArgIndex() {
    return mConstructorArgIndex;
  }

  void setConstructorArgIndex(Integer constructorArgIndex) {
    this.mConstructorArgIndex = constructorArgIndex;
  }

  public String[] getAlternateFieldNames() {
    return mAlternateFieldNames;
  }

  public void setAlternateFieldNames(String[] alternateFieldNames) {
    mAlternateFieldNames = alternateFieldNames;
  }

  JsonField.TypeMapping getMapping() {
    return mMapping;
  }

  void setMapping(JsonField.TypeMapping mapping) {
    this.mMapping = mapping;
  }

  public CodeFormatter getValueExtractFormatter() {
    return mValueExtractFormatter;
  }

  public void setValueExtractFormatter(CodeFormatter codeFormatter) {
    mValueExtractFormatter = codeFormatter;
  }

  public CodeFormatter getAssignmentFormatter() {
    return mAssignmentFormatter;
  }

  public void setAssignmentFormatter(CodeFormatter assignmentFormatter) {
    mAssignmentFormatter = assignmentFormatter;
  }

  public CodeFormatter getSerializeCodeFormatter() {
    return mSerializeCodeFormatter;
  }

  public void setSerializeCodeFormatter(CodeFormatter serializeCodeFormatter) {
    mSerializeCodeFormatter = serializeCodeFormatter;
  }

  TypeUtils.CollectionType getCollectionType() {
    return mCollectionType;
  }

  void setCollectionType(TypeUtils.CollectionType collectionType) {
    mCollectionType = collectionType;
  }

  TypeUtils.ParseType getParseType() {
    return mParseType;
  }

  void setParseType(TypeUtils.ParseType parseType) {
    mParseType = parseType;
  }

  public String getPackageName() {
    return mPackageName;
  }

  public void setPackageName(String packageName) {
    mPackageName = packageName;
  }

  public TypeUtils.ParseType getJsonAdapterOrParseType() {
    if (getJsonAdapterParseType() != null) {
      return getJsonAdapterParseType();
    }
    return getParseType();
  }

  public TypeUtils.ParseType getJsonAdapterParseType() {
    return mJsonAdapterParseType;
  }

  public void setJsonAdapterParseType(TypeUtils.ParseType jsonAdapterParseType) {
    this.mJsonAdapterParseType = jsonAdapterParseType;
  }

  public String getJsonAdapterFromJsonMethod() {
    return mJsonAdapterFromJsonMethod;
  }

  public void setJsonAdapterFromJsonMethod(String jsonAdapterFromJsonMethod) {
    this.mJsonAdapterFromJsonMethod = jsonAdapterFromJsonMethod;
  }

  public String getJsonAdapterToJsonMethod() {
    return mJsonAdapterToJsonMethod;
  }

  public void setJsonAdapterToJsonMethod(String jsonAdapterToJsonMethod) {
    this.mJsonAdapterToJsonMethod = jsonAdapterToJsonMethod;
  }

  String getParsableType() {
    return mParsableType;
  }

  void setParsableType(String parsableType) {
    mParsableType = parsableType;
  }

  /**
   * This is the name of the helper parser class, without the suffix applied. Some parsable types do
   * not generate helper classes at all (interfaces), so this value can be null.
   *
   * @return
   */
  @Nullable
  String getParsableTypeParserClass() {
    return mParsableTypeParserClass;
  }

  void setParsableTypeParserClass(String parsableTypeParserClass) {
    mParsableTypeParserClass = parsableTypeParserClass;
  }

  String getEnumType() {
    return mEnumType;
  }

  void setEnumType(String enumType) {
    mEnumType = enumType;
  }

  public boolean needsImportFrom(String packageName) {
    return (getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT
            || getParseType() == TypeUtils.ParseType.UNSUPPORTED)
        && getPackageName() != null
        && !getPackageName().equals(packageName);
  }

  public boolean hasParserHelperClass() {
    return !StringUtil.isNullOrEmpty(getParsableTypeParserClass());
  }

  public void setIsInterface(boolean isInterface) {
    mIsInterface = isInterface;
  }

  public boolean isInterface() {
    return mIsInterface;
  }

  void setFormatterImports(String[] formatterImports) {
    mFormatterImports = Arrays.asList(formatterImports);
  }

  List<String> getFormatterImports() {
    return mFormatterImports == null ? Collections.<String>emptyList() : mFormatterImports;
  }

  public boolean isWildcard() {
    return mIsWildcard;
  }

  public void setIsWildcard(boolean wildcard) {
    mIsWildcard = wildcard;
  }

  public boolean isNullable() {
    return mIsNullable;
  }

  public void setIsNullable(boolean nullable) {
    mIsNullable = nullable;
  }
}
