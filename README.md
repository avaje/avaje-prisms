[![Discord](https://img.shields.io/discord/1074074312421683250?color=%237289da&label=discord)](https://discord.gg/Qcqf9R27BR)
[![Build](https://github.com/avaje/avaje-prisms/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-prisms/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-prisms/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-prisms/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-prisms/blob/master/LICENSE)
[![Maven Central : avaje-prisms](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-prisms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-prisms)
[![javadoc](https://javadoc.io/badge2/io.avaje/avaje-prisms/javadoc.svg?color=purple)](https://javadoc.io/doc/io.avaje/avaje-prisms)

# [Avaje Prisms](https://avaje.io/prisms/)

Fork of the legendary [hickory annotation processor](https://javadoc.io/static/com.jolira/hickory/1.0.0/net/java/dev/hickory/prism/package-summary.html). Hickory has served pretty well since it was created in 2010, but it's unmaintained and doesn't have module support. 



## What's a Prism?

When writing annotation processors the two conventional mechanisms to access the annotations are both awkward. `Element.getAnnotation()` can throw Exceptions if the annotation or its members are not semantically correct, and it can also fail to work on some modular projects. (This is one the reasons why `<annotationProcessorPaths>` is required for modular projects but it is seriously limited and technically not correct either (See [MCOMPILER-412](https://issues.apache.org/jira/browse/MCOMPILER-412)) Moreover, when calling a member with a `Class` return type, you need to catch an exception to extract the `TypeMirror`.

On the other hand, `AnnotationMirror` and `AnnotationValue` do a good job of modeling both correct and incorrect annotations, but provide no simple mechanism to determine whether it is correct or incorrect, and provide no convenient functionality to access the member values in a simple type-specific way. While `AnnotationMirror` and `AnnotationValue` provide an ideal mechanism for dealing with unknown annotations, they are inconvenient for reading member values from known annotations.

A Prism provides a solution to this problem by combining the advantages of the pure reflective model of `AnnotationMirror` and the runtime (real) model provided by Element.getAnnotation(), hence the term Prism to capture this idea of partial reflection.

A prism has the same member methods as the annotation except that the return types are translated from runtime types to compile time types as follows...

- Primitive members return their equivalent wrapper class in the prism.
- Class members return a TypeMirror from the mirror API.
- Enum members return a String being the name of the enum constant (because the constant value in the mirror API might not match those available in the runtime it cannot consistently return the appropriate enum).
- String members return Strings.
- Annotation members return a Prism of the annotation. If a prism for that annotation is generated from the same @GeneratePrisms annotation as the prism that uses it, then an instance of that prism will be returned. Otherwise, a Prism for that annotation is supplied as an inner class of the dependant Prism. the name of which is the simple name of the referenced annotation type.
- Array members return a List<X> where X is the appropriate prism mapping of the array component as above.

## How to use

#### 1. Add avaje-prisms as a dependency in your annotation processor (AP).
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-prisms</artifactId>
  <version>${avaje.prism.version}</version>
  <optional>true</optional>
  <scope>provided</scope>
</dependency>
```
When working with Java modules you need to add prisms as a static dependency.
```java
module my.processor {
  requires static io.avaje.prisms;
}
```
#### 2. In your AP's pom.xml, replace `<compilerArgument>-proc:none</compilerArgument>` with this annotation processor
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration> 
    <annotationProcessorPaths>
      <path>
          <groupId>io.avaje</groupId>
          <artifactId>avaje-prisms</artifactId>
          <version>${avaje.prism.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```
This ensures that only the prism generator will run at build time.

#### 3. Add `@GeneratePrism` targeting an annotation to a package-info.java/class.

```java
// package-info.java
@GeneratePrism(MyExampleAnnotation.class)
//@GenerateUtils optionally can add this to generate a helper class
package org.example
```

#### 4. Use the Generated Prism Class

```java
 void someFunction(Element element) {
    
MyExampleAnnotationPrism exampleAnnotation = MyExampleAnnotationPrism.getInstanceOn(element);
//can get the original annotation type as a string
String annotationQualifiedType = MyExampleAnnotationPrism.PRISM_TYPE

//can easily retrieve the annotation values as if the annotation was present on the classpath.
exampleAnnotation.getValue()
  ...
    }
```

### `META-INF/services` Generation
Avaje prisms will try to detect your processor class and register an entry to `META-INF/services/javax.annotation.processing.Processor` after compilation. Doing this means you can omit the compiler plugin configuration. (`<compilerArgument>-proc:none</compilerArgument>` and step 2 in the how to use section)   

Services entries will be added if a concrete processor class has any of the following annotations:

- Any avaje prism annotation
- `@SupportedAnnotationTypes`
- `@SupportedOptions`
- `@SupportedSourceVersion`

### `@GenerateAPContext`
If you add the annotation, [this Helper Class](https://github.com/avaje/avaje-prisms/blob/main/prism-core/src/main/java/io/avaje/prism/internal/APContext.java) will be generated into your project to allow you to access the processing environment statically from anywhere.

To initialize/cleanup the generated `APContext` do the below: 
```java
@GenerateAPContext
public final class MyProcessor extends AbstractProcessor {

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    APContext.init(env);
  }

  @Override
  public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
    if (renv.processingOver()) {
      APContext.clear();
      return false;
    }
    //do whatever processing you need
  }
}
```
## Differences from Hickory

- Upgrades from JDK 6 to 11
- Adds modular support via module-info
- `@GeneratedPrism` is now repeatable
- Can choose what classes the generated Prisms inherit.
- Generates an `isPresent` method to check if an element has the target annotation easily
- Generates `Optional` factory methods  
- Generates a `getAllInstances` method to retrieve a list of prisms from an element (`@Repeatable` annotations only)
- Generates a `getAllOnMetaAnnotations` method to retrieve a list of prisms from an element's annotations (Meta annotations only)
- Exposes the fully qualified type of the target annotation as a string.
- `getInstance` returns null instead of throwing exceptions when the provided mirror doesn't match the prism target
- null annotation array values are returned as empty lists
- META-INF/services generation

## Related Works
- [Pistachio](https://github.com/jstachio/pistachio)
- [Mapstruct GemTools](https://github.com/mapstruct/tools-gem) ([Docs](https://mapstruct.org/news/2020-02-03-announcing-gem-tools/))
