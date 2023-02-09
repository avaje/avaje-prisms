# Avaje Prisms

Fork of the [hickory annotation processer](https://javadoc.io/static/com.jolira/hickory/1.0.0/net/java/dev/hickory/prism/package-summary.html). Upgrades from java 6 to 8, and adds an Automatic-Module-Name.

A lot of folks are not aware of this but you should almost never use the actual annotation classes in an annotation processor, even your own.

Why is it problematic? An annotation processor is not guaranteed to have classes loaded, if your annotation references other classes (e.g. has a Class<?> parameter) that are not yours you will have issues, *especially* if you are dealing with modularized projects.

# How to use

#### 1. Add avaje-prisms as a dependency in your annotation processor.
```xml
    <dependency>
      <groupId>io.avaje</groupId>
      <artifactId>avaje-prisms</artifactId>
      <version>1.1</version>
      <optional>true</optional>
      <scope>provided</scope>
    </dependency>
```

#### 2. `@GeneratePrism` targeting an annotation to a package-info.java/any class.

```java
@GeneratePrism(MyExampleAnnotation.class)
package org.example
```


#### 3. Use the Genrated Prism Classes



```java
 void someFunction(Element element) {
    
MyExampleAnnotationPrism exampleAnnotation = MyExampleAnnotationPrism.getInstanceOn(element);
  exampleAnnotation.getValue()
  //can call the annotation methods like the annotation was actually present.
  ...
    }
```
