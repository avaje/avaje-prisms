package io.avaje.prism.internal;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/** A Prism representing an {@code @io.avaje.prism.GeneratePrism} annotation. */
final class GeneratePrismPrism {
  /** store prism value of value */
  private final TypeMirror _value;

  /** store prism value of name */
  private final String _name;

  /** store prism value of publicAccess */
  private final Boolean _publicAccess;

  /** store prism value of superClass */
  private final TypeMirror _superClass;

  /** store prism value of superInterfaces */
  private final List<TypeMirror> _superInterfaces;

  public static final String PRISM_TYPE = "io.avaje.prism.GeneratePrism";

  /**
   * An instance of the Values inner class whose
   * methods return the AnnotationValues used to build this prism.
   * Primarily intended to support using Messager.
   */
  final Values values;

  /** Returns true if the prism annotation is present on the element, else false.
   *
   * @param element element.
   * @return true if prism is present.
   */
  static boolean isPresent(Element element) {
    return getInstanceOn(element) != null;
  }

  /** Return a prism representing the {@code @io.avaje.prism.GeneratePrism} annotation on 'e'.
   * similar to {@code element.getAnnotation(io.avaje.prism.GeneratePrism.class)} except that
   * an instance of this class rather than an instance of {@code io.avaje.prism.GeneratePrism}
   * is returned.
   *
   * @param element element.
   * @return prism for element or null if no annotation is found.
   */
  static GeneratePrismPrism getInstanceOn(Element element) {
    final var mirror = getMirror(element);
    if (mirror == null) return null;
    return getInstance(mirror);
  }

  /** Return a Optional representing a nullable {@code @io.avaje.prism.GeneratePrism} annotation on 'e'.
   * similar to {@code element.getAnnotation(io.avaje.prism.GeneratePrism.class)} except that
   * an Optional of this class rather than an instance of {@code io.avaje.prism.GeneratePrism}
   * is returned.
   *
   * @param element element.
   * @return prism optional for element.
   */
  static Optional<GeneratePrismPrism> getOptionalOn(Element element) {
    final var mirror = getMirror(element);
    if (mirror == null) return Optional.empty();
    return getOptional(mirror);
  }

  /** Return a list of prisms representing the {@code @io.avaje.prism.GeneratePrism} annotation on 'e'.
   * similar to {@code e.getAnnotationsByType(io.avaje.prism.GeneratePrism.class)} except that
   * instances of this class rather than instances of {@code io.avaje.prism.GeneratePrism}
   * is returned.
   *
   * @param element element.
   * @return list of prisms on the element.
   */
  static List<GeneratePrismPrism> getAllInstancesOn(Element element) {
    return getMirrors(element)
        .map(GeneratePrismPrism::getInstance)
        .collect(toList());
  }

  /** Return a prism of the {@code @io.avaje.prism.GeneratePrism} annotation whose mirror is mirror.
   *
   * @param mirror mirror.
   * @return prism for mirror or null if mirror is an incorrect type.
   */
  static GeneratePrismPrism getInstance(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;

    return new GeneratePrismPrism(mirror);
  }

  /** Return a {@code Optional<GeneratePrismPrism>} representing a {@code @io.avaje.prism.GeneratePrism} annotation mirror.
   * similar to {@code e.getAnnotation(io.avaje.prism.GeneratePrism.class)} except that
   * an Optional of this class rather than an instance of {@code io.avaje.prism.GeneratePrism}
   * is returned.
   *
   * @param mirror mirror.
   * @return prism optional for mirror.
   */
  static Optional<GeneratePrismPrism> getOptional(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return Optional.empty();

    return Optional.of(new GeneratePrismPrism(mirror));
  }

  private GeneratePrismPrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member : ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    _value = getValue("value", TypeMirror.class);
    _name = getValue("name", String.class);
    _publicAccess = getValue("publicAccess", Boolean.class);
    _superClass = getValue("superClass", TypeMirror.class);
    _superInterfaces = getArrayValues("superInterfaces", TypeMirror.class);
    this.values = new Values(memberValues);
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Returns a TypeMirror representing the value of the {@code java.lang.Class<? extends java.lang.annotation.Annotation> value()} member of the Annotation.
   * @see io.avaje.prism.GeneratePrism#value()
   */
  TypeMirror value() { return _value; }

  /**
   * Returns a String representing the value of the {@code java.lang.String name()} member of the Annotation.
   * @see io.avaje.prism.GeneratePrism#name()
   */
  String name() { return _name; }

