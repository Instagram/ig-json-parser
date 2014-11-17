// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Apply this to a field in a class annotated with {@link JsonType}.  This tells the annotation
 * processor which fields exist, and how they may to/from the json object.
 */
@Retention(CLASS) @Target(FIELD)
public @interface JsonField {
  /**
   * This controls how we deal with type mismatches.  If a {@link TypeMapping#EXACT} mapping is
   * requested, the json data type must exactly match the java data type.  If the destination field
   * is a java object and there is a data type mismatch, the field will be set to null.   If the
   * destination field is a primitive, there is no way for us to indicate that a mismatch has
   * occurred, so we are forced to throw a {@link JsonException}.
   *
   * If a {@link TypeMapping#COERCED} mapping is requested, we will do our best to coerce it into
   * the proper type.
   */
  public enum TypeMapping { EXACT, COERCED }

  /**
   * This is the field name in json.
   */
  String fieldName();

  /**
   * Alternate field names which should be parsed to the same field. Only used during
   * deserialization.
   */
  String [] alternateFieldNames() default {};

  /**
   * This controls how we deal with type mismatches.  Note that this is ignored if
   * {@link #valueExtractFormatter()} is specified.
   * @see TypeMapping
   * @see #valueExtractFormatter()
   */
  TypeMapping mapping() default TypeMapping.COERCED;

  /**
   * This string allows consumers to override how we extract the value from the {@link JsonParser}
   * object.  The following formatters will be used to in generating the code:
   * <table border=1 cellspacing=0>
   *   <tr>
   *     <th>parser_object</th>
   *     <td>the instance of {@link JsonParser} being read from
   *   </tr>
   *   <tr>
   *     <th>subobject_class</th>
   *     <td>when parsing a subobject, this refers to the subobject type.
   *     </td>
   *   </tr>
   *   <tr>
   *     <th>subobject_helper_class</th>
   *     <td>when parsing a subobject, this refers to the class responsible for parsing the
   *      subobject type.
   *     </td>
   *   </tr>
   * </table>
   *
   * Sane defaults are provided except in the case of {@link Enum}.
   */
  String valueExtractFormatter() default "";

  /**
   * This string allows consumers to override how we assign the rvalue to the java field.  The
   * following formatters will be used to in generating the code:
   * <table border=1 cellspacing=0>
   *   <tr>
   *     <th>object_varname</th>
   *     <td>the name of the variable referring to the instance of the object being parsed</td>
   *   </tr>
   *   <tr>
   *     <th>field_varname</th>
   *     <td>the name of the variable referring to the field within the object being parsed</td>
   *   </tr>
   *   <tr>
   *     <th>extracted_value</th>
   *     <td>the value parsed, after processing by {@link #valueExtractFormatter()}
   *   </tr>
   * </table>
   *
   * While having both {@link #valueExtractFormatter()} and {@link #fieldAssignmentFormatter()} may
   * seem redundant, they actually serve radically different purposes when we parse arrays.
   * {@link #valueExtractFormatter()} will control how we derive each value in the list, while
   * {@link #fieldAssignmentFormatter()} will control how we assign the list to the java field.
   * <p/>
   * For instance, if we wanted to parse an array of integers, increment each element by 1, and
   * save a sublist:
   * <pre>
   *   &#64;JsonField(valueExtractFormatter=&quot;%1$s.getIntValue() + 1&quot;,
   *              fieldAssignmentFormatter=&quot;${object_varname}.${field_varname} =
   *                                        ${extracted_value}.subList(2, 5)&quot;)
   *   List&lt;Integer&gt; incrementedAndReversed;
   * </pre>
   */
  String fieldAssignmentFormatter() default "";

  /**
   * This string allows consumers to override how we serialize a java field back to json.  The
   * string is used as a formatter to generate the actual code that serializes the data.  The format
   * string can contain the following formatting tokens:
   * <ul>
   *   <li>
   *     ${generator_object}: the name of the variable holding the reference to the json generator
   *     object
   *   </li>
   *   <li>
   *     ${object_varname}: the name of the variable that references the object that encloses the
   *     current field
   *   </li>
   *   <li>
   *     ${field_varname}: the name of the variable that references the current field
   *   </li>
   *   <li>
   *     ${iterator}: the name of the variable that references the current element of an array field
   *   </li>
   *   <li>
   *     ${json_fieldname}: the json field name
   *   </li>
   *   <li>
   *     ${subobject_helper_class}: the class that is responsible for serializing the current field
   *   </li>
   * </ul>
   * <p/>
   * The formatting tokens are not always valid, depending on the nature of the field being
   * serialized.  The following table shows which fields are valid under which situations.
   * <table border=1 cellspacing=0>
   *   <tr>
   *     <th>Field type</th>
   *     <th>${generator_object}</th>
   *     <th>${object_varname}</th>
   *     <th>${field_varname}</th>
   *     <th>${iterator}</th>
   *     <th>${json_fieldname}</th>
   *     <th>${subobject_helper_class}</th>
   *   </tr>
   *   <tr>
   *     <th>Scalars</th>
   *     <td>&#x2714;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *   </tr>
   *   <tr>
   *     <th>Subobject</th>
   *     <td>&#x2714;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2714;</td>
   *   </tr>
   *   <tr>
   *     <th>List of scalars</th>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2717;</td>
   *   </tr>
   *   <tr>
   *     <th>List of subobjects</th>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2714;</td>
   *     <td>&#x2717;</td>
   *     <td>&#x2714;</td>
   *   </tr>
   * </table>
   * <p/>
   * Sane defaults are provided except in the case of {@link Enum}.  Use of this feature should be
   * an exception rather than the norm.
   */
  String serializeCodeFormatter() default "";
}
