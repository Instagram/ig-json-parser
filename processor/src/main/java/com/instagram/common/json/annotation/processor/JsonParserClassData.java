/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import static com.instagram.common.json.annotation.processor.CodeFormatter.FIELD_ASSIGNMENT;
import static com.instagram.common.json.annotation.processor.CodeFormatter.FIELD_CODE_SERIALIZATION;
import static com.instagram.common.json.annotation.processor.CodeFormatter.LOCAL_ASSIGNMENT;
import static com.instagram.common.json.annotation.processor.CodeFormatter.VALUE_EXTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.instagram.common.json.JsonAnnotationProcessorConstants;
import com.instagram.common.json.JsonCallback;
import com.instagram.common.json.JsonFactoryHolder;
import com.instagram.common.json.JsonHelper;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.util.Console;
import com.instagram.common.json.annotation.util.ProcessorClassData;
import com.instagram.common.json.annotation.util.TypeUtils;
import com.instagram.javawriter.JavaWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;

/**
 * This collects the data about the fields of a class, and generates the java code to parse the
 * object.
 */
public class JsonParserClassData extends ProcessorClassData<String, TypeData> {

  /** These are all the default formatters. */
  private static final CodeFormatter DEFAULT_ASSIGNMENT_FORMATTER =
      FIELD_ASSIGNMENT.forString("${object_varname}.${field_varname} = ${extracted_value}");

  private static final CodeFormatter DEFAULT_ASSIGNMENT_FORMATTER_KOTLIN =
      FIELD_ASSIGNMENT.forString("${object_varname}.${field_varname_setter}(${extracted_value})");
  private static final CodeFormatter LOCAL_ASSIGNMENT_FORMATTER =
      LOCAL_ASSIGNMENT.forString("${local_varname} = ${extracted_value}");

  private static final Map<TypeUtils.ParseType, CodeFormatter> sExactFormatters = new HashMap<>();
  private static final Map<TypeUtils.ParseType, CodeFormatter> sCoercedFormatters = new HashMap<>();
  private static final Map<TypeUtils.ParseType, String> sJavaTypes = new HashMap<>();

