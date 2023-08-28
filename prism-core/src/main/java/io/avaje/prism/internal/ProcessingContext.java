package io.avaje.prism.internal;

import static java.util.function.Predicate.not;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
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
    private ModuleElement module;

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

  static void logWarn(Element e, String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
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

  static Filer filer() {
    return CTX.get().filer;
  }

  public static boolean isAssignable2Interface(Element type, String superType) {
    return Optional.ofNullable(type).stream()
        .flatMap(ProcessingContext::superTypes)
        .anyMatch(superType::equals);
  }

  public static Stream<String> superTypes(Element element) {
    final var types = CTX.get().typeUtils;
    return types.directSupertypes(element.asType()).stream()
        .filter(type -> !type.toString().contains("java.lang.Object"))
        .map(superType -> (TypeElement) types.asElement(superType))
        .flatMap(e -> Stream.concat(superTypes(e), Stream.of(e)))
        .map(Object::toString);
  }

  public static void setProjectModuleElement(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (CTX.get().module == null) {
      CTX.get().module =
          annotations.stream()
              .map(roundEnv::getElementsAnnotatedWith)
              .filter(not(Collection::isEmpty))
              .findAny()
              .map(s -> s.iterator().next())
              .map(CTX.get().elementUtils::getModuleOf)
              .orElse(null);
    }
  }

  public static ModuleElement getProjectModuleElement() {
    return CTX.get().module;
  }
}
