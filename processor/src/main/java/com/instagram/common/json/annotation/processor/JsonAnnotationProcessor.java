/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import static com.instagram.common.json.annotation.processor.CodeFormatter.FIELD_ASSIGNMENT;
import static com.instagram.common.json.annotation.processor.CodeFormatter.FIELD_CODE_SERIALIZATION;
import static com.instagram.common.json.annotation.processor.CodeFormatter.VALUE_EXTRACT;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;

import com.instagram.common.json.JsonAnnotationProcessorConstants;
import com.instagram.common.json.annotation.FromJson;
import com.instagram.common.json.annotation.JsonAdapter;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.ToJson;
import com.instagram.common.json.annotation.util.Console;
import com.instagram.common.json.annotation.util.ProcessorClassData;
import com.instagram.common.json.annotation.util.TypeUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

/**
 * This annotation processor is run at compile time to find classes annotated with {@link JsonType}.
 * Deserializers are generated for such classes.
 */
@SupportedOptions({"generateSerializers"})
public class JsonAnnotationProcessor extends AbstractProcessor {
  private Messager mMessager;
  private Elements mElements;
  private Types mTypes;
  private Filer mFiler;
  private TypeUtils mTypeUtils;

  private boolean mGenerateSerializers;
  private boolean mOmitSomeMethodBodies;

  private static class State {
    private Map<TypeElement, JsonParserClassData> mClassElementToInjectorMap;

    State() {
      mClassElementToInjectorMap = new LinkedHashMap<>();
    }
  }

  private State mState;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    mMessager = env.getMessager();
    mElements = env.getElementUtils();
    mTypes = env.getTypeUtils();
    mFiler = env.getFiler();
    mTypeUtils = new TypeUtils(mTypes, mMessager);

