package io.avaje.prism.internal;

import java.io.PrintWriter;

import javax.lang.model.type.DeclaredType;

public class FactoryMethodWriter {
  private final String indent;
  private final PrintWriter out;
  private final String name;
  private final DeclaredType typeMirror;
  private final String access;
  private final String annName;
  private final boolean inner;
  private final boolean repeatable;
  private final boolean meta;
  private final String prismShortName;
  private final String annShortName;

  FactoryMethodWriter(GenerateContext ctx, boolean inner) {
    this.indent = ctx.indent();
    this.out = ctx.out();
    this.name = ctx.name();
    this.prismShortName = Util.shortName(ctx.name());
    this.typeMirror = ctx.typeMirror();
    this.access = ctx.access();
    this.annName = ctx.annName();
    this.annShortName = ctx.getShortName();
    this.inner = inner;
    this.repeatable = Util.isRepeatable(typeMirror);
    this.meta = Util.isMeta(typeMirror);
  }

  void write() {
    if (!inner) {
      writeIsInstance();
      writeIsPresent();
      writeGetInstanceOn();
      writeGetOptionalOn();

      if (repeatable) {
        writeGetAllInstances();
      }
      if (meta) {
        writeGetAllOnMeta();
      }
    }
    writeGetInstance();
    writeGetOptional();
  }

  private void writeIsInstance() {
	out.format("%s  /** Returns true if the mirror is an instance of {@link %s @%s} is present on the element, else false.\n", indent, annName, annShortName);
    out.format("%s   *\n", indent);
    out.format("%s   * @param mirror mirror. \n", indent);
    out.format("%s   * @return true if prism is present. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic boolean isInstance(AnnotationMirror mirror) {\n", indent, access);
    out.format("%s    return getInstance(mirror) != null;\n", indent);
    out.format("%s  }\n\n", indent);
  }

  private void writeIsPresent() {
    // Is Present
    out.format("%s  /** Returns true if {@link %s @%s} is present on the element, else false.\n", indent, annName, annShortName);
    out.format("%s   *\n", indent);
    out.format("%s   * @param element element. \n", indent);
	out.format("%s   * @return true if annotation is present on the element. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic boolean isPresent(Element element) {\n", indent, access);
    out.format("%s    return getInstanceOn(element) != null;\n", indent);
    out.format("%s  }\n\n", indent);
  }

  private void writeGetInstanceOn() {
    // get single instance
    out.format("%s  /** Return a prism representing the {@link %s @%s} annotation present on the given element. \n", indent, annName, annShortName);
    out.format("%s   * similar to {@code element.getAnnotation(%s.class)} except that \n", indent, annShortName);
    out.format("%s   * an instance of this class rather than an instance of {@link %s @%s}\n", indent, annName, annShortName);
	out.format("%s   * is returned.\n", indent);
	out.format("%s   *\n", indent);
    out.format("%s   * @param element element. \n", indent);
	out.format("%s   * @return prism on element or null if no annotation is found. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic %s getInstanceOn(Element element) {\n", indent, access, name);
    out.format("%s    final var mirror = getMirror(element);\n", indent);
    out.format("%s    if (mirror == null) return null;\n", indent);
    out.format("%s    return getInstance(mirror);\n", indent);
    out.format("%s  }\n\n", indent);
  }

  void writeGetOptionalOn() {

    // getOptionalOn
    out.format("%s  /** Return a Optional representing a nullable {@link %s @%s} annotation on the given element. \n", indent, annName, annShortName, annName, annShortName);
    out.format("%s   * similar to {@link element.getAnnotation(%s.class)} except that \n", indent, annName);
    out.format("%s   * an Optional of this class rather than an instance of {@link %s}\n", indent, annName);
    out.format("%s   * is returned.\n", indent);
	out.format("%s   *\n", indent);
	out.format("%s   * @param element element. \n", indent);
	out.format("%s   * @return prism optional for element. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic Optional<%s> getOptionalOn(Element element) {\n", indent, access, name);
    out.format("%s    final var mirror = getMirror(element);\n", indent);
    out.format("%s    if (mirror == null) return Optional.empty();\n", indent);
    out.format("%s    return getOptional(mirror);\n", indent);
    out.format("%s  }\n\n", indent);
  }

