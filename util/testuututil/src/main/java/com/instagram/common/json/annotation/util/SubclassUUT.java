/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.util;

/**
 * Subclass of the type inspection unit under test. This allows us to verify that we can traverse
 * the type hierarchy correctly.
 */
@MarkedTypes
class SubclassUUT extends TypeInspectionUUT {

  @TypeTesting int subclassInteger;

  boolean subclassInteger__IsList;
  String subclassInteger__ParseType;
  String subclassInteger__ParseTypeGeneratedClass;
}
