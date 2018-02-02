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
  public static final String DEFAULT_VALUE_EXTRACT_FORMATTER =
      "${subobject_helper_class}.parseFromJson(${parser_object})";

  enum TriState {
    DEFAULT,
    YES,
    NO;
  }

  /**
   * This annotation specifies that a method with the name specified by
   * {@link #POSTPROCESSING_METHOD_NAME} (currently "postprocess") on the class that is being
   * generated that should be called once parsing is finished.
   *
   * <p>Note that this will not be called when parsing a subclass of this JsonType. In that
   * case, you must specify postprocessingEnabled = true on the subclass, and call the super
   * method explicitly.
   */
  boolean postprocessingEnabled() default false;

  /**
   * Use the specified value extract formatter to parse this object whenever it is encountered by
   * the parser. This can be used as an 'escape hatch' to parse non-standard JSON or a way to add
   * additional default behavior to the standard parser. This can also be used to extract interface
   * values: a {@link JsonType} annotation on an interface will not generate a JsonHelper, but
   * it can be used to hook up a {@link JsonType#valueExtractFormatter()} for that type.
   *
   * <p>This does not change the generated JsonHelper code for this object; rather it changes
   * the JsonHelper code for all objects that refer to this object. However, using this parameter
   * will change the visibility of the JsonHelper parse methods to <b>protected</b> to avoid
   * mistakes by client code which might call those methods directly by accident.
   *
   * <p>Interfaces do not generate parse code, so compilation will fail if an interface appears
   * in a {@link JsonField} without a valueExtractFormatter on either the {@link JsonField} or
   * the {@link JsonType}.
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
   * Use the specified serialization formatter to generate JSON for this object. Like
   * {@link JsonType#valueExtractFormatter()}, this can be used to extend the serializer. Also like
   * {@link JsonType#valueExtractFormatter()}, this can be used to hook up a
   * {@link JsonType#serializeCodeFormatter()} for an interface type, which will not generate its
   * own JsonHelper.
   *
   * <p>Interfaces do not generate serialization code, so compilation will fail if an interface
   * is referenced in a serializer without either {@link JsonType}'s {@link JsonType#serializeCodeFormatter()}
   * or {@link JsonField#serializeCodeFormatter()} provided.
   *
   * <p>Valid formatting tokens:</p>
   *
   * <ul>
   *   <li>
   *     ${generator_object}: the name of the variable holding the reference to the json generator
   *     object
   *   </li>
   *   <li>
   *    ${subobject}: a reference to the instance being serialized
   *   </li>
   *   <li>
   *     ${subobject_helper_class}: name of the subobject's JsonHelper class. Not valid for
   *     interfaces.
   *   </li>
   * </ul>
   *
   * <p> See {@link JsonField#serializeCodeFormatter()} for more details.
   *
   * @return
   */
  String serializeCodeFormatter() default "";

  /**
   * If set to YES, or NO, will override the global option for generating serializer methods.
   * Preventing generation of serializer methods when you don't use them may help save on binary size of
   * the generated code.
   */
  TriState generateSerializer() default TriState.DEFAULT;

  /**
   * This annotation specifies that during the serialization the getters will be used for getting field value rather than reading the field
   * value directly. The getters should be named as standard JavaBean getters, namely prefixed with 'get' and camel-cased field name.
   */
  boolean useGetters() default false;

  /**
   * Additional imports to include in generated code for this class. These imports are visible
   * from formatter code on {@link JsonField}.
   * They will not be visible from formatters on {@link JsonType}.
   *
   * <p>These imports will be unconditionally added to this class's generated JsonHelper.</p>
   */
  String [] imports() default {};

  /**
   * Additional imports to include in generated code that refers to this class. These imports are
   * visible from formatter code on {@link JsonType}. They will not be visible from formatters
   * on {@link JsonField}.
   *
   * <p>These imports will be added to generated JsonHelpers that refer to this class.</p>
   */
  String [] typeFormatterImports() default {};
}
