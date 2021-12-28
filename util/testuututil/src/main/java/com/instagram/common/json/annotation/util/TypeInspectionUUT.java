/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

import java.util.List;
import java.util.Queue;

/** Basic class that contains all the supported types. */
@MarkedTypes
class TypeInspectionUUT {

  //////
  // primitives and their boxed counterparts.
  @TypeTesting boolean primitiveBoolean;

  boolean primitiveBoolean__IsList;
  String primitiveBoolean__ParseType;
  String primitiveBoolean__ParseTypeGeneratedClass;

  @TypeTesting Boolean boxedBoolean;

  boolean boxedBoolean__IsList;
  String boxedBoolean__ParseType;
  String boxedBoolean__ParseTypeGeneratedClass;

  @TypeTesting int primitiveInteger;

  boolean primitiveInteger__IsList;
  String primitiveInteger__ParseType;
  String primitiveInteger__ParseTypeGeneratedClass;

  @TypeTesting Integer boxedInteger;

  boolean boxedInteger__IsList;
  String boxedInteger__ParseType;
  String boxedInteger__ParseTypeGeneratedClass;

  @TypeTesting long primitiveLong;

  boolean primitiveLong__IsList;
  String primitiveLong__ParseType;
  String primitiveLong__ParseTypeGeneratedClass;

  @TypeTesting Long boxedLong;

  boolean boxedLong__IsList;
  String boxedLong__ParseType;
  String boxedLong__ParseTypeGeneratedClass;

  @TypeTesting float primitiveFloat;

  boolean primitiveFloat__IsList;
  String primitiveFloat__ParseType;
  String primitiveFloat__ParseTypeGeneratedClass;

  @TypeTesting Float boxedFloat;

  boolean boxedFloat__IsList;
  String boxedFloat__ParseType;
  String boxedFloat__ParseTypeGeneratedClass;

  @TypeTesting double primitiveDouble;

  boolean primitiveDouble__IsList;
  String primitiveDouble__ParseType;
  String primitiveDouble__ParseTypeGeneratedClass;

  @TypeTesting Double boxedDouble;

  boolean boxedDouble__IsList;
  String boxedDouble__ParseType;
  String boxedDouble__ParseTypeGeneratedClass;

  enum Foo {
    VALUE0,
    VALUE1,
  };

  @TypeTesting Foo enumInstance;

  boolean enumInstance__IsList;
  String enumInstance__ParseType;
  String enumInstance__ParseTypeGeneratedClass;

  //////
  // strings.
  @TypeTesting String string;

  boolean string__IsList;
  String string__ParseType;
  String string__ParseTypeGeneratedClass;

  //////
  // collection types.
  @TypeTesting List<Integer> integerList;

  boolean integerList__IsList;
  String integerList__ParseType;
  String integerList__ParseTypeGeneratedClass;

  @TypeTesting Queue<Integer> integerQueue;

  boolean integerQueue__IsList;
  String integerQueue__ParseType;
  String integerQueue__ParseTypeGeneratedClass;

  @TypeTesting InheritedExtendsSpecifiesType integerInheritedList;

  boolean integerInheritedList__IsList;
  String integerInheritedList__ParseType;
  String integerInheritedList__ParseTypeGeneratedClass;

  private abstract static class InheritedExtendsSpecifiesType implements List<Integer> {}

  @TypeTesting InheritedExtendsNoType unspecifiedInheritedList;

  boolean unspecifiedInheritedList__IsList;
  String unspecifiedInheritedList__ParseType;
  String unspecifiedInheritedList__ParseTypeGeneratedClass;

  private abstract static class InheritedExtendsNoType implements List {}

  //////
  // nesting
  @TypeTesting TypeInspectionUUT nestedData;

  boolean nestedData__IsList;
  String nestedData__ParseType;
  String nestedData__ParseTypeGeneratedClass;

  @TypeTesting InnerClassUUT nestedInnerClassData;

  boolean nestedInnerClassData__IsList;
  String nestedInnerClassData__ParseType;
  String nestedInnerClassData__ParseTypeGeneratedClass;

  @TypeTesting List<TypeInspectionUUT> nestedDataList;

  boolean nestedDataList__IsList;
  String nestedDataList__ParseType;
  String nestedDataList__ParseTypeGeneratedClass;

  @TypeTesting List<InnerClassUUT> nestedInnerClassDataList;

  boolean nestedInnerClassDataList__IsList;
  String nestedInnerClassDataList__ParseType;
  String nestedInnerClassDataList__ParseTypeGeneratedClass;

  @MarkedTypes
  public static class InnerClassUUT {
    @TypeTesting String string;

    boolean string__IsList;
    String string__ParseType;
    String string__ParseTypeGeneratedClass;
  }
}
