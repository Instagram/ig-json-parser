/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import java.util.Locale;
import javax.lang.model.element.ElementKind;

class AccessorMetadata {
  final TypeData.DeserializeType deserializeType;
  final TypeData.SerializeType serializeType;
  final String getterName;
  final String setterName;
  final String memberVariableName;

  AccessorMetadata(
      TypeData.DeserializeType deserializeType,
      TypeData.SerializeType serializeType,
      String getterName,
      String setterName,
      String memberVariableName) {
    this.deserializeType = deserializeType;
    this.serializeType = serializeType;
    this.getterName = getterName;
    this.setterName = setterName;
    this.memberVariableName = memberVariableName;
  }

  static AccessorMetadata create(
      String simpleName,
      boolean isStrict,
      boolean isKotlin,
      boolean useGetters,
      ElementKind elementKind) {
    TypeData.DeserializeType deserializeType = TypeData.DeserializeType.NONE;
    TypeData.SerializeType serializeType = TypeData.SerializeType.NONE;
    String getterName = null;
    String setterName = null;
    String memberVariableName = null;
    if (isStrict && elementKind == ElementKind.PARAMETER) {
      deserializeType = TypeData.DeserializeType.PARAM;
      serializeType = TypeData.SerializeType.GETTER;
      getterName = getGetterName(simpleName, isKotlin);
    } else if (isKotlin) {
      deserializeType = TypeData.DeserializeType.SETTER;
      setterName = getSetterName(simpleName, isKotlin);
      serializeType = TypeData.SerializeType.GETTER;
      getterName = getGetterName(simpleName, isKotlin);
    } else {
      deserializeType = TypeData.DeserializeType.FIELD;
      memberVariableName = simpleName;
      if (useGetters) {
        serializeType = TypeData.SerializeType.GETTER;
        getterName = getGetterName(simpleName, isKotlin);
      } else {
        serializeType = TypeData.SerializeType.FIELD;
      }
    }
    return new AccessorMetadata(
        deserializeType, serializeType, getterName, setterName, memberVariableName);
  }

  boolean checkMetadataMismatch(TypeData data) {
    return (data.getDeserializeType() != TypeData.DeserializeType.NONE
            && data.getDeserializeType() != deserializeType)
        || (data.getSerializeType() != TypeData.SerializeType.NONE
            && data.getSerializeType() != serializeType)
        || (data.getGetterName() != null && !data.getGetterName().equals(getterName))
        || (data.getSetterName() != null && !data.getSetterName().equals(setterName))
        || (data.getMemberVariableName() != null
            && !data.getMemberVariableName().equals(memberVariableName));
  }

  static String getGetterName(String fieldName, boolean isKotlin) {
    if (isKotlinIsSpecialPrefixCase(fieldName, isKotlin)) {
      return fieldName;
    } else {
      return "get" + capitalize(fieldName);
    }
  }

  static String getSetterName(String fieldName, boolean isKotlin) {
    if (isKotlinIsSpecialPrefixCase(fieldName, isKotlin)) {
      return "set" + capitalize(fieldName.substring(2));
    } else {
      return "set" + capitalize(fieldName);
    }
  }

  static boolean isKotlinIsSpecialPrefixCase(String fieldName, boolean isKotlin) {
    return isKotlin
        && fieldName.length() > 2
        && fieldName.startsWith("is")
        && Character.isUpperCase(fieldName.charAt(2));
  }

  static String capitalize(String str) {
    return String.valueOf(str.charAt(0)).toUpperCase(Locale.getDefault()) + str.substring(1);
  }
}
