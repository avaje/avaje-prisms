package io.avaje.prism.internal;

import static java.util.function.Predicate.not;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
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
import javax.tools.StandardLocation;

/**
 * Utility class that stores the {@link ProcessingEnvironment} and provides various helper methods
 */
@Generated("avaje-prism-generator")
final class APContext {

  private static int jdkVersion;
  private static boolean previewEnabled;
  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private APContext() {}

  public static final class Ctx {
    private final ProcessingEnvironment processingEnv;
    private final Messager messager;
    private final Filer filer;
    private final Elements elementUtils;
    private final Types typeUtils;
    private ModuleElement module;
    private final boolean isTestCompilation;

    private Ctx(ProcessingEnvironment processingEnv) {

      this.processingEnv = processingEnv;
      messager = processingEnv.getMessager();
      filer = processingEnv.getFiler();
      elementUtils = processingEnv.getElementUtils();
      typeUtils = processingEnv.getTypeUtils();
      boolean test;
      try {
        test =
            filer
                .createResource(StandardLocation.CLASS_OUTPUT, "", UUID.randomUUID().toString())
                .toUri()
                .toString()
                .contains("test-classes");

      } catch (Exception e) {
        test = false;
      }
      isTestCompilation = test;
    }
  }

  /**
   * Initialize the ThreadLocal containing the Processing Enviroment. this typically should be
   * called during the init phase of processing. Be sure to run the clear method at the last round
   * of processing
   *
   * @param processingEnv the current annotation processing enviroment
   */
  public static void init(ProcessingEnvironment processingEnv) {
    CTX.set(new Ctx(processingEnv));
    jdkVersion = processingEnv.getSourceVersion().ordinal();
    previewEnabled = jdkVersion >= 13 && initPreviewEnabled(processingEnv);
  }

  private static boolean initPreviewEnabled(ProcessingEnvironment processingEnv) {
    try {
      return (boolean)
          ProcessingEnvironment.class.getDeclaredMethod("isPreviewEnabled").invoke(processingEnv);
    } catch (final Throwable e) {
      return false;
    }
  }

  /**
   * Initialize the ThreadLocal containing the {@link ProcessingEnvironment}. Be sure to run the
   * clear method at the last round of processing
   *
   * @param context the current annotation processing enviroment
   */
  public static void init(Ctx context) {
    CTX.set(context);
  }

  /** Clears the ThreadLocal containing the {@link ProcessingEnvironment}. */
  public static void clear() {
    CTX.remove();
  }

  /**
   * Returns the source version that any generated source and class files should conform to
   *
   * @return the source version as an int
   */
  public static int jdkVersion() {
    return jdkVersion;
  }

  /**
   * Returns whether {@code --preview-enabled} has been added to compiler flags.
   *
   * @return true if preview features are enabled
   */
  public static boolean previewEnabled() {
    return previewEnabled;
  }

  private static Ctx getCtx() {
    var ctx = CTX.get();
    if (ctx == null) {
      throw new IllegalStateException("APContext.init() has not been called");
    }
    return CTX.get();
  }

