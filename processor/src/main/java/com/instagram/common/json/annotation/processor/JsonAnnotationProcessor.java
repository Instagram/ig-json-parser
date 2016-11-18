// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.instagram.common.json.JsonAnnotationProcessorConstants;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.util.Console;
import com.instagram.common.json.annotation.util.ProcessorClassData;
import com.instagram.common.json.annotation.util.TypeUtils;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.*;

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
  }

  private boolean toBooleanDefaultTrue(String value) {
    return value == null || !value.equalsIgnoreCase("false");
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
      gatherFieldAnnotations(env);

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
          error(typeElement,
              "Unable to write injector for type %s: %s", typeElement, e.getMessage());
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

  /**
   * This finds the classes that are annotated with {@link JsonType}.
   */
  private void gatherClassAnnotations(RoundEnvironment env) {
    // Process each @TypeTesting elements.
    for (Element element : env.getElementsAnnotatedWith(JsonType.class)) {
      try {
        processClassAnnotation(element);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate injector for @JsonType.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  /**
   * This processes a single class that is annotated with {@link JsonType}.  It verifies that the
   * class is public and creates an {@link ProcessorClassData} for it.
   */
  private void processClassAnnotation(Element element) {
    boolean abstractClass = false;
    TypeElement typeElement = (TypeElement) element;

    // Verify containing class visibility is not private.
    if (element.getModifiers().contains(PRIVATE)) {
      error(element, "@%s %s may not be applied to private classes. (%s.%s)",
          JsonType.class.getSimpleName(), typeElement.getQualifiedName(),
          element.getSimpleName());
      return;
    }
    if (element.getModifiers().contains(ABSTRACT)) {
      abstractClass = true;
    }

    JsonParserClassData injector = mState.mClassElementToInjectorMap.get(typeElement);
    if (injector == null) {
      JsonType annotation = element.getAnnotation(JsonType.class);

      String parentGeneratedClassName = null;

      TypeMirror superclass = typeElement.getSuperclass();
      // walk up the superclass hierarchy until we find another class we know about.
      while (superclass.getKind() != TypeKind.NONE) {
        TypeElement superclassElement = (TypeElement) mTypes.asElement(superclass);

        if (superclassElement.getAnnotation(JsonType.class) != null) {
          String superclassPackageName = mTypeUtils.getPackageName(mElements, superclassElement);
          parentGeneratedClassName = superclassPackageName + "." +
              mTypeUtils.getPrefixForGeneratedClass(superclassElement, superclassPackageName) +
              JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX;

          break;
        }

        superclass = superclassElement.getSuperclass();
      }


      boolean generateSerializer = annotation.generateSerializer() == JsonType.TriState.DEFAULT ?
              mGenerateSerializers :
              annotation.generateSerializer() == JsonType.TriState.YES;

      String packageName = mTypeUtils.getPackageName(mElements, typeElement);
      injector = new JsonParserClassData(
          packageName,
          typeElement.getQualifiedName().toString(),
          mTypeUtils.getClassName(typeElement, packageName),
          mTypeUtils.getPrefixForGeneratedClass(typeElement, packageName) +
              JsonAnnotationProcessorConstants.HELPER_CLASS_SUFFIX,
          new ProcessorClassData.AnnotationRecordFactory<String, TypeData>() {

            @Override
            public TypeData createAnnotationRecord(String key) {
              return new TypeData();
            }
          },
          abstractClass,
          generateSerializer,
          parentGeneratedClassName,
          annotation);
      mState.mClassElementToInjectorMap.put(typeElement, injector);
    }
  }

  /**
   * This finds the fields that are annotated with {@link JsonField}.
   */
  private void gatherFieldAnnotations(RoundEnvironment env) {
    // Process each @TypeTesting elements.
    for (Element element : env.getElementsAnnotatedWith(JsonField.class)) {
      try {
        processFieldAnnotation(element);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate view injector for @JsonField.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  /**
   * This processes a single field annotated with {@link JsonField}.  It locates the enclosing
   * class and then gathers data on the declared type of the field.
   */
  private void processFieldAnnotation(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isFieldAnnotationValid(JsonField.class, element)) {
      return;
    }

    TypeMirror type = element.asType();

    JsonParserClassData injector = mState.mClassElementToInjectorMap.get(enclosingElement);

    TypeData data = injector.getOrCreateRecord(element.getSimpleName().toString());

    JsonField annotation = element.getAnnotation(JsonField.class);

    data.setFieldName(annotation.fieldName());
    data.setAlternateFieldNames(annotation.alternateFieldNames());
    data.setMapping(annotation.mapping());
    data.setValueExtractFormatter(annotation.valueExtractFormatter());
    data.setAssignmentFormatter(annotation.fieldAssignmentFormatter());
    data.setSerializeCodeFormatter(annotation.serializeCodeFormatter());
    TypeUtils.CollectionType collectionType = mTypeUtils.getCollectionType(type);
    data.setCollectionType(collectionType);

    if (collectionType != TypeUtils.CollectionType.NOT_A_COLLECTION) {
      // inspect the inner type.
      type = mTypeUtils.getCollectionParameterizedType(type);
    }

    data.setParseType(mTypeUtils.getParseType(type, JsonType.class));
    if (data.getParseType() == TypeUtils.ParseType.PARSABLE_OBJECT) {
      TypeMirror erasedType = mTypes.erasure(type);
      DeclaredType declaredType = (DeclaredType) erasedType;
      TypeElement typeElement = (TypeElement) declaredType.asElement();

      String packageName = mTypeUtils.getPackageName(mElements, typeElement);

      data.setPackageName(packageName);
      data.setParsableType(mTypeUtils.getClassName(typeElement, packageName));
      data.setParsableTypeParserClass(
          mTypeUtils.getPrefixForGeneratedClass(typeElement, packageName));

      if (StringUtil.isNullOrEmpty(data.getValueExtractFormatter())) {
        // Use the parsable object's value extract formatter
        data.setValueExtractFormatter(
            typeElement.getAnnotation(JsonType.class).valueExtractFormatter());
      }
    } else if (data.getParseType() == TypeUtils.ParseType.ENUM_OBJECT) {
      // verify that we have value extract and serializer formatters.
      if (StringUtil.isNullOrEmpty(annotation.valueExtractFormatter()) ||
          (injector.generateSerializer() && StringUtil.isNullOrEmpty(annotation.serializeCodeFormatter()))) {
        error(element,
            "%s: enums must have a value extract formatter, and a serialize code formatter if serialization generation is enabled",
            enclosingElement);
      }
      data.setEnumType(type.toString());
    }
  }

  private boolean isFieldAnnotationValid(
      Class<? extends Annotation> annotationClass,
      Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify containing type.
    if (enclosingElement.getKind() != CLASS) {
      error(enclosingElement, "@%s field may only be contained in classes. (%s.%s)",
          annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    Annotation annotation = enclosingElement.getAnnotation(JsonType.class);
    if (annotation == null) {
      error(
          enclosingElement,
          "@%s field may only be contained in classes annotated with @%s (%s.%s)",
          annotationClass.getSimpleName(),
          JsonType.class.toString(),
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    // Verify containing class visibility is not private.
    if (enclosingElement.getModifiers().contains(PRIVATE)) {
      error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
          annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
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
