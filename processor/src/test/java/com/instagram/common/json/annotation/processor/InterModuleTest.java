/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import static org.junit.Assert.assertEquals;

import com.instagram.common.json.annotation.processor.dependent.SubclassUUT;
import com.instagram.common.json.annotation.processor.dependent.SubclassUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.dependent.SubclassWithAbstractParentUUT;
import com.instagram.common.json.annotation.processor.dependent.SubclassWithAbstractParentUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.dependent.WrapperClassUUT;
import com.instagram.common.json.annotation.processor.dependent.WrapperClassUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.dependent.WrapperEnumUUT;
import com.instagram.common.json.annotation.processor.dependent.WrapperEnumUUT__JsonHelper;
import com.instagram.common.json.annotation.processor.parent.ParentUUT;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

/** Try to do stuff across modules. */
public class InterModuleTest {

  /** Subclasses a java object in a different class. */
  @Test
  public void subclassingTest() throws IOException {
    final int intValue = 25;
    final String stringValue = "hello world\r\n\'\"";
    final int subIntValue = 30;

    SubclassUUT uut = new SubclassUUT();
    uut.parentInt = intValue;
    uut.subclassInt = subIntValue;
    uut.parentString = stringValue;

    // serialize it
    String serialized = SubclassUUT__JsonHelper.serializeToJson(uut);

    SubclassUUT parsed = SubclassUUT__JsonHelper.parseFromJson(serialized);

    assertEquals(uut.parentInt, parsed.parentInt);
    assertEquals(uut.parentString, parsed.parentString);
    assertEquals(uut.subclassInt, parsed.subclassInt);
  }

  /** Subclasses an abstract java object in a different class. */
  @Test
  public void abstractSubclassingTest() throws IOException {
    final int intValue = 25;
    final String stringValue = "hello world\r\n\'\"";
    final int subIntValue = 30;

    SubclassWithAbstractParentUUT uut = new SubclassWithAbstractParentUUT();
    uut.parentInt = intValue;
    uut.subclassInt = subIntValue;
    uut.parentString = stringValue;

    // serialize it
    String serialized = SubclassWithAbstractParentUUT__JsonHelper.serializeToJson(uut);

    SubclassWithAbstractParentUUT parsed =
        SubclassWithAbstractParentUUT__JsonHelper.parseFromJson(serialized);

    assertEquals(uut.parentInt, parsed.parentInt);
    assertEquals(uut.parentString, parsed.parentString);
    assertEquals(uut.subclassInt, parsed.subclassInt);
  }

  /** Includes a java object in a different class. */
  @Test
  public void wrapperTest() throws IOException {
    final int intValue = 25;
    final String stringValue = "hello world\r\n\'\"";

    WrapperClassUUT uut = new WrapperClassUUT();
    uut.parent = new ParentUUT();
    uut.parent.parentInt = intValue;
    uut.parent.parentString = stringValue;

    // serialize it
    String serialized = WrapperClassUUT__JsonHelper.serializeToJson(uut);

    WrapperClassUUT parsed = WrapperClassUUT__JsonHelper.parseFromJson(serialized);

    assertEquals(uut.parent.parentInt, parsed.parent.parentInt);
    assertEquals(uut.parent.parentString, parsed.parent.parentString);
  }

  /** Includes enum collections in a different class. */
  @Test
  public void enumListAndMapWrappers() throws IOException {
    WrapperEnumUUT uut = new WrapperEnumUUT();
    List<MyEnumHolder> list = new ArrayList<>();
    MyEnumHolder enumHolder1 = new MyEnumHolder(MyEnum.FOO);
    MyEnumHolder enumHolder2 = new MyEnumHolder(MyEnum.BAR);

    list.add(enumHolder1);
    list.add(enumHolder2);
    uut.mMyEnumList = list;
    HashMap<String, MyEnumHolder> map = new HashMap<>();
    map.put("key1", enumHolder1);
    map.put("key2", enumHolder2);
    uut.mMyEnumMap = map;

    String serialized = WrapperEnumUUT__JsonHelper.serializeToJson(uut);
    WrapperEnumUUT parsed = WrapperEnumUUT__JsonHelper.parseFromJson(serialized);

    assertEquals(uut.mMyEnumList.size(), parsed.mMyEnumList.size());
    assertEquals(
        uut.mMyEnumList.get(0).getMyEnum().getServerValue(),
        parsed.mMyEnumList.get(0).getMyEnum().getServerValue());
    assertEquals(
        uut.mMyEnumList.get(1).getMyEnum().getServerValue(),
        parsed.mMyEnumList.get(1).getMyEnum().getServerValue());

    assertEquals(uut.mMyEnumMap.size(), 2);
    assertEquals(
        uut.mMyEnumMap.get("key1").getMyEnum().getServerValue(),
        parsed.mMyEnumMap.get("key1").getMyEnum().getServerValue());
    assertEquals(
        uut.mMyEnumMap.get("key2").getMyEnum().getServerValue(),
        parsed.mMyEnumMap.get("key2").getMyEnum().getServerValue());
  }
}
