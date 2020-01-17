// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.util;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Annotation we apply to classes we want to run the test annotation processor on. */
@Retention(CLASS)
@Target(TYPE)
@interface MarkedTypes {}