    Map<String, String> options = env.getOptions();
    mGenerateSerializers = toBooleanDefaultTrue(options.get("generateSerializers"));
    mOmitSomeMethodBodies =
        toBooleanDefaultFalse(options.get("com.facebook.buck.java.generating_abi"));
  }

  private boolean toBooleanDefaultTrue(String value) {
    return value == null || !value.equalsIgnoreCase("false");
  }

  private boolean toBooleanDefaultFalse(String value) {
    return value != null && value.equalsIgnoreCase("true");
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> supportTypes = new LinkedHashSet<String>();
    supportTypes.add(JsonField.class.getCanonicalName());
    supportTypes.add(JsonType.class.getCanonicalName());

    return supportTypes;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
    Console.reportErrors(env, processingEnv.getMessager());

    try {
      // each round of processing requires a clean state.
      mState = new State();

      gatherClassAnnotations(env);
      if (!mOmitSomeMethodBodies) {
        // Field annotations are only needed if we're generating method bodies.
        gatherFieldAnnotations(env);
      }

      for (Map.Entry<TypeElement, JsonParserClassData> entry :
          mState.mClassElementToInjectorMap.entrySet()) {
        TypeElement typeElement = entry.getKey();
        JsonParserClassData injector = entry.getValue();

        try {
          JavaFileObject jfo = mFiler.createSourceFile(injector.getInjectedFqcn(), typeElement);
          Writer writer = jfo.openWriter();
          writer.write(injector.getJavaCode(processingEnv.getMessager()));
          writer.flush();
          writer.close();
        } catch (IOException e) {
          error(
              typeElement, "Unable to write injector for type %s: %s", typeElement, e.getMessage());
        }
      }

      return true;
    } catch (Throwable ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      error("annotation exception: %s cause: %s", ex.toString(), sw.toString());
      return false;
    }
  }

  /** This finds the classes that are annotated with {@link JsonType}. */
  private void gatherClassAnnotations(RoundEnvironment env) {
    // Process each @TypeTesting elements.
    for (Element element : env.getElementsAnnotatedWith(JsonType.class)) {
      try {
        processClassAnnotation(element);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate injector for @JsonType.\n\n%s", stackTrace.toString());
      }
    }
  }

  private boolean isTypeElementKotlin(TypeElement typeElement) {
    try {
      Class<? extends Annotation> metaDataClass =
          Class.forName("kotlin.Metadata").asSubclass(Annotation.class);
      return typeElement.getAnnotation(metaDataClass) != null;
    } catch (ClassNotFoundException e) {
      // not kotlin
    }
    return false;
  }

  /**
   * This processes a single class that is annotated with {@link JsonType}. It verifies that the
   * class is public and creates an {@link ProcessorClassData} for it.
   */
  private void processClassAnnotation(Element element) {
    boolean abstractClass = false;
    TypeElement typeElement = (TypeElement) element;

    // The annotation should be validated for an interface, but no code should be generated.
    JsonType annotation = element.getAnnotation(JsonType.class);
    if (element.getKind() == INTERFACE) {
      return;
    }

    if (annotation.strict()) {
      TypeMirror typeMirror = typeElement.getSuperclass();
      while (typeMirror instanceof DeclaredType) {
        TypeElement parentTypeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
        if (parentTypeElement.getAnnotation(JsonType.class) != null) {
          error(
              element,
              "@JsonType strict=true can not be applied to classes that subclass other JsonType classes. (%s,%s)",
              typeElement.getQualifiedName(),
              parentTypeElement.getQualifiedName());
          return;
        }
        typeMirror = parentTypeElement.getSuperclass();
      }
    }

    boolean isKotlin = isTypeElementKotlin(typeElement);

    // Verify containing class visibility is not private.
    if (element.getModifiers().contains(PRIVATE)) {
      error(
          element,
          "@JsonType %s may not be applied to private classes. (%s.%s)",
          typeElement.getQualifiedName(),
          element.getSimpleName());
      return;
    }
    if (element.getModifiers().contains(ABSTRACT)) {
      abstractClass = true;
    }

    JsonParserClassData injector = mState.mClassElementToInjectorMap.get(typeElement);
    if (injector == null) {

      String parentGeneratedClassName = null;

      if (!mOmitSomeMethodBodies) {
        // Superclass info is only needed if we're generating method bodies.
        TypeMirror superclass = typeElement.getSuperclass();
        // walk up the superclass hierarchy until we find another class we know about.
        while (superclass.getKind() != TypeKind.NONE) {
          TypeElement superclassElement = (TypeElement) mTypes.asElement(superclass);

          if (superclassElement.getAnnotation(JsonType.class) != null) {
            String superclassPackageName = mTypeUtils.getPackageName(mElements, superclassElement);
            parentGeneratedClassName =
                superclassPackageName
                    + "."
                    + mTypeUtils.getPrefixForGeneratedClass(
                        superclassElement, superclassPackageName)
                    + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX;

            break;
          }

          superclass = superclassElement.getSuperclass();
        }
      }

      boolean generateSerializer =
          annotation.generateSerializer() == JsonType.TriState.DEFAULT
              ? mGenerateSerializers
              : annotation.generateSerializer() == JsonType.TriState.YES;

      String packageName = mTypeUtils.getPackageName(mElements, typeElement);
      injector =
          new JsonParserClassData(
              packageName,
              typeElement.getQualifiedName().toString(),
              mTypeUtils.getClassName(typeElement, packageName),
              mTypeUtils.getPrefixForGeneratedClass(typeElement, packageName)
                  + JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX,
              new ProcessorClassData.AnnotationRecordFactory<String, TypeData>() {

                @Override
                public TypeData createAnnotationRecord(String key) {
                  return new TypeData();
                }
              },
              abstractClass,
              generateSerializer,
              mOmitSomeMethodBodies,
              parentGeneratedClassName,
              annotation,
              isKotlin,
              annotation.strict());
      mState.mClassElementToInjectorMap.put(typeElement, injector);
    }
  }

  /** This finds the fields that are annotated with {@link JsonField}. */
  private void gatherFieldAnnotations(RoundEnvironment env) {
    // Process each @TypeTesting elements.
    for (Element element : env.getElementsAnnotatedWith(JsonField.class)) {
      try {
        processFieldAnnotation(element);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(
            element,
            "Unable to generate view injector for @JsonField.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  /**
   * This processes a single field annotated with {@link JsonField}. It locates the enclosing class
   * and then gathers data on the declared type of the field.
   */
  private void processFieldAnnotation(Element element) {

    // Verify common generated code restrictions.
    if (!isFieldAnnotationValid(element)) {
      return;
    }

    TypeElement classElement = null;

    if (element.getKind() == PARAMETER) {
      Element constructorElement = (Element) element.getEnclosingElement();
      classElement = (TypeElement) constructorElement.getEnclosingElement();
    } else {
      classElement = (TypeElement) element.getEnclosingElement();
    }
    JsonType jsonTypeAnnotation = classElement.getAnnotation(JsonType.class);

    boolean isStrict = jsonTypeAnnotation.strict();

    boolean isKotlin = isTypeElementKotlin(classElement);

    TypeMirror type = element.asType();

    JsonParserClassData injector = mState.mClassElementToInjectorMap.get(classElement);

    JsonField annotation = element.getAnnotation(JsonField.class);

    TypeData data = injector.getOrCreateRecord(annotation.fieldName().toString());

    boolean isNullable = isFieldElementNullable(element);

    AccessorMetadata accessorMetadata =
        AccessorMetadata.create(
            element.getSimpleName().toString(),
            isStrict,
            isKotlin,
            jsonTypeAnnotation.useGetters(),
            element.getKind());

    if (accessorMetadata.checkMetadataMismatch(data)) {
      error(
          element,
          "%s: Detected multiple annotations with the same field name. Field names must be unique within given class.",
          classElement);
    }

    data.setSerializeType(accessorMetadata.serializeType);
    data.setDeserializeType(accessorMetadata.deserializeType);
    data.setGetterName(accessorMetadata.getterName);
    data.setSetterName(accessorMetadata.setterName);
    data.setMemberVariableName(accessorMetadata.memberVariableName);

    data.setFieldName(annotation.fieldName());
    data.setIsNullable(isNullable);
    data.setAlternateFieldNames(annotation.alternateFieldNames());
    data.setMapping(annotation.mapping());
    data.setValueExtractFormatter(VALUE_EXTRACT.forString(annotation.valueExtractFormatter()));
    data.setAssignmentFormatter(FIELD_ASSIGNMENT.forString(annotation.fieldAssignmentFormatter()));
    data.setSerializeCodeFormatter(
        FIELD_CODE_SERIALIZATION.forString(annotation.serializeCodeFormatter()));
    TypeUtils.CollectionType collectionType = mTypeUtils.getCollectionType(type);
    data.setCollectionType(collectionType);

    if (collectionType != TypeUtils.CollectionType.NOT_A_COLLECTION) {
      // inspect the inner type.
      type = mTypeUtils.getCollectionParameterizedType(type);
    }

    data.setParseType(mTypeUtils.getParseType(type, JsonType.class));

    boolean skipEnumValidationCheck = setJsonAdapterIfApplicable(type, injector, data, annotation);

    /**
     * UNSUPPORTED can be parsed if valueExtractFormatter and or serializeCodeFormatter have been
     * provided
     */
    if (data.getParseType() == TypeUtils.ParseType.UNSUPPORTED) {
      TypeMirror erasedType = mTypes.erasure(type);
      DeclaredType declaredType = (DeclaredType) erasedType;
      TypeElement typeElement = (TypeElement) declaredType.asElement();

      String packageName = mTypeUtils.getPackageName(mElements, typeElement);

      data.setPackageName(packageName);
      data.setParsableType(mTypeUtils.getClassName(typeElement, packageName));

      CodeFormatter.Factory serializeCodeType =
          typeElement.getKind() == INTERFACE
              ? CodeFormatter.CLASS_CODE_SERIALIZATION
              : CodeFormatter.INTERFACE_CODE_SERIALIZATION;

      data.setIsInterface(typeElement.getKind() == INTERFACE);
      data.setIsWildcard(type != null && type.getKind() == TypeKind.WILDCARD);
    } else if (data.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
      TypeMirror erasedType = mTypes.erasure(type);
      DeclaredType declaredType = (DeclaredType) erasedType;
      TypeElement typeElement = (TypeElement) declaredType.asElement();

      String packageName = mTypeUtils.getPackageName(mElements, typeElement);

      data.setPackageName(packageName);
      data.setParsableType(mTypeUtils.getClassName(typeElement, packageName));
      data.setParsableTypeParserClass(
          mTypeUtils.getPrefixForGeneratedClass(typeElement, packageName));

      JsonType typeAnnotation = typeElement.getAnnotation(JsonType.class);
      // Use the parsable object's value extract formatter if existing one is empty
      data.setValueExtractFormatter(
          data.getValueExtractFormatter()
              .orIfEmpty(VALUE_EXTRACT.forString(typeAnnotation.valueExtractFormatter())));

      CodeFormatter.Factory serializeCodeType =
          typeElement.getKind() == INTERFACE
              ? CodeFormatter.CLASS_CODE_SERIALIZATION
              : CodeFormatter.INTERFACE_CODE_SERIALIZATION;
      data.setSerializeCodeFormatter(
          data.getSerializeCodeFormatter()
              .orIfEmpty(serializeCodeType.forString(typeAnnotation.serializeCodeFormatter())));

      data.setIsInterface(typeElement.getKind() == INTERFACE);
      data.setIsWildcard(type != null && type.getKind() == TypeKind.WILDCARD);
      data.setFormatterImports(typeAnnotation.typeFormatterImports());
    } else if (data.getParseType() == TypeUtils.ParseType.ENUM_OBJECT) {
      // verify that we have value extract and serializer formatters.
      if (!skipEnumValidationCheck
          && (StringUtil.isNullOrEmpty(annotation.valueExtractFormatter())
              || (injector.generateSerializer()
                  && StringUtil.isNullOrEmpty(annotation.serializeCodeFormatter())))) {
        error(
            element,
            "%s: Annotate the enum with @%s (see annotation docs for details). "
                + "If that is undesirable you must have a value extract formatter, "
                + "and a serialize code formatter if serialization generation is enabled",
            classElement,
            JsonAdapter.class.getSimpleName());
      }
      data.setEnumType(type.toString());
    }
  }

  /**
   * Sets up JsonAdapter data for the annotation processor if applicable.
   *
   * @return true if we can skip enum validation of formatters, as we do not need them if we have a
   *     json adapter, false otherwise
   */
  private boolean setJsonAdapterIfApplicable(
      TypeMirror type, JsonParserClassData injector, TypeData data, JsonField annotation) {
    // If there are custom formatters applied, it takes precedence over the json adapter of the
    // type.
    boolean eligibleToUseJsonAdapter =
        data.getParseType() == TypeUtils.ParseType.ENUM_OBJECT
            && annotation.valueExtractFormatter().isEmpty()
            && annotation.fieldAssignmentFormatter().isEmpty()
            && annotation.serializeCodeFormatter().isEmpty();

    boolean skipEnumValidationCheck = false;

    if (eligibleToUseJsonAdapter) {
      DeclaredType declaredType = (DeclaredType) type;
      Element typeElement = declaredType.asElement();
      JsonAdapter adapterAnnotation = typeElement.getAnnotation(JsonAdapter.class);
      if (adapterAnnotation != null) {
        TypeElement adapterTypeElement =
            AnnotationMirrorUtils.getAnnotationValueAsTypeElement(
                typeElement, mTypes, JsonAdapter.class, "adapterClass");

        ExecutableElement fromJson = null;
        ExecutableElement toJson = null;

        for (Element enclosedElement : adapterTypeElement.getEnclosedElements()) {
          if (enclosedElement.getKind() == METHOD) {
            if (enclosedElement.getAnnotation(FromJson.class) != null) {
              fromJson = (ExecutableElement) enclosedElement;
            } else if (enclosedElement.getAnnotation(ToJson.class) != null) {
              toJson = (ExecutableElement) enclosedElement;
            }
          }
          if (fromJson != null && (!injector.generateSerializer() || toJson != null)) {
            break;
          }
        }

        String qualifiedName = adapterTypeElement.getQualifiedName().toString();

        TypeMirror fromJsonParameterTypeMirror = null;

        // handle fromJson
        if (fromJson == null) {
          error(
              "%s: method with @%s annotation must be present",
              type, FromJson.class.getSimpleName());
        } else if (!mTypes.isSameType(fromJson.getReturnType(), type)) {
          error(
              fromJson,
              "@%s must return the correct type, expected type: %s",
              FromJson.class.getSimpleName(),
              type);
        } else if (fromJson.getParameters().size() != 1) {
          error(
              fromJson,
              "%s: @%s must have exactly one parameter, the json type expected (String, Integer, etc.)",
              type,
              FromJson.class.getSimpleName());
        } else {
          fromJsonParameterTypeMirror = fromJson.getParameters().get(0).asType();
          data.setJsonAdapterFromJsonMethod(
              qualifiedName + "." + fromJson.getSimpleName().toString());
        }

        // handle toJson
        if (injector.generateSerializer() && fromJsonParameterTypeMirror != null) {
          if (toJson == null) {
            error(
                "%s: method with @%s annotation must be present",
                type, ToJson.class.getSimpleName());
          } else if (toJson.getParameters().size() != 1) {
            error(
                toJson,
                "%s: @%s must have exactly one parameter, the type of the field.",
                type,
                ToJson.class.getSimpleName());
          } else if (!mTypes.isSameType(toJson.getParameters().get(0).asType(), type)) {
            error(
                toJson,
                "@%s must take the correct type, expected type: %s",
                ToJson.class.getSimpleName(),
                type);
          } else if (!mTypes.isSameType(toJson.getReturnType(), fromJsonParameterTypeMirror)) {
            error(
                fromJson,
                "@%s must return the correct type, expected type: %s",
                ToJson.class.getSimpleName(),
                fromJsonParameterTypeMirror);
          } else {
            data.setJsonAdapterToJsonMethod(
                qualifiedName + "." + toJson.getSimpleName().toString());
          }
        }
        data.setJsonAdapterParseType(mTypeUtils.getParseType(fromJsonParameterTypeMirror, null));
        skipEnumValidationCheck = true;
      }
    }
    return skipEnumValidationCheck;
  }

  private boolean isFieldElementNullable(Element element) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      // Support a variety of Nullable annotations (Android, Java, JetBrains, ...) by just checking
      // the name
      if (annotationMirror
          .getAnnotationType()
          .asElement()
          .getSimpleName()
          .toString()
          .equals("Nullable")) {
        return true;
      }
    }
    return false;
  }

  private boolean isFieldAnnotationValid(Element element) {
    TypeElement classElement = null;
    boolean maybeCheckGetter = false;
    if (element.getKind() == PARAMETER) {
      ExecutableElement constructorElement = (ExecutableElement) element.getEnclosingElement();

      classElement = (TypeElement) constructorElement.getEnclosingElement();

      Annotation jsonType = classElement.getAnnotation(JsonType.class);
      if (jsonType != null && ((JsonType) jsonType).strict()) {
        for (VariableElement variableElement : constructorElement.getParameters()) {
          Annotation annotation = variableElement.getAnnotation(JsonField.class);
          if (annotation == null) {
            error(
                constructorElement,
                "There must be a JsonField annotation for every parameter in %s. The parameter %s does not have one.",
                constructorElement.getSimpleName(),
                variableElement.getSimpleName());
            return false;
          }
        }
      }

      maybeCheckGetter = true;
    } else {
      classElement = (TypeElement) element.getEnclosingElement();
    }

    // Verify containing type.
    if (classElement.getKind() != CLASS) {
      error(
          classElement,
          "JsonField field may only be contained in classes. (%s.%s)",
          classElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    Annotation annotation = classElement.getAnnotation(JsonType.class);

    if (annotation == null) {
      error(
          classElement,
          "JsonField field may only be contained in classes annotated with @JsonType (%s.%s)",
          classElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    if (maybeCheckGetter && ((JsonType) annotation).generateSerializer() != JsonType.TriState.NO) {
      boolean isKotlin = isTypeElementKotlin(classElement);
      String getterName =
          AccessorMetadata.getGetterName(element.getSimpleName().toString(), isKotlin);
      boolean foundGetter = false;
      for (Element enclosedElement : classElement.getEnclosedElements()) {
        if (enclosedElement.getSimpleName().toString().equals(getterName)) {
          foundGetter = true;
        }
      }
      if (!foundGetter) {
        error(
            classElement,
            "Found param (%s) annotated with JsonField but expected getter on class %s with name %s.",
            element.getSimpleName(),
            classElement.getQualifiedName(),
            getterName);
        return false;
      }
    }

    // Verify containing class visibility is not private.
    if (classElement.getModifiers().contains(PRIVATE)) {
      error(
          classElement,
          "@JsonField %s may not be contained in private classes. (%s.%s)",
          classElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    return true;
  }

  private void error(String message, Object... args) {
    Console.error(processingEnv.getMessager(), message, args);
  }

  private void error(Element element, String message, Object... args) {
    Console.error(processingEnv.getMessager(), element, message, args);
  }

  private void warning(String message, Object... args) {
    Console.warning(processingEnv.getMessager(), message, args);
  }
}
