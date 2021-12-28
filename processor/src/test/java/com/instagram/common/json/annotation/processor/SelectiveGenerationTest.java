/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.fasterxml.jackson.core.JsonGenerator;
import com.instagram.common.json.annotation.processor.noserializers.NoSerializerGlobalUUT;
import com.instagram.common.json.annotation.processor.noserializers.NoSerializerGlobalUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.NoSerializerClassUUT;
import com.instagram.common.json.annotation.processor.uut.NoSerializerClassUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.SimpleParseUUT;
import com.instagram.common.json.annotation.processor.uut.SimpleParseUUT__JsonHelper;
import java.lang.reflect.Method;
import org.junit.Test;

public class SelectiveGenerationTest {

  @Test
  public void testMissingSerializersGlobalSwitch() throws Exception {

    Method serializeMethod =
        SimpleParseUUT__JsonHelper.class.getMethod("serializeToJson", SimpleParseUUT.class);
    assertNotNull(serializeMethod);

    serializeMethod =
        SimpleParseUUT__JsonHelper.class.getMethod(
            "serializeToJson", JsonGenerator.class, SimpleParseUUT.class, boolean.class);
    assertNotNull(serializeMethod);

    try {
      serializeMethod = null;
      serializeMethod =
          NoSerializerGlobalUUT__JsonHelper.class.getMethod(
              "serializeToJson", NoSerializerGlobalUUT.class);
    } catch (NoSuchMethodException ignored) {
    }
    assertNull(serializeMethod);

    try {
      serializeMethod = null;
      serializeMethod =
          NoSerializerGlobalUUT__JsonHelper.class.getMethod(
              "serializeToJson", JsonGenerator.class, NoSerializerGlobalUUT.class, boolean.class);
    } catch (NoSuchMethodException ignored) {
    }
    assertNull(serializeMethod);
  }

  @Test
  public void testMissingSerializersClasSwitch() throws Exception {

    Method serializeMethod =
        SimpleParseUUT__JsonHelper.class.getMethod("serializeToJson", SimpleParseUUT.class);
    assertNotNull(serializeMethod);

    serializeMethod =
        SimpleParseUUT__JsonHelper.class.getMethod(
            "serializeToJson", JsonGenerator.class, SimpleParseUUT.class, boolean.class);
    assertNotNull(serializeMethod);

    try {
      serializeMethod = null;
      serializeMethod =
          NoSerializerClassUUT__JsonHelper.class.getMethod(
              "serializeToJson", NoSerializerClassUUT.class);
    } catch (NoSuchMethodException ignored) {
    }
    assertNull(serializeMethod);

    try {
      serializeMethod = null;
      serializeMethod =
          NoSerializerClassUUT__JsonHelper.class.getMethod(
              "serializeToJson", JsonGenerator.class, NoSerializerClassUUT.class, boolean.class);
    } catch (NoSuchMethodException ignored) {
    }
    assertNull(serializeMethod);
  }
}
