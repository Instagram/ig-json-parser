// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Syntactic sugar wrapper for {@link StrSubstitutor}.
 */
class StrFormat {
  private final String mFormatString;
  private final Map<String, String> mInternalMap;
  private final Set<String> mSupportedParameters;

  private StrFormat(String formatString, Set<String> supportedParameters) {
    mFormatString = formatString;
    mInternalMap = new HashMap<String, String>();
    mSupportedParameters = supportedParameters;
  }

  StrFormat addParam(String variableName, String replacementText) {
    if (mSupportedParameters.contains(variableName)) {
      mInternalMap.put(variableName, replacementText);
    }
    return this;
  }

  String format() {
    return StrSubstitutor.replace(mFormatString, mInternalMap);
  }

  static StrFormat createStringFormatter(CodeFormatter formatter) {
    return new StrFormat(
        formatter.getFormatterString(),
        formatter.getSupportedParameters());
  }
}
