/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Verifies that we can correctly identify the declared type of fields. */
public class TypeUtilsTest {

  @Test
  public void testTypeIdentification() {
    TypeInspectionUUT object = new TypeInspectionUUT();
    TypeInspectionUUT__Test.injectTypeData(object);

    verifyBaseClass(object);
  }

  @Test
  public void testSubclassTypeIdentification() {
    SubclassUUT object = new SubclassUUT();
    SubclassUUT__Test.injectTypeData(object);

    assertFalse(object.subclassInteger__IsList);
    assertEquals(object.subclassInteger__ParseType, TypeUtils.ParseType.INTEGER.toString());

    verifyBaseClass(object);
  }

  private void verifyBaseClass(TypeInspectionUUT object) {
    assertFalse(object.primitiveBoolean__IsList);
    assertEquals(object.primitiveBoolean__ParseType, TypeUtils.ParseType.BOOLEAN.toString());
    assertNull(object.primitiveBoolean__ParseTypeGeneratedClass);

    assertFalse(object.boxedBoolean__IsList);
    assertEquals(object.boxedBoolean__ParseType, TypeUtils.ParseType.BOOLEAN_OBJECT.toString());
    assertNull(object.boxedBoolean__ParseTypeGeneratedClass);

    assertFalse(object.primitiveInteger__IsList);
    assertEquals(object.primitiveInteger__ParseType, TypeUtils.ParseType.INTEGER.toString());
    assertNull(object.primitiveInteger__ParseTypeGeneratedClass);

    assertFalse(object.boxedInteger__IsList);
    assertEquals(object.boxedInteger__ParseType, TypeUtils.ParseType.INTEGER_OBJECT.toString());
    assertNull(object.boxedInteger__ParseTypeGeneratedClass);

    assertFalse(object.primitiveLong__IsList);
    assertEquals(object.primitiveLong__ParseType, TypeUtils.ParseType.LONG.toString());
    assertNull(object.primitiveLong__ParseTypeGeneratedClass);

    assertFalse(object.boxedLong__IsList);
    assertEquals(object.boxedLong__ParseType, TypeUtils.ParseType.LONG_OBJECT.toString());
    assertNull(object.boxedLong__ParseTypeGeneratedClass);

    assertFalse(object.primitiveFloat__IsList);
    assertEquals(object.primitiveFloat__ParseType, TypeUtils.ParseType.FLOAT.toString());
    assertNull(object.primitiveFloat__ParseTypeGeneratedClass);

    assertFalse(object.boxedFloat__IsList);
    assertEquals(object.boxedFloat__ParseType, TypeUtils.ParseType.FLOAT_OBJECT.toString());
    assertNull(object.boxedFloat__ParseTypeGeneratedClass);

    assertFalse(object.primitiveDouble__IsList);
    assertEquals(object.primitiveDouble__ParseType, TypeUtils.ParseType.DOUBLE.toString());
    assertNull(object.primitiveDouble__ParseTypeGeneratedClass);

    assertFalse(object.boxedDouble__IsList);
    assertEquals(object.boxedDouble__ParseType, TypeUtils.ParseType.DOUBLE_OBJECT.toString());
    assertNull(object.boxedDouble__ParseTypeGeneratedClass);

    assertFalse(object.enumInstance__IsList);
    assertEquals(object.enumInstance__ParseType, TypeUtils.ParseType.ENUM_OBJECT.toString());
    assertNull(object.enumInstance__ParseTypeGeneratedClass);

    assertFalse(object.string__IsList);
    assertEquals(object.string__ParseType, TypeUtils.ParseType.STRING.toString());
    assertNull(object.string__ParseTypeGeneratedClass);

    assertTrue(object.integerList__IsList);
    assertEquals(object.integerList__ParseType, TypeUtils.ParseType.INTEGER_OBJECT.toString());
    assertNull(object.integerList__ParseTypeGeneratedClass);

    assertFalse(object.integerInheritedList__IsList);
    assertEquals(
        object.integerInheritedList__ParseType, TypeUtils.ParseType.UNSUPPORTED.toString());
    assertNull(object.integerInheritedList__ParseTypeGeneratedClass);

    assertFalse(object.unspecifiedInheritedList__IsList);
    assertEquals(
        object.unspecifiedInheritedList__ParseType, TypeUtils.ParseType.UNSUPPORTED.toString());
    assertNull(object.unspecifiedInheritedList__ParseTypeGeneratedClass);

    assertFalse(object.nestedData__IsList);
    assertEquals(object.nestedData__ParseType, TypeInspectionUUT.class.getCanonicalName());
    assertEquals(
        object.nestedData__ParseTypeGeneratedClass,
        transformName(TypeInspectionUUT.class.getName()));

    assertFalse(object.nestedInnerClassData__IsList);
    assertEquals(
        object.nestedInnerClassData__ParseType,
        TypeInspectionUUT.InnerClassUUT.class.getCanonicalName());
    assertEquals(
        object.nestedInnerClassData__ParseTypeGeneratedClass,
        transformName(TypeInspectionUUT.InnerClassUUT.class.getName()));

    assertTrue(object.nestedDataList__IsList);
    assertEquals(object.nestedDataList__ParseType, TypeInspectionUUT.class.getCanonicalName());
    assertEquals(
        object.nestedDataList__ParseTypeGeneratedClass,
        transformName(TypeInspectionUUT.class.getName()));

    assertTrue(object.nestedInnerClassDataList__IsList);
    assertEquals(
        object.nestedInnerClassDataList__ParseType,
        TypeInspectionUUT.InnerClassUUT.class.getCanonicalName());
    assertEquals(
        object.nestedInnerClassDataList__ParseTypeGeneratedClass,
        transformName(TypeInspectionUUT.InnerClassUUT.class.getName()));
  }

  /**
   * Inner classes are represented in java as OUTER_CLASS$INNER_CLASS. Since we want to avoid the
   * usage of $ in the generated class names, we replace the $ with a _.
   */
  private String transformName(String stringName) {
    return stringName.replace('$', '_');
  }
}
