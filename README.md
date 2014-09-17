ig-json-parser
==============

Fast JSON parser for java projects


Getting started
===============

The easiest way to get started is to look at maven-example.  For more
comprehensive examples, check out the unit tests or the demo.


Maven
-----

To use this library, add this to your build.gradle file:

    ext {
      generatedSourcesDir = file("gen-src/main/java")
    }
    
    repositories {
      mavenCentral()
    }
    
    sourceSets {
      main {
        java {
          srcDir 'src/main/java'
        }
      }
      gensrc {
        java {
          srcDir 'gen-src/main/java'
        }
      }
    }
    
    compileJava {
      doFirst {
        // Directory should exists before compilation started.
        generatedSourcesDir.mkdirs()
      }
      options.compilerArgs += [
                           '-processor',
                           'com.instagram.common.json.annotation.processor.JsonAnnotationProcessor',
                           '-s',
                           generatedSourcesDir
      ]
    }
    dependencies {
      compile group: 'com.instagram', name: 'ig-json-parser-processor', version: '0.0.3+'
    }


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


Serializer/deserializer
-----------------------

Compiling your model classes with the annotations will automatically
generate the serializer and deserializer.  They will be in a generated
class with the same name as your class, except with the suffix
`__JsonHelper`.  For example, to deserialize the `Dessert` class above,
simply run the code:

    Dessert parsed = Dessert__JsonHelper.parseFromJson(inputJsonString);

To serialize a class, run:

    String serialized = Dessert__JsonHelper.serializeToJson(dessertObject);


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
