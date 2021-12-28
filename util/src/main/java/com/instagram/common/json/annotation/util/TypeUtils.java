/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** Utility functions to get the declared types of fields. */
public class TypeUtils {

  public enum ParseType {
    UNSUPPORTED,
    BOOLEAN,
    BOOLEAN_OBJECT,
    INTEGER,
    INTEGER_OBJECT,
    LONG,
    LONG_OBJECT,
    FLOAT,
    FLOAT_OBJECT,
    DOUBLE,
    DOUBLE_OBJECT,
    STRING,
    PARSABLE_OBJECT,
    ENUM_OBJECT,
    WILDCARD,
  }

  public enum CollectionType {
    NOT_A_COLLECTION,
    LIST,
    ARRAYLIST,
    HASHMAP,
    QUEUE,
    SET,
  }

  private static final String JAVA_LANG_STRING = "java.lang.String";
  private static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
  private static final String JAVA_LANG_INTEGER = "java.lang.Integer";
  private static final String JAVA_LANG_LONG = "java.lang.Long";
  private static final String JAVA_LANG_FLOAT = "java.lang.Float";
  private static final String JAVA_LANG_DOUBLE = "java.lang.Double";
  private static final String JAVA_UTIL_LIST = "java.util.List<?>";
  private static final String JAVA_UTIL_LIST_UNTYPED = "java.util.List";
  private static final String JAVA_UTIL_ARRAYLIST = "java.util.ArrayList<?>";
  private static final String JAVA_UTIL_ARRAYLIST_UNTYPED = "java.util.ArrayList";
  private static final String JAVA_UTIL_HASHMAP = "java.util.HashMap<?,?>";
  private static final String JAVA_UTIL_HASHMAP_UNTYPED = "java.util.HashMap";
  private static final String JAVA_UTIL_QUEUE = "java.util.Queue<?>";
  private static final String JAVA_UTIL_QUEUE_UNTYPED = "java.util.Queue";
  private static final String JAVA_UTIL_SET = "java.util.Set<?>";
  private static final String JAVA_UTIL_SET_UNTYPED = "java.util.Set";
  private static final String JAVA_LANG_ENUM = "java.lang.Enum<?>";

  private final Types mTypes;
  private final Messager mMessager;

  public TypeUtils(Types types, Messager messager) {
    mTypes = types;
    mMessager = messager;
  }

  public ParseType getParseType(
      TypeMirror typeMirror, @Nullable Class<? extends Annotation> typeAnnotationClass) {
    if (typeMirror == null) {
      return ParseType.UNSUPPORTED;
    } else if (JAVA_LANG_STRING.equals(typeMirror.toString())) {
      return ParseType.STRING;
    } else if (typeMirror.getKind() == TypeKind.BOOLEAN) {
      return ParseType.BOOLEAN;
    } else if (JAVA_LANG_BOOLEAN.equals(typeMirror.toString())) {
      return ParseType.BOOLEAN_OBJECT;
    } else if (typeMirror.getKind() == TypeKind.INT) {
      return ParseType.INTEGER;
    } else if (JAVA_LANG_INTEGER.equals(typeMirror.toString())) {
      return ParseType.INTEGER_OBJECT;
    } else if (typeMirror.getKind() == TypeKind.LONG) {
      return ParseType.LONG;
    } else if (JAVA_LANG_LONG.equals(typeMirror.toString())) {
      return ParseType.LONG_OBJECT;
    } else if (typeMirror.getKind() == TypeKind.FLOAT) {
      return ParseType.FLOAT;
    } else if (JAVA_LANG_FLOAT.equals(typeMirror.toString())) {
      return ParseType.FLOAT_OBJECT;
    } else if (typeMirror.getKind() == TypeKind.DOUBLE) {
      return ParseType.DOUBLE;
    } else if (JAVA_LANG_DOUBLE.equals(typeMirror.toString())) {
      return ParseType.DOUBLE_OBJECT;
    } else if (typeMirror.getKind() == TypeKind.WILDCARD) {
      // If it's a wildcard, assume that annotations are added to properly parse it.
      return ParseType.PARSABLE_OBJECT;
    } else if (typeMirror instanceof DeclaredType) {
      DeclaredType type = (DeclaredType) typeMirror;
      Element element = type.asElement();

      Annotation annotation =
          typeAnnotationClass != null ? element.getAnnotation(typeAnnotationClass) : null;
      if (annotation != null && EnumSet.of(CLASS, INTERFACE).contains(element.getKind())) {
        return ParseType.PARSABLE_OBJECT;
      }

      // is it an enum?
      if (element instanceof TypeElement) {
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass instanceof DeclaredType) {
          DeclaredType superclassDeclaredType = (DeclaredType) superclass;

          if (JAVA_LANG_ENUM.equals(getCanonicalTypeName(superclassDeclaredType))) {
            return ParseType.ENUM_OBJECT;
          }
        }
      }
    }

