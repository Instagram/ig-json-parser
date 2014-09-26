// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.processing.*;
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
import java.util.Map;
import java.util.Set;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * This annotation processor generates a class that traverses the fields of a class and records
 * information about the declared types of the fields.
 */
public class TestAnnotationProcessor extends AbstractProcessor {

  public static final String TYPE_DATA_SUFFIX = "__Test";

  private Elements mElements;
  private Types mTypes;
  private Filer mFiler;
  private TypeUtils mTypeUtils;
  private static class State {
    private Map<TypeElement, TypeGathererClassData> mClassElementToInjectorMap;

    State() {
      mClassElementToInjectorMap = Maps.newHashMap();
    }
  }
  private State mState;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    mElements = env.getElementUtils();
    mTypes = env.getTypeUtils();
    mFiler = env.getFiler();
    mTypeUtils = new TypeUtils(mTypes, /*Console.getNullMessager()*/ env.getMessager());
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> supportTypes = Sets.newLinkedHashSet();
    supportTypes.add(TypeTesting.class.getCanonicalName());
    supportTypes.add(MarkedTypes.class.getCanonicalName());

    return supportTypes;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
    try {
      // each round of processing requires a clean state.
      mState = new State();

      gatherClassAnnotations(env);
      gatherFieldAnnotations(env);

      for (Map.Entry<TypeElement, TypeGathererClassData> entry :
          mState.mClassElementToInjectorMap.entrySet()) {
        TypeElement typeElement = entry.getKey();
        TypeGathererClassData injector = entry.getValue();

        try {
          JavaFileObject jfo = mFiler.createSourceFile(injector.getInjectedFqcn(), typeElement);
          Writer writer = jfo.openWriter();
          writer.write(injector.getJavaCode(Console.getNullMessager()));
          writer.flush();
          writer.close();
        } catch (IOException e) {
          error(typeElement, "Unable to write injector for type %s: %s",
              typeElement, e.getMessage());
        }
      }

      return true;
    } catch (Throwable ex) {
      error("exception: %s cause: %s", ex.toString(), ex.getCause());
      return false;
    }
  }

  private void gatherClassAnnotations(RoundEnvironment env) {
    // Process each @TypeTesting elements.
    for (Element element : env.getElementsAnnotatedWith(MarkedTypes.class)) {
      try {
        processClassAnnotation(element);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate view injector for @TypeTesting.\n\n%s",
            stackTrace.toString());
      }
    }

    for (Map.Entry<TypeElement, TypeGathererClassData> entry :
        mState.mClassElementToInjectorMap.entrySet()) {
      TypeMirror superclass = entry.getKey().getSuperclass();

      // walk up the superclass hierarchy until we find another class we know about.
      while (superclass.getKind() != TypeKind.NONE) {
        TypeElement element = (TypeElement) mTypes.asElement(superclass);

        TypeGathererClassData injector = mState.mClassElementToInjectorMap.get(element);
        if (injector != null) {
          entry.getValue().setParentClassData(injector);
          break;
        }

        superclass = element.getSuperclass();
      }
    }
  }

  private void processClassAnnotation(Element element) {
    TypeElement typeElement = (TypeElement) element;

    // Verify containing class visibility is not private.
    if (element.getModifiers().contains(PRIVATE)) {
      error(element, "@%s %s may not be applied to private classes. (%s.%s)",
          MarkedTypes.class.getSimpleName(), typeElement.getQualifiedName(),
          element.getSimpleName());
      return;
    }

    TypeGathererClassData injector = mState.mClassElementToInjectorMap.get(typeElement);
    if (injector == null) {
      String packageName = mTypeUtils.getPackageName(mElements, typeElement);
      injector = new TypeGathererClassData(
          packageName,
          typeElement.getQualifiedName().toString(),
          mTypeUtils.getClassName(typeElement, packageName),
          mTypeUtils.getPrefixForGeneratedClass(typeElement, packageName) + TYPE_DATA_SUFFIX,
          new ProcessorClassData.AnnotationRecordFactory<String, FieldData>() {

            @Override
            public FieldData createAnnotationRecord(String key) {
              return new FieldData();
            }
          });
      mState.mClassElementToInjectorMap.put(typeElement, injector);
    }
  }

  private void gatherFieldAnnotations(RoundEnvironment env) {
    // Process each @TypeTesting elements.
    for (Element element : env.getElementsAnnotatedWith(TypeTesting.class)) {
      try {
        processFieldAnnotation(element);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate view injector for @TypeTesting.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  private void processFieldAnnotation(Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isFieldAnnotationValid(TypeTesting.class, element)) {
      return;
    }

    TypeMirror type = element.asType();

    TypeGathererClassData injector = mState.mClassElementToInjectorMap.get(enclosingElement);

    FieldData data = injector.getOrCreateRecord(element.getSimpleName().toString());
    if (data.mIsList =
        (mTypeUtils.getCollectionType(type) != TypeUtils.CollectionType.NOT_A_COLLECTION)) {
      // inspect the inner type.
      type = mTypeUtils.getCollectionParameterizedType(type);
    }

    data.mParseType = mTypeUtils.getParseType(type, MarkedTypes.class);
    if (data.mParseType == TypeUtils.ParseType.PARSABLE_OBJECT) {
      TypeMirror erasedType = mTypes.erasure(type);
      DeclaredType declaredType = (DeclaredType) erasedType;
      TypeElement typeElement = (TypeElement) declaredType.asElement();

      String packageName = mTypeUtils.getPackageName(mElements, typeElement);
      String className = mTypeUtils.getClassName(typeElement, packageName);
      String parserClassName = mTypeUtils.getPrefixForGeneratedClass(typeElement, packageName);

      data.mParsableType = packageName + "." + className;
      data.mParsableTypeGeneratedClass = packageName + "." + parserClassName;
    }
  }

  private boolean isFieldAnnotationValid(Class<? extends Annotation> annotationClass,
      Element element) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify containing type.
    if (enclosingElement.getKind() != CLASS) {
      error(enclosingElement, "@%s field may only be contained in classes. (%s.%s)",
          annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    Annotation annotation = enclosingElement.getAnnotation(MarkedTypes.class);
    if (annotation == null) {
      error(enclosingElement,
          "@%s field may only be contained in classes annotated with @%s (%s.%s)",
          annotationClass.getSimpleName(),
          MarkedTypes.class.toString(),
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      return false;
    }

    // Verify containing class visibility is not private.
    if (enclosingElement.getModifiers().contains(PRIVATE)) {
      error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
          annotationClass.getSimpleName(),
          enclosingElement.getQualifiedName(),
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
}
