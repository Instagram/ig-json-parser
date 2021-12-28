/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.text.StrSubstitutor;

/** Syntactic sugar wrapper for {@link StrSubstitutor}. */
class StrFormat {
  private final String mFormatString;
  private final Map<String, String> mInternalMap;
  private final Set<String> mSupportedTokens;

  private StrFormat(String formatString, Set<String> supportedTokens) {
    mFormatString = formatString;
    mInternalMap = new HashMap<>();
    mSupportedTokens = supportedTokens;
  }

  StrFormat addParam(String variableName, String replacementText) {
    if (mSupportedTokens.contains(variableName)) {
      mInternalMap.put(variableName, replacementText);
    }
    return this;
  }

  String format() {
    return StrSubstitutor.replace(mFormatString, mInternalMap);
  }

  static StrFormat createStringFormatter(CodeFormatter formatter) {
    return new StrFormat(formatter.getFormatterString(), formatter.getSupportedTokens());
  }
}
