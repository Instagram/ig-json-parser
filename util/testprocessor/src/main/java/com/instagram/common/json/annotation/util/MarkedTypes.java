// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation we apply to classes we want to run the test annotation processor on.
 */
@Retention(CLASS) @Target(TYPE)
@interface MarkedTypes {
}