  /**
   * Prints an error at the location of the element.
   *
   * @param e the element to use as a position hint
   * @param msg the message, or an empty string if none
   * @param args {@code String#format} arguments
   */
  public static void logError(Element e, String msg, Object... args) {
    messager().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  /**
   * Prints an error.
   *
   * @param msg the message, or an empty string if none
   * @param args {@code String#format} arguments
   */
  public static void logError(String msg, Object... args) {
    messager().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  /**
   * Prints an warning at the location of the element.
   *
   * @param e the element to use as a position hint
   * @param msg the message, or an empty string if none
   * @param args {@code String#format} arguments
   */
  public static void logWarn(Element e, String msg, Object... args) {
    messager().printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
  }

  /**
   * Prints a warning.
   *
   * @param msg the message, or an empty string if none
   * @param args {@code String#format} arguments
   */
  public static void logWarn(String msg, Object... args) {
    messager().printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  /**
   * Prints a note.
   *
   * @param msg the message, or an empty string if none
   * @param args {@code String#format} arguments
   */
  public static void logNote(Element e, String msg, Object... args) {
    messager().printMessage(Diagnostic.Kind.NOTE, String.format(msg, args), e);
  }

  /**
   * Prints a note at the location of the element.
   *
   * @param e the element to use as a position hint
   * @param msg the message, or an empty string if none
   * @param args {@code String#format} arguments
   */
  public static void logNote(String msg, Object... args) {
    messager().printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  /**
   * Create a file writer for the given class name.
   *
   * @param name canonical (fully qualified) name of the principal class or interface being declared
   *     in this file or a package name followed by {@code ".package-info"} for a package
   *     information file
   * @param originatingElements class, interface, package, or module elements causally associated
   *     with the creation of this file, may be elided or {@code null}
   * @return a JavaFileObject to write the new source file
   */
  public static JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
      throws IOException {
    return filer().createSourceFile(name, originatingElements);
  }

  /**
   * Returns a type element given its canonical name.
   *
   * @param name the canonical name
   * @return the named type element, or null if no type element can be uniquely determined
   */
  public static TypeElement typeElement(String name) {
    return elements().getTypeElement(name);
  }

  /**
   * Returns the element corresponding to a type.The type may be a DeclaredType or
   * TypeVariable.Returns null if the type is not one with a corresponding element.
   *
   * @param t the type to map to an element
   * @return the element corresponding to the given type
   */
  public static TypeElement asTypeElement(TypeMirror t) {

    return (TypeElement) types().asElement(t);
  }

  /**
   * Get current {@link ProcessingEnvironment}
   *
   * @return the enviroment
   */
  public static ProcessingEnvironment processingEnv() {
    return getCtx().processingEnv;
  }

  /**
   * Get current {@link Filer} from the {@link ProcessingEnvironment}
   *
   * @return the filer
   */
  public static Filer filer() {
    return getCtx().filer;
  }

  /**
   * Get current {@link Elements} from the {@link ProcessingEnvironment}
   *
   * @return the filer
   */
  public static Elements elements() {
    return getCtx().elementUtils;
  }

  /**
   * Get current {@link Messager} from the {@link ProcessingEnvironment}
   *
   * @return the messager
   */
  public static Messager messager() {
    return getCtx().messager;
  }

  /**
   * Get current {@link Types} from the {@link ProcessingEnvironment}
   *
   * @return the types
   */
  public static Types types() {
    return getCtx().typeUtils;
  }

  /**
   * Determine whether the first type can be assigned to the second
   *
   * @param type string type to check
   * @param superType the type that should be assignable to.
   * @return true if type can be assinged to supertype
   */
  public static boolean isAssignable(String type, String superType) {
    return type.equals(superType) || isAssignable(typeElement(type), superType);
  }

  /**
   * Determine whether the first type can be assigned to the second
   *
   * @param type type to check
   * @param superType the type that should be assignable to.
   * @return true if type can be assinged to supertype
   */
  public static boolean isAssignable(TypeElement type, String superType) {
    return Optional.ofNullable(type).stream()
        .flatMap(APContext::superTypes)
        .anyMatch(superType::equals);
  }

  private static Stream<String> superTypes(TypeElement element) {
    final var types = types();
    return types.directSupertypes(element.asType()).stream()
        .filter(type -> !type.toString().contains("java.lang.Object"))
        .map(superType -> (TypeElement) types.asElement(superType))
        .flatMap(e -> Stream.concat(superTypes(e), Stream.of(e)))
        .map(Object::toString);
  }

  /**
   * Discover the {@link ModuleElement} for the project being processed and set in the context.
   *
   * @param annotations the annotation interfaces requested to be processed
   * @param roundEnv environment for information about the current and prior round
   */
  public static void setProjectModuleElement(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (getCtx().module == null) {
      getCtx().module =
          annotations.stream()
              .map(roundEnv::getElementsAnnotatedWith)
              .filter(not(Collection::isEmpty))
              .findAny()
              .map(s -> s.iterator().next())
              .map(elements()::getModuleOf)
              .orElse(null);
    }
  }

  /**
   * Retrieve the project's {@link ModuleElement}. {@code setProjectModuleElement} must be called
   * before this.
   *
   * @return the {@link ModuleElement} associated with the current project
   */
  public static ModuleElement getProjectModuleElement() {
    return getCtx().module;
  }

  /**
   * Gets a {@link BufferedReader} for the project's {@code module-info.java} source file.
   *
   * <p>Calling {@link ModuleElement}'s {@code getDirectives()} method has a chance of making
   * compilation fail in certain situations. Therefore, manually parsing {@code module-info.java}
   * seems to be the safest way to get module information.
   *
   * @return
   * @throws IOException if unable to read the module-info
   */
  public static BufferedReader getModuleInfoReader() throws IOException {

    var modulePath = isTestCompilation() ? "src/main/test" : "src/main/java";
    // some JVM implementations do not implement SOURCE_PATH so gotta find the module path by trying
    // to find the src folder
    var path =
        Path.of(
            filer()
                .createResource(StandardLocation.CLASS_OUTPUT, "", UUID.randomUUID().toString())
                .toUri());
    var i = 0;
    while (i < 5 && path != null && !path.resolve(modulePath).toFile().exists()) {
      i++;
      path = path.getParent();
    }

    var moduleFile = path.resolve(modulePath + "/module-info.java");
    if (moduleFile.toFile().exists()) {
      return new BufferedReader(new InputStreamReader(moduleFile.toUri().toURL().openStream()));
    }

    // if that fails try same directory
    moduleFile = Path.of("module-info.java");
    if (moduleFile.toFile().exists()) {
      return new BufferedReader(new InputStreamReader(moduleFile.toUri().toURL().openStream()));
    }

    // if that fails try via SOURCE_PATH
    var sourcePath =
        Path.of(filer().getResource(StandardLocation.SOURCE_PATH, "", "module-info.java").toUri());

    return new BufferedReader(new InputStreamReader(sourcePath.toUri().toURL().openStream()));
  }

  /**
   * Given the relative path, gets a {@link Path} from the Maven {@code target}/Gradle {@code build} folder.
   * @param path the relative path of the file in the target/build folder
   *
   * @return the file object
   * @throws IOException if unable to retrieve the file
   */
  public static Path getBuildResource(String path) throws IOException {

    var id = UUID.randomUUID().toString();
    final var uri =
        filer()
            .createResource(StandardLocation.CLASS_OUTPUT, "", path + id)
            .toUri()
            .toString()
            .replaceFirst(id, "")
            .replaceFirst("/classes/java/main", "")
            .replaceFirst("/classes", "");
    var updatedPath = Path.of(URI.create(uri));
    if (path.contains("/")) {
      updatedPath.getParent().toFile().mkdirs();
    }
    return updatedPath;
  }

  /**
   * Return true if the compiler is creating test classes.
   *
   * @return Whether the current apt compilation is for test-compile.
   */
  public static boolean isTestCompilation() {
    return getCtx().isTestCompilation;
  }
}
