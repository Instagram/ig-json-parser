// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json;

import com.fasterxml.jackson.core.JsonFactory;

/**
 * Holds the {@link JsonFactory} singleton.
 */
public class JsonFactoryHolder {
  public static final JsonFactory APP_FACTORY = new JsonFactory();
}
