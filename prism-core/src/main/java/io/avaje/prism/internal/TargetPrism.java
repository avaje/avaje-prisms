package io.avaje.prism.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/** A Prism representing an {@code @java.lang.annotation.Target} annotation. */
class TargetPrism {
  /** store prism value of value */
  private final List<String> _value;

  public static final String PRISM_TYPE = "java.lang.annotation.Target";

  /**
   * An instance of the Values inner class whose methods return the AnnotationValues used to build
   * this prism. Primarily intended to support using Messager.
   */
  final Values values;

  /**
   * Return a prism representing the {@code @java.lang.annotation.Target} annotation on 'e'. similar
   * to {@code element.getAnnotation(java.lang.annotation.Target.class)} except that an instance of
   * this class rather than an instance of {@code java.lang.annotation.Target} is returned.
   */
  static TargetPrism getInstanceOn(Element element) {
    final var mirror = getMirror(PRISM_TYPE, element);
    if (mirror == null) return null;
    return getInstance(mirror);
  }

  /**
   * Return a prism of the {@code @java.lang.annotation.Target} annotation whose mirror is mirror.
   */
  static TargetPrism getInstance(AnnotationMirror mirror) {
    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;

    return new TargetPrism(mirror);
  }

  private TargetPrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member :
        ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    final List<VariableElement> valueMirrors = getArrayValues("value", VariableElement.class);
    _value = new ArrayList<>(valueMirrors.size());
    for (final VariableElement valueMirror : valueMirrors) {
      _value.add(valueMirror.getSimpleName().toString());
    }
    this.values = new Values(memberValues);
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Returns a List<String> representing the value of the {@code value()} member of the Annotation.
   *
   * @see java.lang.annotation.Target#value()
   */
  List<String> value() {
    return _value;
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
   * A class whose members corespond to those of java.lang.annotation.Target but which each return
   * the AnnotationValue corresponding to that member in the model of the annotations. Returns null
   * for defaulted members. Used for Messager, so default values are not useful.
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
  private boolean valid = true;

  private <T> List<T> getArrayValues(String name, final Class<T> clazz) {
    final List<T> result = TargetPrism.getArrayValues(memberValues, defaults, name, clazz);
    if (result == null) valid = false;
    return result;
  }

  private static AnnotationMirror getMirror(String fqn, Element target) {
    for (final AnnotationMirror m : target.getAnnotationMirrors()) {
      final CharSequence mfqn =
          ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();
      if (fqn.contentEquals(mfqn)) return m;
    }
    return null;
  }

  private static <T> List<T> getArrayValues(
      Map<String, AnnotationValue> memberValues,
      Map<String, AnnotationValue> defaults,
      String name,
      final Class<T> clazz) {
    var av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return java.util.List.of();
    }
    if (av.getValue() instanceof List) {
      final List<T> result = new ArrayList<>();
      for (final AnnotationValue v : getValueAsList(av)) {
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
