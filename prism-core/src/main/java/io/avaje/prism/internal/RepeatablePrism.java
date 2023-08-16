package io.avaje.prism.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/** A Prism representing an {@code @java.lang.annotation.Repeatable} annotation. */
class RepeatablePrism {

  public static final String PRISM_TYPE = "java.lang.annotation.Repeatable";

  /**
   * An instance of the Values inner class whose methods return the AnnotationValues used to build
   * this prism. Primarily intended to support using Messager.
   */
  final Values values;

  /** Returns true if the prism annotation is present on the element, else false. */
  static boolean isPresent(Element element) {
    return getInstanceOn(element) != null;
  }

  /**
   * Return a prism representing the {@code @java.lang.annotation.Repeatable} annotation on 'e'.
   * similar to {@code e.getAnnotation(java.lang.annotation.Repeatable.class)} except that an
   * instance of this class rather than an instance of {@code java.lang.annotation.Repeatable} is
   * returned.
   */
  static RepeatablePrism getInstanceOn(Element element) {
    final var mirror = getMirror(PRISM_TYPE, element);
    if (mirror == null) return null;
    return getInstance(mirror);
  }

  /**
   * Return a Optional representing a nullable {@code @java.lang.annotation.Repeatable} annotation
   * on 'e'. similar to {@code element.getAnnotation(java.lang.annotation.Repeatable.class)} except
   * that an Optional of this class rather than an instance of {@code
   * java.lang.annotation.Repeatable} is returned.
   */
  static Optional<RepeatablePrism> getOptionalOn(Element element) {
    final var mirror = getMirror(PRISM_TYPE, element);
    if (mirror == null) return Optional.empty();
    return getOptional(mirror);
  }

  /**
   * Return a prism of the {@code @java.lang.annotation.Repeatable} annotation whose mirror is
   * mirror.
   */
  static RepeatablePrism getInstance(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;

    return new RepeatablePrism(mirror);
  }

  /**
   * Return a {@code Optional<RepeatablePrism>} representing a
   * {@code @java.lang.annotation.Repeatable} annotation mirror. similar to {@code
   * e.getAnnotation(java.lang.annotation.Repeatable.class)} except that an Optional of this class
   * rather than an instance of {@code java.lang.annotation.Repeatable} is returned.
   */
  static Optional<RepeatablePrism> getOptional(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString()))
      return Optional.empty();

    return Optional.of(new RepeatablePrism(mirror));
  }

  private RepeatablePrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member :
        ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    this.values = new Values(memberValues);
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Determine whether the underlying AnnotationMirror has no errors. True if the underlying
   * AnnotationMirror has no errors. When true is returned, none of the methods will return null.
   * When false is returned, a least one member will either return null, or another prism that is
   * not valid.
   */
  final boolean isValid;

  /**
   * The underlying AnnotationMirror of the annotation represented by this Prism. Primarily intended
   * to support using Messager.
   */
  final AnnotationMirror mirror;
  /**
   * A class whose members corespond to those of java.lang.annotation.Repeatable but which each
   * return the AnnotationValue corresponding to that member in the model of the annotations.
   * Returns null for defaulted members. Used for Messager, so default values are not useful.
   */
  static class Values {
    private final Map<String, AnnotationValue> values;

    private Values(Map<String, AnnotationValue> values) {
      this.values = values;
    }
    /**
     * Return the AnnotationValue corresponding to the value() member of the annotation, or null
     * when the default value is implied.
     */
    AnnotationValue value() {
      return values.get("value");
    }
  }

  private final Map<String, AnnotationValue> defaults = new HashMap<>(10);
  private final Map<String, AnnotationValue> memberValues = new HashMap<>(10);
  private final boolean valid = true;

  private static AnnotationMirror getMirror(String fqn, Element target) {
    for (final AnnotationMirror m : target.getAnnotationMirrors()) {
      final CharSequence mfqn =
          ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();
      if (fqn.contentEquals(mfqn)) return m;
    }
    return null;
  }
}
