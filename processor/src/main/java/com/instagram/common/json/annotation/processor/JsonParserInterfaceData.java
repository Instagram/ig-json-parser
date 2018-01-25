package com.instagram.common.json.annotation.processor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.instagram.common.json.JsonFactoryHolder;
import com.instagram.common.json.JsonHelper;
import com.instagram.common.json.JsonSerializationHandler;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.util.Console;
import com.instagram.common.json.annotation.util.ProcessorClassData;
import com.instagram.javawriter.JavaWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import javax.annotation.Nullable;
import javax.annotation.processing.Messager;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;


/**
 * This collects data about the configuration of an interface, and generates the java code to
 * dispatch parsing of that interface to implementations of that interface.
 */
public class JsonParserInterfaceData implements SourceGenerator {
    private final String mClassPackage;
    private final String mQualifiedClassName;
    private final String mSimpleClassName;
    private final String mInjectedClassName;
    private final ProcessorClassData.AnnotationRecordFactory<String, TypeData> mFactory;
    private final String mHandlerTypeName;
    private String mTypeNameGetter;
    private final JsonType mAnnotation;

    public JsonParserInterfaceData(
            String classPackage,
            String qualifiedClassName,
            String simpleClassName,
            String injectedClassName,
            ProcessorClassData.AnnotationRecordFactory<String, TypeData> factory,
            JsonType annotation) {
        mClassPackage = classPackage;
        mQualifiedClassName = qualifiedClassName;
        mSimpleClassName = simpleClassName;
        mInjectedClassName = injectedClassName;
        mFactory = factory;
        mAnnotation = annotation;
        mHandlerTypeName = "JsonSerializationHandler<" + mSimpleClassName + ">";
    }

    @Override
    public String getInjectedFqcn() {
        return mClassPackage + '.' + mInjectedClassName;
    }

    @Override
    public String getJavaCode(Messager messager) {
        StringWriter sw = new StringWriter();
        JavaWriter writer = new JavaWriter(sw);

        try {
            writer.emitPackage(mClassPackage)

                .emitImports(
                    JsonFactoryHolder.class,
                    JsonGenerator.class,
                    JsonHelper.class,
                    JsonParser.class,
                    JsonSerializationHandler.class,
                    JsonToken.class,
                    IOException.class,
                    HashMap.class,
                    Nullable.class)

                .emitEmptyLine()

                .beginType(
                    mInjectedClassName,
                    "class",
                    EnumSet.of(PUBLIC, FINAL),
                    null,
                    "JsonHelper<" + mSimpleClassName + ">")

                .emitField(
                    "HashMap<String," + mHandlerTypeName + ">",
                    "sHandlerMap",
                    EnumSet.of(PRIVATE, STATIC, FINAL),
                    "new HashMap<>()")

                .emitEmptyLine();

            emitRegisterHandlerMethod(writer);

            writer.emitEmptyLine();

            emitUnregisterHandlerMethod(writer);

            writer.emitEmptyLine();

            emitGetHandlerMethod(writer);

            writer.emitEmptyLine();

            emitParseFromJsonFromJsonParser(writer);

            writer.emitEmptyLine();

            emitParseFromJsonFromString(writer);

            writer.emitEmptyLine();

            emitSerializeToJson(writer);

            writer.endType();
            writer.close();
        } catch (IOException ex) {
            Console.error(
                    messager, "IOException while generating %s: %s",
                    mInjectedClassName, ex.toString());
        }
        return sw.toString();
    }