  private void writeGetAllOnMeta() {

    out.format("%s  /** Return a list of prisms representing the {@link %s @%s} meta annotation on all the annotations on the given element. \n", indent, annName, annShortName);
    out.format("%s   * this method will recursively search all the annotations on the element. \n", indent);
	out.format("%s   *\n", indent);
	out.format("%s   * @param element element. \n", indent);
	out.format("%s   * @return list of prisms on the element's annotation. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic List<%s> getAllOnMetaAnnotations(Element element) {\n",indent, access, name);
    out.format("%s    if (element == null || element.getAnnotationMirrors().isEmpty()) return List.of();\n\n",indent);
    out.format("%s    //use a hashset to keep track of seen annotations \n", indent);
    out.format("%s    return getAllOnMetaAnnotations(element, new HashSet<>()).collect(toList());\n", indent);
    out.format("%s  }\n\n", indent);


    out.format("%s  /** Recursively search annotation elements for prisms.\n", indent);
    out.format("%s   * Uses a set to keep track of known annotations to avoid repeats/recursive loop. \n", indent);
	out.format("%s   *\n", indent);
    out.format("%s   * @param element element. \n", indent);
    out.format("%s   * @param seen set that tracks seen elements. \n", indent);
	out.format("%s   * @return stream of prisms on the element's annotation. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  private static Stream<%s> getAllOnMetaAnnotations(Element element, Set<String> seen) {\n",indent, name);
    out.format("%s    if (element == null || element.getAnnotationMirrors().isEmpty()) return Stream.of();\n\n",indent);
    out.format("%s    return element.getAnnotationMirrors().stream()\n", indent);
    out.format("%s      .map(AnnotationMirror::getAnnotationType)\n", indent);
    out.format("%s      //only search annotations \n", indent);
    out.format("%s      .filter(t -> seen.add(t.toString()))\n", indent);
    out.format("%s        .map(DeclaredType::asElement)\n", indent);
    out.format("%s        .flatMap(\n", indent);
    out.format("%s            e ->\n", indent);
    out.format("%s                Stream.concat(\n", indent);
    out.format("%s                    getAllOnMetaAnnotations(e, seen),\n", indent);
    out.format("%s                    getMirrors(element).map(%s::getInstance)));\n", indent, name);
    out.format("%s  }\n\n", indent);
  }

  private void writeGetAllInstances() {
    // get multiple instances
    out.format("%s  /** Return a list of prisms representing the {@link %s @%s} annotation on 'e'. \n",indent, annName, annShortName);
    out.format("%s   * similar to {@code e.getAnnotationsByType(%s.class)} except that \n", indent, annName);
    out.format("%s   * instances of this class rather than instances of {@link %s}\n", indent, annName);
    out.format("%s   * is returned.\n", indent);
	out.format("%s   *\n", indent);
    out.format("%s   * @param element element. \n", indent);
	out.format("%s   * @return list of prisms on the element. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic List<%s> getAllInstancesOn(Element element) {\n", indent, access, name);
    out.format("%s    return getMirrors(element)\n", indent);
    out.format("%s        .map(%s::getInstance)\n", indent, name);
    out.format("%s        .collect(toList());\n", indent);
    out.format("%s  }\n\n", indent);
  }

  private void writeGetInstance() {
    out.format("%s  /** Return a prism of the {@link %s @%s} annotation from an annotation mirror. \n", indent, annName, annShortName);
	out.format("%s   *\n", indent);
    out.format("%s   * @param mirror mirror. \n", indent);
  	out.format("%s   * @return prism for mirror or null if mirror is an incorrect type. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic %s getInstance(AnnotationMirror mirror) {\n", indent, inner ? "private " : access, name);
    out.format("%s    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return null;\n\n", indent);
    out.format("%s    return new %s(mirror);\n", indent, name);
    out.format("%s  }\n\n", indent);
  }

  private void writeGetOptional() {

    // getOptional
    out.format("%s  /** Return an Optional representing a nullable {@link %s @%s} from an annotation mirror. \n", indent, name, prismShortName, annName, annShortName);
    out.format("%s   * similar to {@link e.getAnnotation(%s.class)} except that \n", indent, annName);
    out.format("%s   * an Optional of this class rather than an instance of {@link %s @%s}\n", indent, annName, annShortName);
    out.format("%s   * is returned.\n", indent);
	out.format("%s   *\n", indent);
    out.format("%s   * @param mirror mirror. \n", indent);
	out.format("%s   * @return prism optional for mirror. \n", indent);
    out.format("%s   */\n", indent);
    out.format("%s  %sstatic Optional<%s> getOptional(AnnotationMirror mirror) {\n", indent, inner ? "private " : access, name);
    out.format("%s    if (mirror == null || !PRISM_TYPE.equals(mirror.getAnnotationType().toString())) return Optional.empty();\n\n", indent);
    out.format("%s    return Optional.of(new %s(mirror));\n", indent, name);
    out.format("%s  }\n\n", indent);
  }
}
