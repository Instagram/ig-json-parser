/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.instagram.javawriter.JavaWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.Map;
import javax.annotation.processing.Messager;

/**
 * This gathers data about the declared types of annotated fields and writes code to mutate fields
 * to record that information.
 *
 * <p>If a field's name is XXX, then it generates code to write:
 *
 * <ul>
 *   <li>XXX__IsList - whether a field is a list or a scalar.
 *   <li>XXX__ParseType - the {@link TypeUtils.ParseType} that best matches the field.
 * </ul>
 *
 * As an example, a field declared as <code>int foo;</code> Will generate the following code:
 *
 * <pre>
 *    foo__IsList = false;
 *    foo__ParseType = "INTEGER";
 * </pre>
 */
public class TypeGathererClassData extends ProcessorClassData<String, FieldData> {
  private TypeGathererClassData mParentClassData;

  public TypeGathererClassData(
      String classPackage,
      String qualifiedClassName,
      String simpleClassName,
      String injectedClassName,
      AnnotationRecordFactory<String, FieldData> factory) {
    super(classPackage, qualifiedClassName, simpleClassName, injectedClassName, factory);
  }

  @Override
  public String getJavaCode(Messager messager) {
    StringWriter sw = new StringWriter();
    JavaWriter writer = new JavaWriter(sw);

    try {
      writer
          .emitPackage(mClassPackage)
          .beginType(mInjectedClassName, "class", EnumSet.of(PUBLIC, FINAL))
          .beginMethod(
              "void", "injectTypeData", EnumSet.of(PUBLIC, STATIC), mSimpleClassName, "instance")
          .emitWithGenerator(
              new JavaWriter.JavaGenerator() {
                @Override
                public void emitJava(JavaWriter writer) throws IOException {
                  TypeGathererClassData classWalker = TypeGathererClassData.this;
                  while (classWalker != null) {
                    classWalker.writeFields(writer);
                    classWalker = classWalker.getParentClassData();
                  }
                }
              })
          .endMethod()
          .endType();
    } catch (IOException ex) {
      Console.error(
          messager, "IOException while generating %s: %s", mInjectedClassName, ex.toString());
    }

    return sw.toString();
  }

  protected void writeFields(JavaWriter writer) throws IOException {
    for (Map.Entry<String, FieldData> entry : getIterator()) {
      writer.emitStatement("instance.%s__IsList = %s", entry.getKey(), entry.getValue().mIsList);

      String parseTypeString;
      if (entry.getValue().mParseType == TypeUtils.ParseType.PARSABLE_OBJECT) {
        parseTypeString = entry.getValue().mParsableType;
      } else {
        parseTypeString = entry.getValue().mParseType.toString();
      }
      writer.emitStatement("instance.%s__ParseType = \"%s\"", entry.getKey(), parseTypeString);

      String parseTypeGeneratedClass = entry.getValue().mParsableTypeGeneratedClass;
      writer.emitStatement(
          "instance.%s__ParseTypeGeneratedClass = %s",
          entry.getKey(),
          parseTypeGeneratedClass == null ? null : '\"' + parseTypeGeneratedClass + '\"');
    }
  }

  /** Sets the class data structure for the parent class. */
  void setParentClassData(TypeGathererClassData parentClassData) {
    mParentClassData = parentClassData;
  }

  /**
   * Retrieves the class data structure for the parent class.
   *
   * @return
   */
  private TypeGathererClassData getParentClassData() {
    return mParentClassData;
  }
}
