package io.avaje.prism.internal;

import static io.avaje.prism.internal.APContext.jdkVersion;

import java.io.PrintWriter;
import java.util.UUID;

import javax.tools.StandardLocation;

public class APContextWriter {
  private APContextWriter() {}

  private static String compilerImports() {

    if (jdkVersion() >= 25 || jdkVersion() >= 23 && APContext.previewEnabled()) {
      return "import module java.base;\n" + "import module java.compiler;\n";
    }

    return "import java.io.*;\n"
        + "import java.net.URI;\n"
        + "import java.nio.file.Path;\n"
        + "import java.util.*;\n"
        + "import java.util.stream.Stream;\n"
        + "\n"
        + "import javax.annotation.processing.*;\n"
        + "import javax.annotation.processing.Generated;\n"
        + "import javax.annotation.processing.Messager;\n"
        + "import javax.annotation.processing.ProcessingEnvironment;\n"
        + "import javax.annotation.processing.RoundEnvironment;\n"
        + "import javax.lang.model.element.Element;\n"
        + "import javax.lang.model.element.ModuleElement;\n"
        + "import javax.lang.model.element.TypeElement;\n"
        + "import javax.lang.model.type.TypeMirror;\n"
        + "import javax.lang.model.util.Elements;\n"
        + "import javax.lang.model.util.Types;\n"
        + "import javax.tools.Diagnostic;\n"
        + "import javax.tools.JavaFileObject;\n"
        + "import javax.tools.StandardLocation;\n";
  }

  private static String preview() {
    if (jdkVersion() >= 13) {
      return "    previewEnabled = processingEnv.isPreviewEnabled();\n";
    }
    return "    previewEnabled = jdkVersion >= 13 && initPreviewEnabled(processingEnv);\n"
        + "  }\n"
        + "\n"
        + "  private static boolean initPreviewEnabled(ProcessingEnvironment processingEnv) {\n"
        + "    try {\n"
        + "      return (boolean)\n"
        + "          ProcessingEnvironment.class.getDeclaredMethod(\"isPreviewEnabled\").invoke(processingEnv);\n"
        + "    } catch (final Throwable e) {\n"
        + "      return false;\n"
        + "    }\n";
  }

