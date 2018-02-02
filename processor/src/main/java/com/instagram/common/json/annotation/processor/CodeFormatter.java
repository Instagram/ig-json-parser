package com.instagram.common.json.annotation.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CodeFormatter {
  public static final Factory VALUE_EXTRACT = new Factory(
      "parser_object", "subobject_class", "subobject_helper_class");
  public static final Factory FIELD_ASSIGNMENT = new Factory(
      "object_varname", "field_varname", "extracted_value");
  public static final Factory FIELD_CODE_SERIALIZATION = new Factory(
      "generator_object", "object_varname", "field_varname", "iterator",
      "json_fieldname", "subobject_helper_class");
  public static final Factory CLASS_CODE_SERIALIZATION = new Factory(
      "generator_object", "subobject", "subobject_helper_class");
  public static final Factory INTERFACE_CODE_SERIALIZATION = new Factory(
      "generator_object", "subobject");

  private final String mFormatterString;

  private final Set<String> mSupportedTokens;

  private CodeFormatter(String formatterString, Set<String> supportedTokens) {
    mFormatterString = formatterString;
    mSupportedTokens = supportedTokens;
  }

  public boolean isEmpty() {
    return StringUtil.isNullOrEmpty(mFormatterString);
  }

  public String getFormatterString() {
    return mFormatterString;
  }

  public CodeFormatter orIfEmpty(CodeFormatter defaultFormatter) {
    if (isEmpty()) {
      return defaultFormatter;
    } else {
      return this;
    }
  }

  public Set<String> getSupportedTokens() {
    return mSupportedTokens;
  }

  public static class Factory {
    private Set<String> mSupportedTokens;

    private Factory(String... supportedTokens) {
      if (supportedTokens != null && supportedTokens.length > 0) {
        mSupportedTokens = new HashSet<>(Arrays.asList(supportedTokens));
      } else {
        mSupportedTokens = Collections.emptySet();
      }

    }

    public CodeFormatter forString(String formatterString) {
      return new CodeFormatter(formatterString, mSupportedTokens);
    }
  }
}
