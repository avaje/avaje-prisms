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

/*
 * PrismGenerator.java
 *
 * Created on 27 June 2006, 22:07
 */

package io.avaje.prism.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * An AnnotationProcessor for generating prisms. Do not use this class directly.
 *
 * @author Bruce
 */
// @GeneratePrisms({
//    @GeneratePrism(GeneratePrisms.class),
//    @GeneratePrism(GeneratePrism.class)
// })
@SupportedAnnotationTypes({"io.avaje.prism.GeneratePrism", "io.avaje.prism.GeneratePrisms"})
public final class PrismGenerator extends AbstractProcessor {

  private final Map<String, TypeMirror> generated = new HashMap<>();
  private final Deque<DeclaredType> inners = new ArrayDeque<>();
  private final Set<DeclaredType> seenInners = new HashSet<>();

  private Elements elements;
  private Types types;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    this.elements = env.getElementUtils();
    this.types = env.getTypeUtils();
    ProcessingContext.init(env);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
    if (renv.processingOver()) {
      return true;
    }

    final TypeElement a = elements.getTypeElement("io.avaje.prism.GeneratePrism");
    final TypeElement as = elements.getTypeElement("io.avaje.prism.GeneratePrisms");

    for (final Element e : renv.getElementsAnnotatedWith(a)) {
      final GeneratePrismPrism ann = GeneratePrismPrism.getInstanceOn(e);
      if (ann.isValid) {
        generateIfNew(ann, e, Map.of());
      }
    }
    for (final Element e : renv.getElementsAnnotatedWith(as)) {
      final GeneratePrismsPrism ann = GeneratePrismsPrism.getInstanceOn(e);
      if (ann.isValid) {
        final Map<DeclaredType, String> otherPrisms = new HashMap<>();
        for (final GeneratePrismPrism inner : ann.value()) {
          getPrismName(inner);
          otherPrisms.put((DeclaredType) inner.value(), getPrismName(inner));
        }
        for (final GeneratePrismPrism inner : ann.value()) {
          generateIfNew(inner, e, otherPrisms);
        }
      }
    }
    return false;
  }

  private String getPrismName(GeneratePrismPrism ann) {
    String name = ann.name();
    if ("".equals(name)) {
      name = ((DeclaredType) ann.value()).asElement().getSimpleName() + "Prism";
    }
    return name;
  }

  private void generateIfNew(
      GeneratePrismPrism ann, Element e, Map<DeclaredType, String> otherPrisms) {
    final String name = getPrismName(ann);
    String packageName = getPackageName(e);
    // workaround for bug that has been fixed in a later build
    if ("unnamed package".equals(packageName)) {
      packageName = "";
    }
    final String prismFqn = "".equals(packageName) ? name : packageName + "." + name;
    if (generated.containsKey(prismFqn)) {
      // if same value dont need to generate, if different then error
      if (generated.get(prismFqn).equals(ann.value())) {
        return;
      }
      processingEnv
          .getMessager()
          .printMessage(
              Diagnostic.Kind.ERROR,
              String.format(
                  "%s has already been generated for %s", prismFqn, generated.get(prismFqn)),
              e,
              ann.mirror);
      return;
    }
    generatePrism(
        name,
        packageName,
        (DeclaredType) ann.value(),
        ann.publicAccess() ? "public " : "",
        otherPrisms);
    generated.put(prismFqn, ann.value());
  }

  private String getPackageName(Element e) {
    while (e.getKind() != ElementKind.PACKAGE) {
      e = e.getEnclosingElement();
    }
    return ((PackageElement) e).getQualifiedName().toString();
  }

  private void generatePrism(
      String name,
      String packageName,
      DeclaredType typeMirror,
      String access,
      Map<DeclaredType, String> otherPrisms) {
    inners.clear();
    seenInners.clear();
    final String prismFqn = "".equals(packageName) ? name : packageName + "." + name;
    PrintWriter out = null;
    try {
      out = new PrintWriter(processingEnv.getFiler().createSourceFile(prismFqn).openWriter());
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    }
    try {

      if (!"".equals(packageName)) {
        out.format("package %s;%n%n", packageName);
      }
      final var isMeta = Util.isMeta(typeMirror);
      if (Util.isRepeatable(typeMirror) || isMeta) {
        out.format("import static java.util.stream.Collectors.*;%n");
        out.format("import java.util.stream.Stream;%n");
      }

      if (isMeta) {
        out.format("import javax.lang.model.type.DeclaredType;%n");
        out.format("import java.util.Set;%n");
        out.format("import java.util.HashSet;%n");
      }
      out.format("import java.util.ArrayList;%n");
      out.format("import java.util.List;%n");
      out.format("import java.util.Optional;%n");
      out.format("import java.util.Map;%n");
      out.format("import javax.lang.model.element.AnnotationMirror;%n");
      out.format("import javax.lang.model.element.Element;%n");
      out.format("import javax.lang.model.element.VariableElement;%n");
      out.format("import javax.lang.model.element.AnnotationValue;%n");
      out.format("import javax.lang.model.type.TypeMirror;%n");

      out.format("import java.util.HashMap;%n");
      out.format("import javax.lang.model.element.ExecutableElement;%n");
      out.format("import javax.lang.model.element.TypeElement;%n");
      out.format("import javax.lang.model.util.ElementFilter;%n%n");

      final String annName = ((TypeElement) typeMirror.asElement()).getQualifiedName().toString();
      out.format("/** A Prism representing an {@code @%s} annotation. %n", annName);
      out.format("  */ %n");
      out.format("%sclass %s {%n", access, name);

      // SHOULD make public only if the anotation says so, package by default.
      generateClassBody(new GenerateContext("", out, name, name, typeMirror, access), otherPrisms);
      while (inners.peek() != null) {
        final DeclaredType next = inners.remove();
        final String innerName = next.asElement().getSimpleName().toString() + "Prism";
        ((TypeElement) typeMirror.asElement()).getQualifiedName().toString();
        out.format("    %sstatic class %s {%n", access, innerName);
        generateClassBody(
            new GenerateContext("    ", out, name, innerName, next, access), otherPrisms);
        out.format("    }%n");
      }
      generateStaticMembers(out, Util.isMeta(typeMirror) || Util.isRepeatable(typeMirror));
      out.format("}%n");
    } finally {
      out.close();
    }
    processingEnv
        .getMessager()
        .printMessage(
            Diagnostic.Kind.NOTE,
            String.format("Generated prism %s for @%s", prismFqn, typeMirror));
  }

  private void generateClassBody(final GenerateContext ctx, Map<DeclaredType, String> otherPrisms) {

    final String indent = ctx.indent();
    final PrintWriter out = ctx.out();
    final String name = ctx.name();
    final String outerName = ctx.outerName();
    final DeclaredType typeMirror = ctx.typeMirror();
    final String access = ctx.access();

    final List<PrismWriter> writers = new ArrayList<>();
    for (final ExecutableElement m :
        ElementFilter.methodsIn(typeMirror.asElement().getEnclosedElements())) {
      writers.add(getWriter(m, access, otherPrisms));
    }
    for (final PrismWriter w : writers) {
      w.writeField(indent, out);
    }

    final String annName = ((TypeElement) typeMirror.asElement()).getQualifiedName().toString();

    ctx.setAnnName(annName);
    out.format(
        "%s    public static final String PRISM_TYPE = \"%s\";%n%n",
        indent, ((TypeElement) (typeMirror.asElement())).getQualifiedName());
    out.format("%s    /**%n", indent);
    out.format("%s      * An instance of the Values inner class whose%n", indent);
    out.format(
        "%s      * methods return the AnnotationValues used to build this prism. %n", indent);
    out.format("%s      * Primarily intended to support using Messager.%n", indent);
    out.format("%s      */%n", indent);

    out.format("%s    %sfinal Values values;\n", indent, access);
    final var inner = !"".equals(indent);

    // write factory methods

    new FactoryMethodWriter(ctx, inner).write();

    // write constructor
    out.format("%s    private %s(AnnotationMirror mirror) {%n", indent, name);
    out.print(
        "        for(ExecutableElement key : mirror.getElementValues().keySet()) {\n"
            + "            memberValues.put(key.getSimpleName().toString(), mirror.getElementValues().get(key));\n"
            + "        }\n"
            + "        for(ExecutableElement member : ElementFilter.methodsIn(mirror.getAnnotationType().asElement().getEnclosedElements())) {\n"
            + "            defaults.put(member.getSimpleName().toString(), member.getDefaultValue());\n"
            + "        }\n");
    for (final PrismWriter w : writers) {
      w.writeInitializer(indent, out);
    }
    out.format("%s        this.values = new Values(memberValues);%n", indent);
    out.format("%s        this.mirror = mirror;%n", indent);
    out.format("%s        this.isValid = valid;%n", indent);
    out.format("%s    }%n%n", indent);

    // write methods
    for (final PrismWriter w : writers) {
      w.writeMethod(indent, out);
    }

    // write isValid and getMirror methods
    out.format("%s    /**%n", indent);
    out.format(
        "%s      * Determine whether the underlying AnnotationMirror has no errors.%n", indent);
    out.format("%s      * True if the underlying AnnotationMirror has no errors.%n", indent);
    out.format("%s      * When true is returned, none of the methods will return null.%n", indent);
    out.format(
        "%s      * When false is returned, a least one member will either return null, or another%n",
        indent);
    out.format("%s      * prism that is not valid.%n", indent);
    out.format("%s      */%n", indent);
    out.format("%s    %sfinal boolean isValid;%n", indent, access);
    out.format("%s    %n", indent);
    out.format("%s    /**%n", indent);
    out.format("%s      * The underlying AnnotationMirror of the annotation%n", indent);
    out.format("%s      * represented by this Prism. %n", indent);
    out.format("%s      * Primarily intended to support using Messager.%n", indent);
    out.format("%s      */%n", indent);
    out.format("%s    %sfinal AnnotationMirror mirror;%n", indent, access);

    // write Value class
    out.format("%s    /**%n", indent);
    out.format("%s      * A class whose members corespond to those of %s%n", indent, annName);
    out.format("%s      * but which each return the AnnotationValue corresponding to%n", indent);
    out.format("%s      * that member in the model of the annotations. Returns null for%n", indent);
    out.format(
        "%s      * defaulted members. Used for Messager, so default values are not useful.%n",
        indent);
    out.format("%s      */%n", indent);
    out.format("%s    %sstatic class Values {%n", indent, access);
    out.format("%s       private Map<String, AnnotationValue> values;%n", indent);
    out.format("%s       private Values(Map<String, AnnotationValue> values) {%n", indent);
    out.format("%s           this.values = values;%n", indent);
    out.format("%s       }    %n", indent);
    for (final PrismWriter w : writers) {
      out.format(
          "%s       /** Return the AnnotationValue corresponding to the %s() %n", indent, w.name);
      out.format(
          "%s         * member of the annotation, or null when the default value is implied.%n",
          indent);
      out.format("%s         */%n", indent);
      out.format(
          "%s       %sAnnotationValue %s(){ return values.get(\"%s\");}%n",
          indent, access, w.name, w.name);
    }
    out.format("%s    }%n", indent);
    generateFixedClassContent(indent, out, outerName);
  }

  private PrismWriter getWriter(
      ExecutableElement m, String access, Map<DeclaredType, String> otherPrisms) {

    final WildcardType q = types.getWildcardType(null, null);
    final TypeMirror enumType = types.getDeclaredType(elements.getTypeElement("java.lang.Enum"), q);
    TypeMirror typem = m.getReturnType();
    PrismWriter result = null;
    if (typem.getKind() == TypeKind.ARRAY) {
      typem = ((ArrayType) typem).getComponentType();
      result = new PrismWriter(m, true, access);
    } else {
      result = new PrismWriter(m, false, access);
    }
    if (typem.getKind().isPrimitive()) {
      final String typeName = types.boxedClass((PrimitiveType) typem).getSimpleName().toString();
      result.setMirrorType(typeName);
      result.setPrismType(typeName);
    } else if (typem.getKind() == TypeKind.DECLARED) {
      final DeclaredType type = (DeclaredType) typem;
      // String, enum, annotation, or Class<?>
      if (types.isSameType(type, elements.getTypeElement("java.lang.String").asType())) {
        // String
        result.setMirrorType("String");
        result.setPrismType("String");
      } else if (type.asElement().equals(elements.getTypeElement("java.lang.Class"))) {
        // class<? ...>
        result.setMirrorType("TypeMirror");
        result.setPrismType("TypeMirror");
      } else if (types.isSubtype(type, enumType)) {
        // Enum
        result.setMirrorType("VariableElement");
        result.setPrismType("String");
        result.setM2pFormat("%s.getSimpleName().toString()");
      } else if (types.isSubtype(
          type, elements.getTypeElement("java.lang.annotation.Annotation").asType())) {
        result.setMirrorType("AnnotationMirror");
        final DeclaredType annType = type;
        String prismName = null;
        for (final Entry<DeclaredType, String> entry : otherPrisms.entrySet()) {
          if (types.isSameType(entry.getKey(), annType)) {
            prismName = entry.getValue();
            break;
          }
        }
        if (prismName != null) {
          result.setPrismType(prismName);
          result.setM2pFormat(prismName + ".getInstance(%s)");
        } else {
          // generate its prism as inner class
          final String prismType = annType.asElement().getSimpleName().toString() + "Prism";
          result.setPrismType(prismType);
          result.setM2pFormat(prismType + ".getInstance(%s)");
          // force generation of inner prism class for annotation
          if (seenInners.add(type)) {
            inners.add(type);
          }
        }
      } else {
        System.out.format("Unprocessed type %s", type);
      }
    }
    return result;
  }

  private void generateStaticMembers(PrintWriter out, boolean generateGetMirrors) {
    out.print(
        "    private static AnnotationMirror getMirror(Element target) {\n"
            + "        for (AnnotationMirror m : target.getAnnotationMirrors()) {\n"
            + "            CharSequence mfqn = ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();\n"
            + "            if(PRISM_TYPE.contentEquals(mfqn)) return m;\n"
            + "        }\n"
            + "        return null;\n"
            + "    }\n");
    if (generateGetMirrors)
      out.print(
          "    private static Stream<? extends AnnotationMirror> getMirrors(Element target) {\n"
              + "        return target.getAnnotationMirrors().stream()\n"
              + "            .filter(\n"
              + "                 m -> PRISM_TYPE.contentEquals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName()));\n"
              + "    }\n");
    out.print( "    private static <T> T getValue(Map<String, AnnotationValue> memberValues, Map<String, AnnotationValue> defaults, String name, Class<T> clazz) {\n"
            + "        AnnotationValue av = memberValues.get(name);\n"
            + "        if(av == null) av = defaults.get(name);\n"
            + "        if(av == null) {\n"
            + "            return null;\n"
            + "        }\n"
            + "        if(clazz.isInstance(av.getValue())) return clazz.cast(av.getValue());\n"
            + "        return null;\n"
            + "    }\n"
            + "    private static <T> List<T> getArrayValues(Map<String, AnnotationValue> memberValues, Map<String, AnnotationValue> defaults, String name, final Class<T> clazz) {\n"
            + "        AnnotationValue av = memberValues.get(name);\n"
            + "        if(av == null) av = defaults.get(name);\n"
            + "        if(av == null) {\n"
            + "            return java.util.List.of();\n"
            + "        }\n"
            + "        if(av.getValue() instanceof List) {\n"
            + "            List<T> result = new ArrayList<T>();\n"
            + "            for(AnnotationValue v : getValueAsList(av)) {\n"
            + "                if(clazz.isInstance(v.getValue())) {\n"
            + "                    result.add(clazz.cast(v.getValue()));\n"
            + "                } else{\n"
            + "                    return List.of();\n"
            + "                }\n"
            + "            }\n"
            + "            return result;\n"
            + "        } else {\n"
            + "            return List.of();\n"
            + "        }\n"
            + "    }\n"
            + "    @SuppressWarnings(\"unchecked\")\n"
            + "    private static List<AnnotationValue> getValueAsList(AnnotationValue av) {\n"
            + "        return (List<AnnotationValue>)av.getValue();\n"
            + "    }\n");
  }

  private void generateFixedClassContent(String indent, PrintWriter out, String outerName) {
    out.format(
        "%s    private Map<String, AnnotationValue> defaults = new HashMap<String, AnnotationValue>(10);%n",
        indent);
    out.format(
        "%s    private Map<String, AnnotationValue> memberValues = new HashMap<String, AnnotationValue>(10);%n",
        indent);
    out.format("%s    private boolean valid = true;%n", indent);
    out.format("%n");
    out.format("%s    private <T> T getValue(String name, Class<T> clazz) {%n", indent);
    out.format(
        "%s        T result = %s.getValue(memberValues, defaults, name, clazz);%n",
        indent, outerName);
    out.format("%s        if(result == null) valid = false;%n", indent);
    out.format("%s        return result;%n", indent);
    out.format("%s    } %n", indent);
    out.format("%n");
    out.format(
        "%s    private <T> List<T> getArrayValues(String name, final Class<T> clazz) {%n", indent);
    out.format(
        "%s        List<T> result = %s.getArrayValues(memberValues, defaults, name, clazz);%n",
        indent, outerName);
    out.format("%s        if(result == null) valid = false;%n", indent);
    out.format("%s        return result;%n", indent);
    out.format("%s    }%n", indent);
  }
}
