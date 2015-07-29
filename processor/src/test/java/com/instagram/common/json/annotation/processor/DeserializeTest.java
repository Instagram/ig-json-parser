// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.instagram.common.json.JsonHelper;
import com.instagram.common.json.annotation.processor.support.ExtensibleJSONWriter;
import com.instagram.common.json.annotation.processor.uut.AlternateFieldUUT;
import com.instagram.common.json.annotation.processor.uut.AlternateFieldUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.CustomParseContainerUUT;
import com.instagram.common.json.annotation.processor.uut.CustomParseContainerUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.EnumUUT;
import com.instagram.common.json.annotation.processor.uut.EnumUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.ExactMappingUUT;
import com.instagram.common.json.annotation.processor.uut.ExactMappingUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.FormatterUUT;
import com.instagram.common.json.annotation.processor.uut.FormatterUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.MapUUT;
import com.instagram.common.json.annotation.processor.uut.MapUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.PostprocessingUUT;
import com.instagram.common.json.annotation.processor.uut.PostprocessingUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.SimpleParseUUT;
import com.instagram.common.json.annotation.processor.uut.SimpleParseUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.uut.StrictListParseUUT;
import com.instagram.common.json.annotation.processor.uut.StrictListParseUUT__JsonHelper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.json.JSONWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    final ArrayList<Integer> integerArrayList = Lists.newArrayList(1, 2, 3, 4);
    final Queue<Integer> integerQueue = Queues.newArrayDeque(Arrays.asList(1, 2, 3, 4));
    final Set<Integer> integerSet = Sets.newHashSet(1, 2, 3, 4);
    final int subIntValue = 30;
    final SimpleParseUUT.SubenumUUT subEnum = SimpleParseUUT.SubenumUUT.A;
    final List<SimpleParseUUT.SubenumUUT> subEnumList = Lists.newArrayList(
        SimpleParseUUT.SubenumUUT.A, SimpleParseUUT.SubenumUUT.B);

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
        .key(SimpleParseUUT.INTEGER_ARRAY_LIST_FIELD_NAME)
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
        .key(SimpleParseUUT.INTEGER_SET_FIELD_NAME)
        .array()
        .extend(new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Integer integer : integerSet) {
                  writer.value(integer);
                }
              }
            })
        .endArray()
        .key(SimpleParseUUT.SUBOBJECT_FIELD_NAME)
          .object()
            .key(SimpleParseUUT.SubobjectParseUUT.INT_FIELD_NAME).value(subIntValue)
          .endObject()
        .key(SimpleParseUUT.SUBENUM_FIELD_NAME).value(subEnum.toString())
        .key(SimpleParseUUT.SUBENUM_LIST_FIELD_NAME)
        .array()
        .extend(new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (SimpleParseUUT.SubenumUUT enumValue : subEnumList) {
                  writer.value(enumValue.toString());
                }
              }
            })
        .endArray()
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    SimpleParseUUT uut = SimpleParseUUT__JsonHelper.parseFromJson(jp);

    assertTrue(new SimpleParseUUT__JsonHelper() instanceof JsonHelper);
    assertSame(intValue, uut.intField);
    assertSame(integerValue, uut.integerField.intValue());
    assertEquals(Float.valueOf(floatValue), Float.valueOf(uut.floatField));
    assertEquals(Float.valueOf(floatObjValue), uut.FloatField);
    assertEquals(stringValue, uut.stringField);
    assertEquals(integerList, uut.integerListField);
    assertEquals(integerArrayList, uut.integerArrayListField);
    // NOTE: this is because ArrayDeque hilariously does not implement .equals()/.hashcode().
    assertEquals(Lists.newArrayList(integerQueue),
        Lists.newArrayList(uut.integerQueueField));
    assertEquals(integerSet, uut.integerSetField);
    assertSame(subIntValue, uut.subobjectField.intField);
    assertSame(subEnum, uut.subenumField);
    assertEquals(subEnumList, uut.subenumFieldList);
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

    assertSame(deserializedValue, uut.getValueFormatter());
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

    assertSame(deserializedValue, uut.getFieldAssignmentFormatter());
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

    assertSame(value + 1, uut.getValue());
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
  public void testAlternateFieldNames() throws Exception {
    testAlternateFieldNameHelper(AlternateFieldUUT.FIELD_NAME, "value1");
    testAlternateFieldNameHelper(AlternateFieldUUT.ALTERNATE_FIELD_NAME_1, "value2");
    testAlternateFieldNameHelper(AlternateFieldUUT.ALTERNATE_FIELD_NAME_2, "value3");
  }

  private void testAlternateFieldNameHelper(String fieldName, String value) throws Exception {
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer.object()
        .key(fieldName)
        .value(value)
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    AlternateFieldUUT uut = AlternateFieldUUT__JsonHelper.parseFromJson(jp);

    assertEquals(value, uut.getNameField());
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

  @Test
  public void testCustomParse() throws Exception {
    String value = "hey there";
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer.object()
        .key(CustomParseContainerUUT.INNER_FIELD_NAME)
          .object()
            .key(CustomParseContainerUUT.CustomParseUUT.STRING_FIELD_NAME)
            .value(value)
          .endObject()
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    CustomParseContainerUUT uut = CustomParseContainerUUT__JsonHelper.parseFromJson(jp);

    assertEquals(value, uut.getCustomParseUUT().getStringField());
  }

  @Test
  public void testMap() throws Exception {
    final String stringValue = "hello world\r\n\'\"";
    final int integerValue = 37;

    final Map<String, Integer> stringIntegerMap = Maps.newHashMap();
    stringIntegerMap.put(stringValue, integerValue);

    final Map<String, String> stringStringMap = Maps.newHashMap();
    stringStringMap.put(stringValue, "value");

    final Map<String, Long> stringLongMap = Maps.newHashMap();
    stringLongMap.put("key_min_value", Long.MIN_VALUE);
    stringLongMap.put("key_max_value", Long.MAX_VALUE);

    final Map<String, Double> stringDoubleMap = Maps.newHashMap();
    stringDoubleMap.put("key_min_value", Double.MIN_VALUE);
    stringDoubleMap.put("key_max_value", Double.MAX_VALUE);

    final Map<String, Float> stringFloatMap = Maps.newHashMap();
    stringFloatMap.put("key_min_value", Float.MIN_VALUE);
    stringFloatMap.put("key_max_value", Float.MAX_VALUE);

    final Map<String, Boolean> stringBooleanMap = Maps.newHashMap();
    stringBooleanMap.put("true", Boolean.TRUE);
    stringBooleanMap.put("false", Boolean.FALSE);

    final Map<String, MapUUT.MapObject> stringObjectMap = Maps.newHashMap();
    MapUUT.MapObject mapObject = new MapUUT.MapObject();
    mapObject.subclassInt = integerValue;

    final HashMap<String, String> stringStringHashMap = Maps.newHashMap();
    stringStringHashMap.put(stringValue, "value");

    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    writer
        .object()
        .key(MapUUT.STRING_INTEGER_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, Integer> entry : stringIntegerMap.entrySet()) {
                  writer.key(entry.getKey());
                  writer.value(entry.getValue());
                }
              }
            })
        .endObject()
        .key(MapUUT.STRING_STRING_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
                  writer.key(entry.getKey());
                  writer.value(entry.getValue());
                }
              }
            }
        )
        .endObject()
        .key(MapUUT.STRING_LONG_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, Long> entry : stringLongMap.entrySet()) {
                  writer.key(entry.getKey());
                  writer.value(entry.getValue());
                }
              }
            }
        )
        .endObject()
        .key(MapUUT.STRING_DOUBLE_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, Double> entry : stringDoubleMap.entrySet()) {
                  writer.key(entry.getKey());
                  writer.value(entry.getValue());
                }
              }
            }
        )
        .endObject()
        .key(MapUUT.STRING_FLOAT_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, Float> entry : stringFloatMap.entrySet()) {
                  writer.key(entry.getKey());
                  writer.value(entry.getValue());
                }
              }
            }
        )
        .endObject()
        .key(MapUUT.STRING_BOOLEAN_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, Boolean> entry : stringBooleanMap.entrySet()) {
                  writer.key(entry.getKey());
                  writer.value(entry.getValue());
                }
              }
            }
        )
        .endObject()
        .key(MapUUT.STRING_OBJECT_MAP_FIELD_NAME)
        .object()
        .extend(
            new ExtensibleJSONWriter.Extender() {
              @Override
              public void extend(ExtensibleJSONWriter writer) throws JSONException {
                for (Map.Entry<String, MapUUT.MapObject> entry : stringObjectMap.entrySet()) {
                  writer.key(entry.getKey())
                      .object()
                      .key(MapUUT.MapObject.INT_KEY).value(entry.getValue().subclassInt)
                      .endObject();
                }
              }
            }
        )
        .endObject()
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    MapUUT uut = MapUUT__JsonHelper.parseFromJson(jp);

    assertEquals(stringIntegerMap, uut.stringIntegerMapField);
    assertEquals(stringStringMap, uut.stringStringMapField);
    assertEquals(stringLongMap, uut.stringLongMapField);
    assertEquals(stringDoubleMap, uut.stringDoubleMapField);
    assertEquals(stringFloatMap, uut.stringFloatMapField);
    assertEquals(stringBooleanMap, uut.stringBooleanMapField);
    assertEquals(stringObjectMap, uut.stringObjectMapField);
  }

  @Test
  public void testMapNullValue() throws Exception {
    StringWriter stringWriter = new StringWriter();
    ExtensibleJSONWriter writer = new ExtensibleJSONWriter(stringWriter);

    final String keyWithNullValue = "key_with_null_value";

    writer
        .object()
        .key(MapUUT.STRING_STRING_MAP_FIELD_NAME)
        .object()
        .key(keyWithNullValue)
        .value(null)
        .endObject()
        .endObject();

    String inputString = stringWriter.toString();
    JsonParser jp = new JsonFactory().createParser(inputString);
    jp.nextToken();
    MapUUT uut = MapUUT__JsonHelper.parseFromJson(jp);

    assertTrue(uut.stringStringMapField.containsKey(keyWithNullValue));
    assertNull(uut.stringStringMapField.get(keyWithNullValue));
  }
}