  public static void write(PrintWriter out, String packageName, boolean moduleReader) {
    out.append(
        "package "
            + packageName
            + ";\n"
            + "\n"
            + "import static java.util.function.Predicate.not;\n"
            + "\n"
            + compilerImports()
            + "\n"
            + "/**\n"
            + " * Utiliy Class that stores the {@link ProcessingEnvironment} and provides various helper methods\n"
            + " */\n"
            + "@Generated(\"avaje-prism-generator\")\n"
            + "public final class APContext {\n"
            + "\n"
            + "  private static int jdkVersion;\n"
            + "  private static boolean previewEnabled;\n"
            + "  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();\n"
            + "\n"
            + "  private APContext() {}\n"
            + "\n"
            + "  public static final class Ctx {\n"
            + "    private final ProcessingEnvironment processingEnv;\n"
            + "    private final Messager messager;\n"
            + "    private final Filer filer;\n"
            + "    private final Elements elementUtils;\n"
            + "    private final Types typeUtils;\n"
            + "    private ModuleElement module;\n"
            + "    private final boolean isTestCompilation;\n"
            + (moduleReader ? "    private ModuleInfoReader moduleReader;\n" : "")
            + "\n"
            + "    private Ctx(ProcessingEnvironment processingEnv) {\n"
            + "\n"
            + "      this.processingEnv = processingEnv;\n"
            + "      messager = processingEnv.getMessager();\n"
            + "      filer = processingEnv.getFiler();\n"
            + "      elementUtils = processingEnv.getElementUtils();\n"
            + "      typeUtils = processingEnv.getTypeUtils();\n"
            + "      boolean test;\n"
            + "      try {\n"
            + "        final var output =\n"
            + "            filer\n"
            + "                .createResource(StandardLocation.CLASS_OUTPUT, \"\", UUID.randomUUID().toString())\n"
            + "                .toUri()\n"
            + "                .toString();\n"
            + "        test = output.contains(\"test-classes\") || output.contains(\"/classes/java/test\");\n"
            + "\n"
            + "      } catch (Exception e) {\n"
            + "        test = false;\n"
            + "      }\n"
            + "      isTestCompilation = test;\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Initialize the ThreadLocal containing the Processing Enviroment. this typically should be\n"
            + "   * called during the init phase of processing. Be sure to run the clear method at the last round\n"
            + "   * of processing\n"
            + "   *\n"
            + "   * @param processingEnv the current annotation processing enviroment\n"
            + "   */\n"
            + "  public static void init(ProcessingEnvironment processingEnv) {\n"
            + "    CTX.set(new Ctx(processingEnv));\n"
            + "    jdkVersion = processingEnv.getSourceVersion().ordinal();\n"
            + preview()
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Initialize the ThreadLocal containing the {@link ProcessingEnvironment}. Be sure to run the\n"
            + "   * clear method at the last round of processing\n"
            + "   *\n"
            + "   * @param context the current annotation processing enviroment\n"
            + "   * @param jdkVersion the JDK version number\n"
            + "   * @param preview whether preview features are enabled\n"
            + "   */\n"
            + "  public static void init(Ctx context, int jdkVersion, boolean preview) {\n"
            + "    CTX.set(context);\n"
            + "    APContext.jdkVersion = jdkVersion;\n"
            + "    previewEnabled = preview;\n"
            + "  }"
            + "\n"
            + "  /** Clears the ThreadLocal containing the {@link ProcessingEnvironment}. */\n"
            + "  public static void clear() {\n"
            + "    CTX.remove();\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Returns the source version that any generated source and class files should conform to\n"
            + "   *\n"
            + "   * @return the source version as an int\n"
            + "   */\n"
            + "  public static int jdkVersion() {\n"
            + "    return jdkVersion;\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Returns whether {@code --preview-enabled} has been added to compiler flags.\n"
            + "   *\n"
            + "   * @return true if preview features are enabled\n"
            + "   */\n"
            + "  public static boolean previewEnabled() {\n"
            + "    return previewEnabled;\n"
            + "  }\n"
            + "\n  private static Ctx getCtx() {\n"
            + "    var ctx = CTX.get();\n"
            + "    if (ctx == null) {\n"
            + "      throw new IllegalStateException(\"APContext has not been initialized with APContext.init\");\n"
            + "    }\n"
            + "    return CTX.get();\n"
            + "  }"
            + "  /**\n"
            + "   * Prints an error at the location of the element.\n"
            + "   *\n"
            + "   * @param e the element to use as a position hint\n"
            + "   * @param msg the message, or an empty string if none\n"
            + "   * @param args {@code String#format} arguments\n"
            + "   */\n"
            + "  public static void logError(Element e, String msg, Object... args) {\n"
            + "    messager().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Prints an error.\n"
            + "   *\n"
            + "   * @param msg the message, or an empty string if none\n"
            + "   * @param args {@code String#format} arguments\n"
            + "   */\n"
            + "  public static void logError(String msg, Object... args) {\n"
            + "    messager().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Prints an warning at the location of the element.\n"
            + "   *\n"
            + "   * @param e the element to use as a position hint\n"
            + "   * @param msg the message, or an empty string if none\n"
            + "   * @param args {@code String#format} arguments\n"
            + "   */\n"
            + "  public static void logWarn(Element e, String msg, Object... args) {\n"
            + "    messager().printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Prints a warning.\n"
            + "   *\n"
            + "   * @param msg the message, or an empty string if none\n"
            + "   * @param args {@code String#format} arguments\n"
            + "   */\n"
            + "  public static void logWarn(String msg, Object... args) {\n"
            + "    messager().printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Prints a note.\n"
            + "   *\n"
            + "   * @param msg the message, or an empty string if none\n"
            + "   * @param args {@code String#format} arguments\n"
            + "   */\n"
            + "  public static void logNote(Element e, String msg, Object... args) {\n"
            + "    messager().printMessage(Diagnostic.Kind.NOTE, String.format(msg, args), e);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Prints a note at the location of the element.\n"
            + "   *\n"
            + "   * @param e the element to use as a position hint\n"
            + "   * @param msg the message, or an empty string if none\n"
            + "   * @param args {@code String#format} arguments\n"
            + "   */\n"
            + "  public static void logNote(String msg, Object... args) {\n"
            + "    messager().printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Returns the elements annotated with the given annotation interface.\n"
            + "   *\n"
            + "   * @param round RoundEnviroment to extract the elements\n"
            + "   * @param annotationFQN the fqn of the annotation\n"
            + "   * @return the elements annotated with the given annotation interface,or an empty set if there are\n"
            + "   *     none\n"
            + "   */\n"
            + "  public static Set<? extends Element> elementsAnnotatedWith(\n"
            + "      RoundEnvironment round, String annotationFQN) {\n"
            + "\n"
            + "    return Optional.ofNullable(typeElement(annotationFQN))\n"
            + "        .map(round::getElementsAnnotatedWith)\n"
            + "        .orElse(Set.of());\n"
            + "  }\n\n"
            + "  /**\n"
            + "   * Create a file writer for the given class name.\n"
            + "   *\n"
            + "   * @param name canonical (fully qualified) name of the principal class or interface being declared\n"
            + "   *     in this file or a package name followed by {@code \".package-info\"} for a package\n"
            + "   *     information file\n"
            + "   * @param originatingElements class, interface, package, or module elements causally associated\n"
            + "   *     with the creation of this file, may be elided or {@code null}\n"
            + "   * @return a JavaFileObject to write the new source file\n"
            + "   */\n"
            + "  public static JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)\n"
            + "      throws IOException {\n"
            + "    return filer().createSourceFile(name, originatingElements);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Returns a type element given its canonical name.\n"
            + "   *\n"
            + "   * @param name the canonical name\n"
            + "   * @return the named type element, or null if no type element can be uniquely determined\n"
            + "   */\n"
            + "  public static TypeElement typeElement(String name) {\n"
            + "    return elements().getTypeElement(name);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Returns the element corresponding to a type.The type may be a DeclaredType or\n"
            + "   * TypeVariable.Returns null if the type is not one with a corresponding element.\n"
            + "   *\n"
            + "   * @param t the type to map to an element\n"
            + "   * @return the element corresponding to the given type\n"
            + "   */\n"
            + "  public static TypeElement asTypeElement(TypeMirror t) {\n"
            + "\n"
            + "    return (TypeElement) types().asElement(t);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Get current {@link ProcessingEnvironment}\n"
            + "   *\n"
            + "   * @return the enviroment\n"
            + "   */\n"
            + "  public static ProcessingEnvironment processingEnv() {\n"
            + "    return getCtx().processingEnv;\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Get current {@link Filer} from the {@link ProcessingEnvironment}\n"
            + "   *\n"
            + "   * @return the filer\n"
            + "   */\n"
            + "  public static Filer filer() {\n"
            + "    return getCtx().filer;\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Get current {@link Elements} from the {@link ProcessingEnvironment}\n"
            + "   *\n"
            + "   * @return the filer\n"
            + "   */\n"
            + "  public static Elements elements() {\n"
            + "    return getCtx().elementUtils;\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Get current {@link Messager} from the {@link ProcessingEnvironment}\n"
            + "   *\n"
            + "   * @return the messager\n"
            + "   */\n"
            + "  public static Messager messager() {\n"
            + "    return getCtx().messager;\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Get current {@link Types} from the {@link ProcessingEnvironment}\n"
            + "   *\n"
            + "   * @return the types\n"
            + "   */\n"
            + "  public static Types types() {\n"
            + "    return getCtx().typeUtils;\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Determine whether the first type can be assigned to the second\n"
            + "   *\n"
            + "   * @param type string type to check\n"
            + "   * @param superType the type that should be assignable to.\n"
            + "   * @return true if type can be assinged to supertype\n"
            + "   */\n"
            + "  public static boolean isAssignable(String type, String superType) {\n"
            + "    return type.equals(superType) || isAssignable(typeElement(type), superType);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Determine whether the first type can be assigned to the second\n"
            + "   *\n"
            + "   * @param type type to check\n"
            + "   * @param superType the type that should be assignable to.\n"
            + "   * @return true if type can be assinged to supertype\n"
            + "   */\n"
            + "  public static boolean isAssignable(TypeElement type, String superType) {\n"
            + "    return Optional.ofNullable(type).stream()\n"
            + "        .flatMap(APContext::superTypes)\n"
            + "        .anyMatch(superType::equals);\n"
            + "  }\n"
            + "\n"
            + "  private static Stream<String> superTypes(TypeElement element) {\n"
            + "    final var types = types();\n"
            + "    return types.directSupertypes(element.asType()).stream()\n"
            + "        .filter(type -> !type.toString().contains(\"java.lang.Object\"))\n"
            + "        .map(superType -> (TypeElement) types.asElement(superType))\n"
            + "        .flatMap(e -> Stream.concat(superTypes(e), Stream.of(e)))\n"
            + "        .map(Object::toString);\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Discover the {@link ModuleElement} for the project being processed and set in the context.\n"
            + "   *\n"
            + "   * @param annotations the annotation interfaces requested to be processed\n"
            + "   * @param roundEnv environment for information about the current and prior round\n"
            + "   */\n"
            + "  public static void setProjectModuleElement(\n"
            + "      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {\n"
            + "    if (getCtx().module == null) {\n"
            + "      getCtx().module =\n"
            + "          annotations.stream()\n"
            + "              .map(roundEnv::getElementsAnnotatedWith)\n"
            + "              .filter(not(Collection::isEmpty))\n"
            + "              .findAny()\n"
            + "              .map(s -> s.iterator().next())\n"
            + "              .map(elements()::getModuleOf)\n"
            + "              .orElse(null);\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  /**\n"
            + "   * Retrieve the project's {@link ModuleElement}. {@code setProjectModuleElement} must be called\n"
            + "   * before this.\n"
            + "   *\n"
            + "   * @return the {@link ModuleElement} associated with the current project\n"
            + "   */\n"
            + "  public static ModuleElement getProjectModuleElement() {\n"
            + "    return getCtx().module;\n"
            + "  }\n"
            + "\n"
            + (moduleReader
                ? "  /** Retrieve the root module-info reader if it can be read */\n"
                    + "  public static Optional<ModuleInfoReader> moduleInfoReader() {\n"
                    + "    if (getCtx().moduleReader == null) {\n"
                    + "      try {\n"
                    + "        getCtx().moduleReader = new ModuleInfoReader();\n"
                    + "      } catch (Exception e) {\n"
                    + "        // could not retrieve\n"
                    + "      }\n"
                    + "    }\n"
                    + "    return Optional.ofNullable(getCtx().moduleReader);\n"
                    + "  }\n\n"
                : "")
            + "  /**\n"
            + "   * Gets a {@link BufferedReader} for the project's {@code module-info.java} source file.\n"
            + "   *\n"
            + "   * <p>Calling {@link ModuleElement}'s {@code getDirectives()} method has a chance of making\n"
            + "   * compilation fail in certain situations. Therefore, manually parsing {@code module-info.java}\n"
            + "   * seems to be the safest way to get module information.\n"
            + "   *\n"
            + "   * @return\n"
            + "   * @throws IOException if unable to read the module-info\n"
            + "   */\n"
            + " public static BufferedReader getModuleInfoReader() throws IOException {\n"
            + "\n"
            + "    var modulePath = isTestCompilation() ? \"src/main/test\" : \"src/main/java\";\n"
            + "    // some JVM implementations do not implement SOURCE_PATH so gotta find the module path by trying\n"
            + "    // to find the src folder\n"
            + "    var path = Path.of(filer().createResource(StandardLocation.CLASS_OUTPUT, \"\", UUID.randomUUID().toString()).toUri());\n"
            + "    var i = 0;\n"
            + "    while (i < 5 && path != null && !path.resolve(modulePath).toFile().exists()) {\n"
            + "      i++;\n"
            + "      path = path.getParent();\n"
            + "    }\n"
            + "\n"
            + "    var moduleFile = path.resolve(modulePath + \"/module-info.java\");\n"
            + "    if (moduleFile.toFile().exists()) {\n"
            + "      return new BufferedReader(new InputStreamReader(moduleFile.toUri().toURL().openStream()));\n"
            + "    }\n"
            + "\n"
            + "    // if that fails try via SOURCE_PATH\n"
            + "    var sourcePath =\n"
            + "        Path.of(filer().getResource(StandardLocation.SOURCE_PATH, \"\", \"module-info.java\").toUri());\n"
            + "\n"
            + "    return new BufferedReader(new InputStreamReader(sourcePath.toUri().toURL().openStream()));\n"
            + "  }\n\n"
            + "  /**\n"
            + "   * Given the relative path, gets a {@link Path} from the Maven {@code target}/Gradle {@code build} folder.\n"
            + "   * @param path the relative path of the file in the target/build folder\n"
            + "   *\n"
            + "   * @return the file object\n"
            + "   * @throws IOException if unable to retrieve the file\n"
            + "   */\n"
            + "  public static Path getBuildResource(String path) throws IOException {\n"
            + "\n"
            + "    var id = UUID.randomUUID().toString();\n"
            + "    final var uri =\n"
            + "        filer()\n"
            + "            .createResource(StandardLocation.CLASS_OUTPUT, \"\", path + id)\n"
            + "            .toUri()\n"
            + "            .toString()\n"
            + "            .replaceFirst(id, \"\")\n"
            + "            .replaceFirst(\"/classes/java/main\", \"\")\n"
            + "            .replaceFirst(\"/classes\", \"\");\n"
            + "    var updatedPath = Path.of(URI.create(uri));\n"
            + "    if (path.contains(\"/\")) {\n"
            + "      updatedPath.getParent().toFile().mkdirs();\n"
            + "    }\n"
            + "    return updatedPath;\n"
            + "  }\n  /**\n"
            + "   * Return true if the compiler is creating test classes.\n"
            + "   *\n"
            + "   * @return Whether the current apt compilation is for test-compile.\n"
            + "   */\n"
            + "  public static boolean isTestCompilation() {\n"
            + "    return getCtx().isTestCompilation;\n"
            + "  }\n"
            + "}\n");
  }
}