    private void emitRegisterHandlerMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "void",
                "registerHandler",
                EnumSet.of(PUBLIC, STATIC),
                "String", "typeName",
                mHandlerTypeName, "handler")

            .beginControlFlow("if (sHandlerMap.containsKey(typeName))")
            .emitStatement("final String message = String.format(\n"
                + "\"Duplicate handler type name. %%s is already mapped to an instance of %%s\",\n"
                + "typeName,\nsHandlerMap.get(typeName).getClass().getName())")
            .emitStatement("throw new IllegalArgumentException(message)")
            .endControlFlow()

            .emitStatement("sHandlerMap.put(typeName, handler)")

            .endMethod();
    }

    private static void emitUnregisterHandlerMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "void",
                "unregisterHandler",
                EnumSet.of(PUBLIC, STATIC),
                "String", "typeName")
            .emitStatement("sHandlerMap.remove(typeName)")
            .endMethod();
    }

    private void emitGetHandlerMethod(JavaWriter writer) throws IOException {
        writer
            .beginMethod(
                mHandlerTypeName,
                "getHandler",
                EnumSet.of(PRIVATE, STATIC),
                "String", "typeName")
            .emitStatement("final @Nullable " + mHandlerTypeName
                    + " handler = sHandlerMap.get(typeName)")

            .beginControlFlow("if (handler == null)")
            .emitStatement("final String message = String.format(\n"
                + "\"No JsonSerializationHandler registered for type name: %%s\",\n"
                + "typeName)")
            .emitStatement("throw new IllegalArgumentException(message)")
            .endControlFlow()
            .emitEmptyLine()
            .emitStatement("return handler")

            .endMethod();
    }

    private void emitParseFromJsonFromJsonParser(JavaWriter writer) throws IOException {
        writer.beginMethod(
                mSimpleClassName,
                "parseFromJson",
                EnumSet.of(PUBLIC, STATIC),
                Arrays.asList("JsonParser", "parser"),
                Collections.singletonList("IOException"))

            .beginControlFlow("if (parser.getCurrentToken() != JsonToken.START_ARRAY)")
            .emitStatement("parser.skipChildren()")
            .endControlFlow()

            .emitEmptyLine()
            .emitStatement("parser.nextToken()")
            .beginControlFlow("if (parser.getCurrentToken() != JsonToken.VALUE_STRING)")
            .emitStatement("parser.skipChildren()")
            .endControlFlow()

            .emitEmptyLine()
            .emitStatement("String typeName = parser.getText()")
            .emitStatement("parser.nextToken()")
            .emitStatement(
              "final %s instance = getHandler(typeName).parseFromJson(parser)", mSimpleClassName)
            .emitStatement("parser.nextToken()")
            .emitStatement("return instance")

            .endMethod();
    }

    private void emitParseFromJsonFromString(JavaWriter writer) throws IOException {
        writer.beginMethod(
                mSimpleClassName,
                "parseFromJson",
                EnumSet.of(PUBLIC, STATIC),
                Arrays.asList("String", "inputString"),
                Collections.singletonList("IOException"))

            .emitStatement("JsonParser jp = JsonFactoryHolder.APP_FACTORY.createParser(inputString)")
            .emitStatement("jp.nextToken()")
            .emitStatement("return parseFromJson(jp)")

            .endMethod();
    }

    private void emitSerializeToJson(JavaWriter writer) throws IOException {
        writer
            .emitSingleLineComment("writeStartAndEnd is included for API compatibility, but is ignored.")
            .beginMethod(
                "void",
                "serializeToJson",
                EnumSet.of(PUBLIC, STATIC),
                Arrays.asList(
                        "JsonGenerator", "generator",
                        mSimpleClassName, "object",
                        "boolean", "writeStartAndEndIgnored"),
                Collections.singletonList("IOException"))
            .emitStatement("generator.writeStartArray()")
            .emitStatement("generator.writeString(object.%s())", mTypeNameGetter)
            .emitStatement("getHandler(object.%s()).serializeToJson(generator, object)", mTypeNameGetter)
            .emitStatement("generator.writeEndArray()")
            .endMethod();
    }

    public String getTypeNameGetter() {
        return mTypeNameGetter;
    }

    public void setTypeNameGetter(String typeNameGetter) {
        mTypeNameGetter = typeNameGetter;
    }
}
