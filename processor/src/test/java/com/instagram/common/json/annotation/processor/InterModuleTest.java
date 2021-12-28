/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
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
  public void subclassing_serialize_parse() throws IOException {
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
  public void abstractSubclassing_serialize_parse() throws IOException {
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
  public void simpleWrapper_seralize_parse() throws IOException {
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
  public void enumListAndMapWrappers_serialize_parse() throws IOException {
    WrapperEnumUUT uut = new WrapperEnumUUT();

    List<MyEnum> list = new ArrayList<>();

    list.add(MyEnum.FOO);
    list.add(MyEnum.BAR);
    uut.mMyEnumList = list;
    HashMap<String, MyEnum> map = new HashMap<>();
    map.put("key1", MyEnum.FOO);
    map.put("key2", MyEnum.BAR);
    uut.mMyEnumMap = map;

    String serialized = WrapperEnumUUT__JsonHelper.serializeToJson(uut);
    WrapperEnumUUT parsed = WrapperEnumUUT__JsonHelper.parseFromJson(serialized);

    assertEquals(parsed.mMyEnumList.size(), 2);
    assertEquals(parsed.mMyEnumList.get(0).getServerValue(), MyEnum.FOO.getServerValue());
    assertEquals(parsed.mMyEnumList.get(1).getServerValue(), MyEnum.BAR.getServerValue());

    assertEquals(parsed.mMyEnumMap.size(), 2);
    assertEquals(parsed.mMyEnumMap.get("key1").getServerValue(), MyEnum.FOO.getServerValue());
    assertEquals(parsed.mMyEnumMap.get("key2").getServerValue(), MyEnum.BAR.getServerValue());
  }

  /** Includes enum collections in a different class. */
  @Test
  public void enumHolderListAndMapWrappers_serialize_parse() throws IOException {
    WrapperEnumUUT uut = new WrapperEnumUUT();
    List<MyEnumHolder> holderList = new ArrayList<>();
    MyEnumHolder enumHolder1 = new MyEnumHolder(MyEnum.FOO);
    MyEnumHolder enumHolder2 = new MyEnumHolder(MyEnum.BAR);

    holderList.add(enumHolder1);
    holderList.add(enumHolder2);
    uut.mMyEnumHolderList = holderList;
    HashMap<String, MyEnumHolder> holderMap = new HashMap<>();
    holderMap.put("key1", enumHolder1);
    holderMap.put("key2", enumHolder2);
    uut.mMyEnumHolderMap = holderMap;

    String serialized = WrapperEnumUUT__JsonHelper.serializeToJson(uut);
    WrapperEnumUUT parsed = WrapperEnumUUT__JsonHelper.parseFromJson(serialized);

    assertEquals(parsed.mMyEnumHolderList.size(), 2);
    assertEquals(
        parsed.mMyEnumHolderList.get(0).getMyEnum().getServerValue(), MyEnum.FOO.getServerValue());
    assertEquals(
        parsed.mMyEnumHolderList.get(1).getMyEnum().getServerValue(), MyEnum.BAR.getServerValue());

    assertEquals(parsed.mMyEnumHolderMap.size(), 2);
    assertEquals(
        parsed.mMyEnumHolderMap.get("key1").getMyEnum().getServerValue(),
        MyEnum.FOO.getServerValue());
    assertEquals(
        parsed.mMyEnumHolderMap.get("key2").getMyEnum().getServerValue(),
        MyEnum.BAR.getServerValue());
  }

  /** Includes a collection of generics */
  @Test
  public void collectionOfGenerics_serialize_parse() throws IOException {
    WrapperAnimal animalWrapper = new WrapperAnimal();
    List<Animal<?>> animals = new ArrayList<>();
    Dog d1 = new Dog();
    d1.setName("Alice");
    Dog d2 = new Dog();
    d2.setName("Bob");
    animals.add(d1);
    animals.add(d2);
    animalWrapper.setAnimals(animals);

    WrapperWildcardHelper.Companion.registerJsonTypes();
    String serialized = WrapperAnimal__JsonHelper.serializeToJson(animalWrapper);
    WrapperAnimal parsed = WrapperAnimal__JsonHelper.parseFromJson(serialized);
    assertEquals(parsed.animals.size(), 2);

    Animal a1 = parsed.animals.get(0);
    assertEquals(a1.getId(), "DogAlice");
    assertEquals(((DogParams) a1.buildParams(a1.getName())).nameLength(), 5);

    Animal a2 = parsed.animals.get(1);
    assertEquals(a2.getId(), "DogBob");
    assertEquals(((DogParams) a2.buildParams(a2.getName())).nameLength(), 3);
  }
}
