/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor

import com.google.testing.compile.CompilationRule
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlin.reflect.KClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Testing [AnnotationMirrorUtils] */
class AnnotationMirrorUtilsTest {

  @Rule @JvmField var compilationRule = CompilationRule()

  private lateinit var elements: Elements
  private lateinit var types: Types

  @Before
  fun setUp() {
    elements = compilationRule.elements
    types = compilationRule.types
  }

  @Test
  fun getAnnotationValueAsTypeElementReturnsClassFromAnnotation() {
    val element = elements.getTypeElement(MyClass::class.java.canonicalName)

    val typeElement =
        AnnotationMirrorUtils.getAnnotationValueAsTypeElement(
            element, types, MyAnnotation::class.java, "elementName")

    assertEquals(ExpectedClass::class.java.name, typeElement!!.qualifiedName.toString())
  }

  @Test
  fun getAnnotationValueAsTypeElementReturnsNullIfElementNameIsWrong() {
    val element = elements.getTypeElement(MyClass::class.java.canonicalName)

    val typeElement =
        AnnotationMirrorUtils.getAnnotationValueAsTypeElement(
            element, types, MyAnnotation::class.java, "wrongElementName")

    assertNull(typeElement)
  }
}

class ExpectedClass

annotation class MyAnnotation(val elementName: KClass<*>)

@MyAnnotation(elementName = ExpectedClass::class) class MyClass
