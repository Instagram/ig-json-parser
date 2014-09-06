// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Syntactic sugar wrapper for {@link StrSubstitutor}.
 */
class StrFormat {
  private String mFormatString;
  private Map<String, String> mInternalMap;

  StrFormat(String formatString) {
    mFormatString = formatString;
    mInternalMap = new HashMap<String, String>();
  }

  StrFormat addParam(String variableName, String replacementText) {
    mInternalMap.put(variableName, replacementText);
    return this;
  }

  String format() {
    return StrSubstitutor.replace(mFormatString, mInternalMap);
  }

  static StrFormat createStringFormatter(String formatString) {
    return new StrFormat(formatString);
  }
}
