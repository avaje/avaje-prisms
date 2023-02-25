[![Build](https://github.com/avaje/avaje-prisms/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-prisms/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-prisms/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-prisms/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-prisms/blob/master/LICENSE)
[![Maven Central : avaje-prisms](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-prisms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-prisms)

# Avaje Prisms

Fork of the legendary [hickory annotation processer](https://javadoc.io/static/com.jolira/hickory/1.0.0/net/java/dev/hickory/prism/package-summary.html). Hickory has served well since it was created in 2010, but it doesn't have module support, and isn't likely to recieve it. 

## Differences from Hickory

- Upgrades from JDK 6 to 11
- Adds modular support via module.info
- `@GeneratedPrism` is now repeatable
- Generates a `getAllInstances` method to retrieve a list of prisms from an element
- Generates an `isPresent` method to easily check if an element has the target annotation.
- Generates `Optional` factory methods  
- Exposes the fully qualified type of the target annotation as a string.
- `getInstance` returns null instead of throwing exceptions when the provided mirror doesn't match the prism target
- null annotation array values are returned as empty lists

## What's a Prism?

When writing annotation processors the two conventional mechanisms to access the annotations are both awkward. `Element.getAnnotation()` can throw can throw Exceptions if the annotation being modelled is not semantically correct, and it can also fail to work on modular projects, (This is one the reasons why `<annotationProcessorPaths>` is required for modular projects but it is seriously limited and technically not correct either: [MCOMPILER-391](https://issues.apache.org/jira/browse/MCOMPILER-391) and [MCOMPILER-412](https://issues.apache.org/jira/browse/MCOMPILER-412), and the member methods on the returned `Annotation` can also throw Exceptions if the annotation being modelled is not semantically correct. Moreover when calling a member with a `Class` return type, you need to catch an exception to extract the `TypeMirror`.

On the other hand, `AnnotationMirror` and `AnnotationValue` do a good job of modelling both correct and incorrect annotations, but provide no simple mechanism to determine whether it is correct or incorrect, and provide no convenient functionality to access the member values in a simple type specific way. While `AnnotationMirror` and `AnnotationValue` provide an ideal mechanism for dealing with unknown annotations, they are inconvenient for reading member values from known annotations.

A Prism provides a solution to this problem by combining the advantages of the pure reflective model of `AnnotationMirror` and the runtime (real) model provided by Element.getAnnotation(), hence the term Prism to capture this idea of partial reflection.

A prism has the same member methods as the annotation except that the return types are translated from runtime types to compile time types as follows...

- Primitive members return their equivalent wrapper class in the prism.
- Class members return a TypeMirror from the mirror API.
- Enum members return a String being the name of the enum constant (because the constant value in the mirror API might not match those available in the runtime it cannot consistently return the appropriate enum).
- String members return Strings.
- Annotation members return a Prism of the annotation. If a prism for that annotation is generated from the same @GeneratePrisms annotation as the prism that uses it, then an instance of that prism will be returned. Otherwise a Prism for that annotation is supplied as an inner class of the dependant Prism. the name of which is the simple name of the referenced annotation type.
- Array members return a List<X> where X is the appropriate prism mapping of the array component as above.

## How to use

#### 1. Add avaje-prisms as a dependency in your annotation processor.
```xml
    <dependency>
      <groupId>io.avaje</groupId>
      <artifactId>avaje-prisms</artifactId>
      <version>1.4</version>
      <optional>true</optional>
      <scope>provided</scope>
    </dependency>
```

#### 2. Replace `<compilerArgument>-proc:none</compilerArgument>` with this annotation processor

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration> 
    <annotationProcessorPaths>
      <path>
          <groupId>io.avaje</groupId>
          <artifactId>avaje-prisms</artifactId>
          <version>1.4</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

#### 3. Add `@GeneratePrism` targeting an annotation to a package-info.java/class.

```java
// package-info.java
@GeneratePrism(MyExampleAnnotation.class)
package org.example
```

#### 4. Use the Generated Prism Class

```java
 void someFunction(Element element) {
    
MyExampleAnnotationPrism exampleAnnotation = MyExampleAnnotationPrism.getInstanceOn(element);
//can get the original annotation type as a string
String annotationQualifiedType = MyExampleAnnotationPrism.PRISM_TYPE

//can call the annotation methods as if the annotation was practically present on the classpath.
exampleAnnotation.getValue()
  ...
    }
```

## Related Works
- [Pistachio](https://github.com/jstachio/pistachio)
- [Mapstruct GemTools](https://github.com/mapstruct/tools-gem) ([Docs](https://mapstruct.org/news/2020-02-03-announcing-gem-tools/))