    return ParseType.UNSUPPORTED;
  }

  public static boolean isMapType(CollectionType type) {
    return type == CollectionType.HASHMAP;
  }

  public CollectionType getCollectionType(TypeMirror typeMirror) {
    String erasedType = mTypes.erasure(typeMirror).toString();
    if (JAVA_UTIL_LIST_UNTYPED.equals(erasedType)) {
      return CollectionType.LIST;
    } else if (JAVA_UTIL_ARRAYLIST_UNTYPED.equals(erasedType)) {
      return CollectionType.ARRAYLIST;
    } else if (JAVA_UTIL_HASHMAP_UNTYPED.equals(erasedType)) {
      return CollectionType.HASHMAP;
    } else if (JAVA_UTIL_QUEUE_UNTYPED.equals(erasedType)) {
      return CollectionType.QUEUE;
    } else if (JAVA_UTIL_SET_UNTYPED.equals(erasedType)) {
      return CollectionType.SET;
    }
    return CollectionType.NOT_A_COLLECTION;
  }

  /**
   * If {@code typeMirror} represents a list type ({@link java.util.List}), attempt to divine the
   * type of the contents.
   *
   * <p>Returns null if {@code typeMirror} does not represent a list type or if we cannot divine the
   * type of the contents.
   */
  @Nullable
  public TypeMirror getCollectionParameterizedType(TypeMirror typeMirror) {
    if (!(typeMirror instanceof DeclaredType)) {
      return null;
    }
    DeclaredType declaredType = (DeclaredType) typeMirror;
    Element element = declaredType.asElement();
    if (!(element instanceof TypeElement)) {
      return null;
    }
    TypeElement typeElement = (TypeElement) element;
    List<? extends TypeParameterElement> typeParameterElements = typeElement.getTypeParameters();
    List<TypeMirror> typeArguments = (List<TypeMirror>) declaredType.getTypeArguments();

    if (JAVA_UTIL_QUEUE.equals(getCanonicalTypeName(declaredType))
        || JAVA_UTIL_LIST.equals(getCanonicalTypeName(declaredType))
        || JAVA_UTIL_ARRAYLIST.equals(getCanonicalTypeName(declaredType))
        || JAVA_UTIL_SET.equals(getCanonicalTypeName(declaredType))) {
      // sanity check.
      if (typeParameterElements.size() != 1) {
        throw new IllegalStateException(
            String.format("%s is not expected generic type", declaredType));
      }
      return typeArguments.get(0);
    } else if (JAVA_UTIL_HASHMAP.equals(getCanonicalTypeName(declaredType))) {
      // sanity check.
      if (typeParameterElements.size() != 2) {
        throw new IllegalStateException(
            String.format("%s is not expected generic type", declaredType));
      }
      TypeMirror keyType = typeArguments.get(0);
      TypeMirror valueType = typeArguments.get(1);
      if (!JAVA_LANG_STRING.equals(keyType.toString())) {
        throw new IllegalStateException("Only String keys are supported for map types");
      }
      return valueType;
    }
    return null;
  }

  /**
   * This returns the class name of the type as one would use to reference in code. For most cases,
   * this is pretty straightforward. Inner classes are used with . notation, i.e., if class Y is an
   * inner class of class X, then class Y's class name should be X.Y.
   */
  public String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen);
  }

  /**
   * This returns the prefix used to refer to the generated class. This is different because we
   * generate individual source files for each inner class. For instance, if we have class X with
   * inner classes Y and Z, then we generate three source files.
   *
   * <p>To make this work, we replace the normal dot notation between an outer class and an inner
   * class with a '_', i.e., the generated class for class X will be X_Y&lt;suffix&gt;.
   */
  @Nullable
  public String getPrefixForGeneratedClass(TypeElement type, String packageName) {
    // Interfaces do not currently generate classes
    if (type.getKind() == INTERFACE) {
      return null;
    }
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '_');
  }

  public String getPackageName(Elements elements, TypeElement type) {
    return elements.getPackageOf(type).getQualifiedName().toString();
  }

  /**
   * Returns a string with type parameters replaced with wildcards. This is slightly different from
   * {@link Types#erasure(javax.lang.model.type.TypeMirror)}, which removes all type parameter data.
   *
   * <p>For instance, if there is a field with type List&lt;String&gt;, this returns a string
   * List&lt;?&gt;.
   */
  private String getCanonicalTypeName(DeclaredType declaredType) {
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (!typeArguments.isEmpty()) {
      StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
      typeString.append('<');
      for (int i = 0; i < typeArguments.size(); i++) {
        if (i > 0) {
          typeString.append(',');
        }
        typeString.append('?');
      }
      typeString.append('>');

      return typeString.toString();
    } else {
      return declaredType.toString();
    }
  }
}