  static {
    sExactFormatters.put(
        TypeUtils.ParseType.BOOLEAN, VALUE_EXTRACT.forString("${parser_object}.getBooleanValue()"));
    sExactFormatters.put(
        TypeUtils.ParseType.BOOLEAN_OBJECT,
        VALUE_EXTRACT.forString(
            "((${parser_object}.getCurrentToken() == JsonToken.VALUE_TRUE || "
                + "${parser_object}.getCurrentToken() == JsonToken.VALUE_FALSE) ? "
                + "Boolean.valueOf(${parser_object}.getValueAsBoolean()) : null)"));
    sExactFormatters.put(
        TypeUtils.ParseType.INTEGER, VALUE_EXTRACT.forString("${parser_object}.getIntValue()"));
    sExactFormatters.put(
        TypeUtils.ParseType.INTEGER_OBJECT,
        VALUE_EXTRACT.forString(
            "(${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT ? "
                + "Integer.valueOf(${parser_object}.getValueAsInt()) : null)"));
    sExactFormatters.put(
        TypeUtils.ParseType.LONG, VALUE_EXTRACT.forString("${parser_object}.getLongValue()"));
    sExactFormatters.put(
        TypeUtils.ParseType.LONG_OBJECT,
        VALUE_EXTRACT.forString(
            "(${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT ? "
                + "Long.valueOf(${parser_object}.getValueAsLong()) : null)"));
    sExactFormatters.put(
        TypeUtils.ParseType.FLOAT, VALUE_EXTRACT.forString("${parser_object}.getFloatValue()"));
    sExactFormatters.put(
        TypeUtils.ParseType.FLOAT_OBJECT,
        VALUE_EXTRACT.forString(
            "((${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT || "
                + "${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) ? "
                + "new Float(${parser_object}.getValueAsDouble()) : null)"));
    sExactFormatters.put(
        TypeUtils.ParseType.DOUBLE, VALUE_EXTRACT.forString("${parser_object}.getDoubleValue()"));
    sExactFormatters.put(
        TypeUtils.ParseType.DOUBLE_OBJECT,
        VALUE_EXTRACT.forString(
            "((${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT || "
                + "${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) ? "
                + "Double.valueOf(${parser_object}.getValueAsDouble()) : null)"));
    sExactFormatters.put(
        TypeUtils.ParseType.STRING,
        VALUE_EXTRACT.forString(
            "(${parser_object}.getCurrentToken() == JsonToken.VALUE_STRING ? ${parser_object}.getText() : null)"));

    sCoercedFormatters.put(
        TypeUtils.ParseType.BOOLEAN,
        VALUE_EXTRACT.forString("${parser_object}.getValueAsBoolean()"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.BOOLEAN_OBJECT,
        VALUE_EXTRACT.forString("Boolean.valueOf(${parser_object}.getValueAsBoolean())"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.INTEGER, VALUE_EXTRACT.forString("${parser_object}.getValueAsInt()"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.INTEGER_OBJECT,
        VALUE_EXTRACT.forString("Integer.valueOf(${parser_object}.getValueAsInt())"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.LONG, VALUE_EXTRACT.forString("${parser_object}.getValueAsLong()"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.LONG_OBJECT,
        VALUE_EXTRACT.forString("Long.valueOf(${parser_object}.getValueAsLong())"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.FLOAT,
        VALUE_EXTRACT.forString("((float) ${parser_object}.getValueAsDouble())"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.FLOAT_OBJECT,
        VALUE_EXTRACT.forString("new Float(${parser_object}.getValueAsDouble())"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.DOUBLE, VALUE_EXTRACT.forString("${parser_object}.getValueAsDouble()"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.DOUBLE_OBJECT,
        VALUE_EXTRACT.forString("Double.valueOf(${parser_object}.getValueAsDouble())"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.STRING,
        VALUE_EXTRACT.forString(
            "(${parser_object}.getCurrentToken() == JsonToken.VALUE_NULL ? null : ${parser_object}.getText())"));

    sJavaTypes.put(TypeUtils.ParseType.BOOLEAN_OBJECT, "Boolean");
    sJavaTypes.put(TypeUtils.ParseType.INTEGER_OBJECT, "Integer");
    sJavaTypes.put(TypeUtils.ParseType.LONG_OBJECT, "Long");
    sJavaTypes.put(TypeUtils.ParseType.FLOAT_OBJECT, "Float");
    sJavaTypes.put(TypeUtils.ParseType.DOUBLE_OBJECT, "Double");
    sJavaTypes.put(TypeUtils.ParseType.STRING, "String");

    // We deserialize primitives to objects in strict mode
    sJavaTypes.put(TypeUtils.ParseType.BOOLEAN, "Boolean");
    sJavaTypes.put(TypeUtils.ParseType.INTEGER, "Integer");
    sJavaTypes.put(TypeUtils.ParseType.LONG, "Long");
    sJavaTypes.put(TypeUtils.ParseType.FLOAT, "Float");
    sJavaTypes.put(TypeUtils.ParseType.DOUBLE, "Double");
  }

  /** used to write a single instance of a parsable object. */
  private static final CodeFormatter PARSABLE_OBJECT_SERIALIZE_CALL =
      FIELD_CODE_SERIALIZATION.forString(
          "${subobject_helper_class}.serializeToJson(${generator_object}, ${object_varname}.${field_varname}, true)");

  private static final CodeFormatter PARSABLE_OBJECT_COLLECTION_SERIALIZE_CALL =
      FIELD_CODE_SERIALIZATION.forString(
          "${subobject_helper_class}.serializeToJson(${generator_object}, ${iterator}, true)");

  private static final Map<TypeUtils.ParseType, CodeFormatter> mScalarSerializeCalls =
      new HashMap<>();
  private static final Map<TypeUtils.ParseType, CodeFormatter> mScalarSerializeJsonAdapterCalls =
      new HashMap<>();
  private static final Map<TypeUtils.ParseType, CodeFormatter> mCollectionSerializeCalls =
      new HashMap<>();

  static {
    for (TypeUtils.ParseType value : TypeUtils.ParseType.values()) {
      String scalarWriteMethod = getScalarWriteMethodName(value);
      if (scalarWriteMethod != null) {
        mScalarSerializeCalls.put(
            value,
            FIELD_CODE_SERIALIZATION.forString(
                "${generator_object}."
                    + scalarWriteMethod
                    + "(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
        mScalarSerializeJsonAdapterCalls.put(
            value,
            FIELD_CODE_SERIALIZATION.forString(
                "${generator_object}."
                    + scalarWriteMethod
                    + "(\"${json_fieldname}\", ${adapter_method_name}(${object_varname}.${field_varname}))"));
      }
      String collectionWriteMethod = getCollectionWriteMethodName(value);
      if (collectionWriteMethod != null) {
        mCollectionSerializeCalls.put(
            value,
            FIELD_CODE_SERIALIZATION.forString(
                "${generator_object}." + collectionWriteMethod + "(${iterator})"));
      }
    }
  }

  private final boolean mAbstractClass;
  private final boolean mGenerateSerializer;
  private final boolean mOmitSomeMethodBodies;
  private final String mParentInjectedClassName;
  private final JsonType mAnnotation;
  private final boolean mIsKotlin;
  private final boolean mIsStrict;

  public JsonParserClassData(
      String classPackage,
      String qualifiedClassName,
      String simpleClassName,
      String injectedClassName,
      AnnotationRecordFactory<String, TypeData> factory,
      boolean abstractClass,
      boolean generateSerializer,
      boolean omitSomeMethodBodies,
      String parentInjectedClassName,
      JsonType annotation,
      boolean isKotlin,
      boolean isStrict) {
    super(classPackage, qualifiedClassName, simpleClassName, injectedClassName, factory);
    mAbstractClass = abstractClass;
    mGenerateSerializer = generateSerializer;
    mOmitSomeMethodBodies = omitSomeMethodBodies;
    mParentInjectedClassName = parentInjectedClassName;
    mAnnotation = annotation;
    mIsKotlin = isKotlin;
    mIsStrict = isStrict;
  }

  public boolean generateSerializer() {
    return mGenerateSerializer;
  }

  @Override
  public String getJavaCode(final Messager messager) {
    StringWriter sw = new StringWriter();
    JavaWriter writer = new JavaWriter(sw);

    try {
      writer.emitPackage(mClassPackage);

      Set<String> imports = new HashSet<String>();
      imports.add(IOException.class.getName());
      imports.add(StringWriter.class.getName());
      imports.add(ArrayList.class.getName());
      imports.add(ArrayDeque.class.getName());
      imports.add(HashSet.class.getName());
      imports.add(HashMap.class.getName());
      imports.add(List.class.getName());
      imports.add(Map.class.getName());
      imports.add(Queue.class.getName());
      imports.add(Set.class.getName());
      imports.add(JsonGenerator.class.getName());
      imports.add(JsonParser.class.getName());
      imports.add(JsonToken.class.getName());
      imports.add(JsonFactoryHolder.class.getName());
      imports.add(JsonHelper.class.getName());
      if (mIsStrict) {
        imports.add(JsonCallback.class.getName());
      }

      // Add any additional imports from this class's annotations.
      imports.addAll(Arrays.asList(mAnnotation.imports()));

      int count = 0;

      // Generate the set of imports from the parsable objects referenced.
      for (Map.Entry<String, TypeData> entry : getIterator()) {
        TypeData typeData = entry.getValue();
        typeData.setFieldIndex(count);
        count++;
        if (typeData.needsImportFrom(mClassPackage)) {
          imports.add(typeData.getPackageName() + "." + typeData.getParsableType());
          if (typeData.hasParserHelperClass()) {
            imports.add(
                typeData.getPackageName()
                    + "."
                    + typeData.getParsableTypeParserClass()
                    + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX);
          }
        }
        imports.addAll(typeData.getFormatterImports());
      }
      writer.emitImports(imports);
      writer.emitEmptyLine();

      writer.beginType(
          mInjectedClassName,
          "class",
          EnumSet.of(PUBLIC, FINAL),
          null,
          "JsonHelper<" + mSimpleClassName + ">");
      writer.emitEmptyLine();

      String returnValue =
          mAnnotation.postprocessingEnabled()
              ? ("instance." + JsonType.POSTPROCESSING_METHOD_NAME + "()")
              : "instance";

      if (mIsStrict) {
        if (!mAbstractClass) {
          final String simpleClassName = mSimpleClassName;
          writer
              .beginMethod(
                  mSimpleClassName,
                  "parseFromJson",
                  EnumSet.of(getParseMethodVisibility(), STATIC),
                  Arrays.asList("JsonParser", "jp"),
                  Collections.singletonList("IOException"))
              .emitSingleLineComment("validate that we're on the right token")
              .beginControlFlow("if (jp.getCurrentToken() != JsonToken.START_OBJECT)")
              .emitStatement("jp.skipChildren()")
              .emitStatement("return null")
              .endControlFlow()
              .emitEmptyLine()
              .emitStatement("Object[] parsedProperties = new Object[%d];", count)
              .emitEmptyLine()
              .beginControlFlow("while (jp.nextToken() != JsonToken.END_OBJECT)")
              .emitStatement("String fieldName = jp.getCurrentName()")
              .emitStatement("jp.nextToken()")
              .emitWithGenerator(
                  new JavaWriter.JavaGenerator() {
                    @Override
                    public void emitJava(JavaWriter writer) throws IOException {
                      JsonParserClassData.this.writeProcessFields(messager, writer);
                    }
                  })
              // always skip children.  if we expected an array or an object, we would have
              // consumed the START_ARRAY or START_OBJECT.  therefore, we would only skip
              // forward if we're seeing something unexpected.
              .emitStatement("jp.skipChildren()")
              .endControlFlow()
              .emitEmptyLine()
              .emitWithGenerator(
                  new JavaWriter.JavaGenerator() {
                    @Override
                    public void emitJava(JavaWriter writer) throws IOException {
                      JsonParserClassData.this.writeValidateNonNullFields(
                          simpleClassName, messager, writer);
                    }
                  })
              .emitEmptyLine()
              .emitWithGenerator(
                  new JavaWriter.JavaGenerator() {
                    @Override
                    public void emitJava(JavaWriter writer) throws IOException {
                      JsonParserClassData.this.writeCreateInstance(
                          simpleClassName, messager, writer);
                    }
                  })
              .emitEmptyLine()
              .emitWithGenerator(
                  new JavaWriter.JavaGenerator() {
                    @Override
                    public void emitJava(JavaWriter writer) throws IOException {
                      JsonParserClassData.this.writeFieldAssignments(
                          simpleClassName, messager, writer);
                    }
                  })
              .emitEmptyLine()
              .emitStatement("return %s", returnValue)
              .endMethod()
              .emitEmptyLine();
        }

      } else {
        if (!mAbstractClass) {
          writer
              .beginMethod(
                  mSimpleClassName,
                  "parseFromJson",
                  EnumSet.of(getParseMethodVisibility(), STATIC),
                  Arrays.asList("JsonParser", "jp"),
                  Collections.singletonList("IOException"))
              .emitStatement("%s instance = new %s()", mSimpleClassName, mSimpleClassName)
              .emitEmptyLine()
              .emitSingleLineComment("validate that we're on the right token")
              .beginControlFlow("if (jp.getCurrentToken() != JsonToken.START_OBJECT)")
              .emitStatement("jp.skipChildren()")
              .emitStatement("return null")
              .endControlFlow()
              .emitEmptyLine()
              .beginControlFlow("while (jp.nextToken() != JsonToken.END_OBJECT)")
              .emitStatement("String fieldName = jp.getCurrentName()")
              .emitStatement("jp.nextToken()")
              .emitStatement("processSingleField(instance, fieldName, jp)")
              // always skip children.  if we expected an array or an object, we would have
              // consumed the START_ARRAY or START_OBJECT.  therefore, we would only skip
              // forward if we're seeing something unexpected.
              .emitStatement("jp.skipChildren()")
              .endControlFlow()
              .emitEmptyLine()
              .emitStatement("return %s", returnValue)
              .endMethod()
              .emitEmptyLine();
        }

        writer
            .beginMethod(
                "boolean",
                "processSingleField",
                EnumSet.of(getParseMethodVisibility(), STATIC),
                Arrays.asList(
                    mSimpleClassName, "instance", "String", "fieldName", "JsonParser", "jp"),
                Arrays.asList("IOException"))
            .emitWithGenerator(
                new JavaWriter.JavaGenerator() {
                  @Override
                  public void emitJava(JavaWriter writer) throws IOException {
                    JsonParserClassData.this.writeFields(messager, writer);

                    // if we reached here, we need to call the superclasses processSingleField
                    // method.
                    if (mParentInjectedClassName != null) {
                      writer.emitStatement(
                          "return %s.processSingleField(instance, fieldName, jp)",
                          mParentInjectedClassName);
                    } else {
                      writer.emitStatement("return false");
                    }
                  }
                })
            .endMethod()
            .emitEmptyLine();
      }

      if (!mAbstractClass) {
        writer
            .beginMethod(
                mSimpleClassName,
                "parseFromJson",
                EnumSet.of(getParseMethodVisibility(), STATIC),
                Arrays.asList("String", "inputString"),
                Arrays.asList("IOException"))
            .emitStatement(
                "JsonParser jp = JsonFactoryHolder.APP_FACTORY.createParser(inputString)")
            .emitStatement("jp.nextToken()")
            .emitStatement("return parseFromJson(jp)")
            .endMethod()
            .emitEmptyLine();
      }

      if (mGenerateSerializer) {
        writer
            .beginMethod(
                "void",
                "serializeToJson",
                EnumSet.of(PUBLIC, STATIC),
                Arrays.asList(
                    "JsonGenerator",
                    "generator",
                    mSimpleClassName,
                    "object",
                    "boolean",
                    "writeStartAndEnd"),
                Arrays.asList("IOException"))
            .beginControlFlow("if (writeStartAndEnd)")
            .emitStatement("generator.writeStartObject()")
            .endControlFlow()
            .emitWithGenerator(
                new JavaWriter.JavaGenerator() {
                  @Override
                  public void emitJava(JavaWriter writer) throws IOException {
                    JsonParserClassData.this.writeSerializeCalls(messager, writer);

                    // if we have a superclass, we need to call its serialize method.
                    if (mParentInjectedClassName != null) {
                      writer.emitStatement(
                          mParentInjectedClassName + ".serializeToJson(generator, object, false)");
                    }
                  }
                })
            .beginControlFlow("if (writeStartAndEnd)")
            .emitStatement("generator.writeEndObject()")
            .endControlFlow()
            .endMethod()
            .emitEmptyLine();
      }

      if (mGenerateSerializer && !mAbstractClass) {
        writer
            .beginMethod(
                "String",
                "serializeToJson",
                EnumSet.of(PUBLIC, STATIC),
                Arrays.asList(mSimpleClassName, "object"),
                Arrays.asList("IOException"))
            .emitStatement("StringWriter stringWriter = new StringWriter()")
            .emitStatement(
                "JsonGenerator generator = "
                    + "JsonFactoryHolder.APP_FACTORY.createGenerator(stringWriter)")
            .emitStatement("serializeToJson(generator, object, true)")
            .emitStatement("generator.close()")
            .emitStatement("return stringWriter.toString()")
            .endMethod()
            .emitEmptyLine();
      }

      writer.endType();
    } catch (IOException ex) {
      Console.error(
          messager, "IOException while generating %s: %s", mInjectedClassName, ex.toString());
    }

    return sw.toString();
  }

  private void writeValidateNonNullFields(
      String simpleClassName, Messager messager, JavaWriter writer) throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    writer.beginControlFlow("if (jp instanceof JsonCallback.Provider)");
    writer.emitStatement("JsonCallback callback = ((JsonCallback.Provider)jp).getJsonCallback()");
    for (Map.Entry<String, TypeData> entry : getIterator()) {
      TypeData data = entry.getValue();
      if (mIsStrict
          && !data.isNullable()
          && data.getDeserializeType() == TypeData.DeserializeType.PARAM) {
        writer.beginControlFlow(
            "if (parsedProperties[" + Integer.toString(data.getFieldIndex()) + "] == null)");
        writer.emitStatement(
            "callback.onUnexpectedNull(\"%s\", \"%s\");", data.getFieldName(), simpleClassName);
        writer.endControlFlow();
      }
    }
    writer.endControlFlow();
  }

  private void writeCreateInstance(String simpleClassName, Messager messager, JavaWriter writer)
      throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    StringBuilder args = new StringBuilder();
    boolean hasFirst = false;
    for (Map.Entry<String, TypeData> entry : getIterator()) {
      TypeData data = entry.getValue();
      if (data.getDeserializeType() == TypeData.DeserializeType.PARAM) {
        if (hasFirst) {
          args.append(",");
        }
        args.append(
            "\n      ("
                + getTypeForField(data)
                + ")parsedProperties["
                + Integer.toString(data.getFieldIndex())
                + "]");
        hasFirst = true;
      }
    }

    writer.emitStatement(
        "%s instance = new %s(%s)", simpleClassName, simpleClassName, args.toString());
  }

  private void writeFieldAssignments(String simpleClassName, Messager messager, JavaWriter writer)
      throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    for (Map.Entry<String, TypeData> entry : getIterator()) {
      TypeData data = entry.getValue();

      if (data.getDeserializeType() == TypeData.DeserializeType.FIELD) {
        writer.beginControlFlow(
            "if (parsedProperties[" + Integer.toString(data.getFieldIndex()) + "] != null)");
        writer.emitStatement(
            "instance.%s = (%s)%s",
            data.getMemberVariableName(),
            getTypeForField(data),
            "parsedProperties[" + Integer.toString(data.getFieldIndex()) + "]");
        writer.endControlFlow();
      } else if (data.getDeserializeType() == TypeData.DeserializeType.SETTER) {
        writer.beginControlFlow(
            "if (parsedProperties[" + Integer.toString(data.getFieldIndex()) + "] != null)");
        writer.emitStatement(
            "instance.%s((%s)%s)",
            data.getSetterName(),
            getTypeForField(data),
            "parsedProperties[" + Integer.toString(data.getFieldIndex()) + "]");
        writer.endControlFlow();
      }
    }
  }

  /** this should only be used for casting from a parsed instance to the required type */
  private String getTypeForField(TypeData data) {
    if (data.getParseType() == TypeUtils.ParseType.UNSUPPORTED) {
      StringBuilder sb = new StringBuilder();
      sb.append(data.getParsableType());
      if (data.isWildcard() && data.isInterface() && mIsKotlin) {
        sb.append("<?>");
      }
      return sb.toString();
    }
    if (data.getCollectionType() != TypeUtils.CollectionType.NOT_A_COLLECTION) {
      String innerType = getJavaType(data);
      String interfaceType = mapCollectionTypeToInterfaceType(data.getCollectionType());
      if (TypeUtils.isMapType(data.getCollectionType())) {
        return interfaceType + "<String, " + innerType + ">";
      } else {
        return interfaceType + "<" + innerType + ">";
      }
    } else {
      return getJavaType(data);
    }
  }

  /**
   * This writes the if-else block for the fields in this class.
   *
   * <p>NOTE: This could be optimized further by building a radix trie, and building out the if-else
   * block from traversing the radix trie.
   */
  private void writeProcessFields(Messager messager, JavaWriter writer) throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    boolean firstEntry = true;
    for (Map.Entry<String, TypeData> entry : getIterator()) {
      TypeData data = entry.getValue();
      String member = data.getMemberVariableName();

      String condition = "\"" + data.getFieldName() + "\".equals(fieldName)";

      for (String alternateFieldName : data.getAlternateFieldNames()) {
        condition += "|| \"" + alternateFieldName + "\".equals(fieldName)";
      }

      if (firstEntry) {
        writer.beginControlFlow("if (" + condition + ")");
      } else {
        writer.nextControlFlow("else if (" + condition + ")");
      }

      if (data.getCollectionType() != TypeUtils.CollectionType.NOT_A_COLLECTION) {
        generateCollectionParser(messager, writer, data, member);
        CodeFormatter assignmentFormatter =
            data.getAssignmentFormatter().orIfEmpty(LOCAL_ASSIGNMENT_FORMATTER);
        writer.emitStatement(
            StrFormat.createStringFormatter(assignmentFormatter)
                .addParam(
                    "local_varname",
                    "parsedProperties[" + Integer.toString(data.getFieldIndex()) + "]")
                .addParam("extracted_value", "results")
                .format());
      } else {
        CodeFormatter assignmentFormatter =
            data.getAssignmentFormatter().orIfEmpty(LOCAL_ASSIGNMENT_FORMATTER);
        writer.emitStatement(
            StrFormat.createStringFormatter(assignmentFormatter)
                .addParam(
                    "local_varname",
                    "parsedProperties[" + Integer.toString(data.getFieldIndex()) + "]")
                .addParam("extracted_value", generateExtractRvalue(data, messager, member))
                .format());
      }

      firstEntry = false;
    }

    if (!firstEntry) {
      writer.endControlFlow();
    }
  }

  /**
   * This writes the if-else block for the fields in this class.
   *
   * <p>NOTE: This could be optimized further by building a radix trie, and building out the if-else
   * block from traversing the radix trie.
   */
  private void writeFields(Messager messager, JavaWriter writer) throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    boolean firstEntry = true;
    for (Map.Entry<String, TypeData> entry : getIterator()) {
      TypeData data = entry.getValue();
      String memberVariable = data.getMemberVariableName();
      String setterName = data.getSetterName();

      String condition = "\"" + data.getFieldName() + "\".equals(fieldName)";

      for (String alternateFieldName : data.getAlternateFieldNames()) {
        condition += "|| \"" + alternateFieldName + "\".equals(fieldName)";
      }

      if (firstEntry) {
        writer.beginControlFlow("if (" + condition + ")");
      } else {
        writer.nextControlFlow("else if (" + condition + ")");
      }

      if (data.getCollectionType() != TypeUtils.CollectionType.NOT_A_COLLECTION) {
        generateCollectionParser(messager, writer, data, memberVariable);
        CodeFormatter assignmentFormatter =
            data.getAssignmentFormatter()
                .orIfEmpty(
                    mIsKotlin ? DEFAULT_ASSIGNMENT_FORMATTER_KOTLIN : DEFAULT_ASSIGNMENT_FORMATTER);
        writer.emitStatement(
            StrFormat.createStringFormatter(assignmentFormatter)
                .addParam("object_varname", "instance")
                .addParam("field_varname", memberVariable)
                .addParam("field_varname_setter", setterName)
                .addParam("extracted_value", "results")
                .format());
      } else {
        CodeFormatter assignmentFormatter =
            data.getAssignmentFormatter()
                .orIfEmpty(
                    mIsKotlin ? DEFAULT_ASSIGNMENT_FORMATTER_KOTLIN : DEFAULT_ASSIGNMENT_FORMATTER);
        writer.emitStatement(
            StrFormat.createStringFormatter(assignmentFormatter)
                .addParam("object_varname", "instance")
                .addParam("field_varname", memberVariable)
                .addParam("field_varname_setter", setterName)
                .addParam("extracted_value", generateExtractRvalue(data, messager, memberVariable))
                .format());
      }

      writer.emitStatement("return true");

      firstEntry = false;
    }

    if (!firstEntry) {
      writer.endControlFlow();
    }
  }

  private void generateCollectionParser(
      Messager messager, JavaWriter writer, TypeData data, String member) throws IOException {
    if (TypeUtils.isMapType(data.getCollectionType())) {
      generateMapParser(messager, writer, data, member);
    } else {
      generateArrayParser(messager, writer, data, member);
    }
  }

  /** This writes the code to properly parse an array. */
  private void generateArrayParser(
      Messager messager, JavaWriter writer, TypeData data, String member) throws IOException {
    String innerType = getJavaType(data);
    String interfaceType = mapCollectionTypeToInterfaceType(data.getCollectionType());
    String concreteType = mapCollectionTypeToConcreteType(data.getCollectionType());

    writer
        .emitStatement("%s<%s> results = null", interfaceType, innerType)
        .beginControlFlow("if (jp.getCurrentToken() == JsonToken.START_ARRAY)")
        .emitStatement("results = new %s<%s>()", concreteType, innerType)
        .beginControlFlow("while (jp.nextToken() != JsonToken.END_ARRAY)")
        .emitStatement("%s parsed = %s", innerType, generateExtractRvalue(data, messager, member))
        .beginControlFlow("if (parsed != null)")
        .emitStatement("results.add(parsed)")
        .endControlFlow()
        .endControlFlow()
        .endControlFlow();
  }

  private void generateMapParser(
      Messager messager, JavaWriter writer, TypeData valueTypeData, String member)
      throws IOException {
    TypeData keyTypeData = new TypeData();
    keyTypeData.setParseType(TypeUtils.ParseType.STRING);

    String keyType = getJavaType(keyTypeData);
    String valueType = getJavaType(valueTypeData);

    String interfaceType = mapCollectionTypeToInterfaceType(valueTypeData.getCollectionType());
    String concreteType = mapCollectionTypeToConcreteType(valueTypeData.getCollectionType());

    writer
        .emitStatement("%s<%s, %s> results = null", interfaceType, keyType, valueType)
        .beginControlFlow("if (jp.getCurrentToken() == JsonToken.START_OBJECT)")
        .emitStatement("results = new %s<%s, %s>()", concreteType, keyType, valueType)
        .beginControlFlow("while (jp.nextToken() != JsonToken.END_OBJECT)")
        .emitStatement("%s parsedKey = jp.getText()", keyType)
        .emitStatement("jp.nextToken()")
        .beginControlFlow("if (jp.getCurrentToken() == JsonToken.VALUE_NULL)")
        .emitStatement("results.put(parsedKey, null)")
        .nextControlFlow("else")
        .emitStatement(
            "%s parsedValue = %s",
            valueType, generateExtractRvalue(valueTypeData, messager, member))
        .beginControlFlow("if (parsedValue != null)")
        .emitStatement("results.put(parsedKey, parsedValue)")
        .endControlFlow()
        .endControlFlow()
        .endControlFlow()
        .endControlFlow();
  }

  /**
   * We allow consumers of this library to override how we interact with the jackson to get the
   * value. This generates the code to generate the rvalue expression.
   */
  private String generateExtractRvalue(TypeData data, Messager messager, String member) {
    CodeFormatter valueExtractFormatter = data.getValueExtractFormatter();

    if (valueExtractFormatter.isEmpty()) {
      if (data.isInterface()) {
        Console.error(
            messager,
            "Interface %s cannot be parsed without a valueExtractFormatter "
                + "on either the interface's JsonType or field's JsonField annotation. (%s.%s)",
            data.getParsableType(),
            mSimpleClassName,
            member);
      }
      if (data.getMapping() == JsonField.TypeMapping.EXACT) {
        valueExtractFormatter = sExactFormatters.get(data.getJsonAdapterOrParseType());
      } else if (data.getMapping() == JsonField.TypeMapping.COERCED) {
        valueExtractFormatter = sCoercedFormatters.get(data.getJsonAdapterOrParseType());
      }
    }

    StrFormat strFormat =
        StrFormat.createStringFormatter(valueExtractFormatter)
            .addParam("parser_object", "jp")
            .addParam("subobject_class", data.getParsableType());

    if (!StringUtil.isNullOrEmpty(data.getParsableTypeParserClass())) {
      strFormat.addParam(
          "subobject_helper_class",
          data.getParsableTypeParserClass() + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX);
    }

    String rValue = strFormat.format();
    if (data.getJsonAdapterFromJsonMethod() != null) {
      // If we have a from json method, we call that method with the value
      rValue = data.getJsonAdapterFromJsonMethod() + "(" + rValue + ")";
    }
    return rValue;
  }

  private String getJavaType(TypeData type) {
    if (type.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
      StringBuilder sb = new StringBuilder();
      sb.append(type.getParsableType());
      if (type.isWildcard() && type.isInterface() && mIsKotlin) {
        sb.append("<?>");
      }
      return sb.toString();
    }

    if (type.getParseType() == TypeUtils.ParseType.ENUM_OBJECT) {
      return type.getEnumType();
    }

    String javaType = sJavaTypes.get(type.getJsonAdapterOrParseType());
    if (javaType != null) {
      return javaType;
    }

    throw new IllegalStateException(
        "Could not divine java type for "
            + type.getFieldName()
            + " of type "
            + type.getJsonAdapterOrParseType().toString()
            + " in class "
            + mQualifiedClassName);
  }

  /** This writes the code to serialize this class to a JsonGenerator. */
  private void writeSerializeCalls(Messager messager, JavaWriter writer) throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    for (Map.Entry<String, TypeData> entry : getIterator()) {
      TypeData valueTypeData = entry.getValue();
      String accessor = null;
      if (valueTypeData.getSerializeType() == TypeData.SerializeType.GETTER) {
        accessor = valueTypeData.getGetterName() + "()";
      } else {
        accessor = valueTypeData.getMemberVariableName();
      }
      CodeFormatter serializeCode = valueTypeData.getSerializeCodeFormatter();

      if (valueTypeData.getCollectionType() != TypeUtils.CollectionType.NOT_A_COLLECTION) {

        if (!TypeUtils.isMapType(valueTypeData.getCollectionType())) {

          if (valueTypeData.getJsonAdapterToJsonMethod() != null) {
            String collectionWriteMethod =
                getCollectionWriteMethodName(valueTypeData.getJsonAdapterParseType());
            serializeCode =
                FIELD_CODE_SERIALIZATION.forString(
                    "${generator_object}."
                        + collectionWriteMethod
                        + "(${adapter_method_name}(${iterator}))");
          }

          CodeFormatter defaultFormat =
              valueTypeData.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT
                  ? PARSABLE_OBJECT_COLLECTION_SERIALIZE_CALL
                  : mCollectionSerializeCalls.get(valueTypeData.getJsonAdapterOrParseType());
          serializeCode = serializeCode.orIfEmpty(defaultFormat);

          // needed to do a typecast for erased types
          String interfaceType =
              mapCollectionTypeToInterfaceType(valueTypeData.getCollectionType());
          String listType = getJavaType(entry.getValue());
          writer
              .beginControlFlow("if (object." + accessor + " != null)")
              .emitStatement("generator.writeFieldName(\"%s\")", valueTypeData.getFieldName())
              .emitStatement("generator.writeStartArray()")
              .beginControlFlow(
                  "for ("
                      + listType
                      + " element : ("
                      + interfaceType
                      + "<"
                      + listType
                      + ">)"
                      + "object."
                      + accessor
                      + ")")
              .beginControlFlow("if (element != null)")
              .emitStatement(
                  StrFormat.createStringFormatter(serializeCode)
                      .addParam("generator_object", "generator")
                      .addParam("iterator", "element")
                      .addParam(
                          "subobject_helper_class",
                          valueTypeData.getParsableTypeParserClass()
                              + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX)
                      .addParam("subobject", "element")
                      .addParam("adapter_method_name", valueTypeData.getJsonAdapterToJsonMethod())
                      .format())
              .endControlFlow()
              .endControlFlow()
              .emitStatement("generator.writeEndArray()")
              .endControlFlow();
        } else {
          // map type
          TypeData keyTypeData = new TypeData();
          keyTypeData.setParseType(TypeUtils.ParseType.STRING);

          String keyType = getJavaType(keyTypeData);
          String valueType = getJavaType(valueTypeData);

          CodeFormatter defaultFormat =
              valueTypeData.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT
                  ? PARSABLE_OBJECT_COLLECTION_SERIALIZE_CALL
                  : mCollectionSerializeCalls.get(valueTypeData.getJsonAdapterOrParseType());
          CodeFormatter valueSerializeCode =
              valueTypeData.getSerializeCodeFormatter().orIfEmpty(defaultFormat);

          if (valueTypeData.getJsonAdapterToJsonMethod() != null) {
            String collectionWriteMethod =
                getCollectionWriteMethodName(valueTypeData.getJsonAdapterParseType());
            valueSerializeCode =
                FIELD_CODE_SERIALIZATION.forString(
                    "${generator_object}."
                        + collectionWriteMethod
                        + "(${adapter_method_name}(${iterator}))");
          }

          writer
              .beginControlFlow("if (object." + accessor + " != null)")
              .emitStatement("generator.writeFieldName(\"%s\")", valueTypeData.getFieldName())
              .emitStatement("generator.writeStartObject()")
              .beginControlFlow(
                  "for (Map.Entry<"
                      + keyType
                      + ", "
                      + valueType
                      + "> entry : "
                      + "object."
                      + accessor
                      + ".entrySet())")
              .emitStatement("generator.writeFieldName(entry.getKey().toString())")
              .beginControlFlow("if (entry.getValue() == null)")
              .emitStatement("generator.writeNull()")
              .nextControlFlow("else")
              .emitStatement(getMapSerializeCodeStatement(valueTypeData, valueSerializeCode))
              .endControlFlow()
              .endControlFlow()
              .emitStatement("generator.writeEndObject()")
              .endControlFlow();
        }

      } else {
        if (valueTypeData.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
          if (serializeCode.isEmpty()) {
            if (valueTypeData.isInterface()) {
              Console.error(
                  messager,
                  "Interface %s cannot be serialized without a serializeCodeFormatter "
                      + "on either the interface's JsonType or field's JsonField annotation. (%s.%s)",
                  valueTypeData.getParsableType(),
                  mSimpleClassName,
                  accessor);
            }
            serializeCode = PARSABLE_OBJECT_SERIALIZE_CALL;
          }
          writer
              .beginControlFlow("if (object." + accessor + " != null)")
              .emitStatement("generator.writeFieldName(\"%s\")", valueTypeData.getFieldName())
              .emitStatement(
                  StrFormat.createStringFormatter(serializeCode)
                      .addParam("generator_object", "generator")
                      .addParam("object_varname", "object")
                      .addParam("field_varname", accessor)
                      .addParam("field_varname_setter", valueTypeData.getSetterName())
                      .addParam(
                          "subobject_helper_class",
                          valueTypeData.getParsableTypeParserClass()
                              + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX)
                      .addParam("subobject", "object." + accessor)
                      .format())
              .endControlFlow();
        } else {
          CodeFormatter codeFormatter;
          if (valueTypeData.getJsonAdapterToJsonMethod() != null) {
            codeFormatter =
                mScalarSerializeJsonAdapterCalls.get(valueTypeData.getJsonAdapterOrParseType());
          } else {
            codeFormatter = mScalarSerializeCalls.get(valueTypeData.getJsonAdapterOrParseType());
          }
          serializeCode = serializeCode.orIfEmpty(codeFormatter);

          String statement =
              StrFormat.createStringFormatter(serializeCode)
                  .addParam("generator_object", "generator")
                  .addParam("object_varname", "object")
                  .addParam("field_varname", accessor)
                  .addParam("field_varname_setter", valueTypeData.getSetterName())
                  .addParam("json_fieldname", valueTypeData.getFieldName())
                  .addParam("adapter_method_name", valueTypeData.getJsonAdapterToJsonMethod())
                  .format();

          switch (valueTypeData.getParseType()) {
            case BOOLEAN:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
              writer.emitStatement(statement);
              break;

            default:
              writer
                  .beginControlFlow("if (object." + accessor + " != null)")
                  .emitStatement(statement)
                  .endControlFlow();
          }
        }
      }
    }
  }

  private String getMapSerializeCodeStatement(
      TypeData valueTypeData, CodeFormatter valueSerializeCode) {
    StrFormat strFormat =
        StrFormat.createStringFormatter(valueSerializeCode)
            .addParam("generator_object", "generator")
            .addParam("iterator", "entry.getValue()")
            .addParam("adapter_method_name", valueTypeData.getJsonAdapterToJsonMethod());

    if (valueTypeData.hasParserHelperClass()) {
      strFormat.addParam(
          "subobject_helper_class",
          valueTypeData.getParsableTypeParserClass()
              + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX);
    }
    if (valueTypeData.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
      strFormat.addParam("subobject", "entry.getValue()");
    }

    return strFormat.format();
  }

  private String getGetterName(String fieldName) {
    if (isKotlinIsSpecialPrefixCase(fieldName)) {
      return fieldName + "()";
    } else {
      return "get" + capitalize(fieldName) + "()";
    }
  }

  private String getSetterName(String fieldName) {
    if (isKotlinIsSpecialPrefixCase(fieldName)) {
      return "set" + capitalize(fieldName.substring(2));
    } else {
      return "set" + capitalize(fieldName);
    }
  }

  private boolean isKotlinIsSpecialPrefixCase(String fieldName) {
    return mIsKotlin
        && fieldName.length() > 2
        && fieldName.startsWith("is")
        && Character.isUpperCase(fieldName.charAt(2));
  }

  private static String capitalize(String str) {
    return String.valueOf(str.charAt(0)).toUpperCase(Locale.getDefault()) + str.substring(1);
  }

  private static String getScalarWriteMethodName(TypeUtils.ParseType parseType) {
    switch (parseType) {
      case BOOLEAN:
      case BOOLEAN_OBJECT:
        return "writeBooleanField";
      case INTEGER:
      case INTEGER_OBJECT:
      case LONG:
      case LONG_OBJECT:
      case FLOAT:
      case FLOAT_OBJECT:
      case DOUBLE:
      case DOUBLE_OBJECT:
        return "writeNumberField";
      case STRING:
        return "writeStringField";
      default:
        return null;
    }
  }

  private static String getCollectionWriteMethodName(TypeUtils.ParseType parseType) {
    switch (parseType) {
      case BOOLEAN:
      case BOOLEAN_OBJECT:
        return "writeBoolean";
      case INTEGER:
      case INTEGER_OBJECT:
      case LONG:
      case LONG_OBJECT:
      case FLOAT:
      case FLOAT_OBJECT:
      case DOUBLE:
      case DOUBLE_OBJECT:
        return "writeNumber";
      case STRING:
        return "writeString";
      default:
        return null;
    }
  }

  private static String mapCollectionTypeToInterfaceType(TypeUtils.CollectionType collectionType) {
    switch (collectionType) {
      case LIST:
        return "List";
      case ARRAYLIST:
        return "ArrayList";
      case HASHMAP:
        return "HashMap";
      case QUEUE:
        return "Queue";
      case SET:
        return "Set";
    }
    throw new IllegalStateException("unknown collection type");
  }

  private static String mapCollectionTypeToConcreteType(TypeUtils.CollectionType collectionType) {
    switch (collectionType) {
      case LIST:
      case ARRAYLIST:
        return "ArrayList";
      case HASHMAP:
        return "HashMap";
      case QUEUE:
        return "ArrayDeque";
      case SET:
        return "HashSet";
    }
    throw new IllegalStateException("unknown collection type");
  }

  private Modifier getParseMethodVisibility() {
    return JsonType.DEFAULT_VALUE_EXTRACT_FORMATTER.equals(mAnnotation.valueExtractFormatter())
        ? PUBLIC
        : PROTECTED;
  }
}
