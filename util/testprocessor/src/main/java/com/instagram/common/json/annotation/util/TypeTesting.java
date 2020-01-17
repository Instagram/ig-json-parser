// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Applied to each field we want the annotation processor to understand. */
@Retention(CLASS)
@Target(FIELD)
@interface TypeTesting {}
