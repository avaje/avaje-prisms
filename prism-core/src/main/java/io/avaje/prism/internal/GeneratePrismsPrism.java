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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
/** A Prism representing an {@code @io.avaje.prism.GeneratePrisms} annotation. */
class GeneratePrismsPrism {
  /** store prism value of value */
  private final List<GeneratePrismPrism> _value;

  /**
   * Return a prism representing the {@code @io.avaje.prism.GeneratePrisms} annotation on 'e'.
   * similar to {@code e.getAnnotation(io.avaje.prism.GeneratePrisms.class)} except that an
   * instance of this class rather than an instance of {@code io.avaje.prism.GeneratePrisms} is
   * returned.
   */
  static GeneratePrismsPrism getInstanceOn(Element e) {
    final AnnotationMirror m = getMirror("io.avaje.prism.GeneratePrisms", e);
    if (m == null) return null;
    return getInstance(m);
  }

  /**
   * Return a prism of the {@code @io.avaje.prism.GeneratePrisms} annotation whose mirror is
   * mirror.
   */
  static GeneratePrismsPrism getInstance(AnnotationMirror mirror) {
    return new GeneratePrismsPrism(mirror);
  }

  private GeneratePrismsPrism(AnnotationMirror mirror) {
    for (final ExecutableElement key : mirror.getElementValues().keySet()) {
      memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));
    }
    for (final ExecutableElement member :
        ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {
      defaults.put(member.getSimpleName().toString(), member.getDefaultValue());
    }
    final List<AnnotationMirror> valueMirrors = getArrayValues("value", AnnotationMirror.class);
    _value = new ArrayList<>(valueMirrors.size());
    for (final AnnotationMirror valueMirror : valueMirrors) {
      _value.add(GeneratePrismPrism.getInstance(valueMirror));
    }
    this.mirror = mirror;
    this.isValid = valid;
  }

  /**
   * Returns a List<GeneratePrismPrism> representing the value of the {@code value()} member of the
   * Annotation.
   *
   * @see io.avaje.prism.GeneratePrisms#value()
   */
  List<GeneratePrismPrism> value() {
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

  private final Map<String, AnnotationValue> defaults = new HashMap<>(10);
  private final Map<String, AnnotationValue> memberValues = new HashMap<>(10);
  private boolean valid = true;

  private <T> List<T> getArrayValues(String name, final Class<T> clazz) {
    final List<T> result = GeneratePrismsPrism.getArrayValues(memberValues, defaults, name, clazz);
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
    AnnotationValue av = memberValues.get(name);
    if (av == null) av = defaults.get(name);
    if (av == null) {
      return List.of();
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
