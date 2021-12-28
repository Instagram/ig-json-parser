/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/** Convenience methods for dealing with {@link AnnotationMirror} */
public class AnnotationMirrorUtils {

  @Nullable
  public static TypeElement getAnnotationValueAsTypeElement(
      Element element,
      Types types,
      Class<? extends Annotation> annotationClass,
      String elementName) {
    AnnotationMirror annotationMirror = getAnnotationMirror(element, annotationClass);
    if (annotationMirror == null) {
      return null;
    }
    AnnotationValue annotationValue = getAnnotationValue(annotationMirror, elementName);
    if (annotationValue == null) {
      return null;
    }
    return (TypeElement) types.asElement((TypeMirror) annotationValue.getValue());
  }

  @Nullable
  private static AnnotationValue getAnnotationValue(
      AnnotationMirror annotationMirror, String elementName) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        annotationMirror.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(elementName)) {
        return entry.getValue();
      }
    }
    return null;
  }

  @Nullable
  private static AnnotationMirror getAnnotationMirror(
      Element element, Class<? extends Annotation> annotationClass) {
    String annotationClassName = annotationClass.getName();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationMirror.getAnnotationType().toString().equals(annotationClassName)) {
        return annotationMirror;
      }
    }
    return null;
  }
}
