[![Build](https://github.com/avaje/avaje-prisms/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-prisms/actions/workflows/build.yml)
[![JDK EA](https://github.com/avaje/avaje-prisms/actions/workflows/jdk-ea.yml/badge.svg)](https://github.com/avaje/avaje-prisms/actions/workflows/jdk-ea.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-prisms/blob/master/LICENSE)
[![Maven Central : avaje-prisms](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-prisms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-prisms)

# Avaje Prisms

Fork of the legendary [hickory annotation processer](https://javadoc.io/static/com.jolira/hickory/1.0.0/net/java/dev/hickory/prism/package-summary.html). It has served well since 2007, but it doesn't have module support, and isn't likely to recieve it. This fork upgrades from java 6 to 11, and adds a module-info and other enhancements, such as adding `getAllInstancesOn` and Optional Methods on the generated prisms.

A lot of folks are not aware of this, but you should almost never use the actual annotation classes in an annotation processor, even your own.

Why is it problematic? An annotation processor is not guaranteed to have classes loaded, if your annotation references other classes (e.g. has a Class<?> parameter) that are not yours you will have issues, *especially* if you are dealing with modularized projects.

# How to use

#### 1. Add avaje-prisms as a dependency in your annotation processor.
```xml
    <dependency>
      <groupId>io.avaje</groupId>
      <artifactId>avaje-prisms</artifactId>
      <version>1.3</version>
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

#### 3. `@GeneratePrism` targeting an annotation to a package-info.java/any class.

```java
@GeneratePrism(MyExampleAnnotation.class)
package org.example
```


#### 4. Use the Generated Prism Class

```java
 void someFunction(Element element) {
    
MyExampleAnnotationPrism exampleAnnotation = MyExampleAnnotationPrism.getInstanceOn(element);
//can get the original annotation type as a string
String annotationQualifiedType = MyExampleAnnotationPrism.PRISM_TYPE

//can call the annotation methods as if the annotation was actually present on the classpath.
exampleAnnotation.getValue()
  ...
    }
```
