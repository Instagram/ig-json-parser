// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.util;

import javax.annotation.processing.Messager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The basic construct to hold the annotation data gathered about a class.  Data records are stored
 * in the same order that records are appearing in the class declaration. {@link ProcessorClassData}
 * manages the set of data records, though creation of a record is delegated to a factory.
 *
 * When the gathering phase is complete, a class injector is asked to produce java source code to be
 * written out.
 */
abstract public class ProcessorClassData<AnnotationKeyType, AnnotationRecordType> {

  /**
   * Factory to produce records.
   */
  public interface AnnotationRecordFactory<AnnotationKeyType, AnnotationRecordType> {

    /**
     * Creates a record for a given key.
     */
    public AnnotationRecordType createAnnotationRecord(AnnotationKeyType key);
  }

  protected final String mClassPackage;
  protected final String mQualifiedClassName;
  protected final String mSimpleClassName;
  protected final String mInjectedClassName;
  private final AnnotationRecordFactory<AnnotationKeyType, AnnotationRecordType> mFactory;
  private Map<AnnotationKeyType, AnnotationRecordType> mData;

  /**
   * Creates a ProcessorClassData.
   * @param classPackage the package of the class being inspected.
   * @param qualifiedClassName the fully-qualified class name of the class being inspected.  See
   * {@link Class#getQualifiedName()}.
   * @param simpleClassName the simple class name of the class being inspected. See
   * {@link Class#getSimpleName()}. This may be a partially-qualified name if the class is an
   * inner class.
   * @param injectedClassName the simple class name of the class this injector will write its
   * generated code to.  See {@link Class#getSimpleName()}.
   * @param factory creates data records.
   */
  protected ProcessorClassData(
      String classPackage,
      String qualifiedClassName,
      String simpleClassName,
      String injectedClassName,
      AnnotationRecordFactory<AnnotationKeyType, AnnotationRecordType> factory) {
    mClassPackage = classPackage;
    mQualifiedClassName = qualifiedClassName;
    mSimpleClassName = simpleClassName;
    mInjectedClassName = injectedClassName;
    mFactory = factory;
    mData = new LinkedHashMap<AnnotationKeyType, AnnotationRecordType>();
  }

  /**
   * Retrieves the data record corresponding to a given key.  If the record does not exist, it is
   * created.
   */
  public AnnotationRecordType getOrCreateRecord(AnnotationKeyType key) {
    AnnotationRecordType record = mData.get(key);
    if (record == null) {
      record = mFactory.createAnnotationRecord(key);
      mData.put(key, record);
    }
    return record;
  }

  /**
   * Returns the fully-qualified class name of the class we're generating the source for.
   */
  public String getInjectedFqcn() {
    return mClassPackage + '.' + mInjectedClassName;
  }

  /**
   * Returns an iterator across all the records gathered by this injector.
   */
  protected Iterable<Map.Entry<AnnotationKeyType, AnnotationRecordType>> getIterator() {
    return new Iterable<Map.Entry<AnnotationKeyType, AnnotationRecordType>>() {
      @Override
      public Iterator<Map.Entry<AnnotationKeyType, AnnotationRecordType>> iterator() {
        return mData.entrySet().iterator();
      }
    };
  }

  /**
   * Returns the java code generated.
   */
  abstract public String getJavaCode(Messager messager);
}