  /**
   * Returns a Boolean representing the value of the {@code boolean publicAccess()} member of the Annotation.
   * @see io.avaje.prism.GeneratePrism#publicAccess()
   */
  Boolean publicAccess() { return _publicAccess; }

  /**
   * Returns a TypeMirror representing the value of the {@code java.lang.Class<?> superClass()} member of the Annotation.
   * @see io.avaje.prism.GeneratePrism#superClass()
   */
  TypeMirror superClass() { return _superClass; }

  /**
   * Returns a List&lt;TypeMirror&gt; representing the value of the {@code superInterfaces()} member of the Annotation.
   * @see io.avaje.prism.GeneratePrism#superInterfaces()
   */
  List<TypeMirror> superInterfaces() { return _superInterfaces; }

  /**
   * Determine whether the underlying AnnotationMirror has no errors.
   * True if the underlying AnnotationMirror has no errors.
   * When true is returned, none of the methods will return null.
   * When false is returned, a least one member will either return null, or another
   * prism that is not valid.
   */
   final boolean isValid;

  /**
   * The underlying AnnotationMirror of the annotation
   * represented by this Prism.
   * Primarily intended to support using Messager.
   */
   final AnnotationMirror mirror;
  /**
   * A class whose members corespond to those of io.avaje.prism.GeneratePrism
   * but which each return the AnnotationValue corresponding to
   * that member in the model of the annotations. Returns null for
   * defaulted members. Used for Messager, so default values are not useful.
   */
  static final class Values {
    private final Map<String, AnnotationValue> values;

    private Values(Map<String, AnnotationValue> values) {
      this.values = values;
    }
    /** Return the AnnotationValue corresponding to the value()
     * member of the annotation, or null when the default value is implied.
     */
    AnnotationValue value(){ return values.get("value");}
    /** Return the AnnotationValue corresponding to the name()
     * member of the annotation, or null when the default value is implied.
     */
    AnnotationValue name(){ return values.get("name");}
    /** Return the AnnotationValue corresponding to the publicAccess()
     * member of the annotation, or null when the default value is implied.
     */
    AnnotationValue publicAccess(){ return values.get("publicAccess");}
    /** Return the AnnotationValue corresponding to the superClass()
     * member of the annotation, or null when the default value is implied.
     */
    AnnotationValue superClass(){ return values.get("superClass");}
    /** Return the AnnotationValue corresponding to the superInterfaces()
     * member of the annotation, or null when the default value is implied.
     */
    AnnotationValue superInterfaces(){ return values.get("superInterfaces");}
  }

  private final Map<String, AnnotationValue> defaults = new HashMap<>(10);
  private final Map<String, AnnotationValue> memberValues = new HashMap<>(10);
  private boolean valid = true;

  private <T> T getValue(String name, Class<T> clazz) {
    final T result = GeneratePrismPrism.getValue(memberValues, defaults, name, clazz);
    if (result == null) valid = false;
    return result;
  }

  private <T> List<T> getArrayValues(String name, final Class<T> clazz) {
    final List<T> result = GeneratePrismPrism.getArrayValues(memberValues, defaults, name, clazz);
    if (result == null) valid = false;
    return result;
  }
  private static AnnotationMirror getMirror(Element target) {
    for (final var m : target.getAnnotationMirrors()) {
      final CharSequence mfqn = ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();
      if (PRISM_TYPE.contentEquals(mfqn)) return m;
    }
    return null;
  }

  private static Stream<? extends AnnotationMirror> getMirrors(Element target) {
    return target.getAnnotationMirrors().stream()
        .filter(
             m -> PRISM_TYPE.contentEquals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName()));
  }

  private static <T> T getValue(Map<String, AnnotationValue> memberValues, Map<String, AnnotationValue> defaults, String name, Class<T> clazz) {
    AnnotationValue av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return null;
    }
    if (clazz.isInstance(av.getValue())) return clazz.cast(av.getValue());
    return null;
  }

  private static <T> List<T> getArrayValues(Map<String, AnnotationValue> memberValues, Map<String, AnnotationValue> defaults, String name, final Class<T> clazz) {
    AnnotationValue av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return List.of();
    }
    if (av.getValue() instanceof List) {
      final List<T> result = new ArrayList<>();
      for (final var v : getValueAsList(av)) {
        if (clazz.isInstance(v.getValue())) {
          result.add(clazz.cast(v.getValue()));
        } else {
          return List.of();
        }
      }
      return result;
    } else {
      return List.of();
    }
  }

  @SuppressWarnings("unchecked")
  private static List<AnnotationValue> getValueAsList(AnnotationValue av) {
    return (List<AnnotationValue>) av.getValue();
  }
}
