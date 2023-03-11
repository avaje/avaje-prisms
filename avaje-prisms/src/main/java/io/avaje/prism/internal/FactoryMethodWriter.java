package io.avaje.prism.internal;

import static io.avaje.prism.internal.ProcessingContext.asElement;

import java.io.PrintWriter;

import javax.lang.model.type.DeclaredType;

public class FactoryMethodWriter {
  private final String indent;
  private final PrintWriter out;
  private final String name;
  private final DeclaredType typeMirror;
  private final String access;
  private final String annName;
  private final boolean inner;
  private final boolean repeatable;
  private final boolean meta;

  FactoryMethodWriter(GenerateContext ctx, boolean inner) {
    this.indent = ctx.indent();
    this.out = ctx.out();
    this.name = ctx.name();
    this.typeMirror = ctx.typeMirror();
    this.access = ctx.access();
    this.annName = ctx.annName();
    this.inner = inner;
    this.repeatable = RepeatablePrism.isPresent(asElement(typeMirror));

    final var target = TargetPrism.getInstanceOn(asElement(typeMirror));

    this.meta = target == null || target.value().contains("ANNOTATION_TYPE");
  }

  void write() {
    if (!inner) {
      writeIsPresent();
      writeGetInstanceOn();
      writeGetOptionalOn();
      if (repeatable) {
        writeGetAllInstances();
      }
    }
    writeGetInstance();
    writeGetOptional();
  }

  private void writeIsPresent() {
    // Is Present
    out.format(
        "%s    /** Returns true if the prism annotation is present on the element, else false. */%n",
        indent);
    out.format("%s    %sstatic boolean isPresent(Element element) {%n", indent, access);
    out.format("%s        return getInstanceOn(element) != null;%n", indent);
    out.format("%s   }%n%n", indent);
  }

  private void writeGetInstanceOn() {
    // get single instance
    out.format(
        "%s    /** Return a prism representing the {@code @%s} annotation on 'e'. %n",
        indent, annName);
    out.format(
        "%s      * similar to {@code element.getAnnotation(%s.class)} except that %n",
        indent, annName);
    out.format(
        "%s      * an instance of this class rather than an instance of {@code %s}%n",
        indent, annName);
    out.format("%s      * is returned.%n", indent);
    out.format("%s      */%n", indent);
    out.format("%s    %sstatic %s getInstanceOn(Element element) {%n", indent, access, name);
    out.format("%s        AnnotationMirror mirror = getMirror(PRISM_TYPE, element);%n", indent);
    out.format("%s        if(mirror == null) return null;%n", indent);
    out.format("%s        return getInstance(mirror);%n", indent);
    out.format("%s   }%n%n", indent);
  }

  void writeGetOptionalOn() {

    // getOptionalOn
    out.format(
        "%s    /** Return a Optional representing a nullable {@code @%s} annotation on 'e'. %n",
        indent, annName);
    out.format(
        "%s      * similar to {@code element.getAnnotation(%s.class)} except that %n",
        indent, annName);
    out.format(
        "%s      * an Optional of this class rather than an instance of {@code %s}%n",
        indent, annName);
    out.format("%s      * is returned.%n", indent);
    out.format("%s      */%n", indent);
    out.format(
        "%s    %sstatic Optional<%s> getOptionalOn(Element element) {%n", indent, access, name);
    out.format("%s        AnnotationMirror mirror = getMirror(PRISM_TYPE, element);%n", indent);
    out.format("%s        if(mirror == null) return Optional.empty();%n", indent);
    out.format("%s        return getOptional(mirror);%n", indent);
    out.format("%s   }%n%n", indent);
  }

  private void writeGetAllInstances() {
    // get multiple instances
    out.format(
        "%s    /** Return a list of prisms representing the {@code @%s} annotation on 'e'. %n",
        indent, annName);
    out.format(
        "%s      * similar to {@code e.getAnnotationsByType(%s.class)} except that %n",
        indent, annName);
    out.format(
        "%s      * instances of this class rather than instances of {@code %s}%n", indent, annName);
    out.format("%s      * is returned.%n", indent);
    out.format("%s      */%n", indent);
    out.format(
        "%s    %sstatic List<%s> getAllInstancesOn(Element element) {%n", indent, access, name);
    out.format(
        "%s        return getMirrors(PRISM_TYPE, element).stream().map(%s::getInstance).collect(toList());%n",
        indent, name);
    out.format("%s   }%n%n", indent);
  }

  private void writeGetInstance() {
    out.format(
        "%s    /** Return a prism of the {@code @%s} annotation whose mirror is mirror. %n",
        indent, annName);
    out.format("%s      */%n", indent);
    out.format(
        "%s    %sstatic %s getInstance(AnnotationMirror mirror) {%n",
        indent, inner ? "private " : access, name);
    out.format(
        "%s        if(mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;%n%n",
        indent);
    out.format("%s        return new %s(mirror);%n", indent, name);
    out.format("%s    }%n%n", indent);
  }

  private void writeGetOptional() {

    // getOptional
    out.format(
        "%s    /** Return a {@code Optional<%s>} representing a {@code @%s} annotation mirror. %n",
        indent, name, annName);
    out.format(
        "%s      * similar to {@code e.getAnnotation(%s.class)} except that %n", indent, annName);
    out.format(
        "%s      * an Optional of this class rather than an instance of {@code %s}%n",
        indent, annName);
    out.format("%s      * is returned.%n", indent);
    out.format("%s      */%n", indent);
    out.format(
        "%s    %sstatic Optional<%s> getOptional(AnnotationMirror mirror) {%n",
        indent, inner ? "private " : access, name);
    out.format(
        "%s        if(mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return Optional.empty();%n%n",
        indent);
    out.format("%s        return Optional.of(new %s(mirror));%n", indent, name);
    out.format("%s    }%n%n", indent);
  }
}
