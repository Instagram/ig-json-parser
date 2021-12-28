/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static javax.tools.Diagnostic.Kind.WARNING;

import java.util.Locale;
import java.util.WeakHashMap;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/** Utility functions to write to the compiler log. */
public class Console {
  // Contains Messagers for which errors have been reported through this class.
  //
  // In a build system that does in-process compilation (like Buck), it is possible for multiple
  // instances of our annotation processor to be running in parallel. Because this information
  // is only needed for a (hopefully short term) workaround, rather than rework Console to be
  // an instance class per ProcessingEnvironment, we take advantage of some implementation
  // details. Each instance of our processor will be in a different ProcessingEnvironment, and
  // each ProcessingEnvironment has its own Messager, so we can just track the Messagers that
  // have been used to report errors.
  @GuardedBy("Console.class")
  private static final WeakHashMap<Messager, Boolean> messagersWithErrors =
      new WeakHashMap<Messager, Boolean>();

  public static void error(Messager messager, String message, Object... args) {
    setError(messager);
    messager.printMessage(MANDATORY_WARNING, String.format(Locale.US, "ERROR: " + message, args));
  }

  public static void error(Messager messager, Element element, String message, Object... args) {
    setError(messager);
    messager.printMessage(
        MANDATORY_WARNING, String.format(Locale.US, "ERROR: " + message, args), element);
  }

  public static void warning(Messager messager, String message, Object... args) {
    messager.printMessage(WARNING, String.format(Locale.US, message, args));
  }

  /**
   * There's a bug in javac whereby it ignores all generated files if any processor raises an error.
   * This can result in spurious undefined symbol errors that can hide the true problem. Our
   * workaround is to convert errors to warnings during processing, then come back in the last round
   * and report an error.
   */
  public static void reportErrors(RoundEnvironment round, Messager messager) {
    if (round.processingOver() && hasError(messager)) {
      messager.printMessage(
          ERROR,
          "Errors were encountered during annotation processing. "
              + "See the warnings above for details. "
              + "(Errors were converted to warnings to work around a compiler bug.)");
    }
  }

  private static synchronized void setError(Messager messager) {
    messagersWithErrors.put(messager, Boolean.TRUE);
  }

  private static synchronized boolean hasError(Messager messager) {
    return messagersWithErrors.get(messager) == Boolean.TRUE;
  }

  /** Returns a messager that swallows all its output. */
  public static Messager getNullMessager() {
    return new Messager() {
      @Override
      public void printMessage(Diagnostic.Kind kind, CharSequence msg) {}

      @Override
      public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {}

      @Override
      public void printMessage(
          Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {}

      @Override
      public void printMessage(
          Diagnostic.Kind kind,
          CharSequence msg,
          Element e,
          AnnotationMirror a,
          AnnotationValue v) {}
    };
  }
}
