/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.collect.Lists;
import com.instagram.common.json.annotation.processor.support.ExtensibleJSONWriter;
import com.instagram.common.json.annotation.processor.uut.SimpleParseUUT;
import com.instagram.common.json.annotation.processor.uut.SimpleParseUUT__JsonHelper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;

/**
 * Where we attack the parser with poorly formed json, where structures and arrays are interchanged
 * with scalars. These are errors that can result in infinite loops or improper termination of
 * parsing.
 */
public class MalformedJsonTest {
  private final int intValue = 25;
  private final int integerValue = 37;
  private final List<Integer> integerList = Lists.newArrayList(1, 2, 3, 4);

  @Test
  public void arrayInsteadOfScalar() throws IOException, JSONException {
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer
        .object()
        .key(SimpleParseUUT.STRING_FIELD_NAME)
        .array()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Integer integer : integerList) {
                  writer.value(integer);
                }
              }
            })
        .endArray()
        .key(SimpleParseUUT.INT_FIELD_NAME)
        .value(intValue)
        .key(SimpleParseUUT.INTEGER_FIELD_NAME)
        .value(integerValue)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertSame(intValue, uut.intField);
    assertSame(integerValue, uut.integerField.intValue());
    assertNull(uut.stringField);
  }

  @Test
  public void dictInsteadOfScalar() throws IOException, JSONException {
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer
        .object()
        .key(SimpleParseUUT.STRING_FIELD_NAME)
        .object()
        .key("garbage")
        .value("123")
        .endObject()
        .key(SimpleParseUUT.INT_FIELD_NAME)
        .value(intValue)
        .key(SimpleParseUUT.INTEGER_FIELD_NAME)
        .value(integerValue)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertSame(intValue, uut.intField);
    assertSame(integerValue, uut.integerField.intValue());
    assertNull(uut.stringField);
  }

  @Test
  public void scalarInsteadOfArray() throws IOException, JSONException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final int subIntValue = 30;

    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer
        .object()
        .key(SimpleParseUUT.INT_FIELD_NAME)
        .value(intValue)
        .key(SimpleParseUUT.INTEGER_FIELD_NAME)
        .value(integerValue)
        .key(SimpleParseUUT.STRING_FIELD_NAME)
        .value(stringValue)
        .key(SimpleParseUUT.INTEGER_LIST_FIELD_NAME)
        .value(intValue)
        .key(SimpleParseUUT.SUBOBJECT_FIELD_NAME)
        .object()
        .key(SimpleParseUUT.SubobjectParseUUT.INT_FIELD_NAME)
        .value(subIntValue)
        .endObject()
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertSame(intValue, uut.intField);
    assertSame(integerValue, uut.integerField.intValue());
    assertEquals(stringValue, uut.stringField);
    assertNull(uut.integerListField);
    assertSame(subIntValue, uut.subobjectField.intField);
  }

  @Test
  public void scalarInsteadOfObject() throws IOException, JSONException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final List<Integer> integerList = Lists.newArrayList(1, 2, 3, 4);
    final int subIntValue = 30;

    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer
        .object()
        .key(SimpleParseUUT.INT_FIELD_NAME)
        .value(intValue)
        .key(SimpleParseUUT.INTEGER_FIELD_NAME)
        .value(integerValue)
        .key(SimpleParseUUT.STRING_FIELD_NAME)
        .value(stringValue)
        .key(SimpleParseUUT.INTEGER_LIST_FIELD_NAME)
        .array()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Integer integer : integerList) {
                  writer.value(integer);
                }
              }
            })
        .endArray()
        .key(SimpleParseUUT.SUBOBJECT_FIELD_NAME)
        .value(subIntValue)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertSame(intValue, uut.intField);
    assertSame(integerValue, uut.integerField.intValue());
    assertEquals(stringValue, uut.stringField);
    assertEquals(integerList, uut.integerListField);
    assertNull(uut.subobjectField);
  }
}
