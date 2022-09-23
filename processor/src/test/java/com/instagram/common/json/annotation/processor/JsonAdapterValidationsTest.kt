/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import javax.tools.JavaFileObject
import org.junit.Test

/** Testing [AnnotationMirrorUtils] */
class JsonAdapterValidationsTest {

  private val myClass: JavaFileObject =
      JavaFileObjects.forSourceLines(
          "test.MyClass",
          """
    
    package test;
    
    import com.instagram.common.json.annotation.JsonField;
    import com.instagram.common.json.annotation.JsonType;
    
    @JsonType
    class MyClass {
      @JsonField(fieldName = "my_enum")
      MyEnum myEnum;
    }
    
  """
              .trimIndent())

  @Test
  fun succeedsWhenCorrectAnnotationUsage() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;

          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static MyEnum fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static String toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation).succeeded()
  }

  @Test
  fun warningWhenEnumDoesNotHaveJsonAdapterAnnotation() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;

          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static MyEnum fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static String toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining(
            "test.MyClass: Annotate the enum with @JsonAdapter " +
                "(see annotation docs for details). If that is undesirable you must " +
                "have a value extract formatter, and a serialize code formatter if " +
                "serialization generation is enabled")
  }

  @Test
  fun warningWhenMissingFromJsonAnnotatedMethod() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;
          
          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            public static MyEnum fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static String toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining("test.MyEnum: method with @FromJson annotation must be present")
  }

  @Test
  fun warningWhenFromJsonMethodHasIncorrectReturnType() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;
          
          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static Object fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static String toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining(
            "@FromJson must return the correct type, expected type: " + "test.MyEnum")
  }

  @Test
  fun warningWhenFromJsonMethodHasMoreThanOneArgument() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;
          
          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static MyEnum fromJson(String serverValue, String secondArgument) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static String toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining(
            "test.MyEnum: @FromJson must have exactly one parameter, " + "the json type expected")
  }

  @Test
  fun warningWhenMissingToJsonAnnotatedMethod() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;
          
          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static MyEnum fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            public static String toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining("test.MyEnum: method with @ToJson annotation must be present")
  }

  @Test
  fun warningWhenToJsonMethodHasMoreThanOneArgument() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;
          
          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static MyEnum fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static String toJson(MyEnum myEnum, String secondArgument) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining(
            "test.MyEnum: @ToJson must have exactly one parameter, " + "the type of the field.")
  }

  @Test
  fun warningWhenToJsonMethodHasWrongReturnType() {
    val compilation =
        compile(
            JavaFileObjects.forSourceLines(
                "test.MyEnum",
                """
          
          package test;
          
          import com.instagram.common.json.annotation.FromJson;
          import com.instagram.common.json.annotation.JsonAdapter;
          import com.instagram.common.json.annotation.ToJson;
          
          @JsonAdapter(adapterClass = MyEnum.class)
          enum MyEnum {
            ONE("one"),
            TWO("two"),
            NONE("");
            
            String mServerValue;
          
            MyEnum(String serverValue) {
              mServerValue = serverValue;
            }
            
            @FromJson
            public static MyEnum fromJson(String serverValue) {
              for (MyEnum myEnum : values()) {
                if (myEnum.mServerValue.equals(serverValue)) {
                  return myEnum;
                }
              }
              return NONE;
            }
            
            @ToJson
            public static Object toJson(MyEnum myEnum) {
              return myEnum.mServerValue;
            }
          }
          
        """
                    .trimIndent()))

    assertThat(compilation)
        .hadWarningContaining(
            "@ToJson must return the correct type, expected type: " + "java.lang.String")
  }

  private fun compile(file: JavaFileObject) =
      Compiler.javac().withProcessors(JsonAnnotationProcessor()).compile(myClass, file)
}
