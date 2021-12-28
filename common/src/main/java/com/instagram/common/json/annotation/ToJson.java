/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation on the static method that returns the json value for a given type.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @ToJson
 * public static String toJson(MyEnum myEnum) {
 *   return myEnum.getServerValue();
 * }
 * }</pre>
 *
 * For more details see {@link JsonAdapter}
 */
@Retention(CLASS)
@Target(METHOD)
public @interface ToJson {}
