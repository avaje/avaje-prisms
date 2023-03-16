package io.avaje.prism.internal;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private ProcessingContext() {}

  static final class Ctx {
    private final Messager messager;
    private final Filer filer;
    private final Elements elementUtils;
    private final Types typeUtils;
    public Ctx(ProcessingEnvironment processingEnv) {

      messager = processingEnv.getMessager();
      filer = processingEnv.getFiler();
      elementUtils = processingEnv.getElementUtils();
      typeUtils = processingEnv.getTypeUtils();
    }
  }

  public static void init(ProcessingEnvironment processingEnv) {
    CTX.set(new Ctx(processingEnv));
  }

  /** Log an error message. */
  static void logError(Element e, String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  static void logError(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  static void logWarn(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  static void logDebug(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  /** Create a file writer for the given class name. */
  static JavaFileObject createWriter(String cls) throws IOException {
    return CTX.get().filer.createSourceFile(cls);
  }

  static TypeElement element(String rawType) {
    return CTX.get().elementUtils.getTypeElement(rawType);
  }

  static Types types() {
    return CTX.get().typeUtils;
  }

  static TypeElement elementMaybe(String rawType) {
    if (rawType == null) {
      return null;
    } else {
      return CTX.get().elementUtils.getTypeElement(rawType);
    }
  }

  static Element asElement(TypeMirror returnType) {

    return CTX.get().typeUtils.asElement(returnType);
  }

  public static void clear() {
    CTX.remove();
  }
}
