package io.avaje.prism.internal;

import static io.avaje.prism.internal.APContext.asElement;

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

  static String shortName(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }
}
