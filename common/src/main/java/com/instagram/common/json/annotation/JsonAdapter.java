/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used as an annotation on enums which requires custom serialization/deserialization logic.
 *
 * <p>Annotate the class which has the {@link FromJson} and {@link ToJson} annotated static methods.
 * {@link FromJson} is mandatory, while {@link ToJson} is only required you have also enabled
 * serialization.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @JsonAdapter(adapterClass = MyEnumJsonAdapter.class)
 * public enum MyEnum {
 *   ONE("one"),
 *   TWO("two"),
 *   NONE("");
 *
 *   private String mServerValue;
 *
 *   MyEnum(String serverValue) {
 *     mServerValue = serverValue;
 *   }
 *
 *   public String getServerValue() {
 *     return mServerValue;
 *   }
 * }
 *
 * public class MyEnumJsonAdapter {
 *   @FromJson
 *   public static MyEnum fromJson(String serverValue) {
 *     for (MyEnum myEnum : MyEnum.values()) {
 *       if (myEnum.getServerValue().equals(serverValue)) {
 *         return myEnum;
 *       }
 *     }
 *     return NONE;
 *   }
 *
 *   @ToJson
 *   public static String toJson(MyEnum myEnum) {
 *     return myEnum.getServerValue();
 *   }
 * }
 * }</pre>
 *
 * The return type of the {@link FromJson} method should be the same as the parameter for the {@link
 * ToJson} method. Similarly the return type of the {@link ToJson} method should be the same as the
 * parameter for the {@link FromJson} method and is the type from the original json. E.g. if the
 * json field you are parsing to an enum is a json string, use String. If it is a json number, use a
 * number (Integer, int, Long, long etc.). Same goes for boolean.
 *
 * <p>Note: This annotation is currently only working on enums, as it is recommended to create
 * classes for nested structures which saves you from having to create an adapter entirely! Feel
 * free to put up a diff or proposal to broaden this if you discover other valid use cases.
 */
@Retention(CLASS)
@Target(TYPE)
public @interface JsonAdapter {
  /**
   * The class which contains the {@link FromJson} and {@link ToJson} static methods.
   *
   * <p>If the enum contains those static methods itself, you can just point it directly to the enum
   * class itself. However, it might not be a bad idea to separate serialization logic to another
   * class to keep the separation of concerns clear.
   */
  Class<?> adapterClass();
}
