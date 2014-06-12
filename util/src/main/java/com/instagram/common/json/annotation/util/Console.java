// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import java.util.Locale;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Utility functions to write to the compiler log.
 */
public class Console {
  public static void error(Messager messager, String message, Object... args) {
    messager.printMessage(ERROR, String.format(Locale.US, message, args));
  }

  public static void error(Messager messager, Element element, String message, Object... args) {
    messager.printMessage(ERROR, String.format(Locale.US, message, args), element);
  }

  public static void warning(Messager messager, String message, Object... args) {
    messager.printMessage(WARNING, String.format(Locale.US, message, args));
  }

  /**
   * Returns a messager that swallows all its output.
   */
  public static Messager getNullMessager() {
    return new Messager() {
      @Override
      public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
      }

      @Override
      public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
      }

      @Override
      public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e,
          AnnotationMirror a) {
      }

      @Override
      public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e,
          AnnotationMirror a, AnnotationValue v) {
      }
    };
  }
}
