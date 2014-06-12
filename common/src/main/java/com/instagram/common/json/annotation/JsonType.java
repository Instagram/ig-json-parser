// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * This annotation is applied to any class for which a json parser should be automatically
 * generated.
 */
@Retention(CLASS) @Target(TYPE)
public @interface JsonType {
  public static final String POSTPROCESSING_METHOD_NAME = "postprocess";

  /**
   * This annotation specifies that a method with the name specified by
   * {@link #POSTPROCESSING_METHOD_NAME} (currently "postprocess") on the class that is being
   * generated that should be called once parsing is finished.
   */
  boolean postprocessingEnabled() default false;
}
