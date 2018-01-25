package com.instagram.common.json.annotation;

/**
 * This annotation specifies a String getter that will return the runtime type of the object.
 * This is used for interface types, which must serialize out a description of the JsonType
 * that will be used to deserialize the object.
 */
public @interface JsonTypeName {
}
