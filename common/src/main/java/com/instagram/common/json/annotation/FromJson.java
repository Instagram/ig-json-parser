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
 * Annotation on the static method that takes the json field value (string, number or boolean) and
 * returns the type.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @FromJson
 * public static MyEnum fromJson(String serverValue) {
 *   for (MyEnum myEnum : MyEnum.values()) {
 *     if (myEnum.getServerValue().equals(serverValue)) {
 *       return myEnum;
 *     }
 *   }
 *   return MyEnum.NONE;
 * }
 * }</pre>
 *
 * For more details see {@link JsonAdapter}
 */
@Retention(CLASS)
@Target(METHOD)
public @interface FromJson {}
