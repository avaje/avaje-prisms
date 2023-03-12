/*
Copyright (c) 2006,2007, Bruce Chapman

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation and/or
      other materials provided with the distribution.
    * Neither the name of the Hickory project nor the names of its contributors
      may be used to endorse or promote products derived from this software without
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package io.avaje.prism.internal;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/** A Prism representing an {@code @io.avaje.prism.GeneratePrism} annotation. */
class GeneratePrismPrism {
  /** store prism value of value */
  private final TypeMirror _value;

  /** store prism value of name */
  private final String _name;

  /** store prism value of publicAccess */
  private final Boolean _publicAccess;

  /**
   * An instance of the Values inner class whose methods return the AnnotationValues used to build
   * this prism. Primarily intended to support using Messager.
   */
  final Values values;
  /**
   * Return a prism representing the {@code @io.avaje.prism.GeneratePrism} annotation on 'e'.
   * similar to {@code e.getAnnotation(io.avaje.prism.GeneratePrism.class)} except that an instance
   * of this class rather than an instance of {@code io.avaje.prism.GeneratePrism} is returned.
   */
  static GeneratePrismPrism getInstanceOn(Element e) {
    final AnnotationMirror m = getMirror("io.avaje.prism.GeneratePrism", e);
    if (m == null) return null;
    return getInstance(m);
  }

  /**
   * Return a prism of the {@code @io.avaje.prism.GeneratePrism} annotation whose mirror is mirror.
   */
  static GeneratePrismPrism getInstance(AnnotationMirror mirror) {
    return new GeneratePrismPrism(mirror);
  }

  private GeneratePrismPrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member :
        ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    _value = getValue("value", TypeMirror.class);
    _name = getValue("name", String.class);
    _publicAccess = getValue("publicAccess", Boolean.class);
    this.values = new Values(memberValues);
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Returns a TypeMirror representing the value of the {@code java.lang.Class<? extends
   * java.lang.annotation.Annotation> value()} member of the Annotation.
   *
   * @see io.avaje.prism.GeneratePrism#value()
   */
  TypeMirror value() {
    return _value;
  }

  /**
   * Returns a String representing the value of the {@code java.lang.String name()} member of the
   * Annotation.
   *
   * @see io.avaje.prism.GeneratePrism#name()
   */
  String name() {
    return _name;
  }

  /**
   * Returns a Boolean representing the value of the {@code boolean publicAccess()} member of the
   * Annotation.
   *
   * @see io.avaje.prism.GeneratePrism#publicAccess()
   */
  Boolean publicAccess() {
    return _publicAccess;
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
   * A class whose members corespond to those of io.avaje.prism.GeneratePrism but which each return
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
    /**
     * Return the AnnotationValue corresponding to the name() member of the annotation, or null when
     * the default value is implied.
     */
    AnnotationValue name() {
      return values.get("name");
    }
    /**
     * Return the AnnotationValue corresponding to the publicAccess() member of the annotation, or
     * null when the default value is implied.
     */
    AnnotationValue publicAccess() {
      return values.get("publicAccess");
    }
  }

  private final Map<String, AnnotationValue> defaults = new HashMap<>(10);
  private final Map<String, AnnotationValue> memberValues = new HashMap<>(10);
  private boolean valid = true;

  private <T> T getValue(String name, Class<T> clazz) {
    final T result = GeneratePrismPrism.getValue(memberValues, defaults, name, clazz);
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

  private static <T> T getValue(
      Map<String, AnnotationValue> memberValues,
      Map<String, AnnotationValue> defaults,
      String name,
      Class<T> clazz) {
    AnnotationValue av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return null;
    }
    if (clazz.isInstance(av.getValue())) return clazz.cast(av.getValue());
    return null;
  }
}
