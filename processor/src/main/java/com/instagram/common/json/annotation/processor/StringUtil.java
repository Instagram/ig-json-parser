// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

public class StringUtil {

  /**
   * We don't want to pull in guava strings just for this one function.
   */
  /*package*/ static boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }
}
