package io.avaje.prism.internal;

import static io.avaje.prism.internal.ProcessingContext.asElement;

import javax.lang.model.type.TypeMirror;

class Util {
  private Util() {}

  static boolean isRepeatable(TypeMirror typeMirror) {
    return RepeatablePrism.isPresent(asElement(typeMirror));
  }

  static boolean isMeta(TypeMirror typeMirror) {
    final var target = TargetPrism.getInstanceOn(asElement(typeMirror));
    return target == null || target.value().contains("ANNOTATION_TYPE");
  }
}
