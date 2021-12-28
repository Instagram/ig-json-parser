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
 * This annotation is applied to any class for which a json parser should be automatically
 * generated.
 */
@Retention(CLASS)
@Target(TYPE)
public @interface JsonType {

  public static final String POSTPROCESSING_METHOD_NAME = "postprocess";
  public static final String DEFAULT_VALUE_EXTRACT_FORMATTER =
      "${subobject_helper_class}.parseFromJson(${parser_object})";

  enum TriState {
    DEFAULT,
    YES,
    NO;
  }

  /**
   * Strict mode is currently "in development" so the following will be subject to change.
   *
   * <p>Strict mode is not supported when subclassing a class annotated with JsonType.
   *
   * <p>This annotation specifies the model should be rendered with strict parsing enabled. Strict
   * mode is a Kotlin first mode which leverages the advantages of Kotlin data classes. Here is an
   * example Kotlin data class annotated in strict mode: <code>
   * @JsonType(strict = true)
   * data class KotlinImmutableObject(
   * @JsonField(fieldName = "data1") val data1: String,
   * @JsonField(fieldName = "data2") val data2: Int
   * ) {
   * @JsonField(fieldName = "data3") var data3: String? = null
   * }
   * </code>
   *
   * <p>If you would like to annotate a java class, you can use strict mode, but there are naming
   * requirements for construct params and getter functions on the java class in order for this to
   * work: <code>
   * @JsonType(strict = true)
   * public class ImmutableObject {
   * private final String mData1;
   * private final Integer mData2;
   *
   * @JsonField(fieldName = "data3")
   * Integer mData3;
   *
   * public ImmutableObject(
   * @JsonField(fieldName = "data1") String data1, @JsonField(fieldName = "data2") Integer data2) {
   * mData1 = data1;
   * mData2 = data2;
   * }
   *
   * public String getData1() {
   * return mData1;
   * }
   *
   * public Integer getData2() {
   * return mData2;
   * }
   * }
   * </code>
   *
   * <p>Note that the params data1 and data2 have getters with the name getData1 and getData2. These
   * methods are not required when generateSerializer = TriState.NO
   */
  boolean strict() default false;

  /**
   * This annotation specifies that a method with the name specified by {@link
   * #POSTPROCESSING_METHOD_NAME} (currently "postprocess") on the class that is being generated
   * that should be called once parsing is finished.
   *
   * <p>Note that this will not be called when parsing a subclass of this JsonType. In that case,
   * you must specify postprocessingEnabled = true on the subclass, and call the super method
   * explicitly.
   */
  boolean postprocessingEnabled() default false;

  /**
   * Use the specified value extract formatter to parse this object whenever it is encountered by
   * the parser. This can be used as an 'escape hatch' to parse non-standard JSON or a way to add
   * additional default behavior to the standard parser. This can also be used to extract interface
   * values: a {@link JsonType} annotation on an interface will not generate a JsonHelper, but it
   * can be used to hook up a {@link JsonType#valueExtractFormatter()} for that type.
   *
   * <p>This does not change the generated JsonHelper code for this object; rather it changes the
   * JsonHelper code for all objects that refer to this object. However, using this parameter will
   * change the visibility of the JsonHelper parse methods to <b>protected</b> to avoid mistakes by
   * client code which might call those methods directly by accident.
   *
   * <p>Interfaces do not generate parse code, so compilation will fail if an interface appears in a
   * {@link JsonField} without a valueExtractFormatter on either the {@link JsonField} or the {@link
   * JsonType}.
   *
   * <p>See {@link JsonField#valueExtractFormatter()} for a description of the available
   * substitutions.
   *
   * <p>Note that this will be disregarded when parsing a subclass of this JsonType.
   *
   * @return A value extract formatter
   */
  String valueExtractFormatter() default DEFAULT_VALUE_EXTRACT_FORMATTER;

  /**
   * Use the specified serialization formatter to generate JSON for this object. Like {@link
   * JsonType#valueExtractFormatter()}, this can be used to extend the serializer. Also like {@link
   * JsonType#valueExtractFormatter()}, this can be used to hook up a {@link
   * JsonType#serializeCodeFormatter()} for an interface type, which will not generate its own
   * JsonHelper.
   *
   * <p>Interfaces do not generate serialization code, so compilation will fail if an interface is
   * referenced in a serializer without either {@link JsonType}'s {@link
   * JsonType#serializeCodeFormatter()} or {@link JsonField#serializeCodeFormatter()} provided.
   *
   * <p>Valid formatting tokens:
   *
   * <ul>
   *   <li>${generator_object}: the name of the variable holding the reference to the json generator
   *       object
   *   <li>${subobject}: a reference to the instance being serialized
   *   <li>${subobject_helper_class}: name of the subobject's JsonHelper class. Not valid for
   *       interfaces.
   * </ul>
   *
   * <p>See {@link JsonField#serializeCodeFormatter()} for more details.
   *
   * @return
   */
  String serializeCodeFormatter() default "";

  /**
   * If set to YES, or NO, will override the global option for generating serializer methods.
   * Preventing generation of serializer methods when you don't use them may help save on binary
   * size of the generated code.
   */
  TriState generateSerializer() default TriState.DEFAULT;

  /**
   * This annotation specifies that during the serialization the getters will be used for getting
   * field value rather than reading the field value directly. The getters should be named as
   * standard JavaBean getters, namely prefixed with 'get' and camel-cased field name.
   */
  boolean useGetters() default false;

  /**
   * Additional imports to include in generated code for this class. These imports are visible from
   * formatter code on {@link JsonField}. They will not be visible from formatters on {@link
   * JsonType}.
   *
   * <p>These imports will be unconditionally added to this class's generated JsonHelper.
   */
  String[] imports() default {};

  /**
   * Additional imports to include in generated code that refers to this class. These imports are
   * visible from formatter code on {@link JsonType}. They will not be visible from formatters on
   * {@link JsonField}.
   *
   * <p>These imports will be added to generated JsonHelpers that refer to this class.
   */
  String[] typeFormatterImports() default {};
}
