// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.instagram.common.json.JsonAnnotationProcessorConstants;
import com.instagram.common.json.JsonFactoryHolder;
import com.instagram.common.json.JsonHelper;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.util.Console;
import com.instagram.common.json.annotation.util.ProcessorClassData;
import com.instagram.common.json.annotation.util.TypeUtils;
import com.instagram.javawriter.JavaWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import static com.instagram.common.json.annotation.processor.CodeFormatter.FIELD_ASSIGNMENT;
import static com.instagram.common.json.annotation.processor.CodeFormatter.FIELD_CODE_SERIALIZATION;
import static com.instagram.common.json.annotation.processor.CodeFormatter.VALUE_EXTRACT;
import static javax.lang.model.element.Modifier.*;

/**
 * This collects the data about the fields of a class, and generates the java code to parse the
 * object.
 */
public class JsonParserClassData extends ProcessorClassData<String, TypeData> {

  private final boolean mAbstractClass;
  private final boolean mGenerateSerializer;
  private final boolean mOmitSomeMethodBodies;
  private final String mParentInjectedClassName;
  private final JsonType mAnnotation;

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
      JsonType annotation) {
    super(classPackage, qualifiedClassName, simpleClassName, injectedClassName, factory);
    mAbstractClass = abstractClass;
    mGenerateSerializer = generateSerializer;
    mOmitSomeMethodBodies = omitSomeMethodBodies;
    mParentInjectedClassName = parentInjectedClassName;
    mAnnotation = annotation;
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

      writer.emitImports(
          IOException.class,
          StringWriter.class,
          ArrayList.class,
          ArrayDeque.class,
          HashSet.class,
          HashMap.class,
          List.class,
          Map.class,
          Queue.class,
          Set.class,
          JsonGenerator.class,
          JsonParser.class,
          JsonToken.class,
          JsonFactoryHolder.class,
          JsonHelper.class);

      Set<String> imports = new HashSet<String>();
      // Add any additional imports from this class's annotations.
      imports.addAll(Arrays.asList(mAnnotation.imports()));

      // Generate the set of imports from the parsable objects referenced.
      for (Map.Entry<String, TypeData> entry : getIterator()) {
        TypeData typeData = entry.getValue();
        if (typeData.needsImportFrom(mClassPackage)) {
          imports.add(typeData.getPackageName() + "." + typeData.getParsableType());
          if (typeData.hasParserHelperClass()) {
            imports.add(
                typeData.getPackageName() + "." + typeData.getParsableTypeParserClass() +
                    JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX);
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

      String returnValue = mAnnotation.postprocessingEnabled() ?
          ("instance." + JsonType.POSTPROCESSING_METHOD_NAME + "()") : "instance";

      if (!mAbstractClass) {
        writer
            .beginMethod(
                mSimpleClassName,
                "parseFromJson",
                EnumSet.of(getParseMethodVisibility(), STATIC),
                Arrays.asList("JsonParser", "jp"),
                Arrays.asList("IOException"))
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
              Arrays.asList(mSimpleClassName, "instance", "String", "fieldName", "JsonParser", "jp"),
              Arrays.asList("IOException"))
          .emitWithGenerator(
              new JavaWriter.JavaGenerator() {
                @Override
                public void emitJava(JavaWriter writer) throws IOException {
                  JsonParserClassData.this.writeFields(messager, writer);

                  // if we reached here, we need to call the superclasses processSingleField
                  // method.
                  if (mParentInjectedClassName != null) {
                    writer.emitStatement("return %s.processSingleField(instance, fieldName, jp)",
                        mParentInjectedClassName);
                  } else {
                    writer.emitStatement("return false");
                  }
                }
              })
          .endMethod()
          .emitEmptyLine();

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
                  Arrays.asList("JsonGenerator", "generator",
                      mSimpleClassName, "object",
                      "boolean", "writeStartAndEnd"),
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
                        writer.emitStatement(mParentInjectedClassName +
                            ".serializeToJson(generator, object, false)");
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
                "JsonGenerator generator = " +
                    "JsonFactoryHolder.APP_FACTORY.createGenerator(stringWriter)")
            .emitStatement("serializeToJson(generator, object, true)")
            .emitStatement("generator.close()")
            .emitStatement("return stringWriter.toString()")
            .endMethod()
            .emitEmptyLine();
      }

      writer.endType();
    } catch (IOException ex) {
      Console.error(
          messager, "IOException while generating %s: %s",
          mInjectedClassName, ex.toString());
    }

    return sw.toString();
  }

  /**
   * This writes the if-else block for the fields in this class.
   * <p/>
   * NOTE: This could be optimized further by building a radix trie, and building out the if-else
   * block from traversing the radix trie.
   */
  private void writeFields(Messager messager, JavaWriter writer) throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    boolean firstEntry = true;
    for (Map.Entry<String, TypeData> entry : getIterator()) {
      String member = entry.getKey();
      TypeData data = entry.getValue();

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
        CodeFormatter assignmentFormatter = data.getAssignmentFormatter()
            .orIfEmpty(DEFAULT_ASSIGNMENT_FORMATTER);
        writer.emitStatement(
            StrFormat.createStringFormatter(assignmentFormatter)
                .addParam("object_varname", "instance")
                .addParam("field_varname", member)
                .addParam("extracted_value", "results")
                .format());
      } else {
        String rValue = generateExtractRvalue(data, messager, member);
        CodeFormatter assignmentFormatter = data.getAssignmentFormatter()
            .orIfEmpty(DEFAULT_ASSIGNMENT_FORMATTER);
        writer.emitStatement(
            StrFormat.createStringFormatter(assignmentFormatter)
                .addParam("object_varname", "instance")
                .addParam("field_varname", member)
                .addParam("extracted_value", rValue)
                .format());
      }

      writer.emitStatement("return true");

      firstEntry = false;
    }

    if (firstEntry == false) {
      writer.endControlFlow();
    }
  }

  private void generateCollectionParser(Messager messager, JavaWriter writer, TypeData data, String member)
      throws IOException {
    if (TypeUtils.isMapType(data.getCollectionType())) {
      generateMapParser(messager, writer, data, member);
    } else {
      generateArrayParser(messager, writer, data, member);
    }
  }

  /**
   * This writes the code to properly parse an array.
   */
  private void generateArrayParser(Messager messager, JavaWriter writer, TypeData data, String member)
      throws IOException {
    String innerType = getJavaType(messager, data);
    String interfaceType = mapCollectionTypeToInterfaceType(data.getCollectionType());
    String concreteType = mapCollectionTypeToConcreteType(data.getCollectionType());

    writer.emitStatement("%s<%s> results = null", interfaceType, innerType)
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

  private void generateMapParser(Messager messager, JavaWriter writer, TypeData valueTypeData, String member)
      throws IOException {
    TypeData keyTypeData = new TypeData();
    keyTypeData.setParseType(TypeUtils.ParseType.STRING);

    String keyType = getJavaType(messager, keyTypeData);
    String valueType = getJavaType(messager, valueTypeData);

    String interfaceType = mapCollectionTypeToInterfaceType(valueTypeData.getCollectionType());
    String concreteType = mapCollectionTypeToConcreteType(valueTypeData.getCollectionType());

    writer.emitStatement("%s<%s, %s> results = null", interfaceType, keyType, valueType)
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
            valueType,
            generateExtractRvalue(valueTypeData, messager, member))
        .beginControlFlow("if (parsedValue != null)")
        .emitStatement("results.put(parsedKey, parsedValue)")
        .endControlFlow()
        .endControlFlow()
        .endControlFlow()
        .endControlFlow();
  }

  /**
   * We allow consumers of this library to override how we interact with the jackson to get the
   * value.  This generates the code to generate the rvalue expression.
   */
  private String generateExtractRvalue(TypeData data, Messager messager, String member) {
    CodeFormatter valueExtractFormatter = data.getValueExtractFormatter();

    if (valueExtractFormatter.isEmpty()) {
      if (data.isInterface()) {
        Console.error(
                messager,
                "Interface %s cannot be parsed without a valueExtractFormatter "
                        + "on either the interface's %s or field's %s annotation. (%s.%s)",
                data.getParsableType(),
                JsonType.class.getSimpleName(),
                JsonField.class.getSimpleName(),
                mSimpleClassName,
                member);
      }
      if (data.getMapping() == JsonField.TypeMapping.EXACT) {
        valueExtractFormatter = sExactFormatters.get(data.getParseType());
      } else if (data.getMapping() == JsonField.TypeMapping.COERCED) {
        valueExtractFormatter = sCoercedFormatters.get(data.getParseType());
      }
    }

    StrFormat strFormat = StrFormat.createStringFormatter(valueExtractFormatter)
            .addParam("parser_object", "jp")
            .addParam("subobject_class", data.getParsableType());

    if (!StringUtil.isNullOrEmpty(data.getParsableTypeParserClass())) {
      strFormat.addParam(
                      "subobject_helper_class",
                      data.getParsableTypeParserClass() +
                              JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX);
    }

    return strFormat.format();
  }

  private String getJavaType(Messager messager, TypeData type) {
    if (type.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
      return type.getParsableType();
    }

    if (type.getParseType() == TypeUtils.ParseType.ENUM_OBJECT) {
      return type.getEnumType();
    }

    String javaType = sJavaTypes.get(type.getParseType());
    if (javaType != null) {
      return javaType;
    }

    throw new IllegalStateException(
        "Could not divine java type for " + type.getFieldName() + " in class " +
            mQualifiedClassName);
  }

  // These are all the default formatters.
  private static CodeFormatter DEFAULT_ASSIGNMENT_FORMATTER = FIELD_ASSIGNMENT.forString(
      "${object_varname}.${field_varname} = ${extracted_value}");

  private static Map<TypeUtils.ParseType, CodeFormatter> sExactFormatters =
      new HashMap<TypeUtils.ParseType, CodeFormatter>();
  private static Map<TypeUtils.ParseType, CodeFormatter> sCoercedFormatters =
      new HashMap<TypeUtils.ParseType, CodeFormatter>();
  private static Map<TypeUtils.ParseType, String> sJavaTypes =
      new HashMap<TypeUtils.ParseType, String>();

  static {
    sExactFormatters.put(TypeUtils.ParseType.BOOLEAN,
        VALUE_EXTRACT.forString("${parser_object}.getBooleanValue()"));
    sExactFormatters.put(TypeUtils.ParseType.BOOLEAN_OBJECT,
        VALUE_EXTRACT.forString("((${parser_object}.getCurrentToken() == JsonToken.VALUE_TRUE || " +
            "${parser_object}.getCurrentToken() == JsonToken.VALUE_FALSE) ? " +
            "Boolean.valueOf(${parser_object}.getValueAsBoolean()) : null)"));
    sExactFormatters.put(TypeUtils.ParseType.INTEGER,
        VALUE_EXTRACT.forString("${parser_object}.getIntValue()"));
    sExactFormatters.put(TypeUtils.ParseType.INTEGER_OBJECT,
        VALUE_EXTRACT.forString("(${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT ? " +
            "Integer.valueOf(${parser_object}.getValueAsInt()) : null)"));
    sExactFormatters.put(TypeUtils.ParseType.LONG,
        VALUE_EXTRACT.forString("${parser_object}.getLongValue()"));
    sExactFormatters.put(TypeUtils.ParseType.LONG_OBJECT,
        VALUE_EXTRACT.forString("(${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT ? " +
            "Long.valueOf(${parser_object}.getValueAsLong()) : null)"));
    sExactFormatters.put(TypeUtils.ParseType.FLOAT,
        VALUE_EXTRACT.forString("${parser_object}.getFloatValue()"));
    sExactFormatters.put(TypeUtils.ParseType.FLOAT_OBJECT,
        VALUE_EXTRACT.forString("((${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT || " +
            "${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) ? " +
            "new Float(${parser_object}.getValueAsDouble()) : null)"));
    sExactFormatters.put(TypeUtils.ParseType.DOUBLE,
        VALUE_EXTRACT.forString("${parser_object}.getDoubleValue()"));
    sExactFormatters.put(TypeUtils.ParseType.DOUBLE_OBJECT,
        VALUE_EXTRACT.forString("((${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT || " +
            "${parser_object}.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) ? " +
            "Double.valueOf(${parser_object}.getValueAsDouble()) : null)"));
    sExactFormatters.put(TypeUtils.ParseType.STRING,
        VALUE_EXTRACT.forString("(${parser_object}.getCurrentToken() == JsonToken.VALUE_STRING ? ${parser_object}.getText() : null)"));

    sCoercedFormatters.put(TypeUtils.ParseType.BOOLEAN,
        VALUE_EXTRACT.forString("${parser_object}.getValueAsBoolean()"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.BOOLEAN_OBJECT,
        VALUE_EXTRACT.forString("Boolean.valueOf(${parser_object}.getValueAsBoolean())"));
    sCoercedFormatters.put(TypeUtils.ParseType.INTEGER,
        VALUE_EXTRACT.forString("${parser_object}.getValueAsInt()"));
    sCoercedFormatters.put(TypeUtils.ParseType.INTEGER_OBJECT,
        VALUE_EXTRACT.forString("Integer.valueOf(${parser_object}.getValueAsInt())"));
    sCoercedFormatters.put(TypeUtils.ParseType.LONG,
        VALUE_EXTRACT.forString("${parser_object}.getValueAsLong()"));
    sCoercedFormatters.put(TypeUtils.ParseType.LONG_OBJECT,
        VALUE_EXTRACT.forString("Long.valueOf(${parser_object}.getValueAsLong())"));
    sCoercedFormatters.put(TypeUtils.ParseType.FLOAT,
        VALUE_EXTRACT.forString("((float) ${parser_object}.getValueAsDouble())"));
    sCoercedFormatters.put(TypeUtils.ParseType.FLOAT_OBJECT,
        VALUE_EXTRACT.forString("new Float(${parser_object}.getValueAsDouble())"));
    sCoercedFormatters.put(TypeUtils.ParseType.DOUBLE,
        VALUE_EXTRACT.forString("${parser_object}.getValueAsDouble()"));
    sCoercedFormatters.put(
        TypeUtils.ParseType.DOUBLE_OBJECT,
        VALUE_EXTRACT.forString("Double.valueOf(${parser_object}.getValueAsDouble())"));
    sCoercedFormatters.put(TypeUtils.ParseType.STRING,
        VALUE_EXTRACT.forString("(${parser_object}.getCurrentToken() == JsonToken.VALUE_NULL ? null : ${parser_object}.getText())"));

    sJavaTypes.put(TypeUtils.ParseType.BOOLEAN_OBJECT, "Boolean");
    sJavaTypes.put(TypeUtils.ParseType.INTEGER_OBJECT, "Integer");
    sJavaTypes.put(TypeUtils.ParseType.LONG_OBJECT, "Long");
    sJavaTypes.put(TypeUtils.ParseType.FLOAT_OBJECT, "Float");
    sJavaTypes.put(TypeUtils.ParseType.DOUBLE_OBJECT, "Double");
    sJavaTypes.put(TypeUtils.ParseType.STRING, "String");
  }

  /**
   * This writes the code to serialize this class to a JsonGenerator.
   */
  private void writeSerializeCalls(Messager messager, JavaWriter writer) throws IOException {
    if (mOmitSomeMethodBodies) {
      return;
    }

    for (Map.Entry<String, TypeData> entry : getIterator()) {
      String member = entry.getKey();
      if (mAnnotation.useGetters()) {
        member = getGetterName(member);
      }
      TypeData data = entry.getValue();
      CodeFormatter serializeCode = data.getSerializeCodeFormatter();

      if (data.getCollectionType() != TypeUtils.CollectionType.NOT_A_COLLECTION) {

        if (!TypeUtils.isMapType(data.getCollectionType())) {

          CodeFormatter defaultFormat = data.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT
              ? PARSABLE_OBJECT_COLLECTION_SERIALIZE_CALL
              : mCollectionSerializeCalls.get(data.getParseType());
          serializeCode = serializeCode.orIfEmpty(defaultFormat);

          // needed to do a typecast for erased types
          String interfaceType = mapCollectionTypeToInterfaceType(data.getCollectionType());
          String listType = getJavaType(messager, entry.getValue());
          writer
              .beginControlFlow("if (object." + member + " != null)")
              .emitStatement("generator.writeFieldName(\"%s\")", data.getFieldName())
              .emitStatement("generator.writeStartArray()")
              .beginControlFlow("for (" + listType +
                  " element : (" + interfaceType + "<" + listType + ">)" +
                  "object." + member + ")")
              .beginControlFlow("if (element != null)")
              .emitStatement(StrFormat.createStringFormatter(serializeCode)
                  .addParam("generator_object", "generator")
                  .addParam("iterator", "element")
                  .addParam(
                      "subobject_helper_class",
                      data.getParsableTypeParserClass() +
                          JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX)
                  .addParam("subobject", "element")
                  .format())
              .endControlFlow()
              .endControlFlow()
              .emitStatement("generator.writeEndArray()")
              .endControlFlow();
        } else {
          // map type
          TypeData keyTypeData = new TypeData();
          keyTypeData.setParseType(TypeUtils.ParseType.STRING);
          TypeData valueTypeData = data;

          String keyType = getJavaType(messager, keyTypeData);
          String valueType = getJavaType(messager, valueTypeData);

          CodeFormatter defaultFormat = valueTypeData.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT
              ? PARSABLE_OBJECT_COLLECTION_SERIALIZE_CALL
              : mCollectionSerializeCalls.get(valueTypeData.getParseType());
          CodeFormatter valueSerializeCode = valueTypeData.getSerializeCodeFormatter()
              .orIfEmpty(defaultFormat);

          writer
              .beginControlFlow("if (object." + member + " != null)")
              .emitStatement("generator.writeFieldName(\"%s\")", valueTypeData.getFieldName())
              .emitStatement("generator.writeStartObject()")
              .beginControlFlow("for (Map.Entry<" + keyType + ", " + valueType + "> entry : " +
                  "object." + member + ".entrySet())")
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
        if (data.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
          if (serializeCode.isEmpty()) {
            if (data.isInterface()) {
              Console.error(
                      messager,
                      "Interface %s cannot be serialized without a serializeCodeFormatter "
                              + "on either the interface's %s or field's %s annotation. (%s.%s)",
                      data.getParsableType(),
                      JsonType.class.getSimpleName(),
                      JsonField.class.getSimpleName(),
                      mSimpleClassName,
                      member);
            }
            serializeCode = PARSABLE_OBJECT_SERIALIZE_CALL;
          }
          writer
              .beginControlFlow("if (object." + member + " != null)")
              .emitStatement("generator.writeFieldName(\"%s\")", data.getFieldName())
              .emitStatement(
                  StrFormat.createStringFormatter(serializeCode)
                      .addParam("generator_object", "generator")
                      .addParam("object_varname", "object")
                      .addParam("field_varname", member)
                      .addParam("subobject_helper_class",
                          data.getParsableTypeParserClass() +
                              JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX)
                      .addParam("subobject", "object." + member)
                      .format())
              .endControlFlow();
        } else {
          serializeCode = serializeCode.orIfEmpty(mScalarSerializeCalls.get(data.getParseType()));

          String statement =
              StrFormat.createStringFormatter(serializeCode)
                  .addParam("generator_object", "generator")
                  .addParam("object_varname", "object")
                  .addParam("field_varname", member)
                  .addParam("json_fieldname", data.getFieldName())
                  .format();

          switch (data.getParseType()) {
            case BOOLEAN:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
              writer.emitStatement(statement);
              break;

            default:
              writer
                  .beginControlFlow("if (object." + member + " != null)")
                  .emitStatement(statement)
                  .endControlFlow();
          }
        }
      }
    }
  }

  private String getMapSerializeCodeStatement(TypeData valueTypeData, CodeFormatter valueSerializeCode) {
    StrFormat strFormat = StrFormat.createStringFormatter(valueSerializeCode)
            .addParam("generator_object", "generator")
            .addParam("iterator", "entry.getValue()");

    if (valueTypeData.hasParserHelperClass()) {
      strFormat.addParam(
              "subobject_helper_class",
              valueTypeData.getParsableTypeParserClass() +
                      JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX);
    }
    if (valueTypeData.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
      strFormat.addParam("subobject", "entry.getValue()");
    }

    return strFormat.format();
  }

  private static String getGetterName(String fieldName) {
    return "get" + String.valueOf(fieldName.charAt(0)).toUpperCase() + fieldName.substring(1) + "()";
  }

  /**
   * used to write a single instance of a parsable object.
   */
  private static final CodeFormatter PARSABLE_OBJECT_SERIALIZE_CALL =
      FIELD_CODE_SERIALIZATION.forString(
          "${subobject_helper_class}.serializeToJson(${generator_object}, ${object_varname}.${field_varname}, true)");
  private static final CodeFormatter PARSABLE_OBJECT_COLLECTION_SERIALIZE_CALL =
      FIELD_CODE_SERIALIZATION.forString(
          "${subobject_helper_class}.serializeToJson(${generator_object}, ${iterator}, true)");

  private static final Map<TypeUtils.ParseType, CodeFormatter> mScalarSerializeCalls =
      new HashMap<TypeUtils.ParseType, CodeFormatter>();
  private static final Map<TypeUtils.ParseType, CodeFormatter> mCollectionSerializeCalls =
      new HashMap<TypeUtils.ParseType, CodeFormatter>();
  static {
    mScalarSerializeCalls.put(TypeUtils.ParseType.BOOLEAN,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeBooleanField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.BOOLEAN_OBJECT,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeBooleanField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.INTEGER,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.INTEGER_OBJECT,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.LONG,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.LONG_OBJECT,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.FLOAT,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.FLOAT_OBJECT,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.DOUBLE,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.DOUBLE_OBJECT,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeNumberField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));
    mScalarSerializeCalls.put(TypeUtils.ParseType.STRING,
        FIELD_CODE_SERIALIZATION.forString(
            "${generator_object}.writeStringField(\"${json_fieldname}\", ${object_varname}.${field_varname})"));

    mCollectionSerializeCalls.put(TypeUtils.ParseType.BOOLEAN,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeBoolean(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.BOOLEAN_OBJECT,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeBoolean(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.INTEGER,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.INTEGER_OBJECT,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.LONG,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.LONG_OBJECT,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.FLOAT,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.FLOAT_OBJECT,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.DOUBLE,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.DOUBLE_OBJECT,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeNumber(${iterator})"));
    mCollectionSerializeCalls.put(TypeUtils.ParseType.STRING,
        FIELD_CODE_SERIALIZATION.forString("${generator_object}.writeString(${iterator})"));

  }

  private String mapCollectionTypeToInterfaceType(TypeUtils.CollectionType collectionType) {
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

  private String mapCollectionTypeToConcreteType(TypeUtils.CollectionType collectionType) {
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
    return JsonType.DEFAULT_VALUE_EXTRACT_FORMATTER.equals(mAnnotation.valueExtractFormatter()) ?
        PUBLIC :
        PROTECTED;
  }
}
