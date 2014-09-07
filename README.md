ig-json-parser
==============

Fast JSON parser for java projects


Getting started
===============

The easiest way to get started is to look at the unit tests or the demo.

Requirements for model classes
------------------------------

There should be a package-visible no-argument constructor for each of your
model classes.  The fields also need to be package-visible.

Each class that needs a serializer/deserializer generated should be
annotated with `@JsonType`.  Each field that needs to be mapped to/from
JSON should be annotated with `@JsonField`.  The `@JsonField` annotation
has one mandatory argument, which is the fieldname for the field in the
JSON.

The following is an example of a very simple model class:

    @JsonType
    class Dessert {
      @JsonField(fieldName="type")
      String type;

      @JsonField(fieldName="rating")
      float rating;
    }

Supported data types
--------------------

The following scalar types are supported:
* String
* boolean/Boolean
* int/Integer
* long/Long
* float/Float
* double/Double

If a json field is another dictionary, it can be represented by another
model class.  That model class must also have the `@JsonType` annotation.

Lists of objects are supported either as Java Lists or Queues.

Advanced features
=================

Postprocessing
--------------

TODO: Document this.  See the documentation in
common/src/main/java/com/instagram/common/json/annotation/JsonType.java in
the meanwhile.

Customized parsing code
-----------------------

TODO: Document this.  See the documentation in
common/src/main/java/com/instagram/common/json/annotation/JsonField.java
in the meanwhile.
