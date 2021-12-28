/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import org.junit.Test

class NoCompileTest {

  var reuseFieldName =
      """
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class ReuseFieldName {

  @JsonField(fieldName = "value")
  String mValue1;

  @JsonField(fieldName = "value")
  String mValue2;
}
"""

  @Test
  fun reuseFieldNameDoesNotCompile() {
    val compilation =
        javac()
            .withProcessors(JsonAnnotationProcessor())
            .compile(JavaFileObjects.forSourceString("ReuseFieldName", reuseFieldName))
    CompilationSubject.assertThat(compilation)
        .hadErrorContainingMatch("Errors were encountered during annotation processing.")
    CompilationSubject.assertThat(compilation)
        .hadWarningContaining("Detected multiple annotations with the same field name.")
  }
}
