// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import com.instagram.common.json.annotation.processor.support.ExtensibleJSONWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.json.JSONException;
import org.json.JSONWriter;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic deserialization tests.
 */
public class DeserializeTest {
  @Test
  public void simpleDeserializeTest() throws IOException, JSONException {
    final int intValue = 25;
    final int integerValue = 37;
    final float floatValue = 1.0f;
    final float floatObjValue = 2.0f;
    final String stringValue = "hello world\r\n\'\"";
    final List<Integer> integerList = Lists.newArrayList(1, 2, 3, 4);
    final Queue<Integer> integerQueue = Queues.newArrayDeque(Arrays.asList(1, 2, 3, 4));
    final int subIntValue = 30;

    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer.object()
        .key(SimpleParseUUT.INT_FIELD_NAME).value(intValue)
        .key(SimpleParseUUT.INTEGER_FIELD_NAME).value(integerValue)
        .key(SimpleParseUUT.FLOAT_FIELD_NAME).value(floatValue)
        .key(SimpleParseUUT.FLOAT_OBJ_FIELD_NAME).value(floatObjValue)
        .key(SimpleParseUUT.STRING_FIELD_NAME).value(stringValue)
        .key(SimpleParseUUT.INTEGER_LIST_FIELD_NAME)
          .array()
          .extend(new ExtensibleJSONWriter.Extender() {
            @Override
            public void extend(ExtensibleJSONWriter writer) throws JSONException {
              for (Integer integer : integerList) {
                writer.value(integer);
              }
            }
          })
          .endArray()
        .key(SimpleParseUUT.INTEGER_QUEUE_FIELD_NAME)
        .array()
        .extend(new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            for (Integer integer : integerQueue) {
              writer.value(integer);
            }
          }
        })
        .endArray()
        .key(SimpleParseUUT.SUBOBJECT_FIELD_NAME)
          .object()
            .key(SimpleParseUUT.SubobjectParseUUT.INT_FIELD_NAME).value(subIntValue)
          .endObject()
      .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertSame(intValue, uut.intField);
    assertSame(integerValue, uut.integerField.intValue());
    assertEquals(Float.valueOf(floatValue), Float.valueOf(uut.floatField));
    assertEquals(Float.valueOf(floatObjValue), uut.FloatField);
    assertEquals(stringValue, uut.stringField);
    assertEquals(integerList, uut.integerListField);
    // NOTE: this is because ArrayDeque hilariously does not implement .equals()/.hashcode().
    assertEquals(Lists.newArrayList(integerQueue),
        Lists.newArrayList(uut.integerQueueField));
    assertSame(subIntValue, uut.subobjectField.intField);
  }

  @Test
  public void valueExtractTest() throws IOException, JSONException {
    final int encodedValue = 25;
    final int deserializedValue = 40;

    StringWriter stringWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(stringWriter);

    writer.object()
        .key(FormatterUUT.VALUE_FORMATTER_FIELD_NAME).value(encodedValue)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    FormatterUUT uut = FormatterUUT__JsonHelper.parseFromJson(jp);

    assertSame(deserializedValue, uut.valueFormatter);
  }

  @Test
  public void fieldAssignmentTest() throws IOException, JSONException {
    final int encodedValue = 25;
    final int deserializedValue = -encodedValue;

    StringWriter stringWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(stringWriter);

    writer.object()
        .key(FormatterUUT.FIELD_ASSIGNMENT_FIELD_NAME).value(encodedValue)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    FormatterUUT uut = FormatterUUT__JsonHelper.parseFromJson(jp);

    assertSame(deserializedValue, uut.fieldAssignmentFormatter);
  }

  @Test
  public void enumTest() throws IOException, JSONException {
    final EnumUUT.EnumType value = EnumUUT.EnumType.VALUE2;

    StringWriter stringWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(stringWriter);

    writer.object()
        .key(EnumUUT.ENUM_FIELD_NAME).value(value.toString())
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    EnumUUT uut = EnumUUT__JsonHelper.parseFromJson(jp);

    assertSame(value, uut.enumField);
  }

  @Test
  public void exactMappingTest() throws IOException, JSONException {
    // boolean exact fail.  should throw exception.
    try {
      parseObjectFromContents(
          new ExtensibleJSONWriter.Extender() {
            @Override
            public void extend(ExtensibleJSONWriter writer) throws JSONException {
              writer.key(ExactMappingUUT.BOOLEAN_FIELD_NAME).value(15);
            }
          });
      fail("primitive type exact mismatches should throw exception");
    } catch (JsonParseException ex) {
      // this is expected.
    }

    // Boolean exact fail.  should be null.
    assertNull(parseObjectFromContents(
        new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            writer.key(ExactMappingUUT.BOOLEAN_OBJ_FIELD_NAME).value(15);
          }
        })
        .BooleanField);

    // int exact fail.  should throw exception.
    try {
      parseObjectFromContents(
          new ExtensibleJSONWriter.Extender() {
            @Override
            public void extend(ExtensibleJSONWriter writer) throws JSONException {
              writer.key(ExactMappingUUT.INT_FIELD_NAME).value(false);
            }
          });
      fail("primitive type exact mismatches should throw exception");
    } catch (JsonParseException ex) {
      // this is expected.
    }

    // Integer exact fail.  should be null.
    assertNull(parseObjectFromContents(
        new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            writer.key(ExactMappingUUT.INTEGER_FIELD_NAME).value(false);
          }
        })
        .IntegerField);

    // long exact fail.  should throw exception.
    try {
      parseObjectFromContents(
          new ExtensibleJSONWriter.Extender() {
            @Override
            public void extend(ExtensibleJSONWriter writer) throws JSONException {
              writer.key(ExactMappingUUT.LONG_FIELD_NAME).value("abc");
            }
          });
      fail("primitive type exact mismatches should throw exception");
    } catch (JsonParseException ex) {
      // this is expected.
    }

    // Long exact fail.  should be null.
    assertNull(parseObjectFromContents(
        new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            writer.key(ExactMappingUUT.LONG_OBJ_FIELD_NAME).value("abc");
          }
        })
        .LongField);

    // float exact fail.  should throw exception.
    try {
      parseObjectFromContents(
          new ExtensibleJSONWriter.Extender() {
            @Override
            public void extend(ExtensibleJSONWriter writer) throws JSONException {
              writer.key(ExactMappingUUT.FLOAT_FIELD_NAME).value("abc");
            }
          });
      fail("primitive type exact mismatches should throw exception");
    } catch (JsonParseException ex) {
      // this is expected.
    }

    // Float exact fail.  should be null.
    assertNull(parseObjectFromContents(
        new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            writer.key(ExactMappingUUT.FLOAT_OBJ_FIELD_NAME).value("abc");
          }
        })
        .FloatField);

    // double exact fail.  should throw exception.
    try {
      parseObjectFromContents(
          new ExtensibleJSONWriter.Extender() {
            @Override
            public void extend(ExtensibleJSONWriter writer) throws JSONException {
              writer.key(ExactMappingUUT.DOUBLE_FIELD_NAME).value("abc");
            }
          });
      fail("primitive type exact mismatches should throw exception");
    } catch (JsonParseException ex) {
      // this is expected.
    }

    // Double exact fail.  should be null.
    assertNull(parseObjectFromContents(
        new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            writer.key(ExactMappingUUT.DOUBLE_OBJ_FIELD_NAME).value("abc");
          }
        })
        .DoubleField);

    // string exact fail.  should be null.
    assertNull(parseObjectFromContents(
        new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
            writer.key(ExactMappingUUT.STRING_FIELD_NAME).value(15);
          }
        })
        .StringField);
  }

  /**
   * Write an object in which the contents are sourced from the extender.  Then parse it as an
   * {@link ExactMappingUUT} object and return it.
   */
  private static ExactMappingUUT parseObjectFromContents(ExtensibleJSONWriter.Extender extender)
      throws IOException, JSONException {
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer.object()
        .extend(extender)
        .endObject();

    return ExactMappingUUT__JsonHelper.parseFromJson(stringWriter.toString());
  }

  @Test
  public void postprocessTest() throws IOException, JSONException {
    final int value = 25;

    StringWriter stringWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(stringWriter);

    writer.object()
        .key(PostprocessingUUT.FIELD_NAME).value(value)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    PostprocessingUUT uut = PostprocessingUUT__JsonHelper.parseFromJson(jp);

    assertSame(value + 1, uut.value);
  }

  @Test
  public void malformedArrayEntry() throws IOException, JSONException {
    final List<Integer> integerList = Lists.newArrayList(1, null, 3, 4);
    final int subIntValue = 30;

    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer.object()
        .key(StrictListParseUUT.INTEGER_LIST_FIELD_NAME)
        .array()
          .extend(new ExtensibleJSONWriter.Extender() {
          @Override
          public void extend(ExtensibleJSONWriter writer) throws JSONException {
              for (Integer integer : integerList) {
                writer.value(integer);
              }
            }
        })
        .endArray()
        .key(StrictListParseUUT.SUBOBJECT_LIST_FIELD_NAME)
        .object()
          .key(StrictListParseUUT.SubobjectParseUUT.INT_FIELD_NAME).value(subIntValue)
        .endObject()
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    StrictListParseUUT uut = StrictListParseUUT__JsonHelper.parseFromJson(jp);

    assertEquals(3, uut.integerListField.size());
    assertEquals(1, uut.integerListField.get(0).intValue());
    assertEquals(3, uut.integerListField.get(1).intValue());
    assertEquals(4, uut.integerListField.get(2).intValue());
  }

  @Test
  public void nullString() throws IOException, JSONException {
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer.object()
        .key(SimpleParseUUT.STRING_FIELD_NAME)
        .value(null)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertNull(uut.stringField);
  }
}
