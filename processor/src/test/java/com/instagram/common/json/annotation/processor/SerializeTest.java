// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.collect.Lists;
import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Basic serialization tests.  It depends on deserialization working correctly but that's an
 * acceptable assumption since if deserialization is broken, {@link DeserializeTest} should fail.
 */
public class SerializeTest {
  @Test
  public void simpleSerializeTest() throws IOException, JSONException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final List<Integer> integerList = Lists.newArrayList(1, 2, 3, 4);
    final int subIntValue = 30;

    SimpleParseUUT source = new SimpleParseUUT();
    source.intField = intValue;
    source.integerField = integerValue;
    source.stringField = stringValue;
    source.integerListField = integerList;
    source.subobjectField = new SimpleParseUUT.SubobjectParseUUT();
    source.subobjectField.intField = subIntValue;

    StringWriter stringWriter = new StringWriter();
    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stringWriter);

    SimpleParseUUT__JsonHelper.serializeToJson(jsonGenerator, source, true);
    jsonGenerator.close();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT parsed = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertSame(source.intField, parsed.intField);
    assertEquals(source.integerField, parsed.integerField);
    assertEquals(source.stringField, parsed.stringField);
    assertEquals(source.integerListField, parsed.integerListField);
    assertSame(source.subobjectField.intField, parsed.subobjectField.intField);
  }

  @Test
  public void stringSerializeTest() throws IOException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final List<Integer> integerList = Lists.newArrayList(1, 2, 3, 4);
    final int subIntValue = 30;

    SimpleParseUUT source = new SimpleParseUUT();
    source.intField = intValue;
    source.integerField = integerValue;
    source.stringField = stringValue;
    source.integerListField = integerList;
    source.subobjectField = new SimpleParseUUT.SubobjectParseUUT();
    source.subobjectField.intField = subIntValue;

    String serialized = SimpleParseUUT__JsonHelper.serializeToJson(source);
    SimpleParseUUT parsed = SimpleParseUUT__JsonHelper.parseFromJson(serialized);

    assertSame(source.intField, parsed.intField);
    assertEquals(source.integerField, parsed.integerField);
    assertEquals(source.stringField, parsed.stringField);
    assertEquals(source.integerListField, parsed.integerListField);
    assertSame(source.subobjectField.intField, parsed.subobjectField.intField);
  }

  @Test
  public void enumTest() throws IOException {
    final EnumUUT.EnumType value = EnumUUT.EnumType.VALUE3;

    EnumUUT source = new EnumUUT();
    source.enumField = value;

    String serialized = EnumUUT__JsonHelper.serializeToJson(source);
    EnumUUT parsed = EnumUUT__JsonHelper.parseFromJson(serialized);

    assertSame(source.enumField, parsed.enumField);
  }

  @Test
  public void nullObject() throws IOException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final List<Integer> integerList = Lists.newArrayList(1, 2, 3, 4);
    final int subIntValue = 30;

    SimpleParseUUT source = new SimpleParseUUT();
    source.intField = intValue;
    // intentionally do not set integerField
    source.stringField = stringValue;
    source.integerListField = integerList;
    source.subobjectField = new SimpleParseUUT.SubobjectParseUUT();
    source.subobjectField.intField = subIntValue;

    String serialized = SimpleParseUUT__JsonHelper.serializeToJson(source);
    SimpleParseUUT parsed = SimpleParseUUT__JsonHelper.parseFromJson(serialized);

    assertSame(source.intField, parsed.intField);
    assertNull(parsed.integerField);
    assertEquals(source.stringField, parsed.stringField);
    assertEquals(source.integerListField, parsed.integerListField);
    assertSame(source.subobjectField.intField, parsed.subobjectField.intField);
  }

  @Test
  public void nullArray() throws IOException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final int subIntValue = 30;

    SimpleParseUUT source = new SimpleParseUUT();
    source.intField = intValue;
    source.integerField = integerValue;
    source.stringField = stringValue;
    // intentionally do not set integerListField
    source.subobjectField = new SimpleParseUUT.SubobjectParseUUT();
    source.subobjectField.intField = subIntValue;

    String serialized = SimpleParseUUT__JsonHelper.serializeToJson(source);
    SimpleParseUUT parsed = SimpleParseUUT__JsonHelper.parseFromJson(serialized);

    assertSame(source.intField, parsed.intField);
    assertEquals(source.integerField, parsed.integerField);
    assertEquals(source.stringField, parsed.stringField);
    assertNull(parsed.integerListField);
    assertSame(source.subobjectField.intField, parsed.subobjectField.intField);
  }

  @Test
  public void nullArrayEntry() throws IOException {
    final int intValue = 25;
    final int integerValue = 37;
    final String stringValue = "hello world\r\n\'\"";
    final List<Integer> integerList = Lists.newArrayList(1, 2, 3, null);
    final int subIntValue = 30;

    SimpleParseUUT source = new SimpleParseUUT();
    source.intField = intValue;
    source.integerField = integerValue;
    source.stringField = stringValue;
    source.integerListField = integerList;
    source.subobjectField = new SimpleParseUUT.SubobjectParseUUT();
    source.subobjectField.intField = subIntValue;

    String serialized = SimpleParseUUT__JsonHelper.serializeToJson(source);
    SimpleParseUUT parsed = SimpleParseUUT__JsonHelper.parseFromJson(serialized);

    assertSame(source.intField, parsed.intField);
    assertEquals(source.integerField, parsed.integerField);
    assertEquals(source.stringField, parsed.stringField);
    assertEquals(source.integerListField.size() - 1, parsed.integerListField.size());
    for (int ix = 0; ix < source.integerListField.size() - 1; ix ++) {
      assertEquals(source.integerListField.get(ix), parsed.integerListField.get(ix));
    }
    assertSame(source.subobjectField.intField, parsed.subobjectField.intField);
  }
}
