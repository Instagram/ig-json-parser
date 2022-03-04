
# ig-json-parser

[![Support Ukraine](https://img.shields.io/badge/Support-Ukraine-FFD500?style=flat&labelColor=005BBB)](https://opensource.fb.com/support-ukraine) [![Build Status](https://travis-ci.org/Instagram/ig-json-parser.svg?branch=master)](https://travis-ci.org/Instagram/ig-json-parser) [![Release](https://jitpack.io/v/Instagram/ig-json-parser.svg)](https://jitpack.io/#Instagram/ig-json-parser)

Fast JSON parser for java projects.


## Getting started

The easiest way to get started is to look at maven-example.  For more
comprehensive examples, check out the unit tests or the demo.


## Gradle

For Java projects, to use this library, add this to your build.gradle file:
```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}

...

dependencies {
  implementation 'com.github.instagram.ig-json-parser:runtime:master-SNAPSHOT' // the runtime
  implementation 'com.github.instagram.ig-json-parser:processor:master-SNAPSHOT' // the annotation processor
}
```

For Android projects using Android Studio 3.0+ or Gradle 4.0+, you can enable the annotation processor as following:

```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}

...

dependencies {
  annotationProcessor 'com.github.instagram.ig-json-parser:processor:master-SNAPSHOT'
  implementation 'com.github.instagram.ig-json-parser:runtime:master-SNAPSHOT'
}
```

If you are using older gradle versions, you can use old `apt` plugin to integrate the annotation processor:

```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}

...

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
  apt 'com.github.instagram.ig-json-parser:processor:master-SNAPSHOT'
  implementation 'com.github.instagram.ig-json-parser:runtime:master-SNAPSHOT'
}
```


If you are using other build systems, please find instructions [here](https://jitpack.io/#Instagram/ig-json-parser)

## Requirements for model classes

There should be a package-visible no-argument constructor for each of your
model classes.  The fields also need to be package-visible.

Each class that needs a serializer/deserializer generated should be
annotated with `@JsonType`.  Each field that needs to be mapped to/from
JSON should be annotated with `@JsonField`.  The `@JsonField` annotation
has one mandatory argument, which is the fieldname for the field in the
JSON.

The following is an example of a very simple model class:
```java
@JsonType
class Dessert {
  @JsonField(fieldName="type")
  String type;

  @JsonField(fieldName="rating")
  float rating;
}
```

## Serializer/deserializer

Compiling your model classes with the annotations will automatically
generate the serializer and deserializer.  They will be in a generated
class with the same name as your class, except with the suffix
`__JsonHelper`.  For example, to deserialize the `Dessert` class above,
simply run the code:

```java
Dessert parsed = Dessert__JsonHelper.parseFromJson(inputJsonString);
```
To serialize a class, run:

```java
String serialized = Dessert__JsonHelper.serializeToJson(dessertObject);
```

## Supported data types

The following scalar types are supported:
* String
* boolean/Boolean
* int/Integer
* long/Long
* float/Float
* double/Double

The following collection types are supported:
* List/ArrayList
* Queue/ArrayDeque
* Map/HashMap
* Set/HashSet

If a json field is another dictionary, it can be represented by another
model class.  That model class must also have the `@JsonType` annotation.

# Proguard

Add the following lines to your proguard-rules file:
```
-dontwarn sun.misc.Unsafe
-dontwarn javax.annotation.**
```

# Advanced features

## Postprocessing

If you need to process your JSON after a first pass, you can change your `@JsonType` annotation to be `@JsonType(postprocess = true)` and add a method to your code and add a method `YourClass postprocess()` which will be called after the JSON is processed (see: `QuestionableDesignChoice` in the example below)

```java
  @JsonType
  public class Example {
    @JsonField(fieldName = "container")
    Container mContainer;

    @JsonType
    public static class Container {
        @JsonField(fieldName = "questionable_design_choice")
        List<QuestionableDesignChoice> mQuestionableDesignChoice;
    }

    @JsonType(postprocessingEnabled = true)
    public static class QuestionableDesignChoice {
        @JsonField(fieldName = "property")
        String mProperty;

        QuestionableDesignChoice postprocess() {
          // post-process things here...
          return this;
        }
    }
}
```

## Customized parsing code

Parsing the supported data types is straightforward. For enums or built-in Java classes, you will need to add customized parsing.

**Value extract formatters** override how we extract the value from the `JsonParser` object, while **serialize code formatters** override how we serialize a java field back to json. We use the serde for PointF in the example below, where a point is represented as an array in json.
```java
  @JsonField(
      fieldName = "position",
      valueExtractFormatter =
          "com.instagram.common.json.android.JsonTypeHelper.deserializePointF(${parser_object})",
      serializeCodeFormatter =
          "com.instagram.common.json.android.JsonTypeHelper.serializePointF("
              + "${generator_object}, \"${json_fieldname}\", ${object_varname}.${field_varname})")
  @Nullable
  protected PointF mPosition;
```

## Optional serializer generation

To save generating serializer code if you only need deserialization, serializer generation can be disabled or enabled
globally and per-class. The default is to generate serializers for all classes. To disable generation globally, pass

    -AgenerateSerializer=false

to the command-line arguments of javac. To override the default generation option for a single class, see
`JsonType.generateSerializer()`.

# Contributing

See the [CONTRIBUTING](.github/CONTRIBUTING.md) file for how to help out.

# License
ig-json-parser is MIT licensed, as found in the [LICENSE](LICENSE) file.

# Privacy Policy and Terms of Use
- [Privacy Policy](https://opensource.facebook.com/legal/privacy)
- [Terms of Use](https://opensource.facebook.com/legal/terms)
