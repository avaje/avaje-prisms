# Avaje Prisms

Fork of the [hickory annotation processer](https://javadoc.io/static/com.jolira/hickory/1.0.0/net/java/dev/hickory/prism/package-summary.html). Upgrades from java 6 to 8, and adds an Automatic-Module-Name.


# How to use.

#### 1. Add avaje-prisms as a dependency in your annotation processor.
```xml
    <dependency>
      <groupId>io.avaje</groupId>
      <artifactId>avaje-prisms</artifactId>
      <version>1.0</version>
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
