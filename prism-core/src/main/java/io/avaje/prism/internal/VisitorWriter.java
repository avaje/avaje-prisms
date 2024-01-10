package io.avaje.prism.internal;

import java.io.PrintWriter;

public class VisitorWriter {
  private VisitorWriter() {}

  public static void write(PrintWriter out, String packageName) {

    out.append(
        "package "
            + packageName
            + ";\n"
            + "\n"
            + "import static java.util.stream.Collectors.toSet;\n"
            + "\n"
            + "import java.util.ArrayList;\n"
            + "import java.util.HashMap;\n"
            + "import java.util.HashSet;\n"
            + "import java.util.LinkedHashMap;\n"
            + "import java.util.List;\n"
            + "import java.util.Locale;\n"
            + "import java.util.Map;\n"
            + "import java.util.Set;\n"
            + "\n"
            + "import javax.annotation.processing.Generated;\n"
            + "import javax.lang.model.element.AnnotationMirror;\n"
            + "import javax.lang.model.element.Element;\n"
            + "import javax.lang.model.element.QualifiedNameable;\n"
            + "import javax.lang.model.element.TypeElement;\n"
            + "import javax.lang.model.type.ArrayType;\n"
            + "import javax.lang.model.type.DeclaredType;\n"
            + "import javax.lang.model.type.ErrorType;\n"
            + "import javax.lang.model.type.ExecutableType;\n"
            + "import javax.lang.model.type.IntersectionType;\n"
            + "import javax.lang.model.type.NoType;\n"
            + "import javax.lang.model.type.NullType;\n"
            + "import javax.lang.model.type.PrimitiveType;\n"
            + "import javax.lang.model.type.TypeKind;\n"
            + "import javax.lang.model.type.TypeMirror;\n"
            + "import javax.lang.model.type.TypeVariable;\n"
            + "import javax.lang.model.type.UnionType;\n"
            + "import javax.lang.model.type.WildcardType;\n"
            + "import javax.lang.model.util.AbstractTypeVisitor9;\n"
            + "\n"
            + "@Generated(\"avaje-prism-generator\")\n"
            + "class TypeMirrorVisitor extends AbstractTypeVisitor9<StringBuilder, StringBuilder>\n"
            + "    implements UType {\n"
            + "\n"
            + "  private final int depth;\n"
            + "\n"
            + "  private final boolean includeAnnotations;\n"
            + "\n"
            + "  private final Map<TypeVariable, String> typeVariables;\n"
            + "  private Set<String> allTypes = new HashSet<>();\n"
            + "  private String mainType;\n"
            + "  private String fullType;\n"
            + "  private final List<UType> params = new ArrayList<>();\n"
            + "  private final List<AnnotationMirror> annotations = new ArrayList<>();\n"
            + "  private List<AnnotationMirror> everyAnnotation = new ArrayList<>();\n"
            + "  private String shortType;\n"
            + "  private TypeKind kind;\n"
            + "\n"
            + "  public static TypeMirrorVisitor create(TypeMirror typeMirror) {\n"
            + "    return create(typeMirror, true);\n"
            + "  }\n"
            + "\n"
            + "  public static TypeMirrorVisitor create(TypeMirror typeMirror, boolean includedAnnotations) {\n"
            + "    final var v = new TypeMirrorVisitor(1, Map.of(), includedAnnotations);\n"
            + "    final StringBuilder b = new StringBuilder();\n"
            + "    v.fullType = typeMirror.accept(v, b).toString();\n"
            + "    return v;\n"
            + "  }\n"
            + "\n"
            + "  private TypeMirrorVisitor() {\n"
            + "    this(1, new HashMap<>(), true);\n"
            + "  }\n"
            + "\n"
            + "  private TypeMirrorVisitor(\n"
            + "      int depth, Map<TypeVariable, String> typeVariables, boolean includeAnnotations) {\n"
            + "    this.includeAnnotations = includeAnnotations;\n"
            + "    this.depth = depth;\n"
            + "    this.typeVariables = new HashMap<>();\n"
            + "    this.typeVariables.putAll(typeVariables);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public Set<String> importTypes() {\n"
            + "    return allTypes.stream().filter(this::notJavaLang).collect(toSet());\n"
            + "  }\n"
            + "\n"
            + "  private boolean notJavaLang(String type) {\n"
            + "    return !type.startsWith(\"java.lang.\") || Character.isLowerCase(type.charAt(10));\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public String shortType() {\n"
            + "    if (shortType == null) {\n"
            + "      shortType = shortRawType(fullType, allTypes);\n"
            + "    }\n"
            + "    return shortType;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public String full() {\n"
            + "    return fullType;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public boolean isGeneric() {\n"
            + "    return fullType.contains(\"<\");\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public List<UType> componentTypes() {\n"
            + "    return params;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public List<AnnotationMirror> annotations() {\n"
            + "    return annotations;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public List<AnnotationMirror> allAnnotationsInType() {\n"
            + "    return everyAnnotation;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public String mainType() {\n"
            + "    return mainType;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public UType param0() {\n"
            + "    return params.isEmpty() ? null : params.get(0);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public UType param1() {\n"
            + "    return params.size() < 2 ? null : params.get(1);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public TypeKind kind() {\n"
            + "    return kind;\n"
            + "  }\n"
            + "\n"
            + "  private static String shortRawType(String rawType, Set<String> allTypes) {\n"
            + "    final Map<String, String> typeMap = new LinkedHashMap<>();\n"
            + "    for (final String val : allTypes) {\n"
            + "      typeMap.put(val, ProcessorUtils.shortType(val));\n"
            + "    }\n"
            + "    String shortRaw = rawType;\n"
            + "    for (final var entry : typeMap.entrySet()) {\n"
            + "      shortRaw = shortRaw.replace(entry.getKey(), entry.getValue());\n"
            + "    }\n"
            + "    return shortRaw;\n"
            + "  }\n"
            + "\n"
            + "  private void child(TypeMirror ct, StringBuilder p, boolean setMain) {\n"
            + "\n"
            + "    var child = new TypeMirrorVisitor(depth + 1, typeVariables, includeAnnotations);\n"
            + "    child.allTypes = allTypes;\n"
            + "    child.everyAnnotation = everyAnnotation;\n"
            + "    var full = ct.accept(child, new StringBuilder()).toString();\n"
            + "    child.fullType = full;\n"
            + "    params.add(child);\n"
            + "    p.append(full);\n"
            + "    if (setMain) {\n"
            + "      mainType = child.mainType;\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  private void child(TypeMirror ct, StringBuilder p) {\n"
            + "    child(ct, p, false);\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitPrimitive(PrimitiveType t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    if (includeAnnotations) {\n"
            + "      for (final var ta : t.getAnnotationMirrors()) {\n"
            + "        p.append(ta.toString()).append(\" \");\n"
            + "        annotations.add(ta);\n"
            + "        everyAnnotation.add(ta);\n"
            + "      }\n"
            + "    }\n"
            + "\n"
            + "    var primitiveStr = t.getKind().toString().toLowerCase(Locale.ROOT);\n"
            + "    if (this.mainType == null) {\n"
            + "      mainType = primitiveStr;\n"
            + "    }\n"
            + "    p.append(primitiveStr);\n"
            + "\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitNull(NullType t, StringBuilder p) {\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitArray(ArrayType t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    boolean mainUnset = this.mainType == null;\n"
            + "    final var ct = t.getComponentType();\n"
            + "    child(ct, p, true);\n"
            + "    boolean first = true;\n"
            + "    if (includeAnnotations) {\n"
            + "      for (final var ta : t.getAnnotationMirrors()) {\n"
            + "        if (first) {\n"
            + "          p.append(\" \");\n"
            + "          first = false;\n"
            + "        }\n"
            + "        p.append(ta.toString()).append(\" \");\n"
            + "        annotations.add(ta);\n"
            + "        everyAnnotation.add(ta);\n"
            + "      }\n"
            + "    }\n"
            + "    p.append(\"[]\");\n"
            + "    if (mainUnset) {\n"
            + "      mainType += \"[]\";\n"
            + "    }\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitDeclared(DeclaredType t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    final String fqn = fullyQualfiedName(t, includeAnnotations);\n"
            + "    var trimmed = fullyQualfiedName(t, false);\n"
            + "    allTypes.add(ProcessorUtils.extractEnclosingFQN(trimmed));\n"
            + "\n"
            + "    if (this.mainType == null) {\n"
            + "      mainType = trimmed;\n"
            + "    }\n"
            + "    p.append(fqn);\n"
            + "    final var tas = t.getTypeArguments();\n"
            + "    if (!tas.isEmpty()) {\n"
            + "      p.append(\"<\");\n"
            + "      boolean first = true;\n"
            + "      for (final var ta : tas) {\n"
            + "        if (!first) {\n"
            + "          p.append(\", \");\n"
            + "        }\n"
            + "        child(ta, p);\n"
            + "        first = false;\n"
            + "      }\n"
            + "      p.append(\">\");\n"
            + "    }\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  String fullyQualfiedName(DeclaredType t, boolean includeAnnotations) {\n"
            + "    final TypeElement element = (TypeElement) t.asElement();\n"
            + "    final var typeUseAnnotations = t.getAnnotationMirrors();\n"
            + "\n"
            + "    if (typeUseAnnotations.isEmpty() || !includeAnnotations) {\n"
            + "      return element.getQualifiedName().toString();\n"
            + "    }\n"
            + "    final StringBuilder sb = new StringBuilder();\n"
            + "    // if not too nested, write annotations before the fqn like @someAnnotation io.YourType\n"
            + "    if (depth < 3) {\n"
            + "      for (final var ta : typeUseAnnotations) {\n"
            + "        sb.append(ta.toString()).append(\" \");\n"
            + "      }\n"
            + "    }\n"
            + "    String enclosedPart;\n"
            + "    final Element enclosed = element.getEnclosingElement();\n"
            + "    if (enclosed instanceof QualifiedNameable) {\n"
            + "      enclosedPart = ((QualifiedNameable) enclosed).getQualifiedName().toString() + \".\";\n"
            + "    } else {\n"
            + "      enclosedPart = \"\";\n"
            + "    }\n"
            + "    sb.append(enclosedPart);\n"
            + "\n"
            + "    // if too nested, write annotations in the fqn like io.@someAnnotation YourType\n"
            + "    if (depth > 2) {\n"
            + "      for (final var ta : typeUseAnnotations) {\n"
            + "        sb.append(ta.toString()).append(\" \");\n"
            + "      }\n"
            + "    }\n"
            + "    for (final var ta : typeUseAnnotations) {\n"
            + "\n"
            + "      final TypeElement annotation = (TypeElement) ta.getAnnotationType().asElement();\n"
            + "      allTypes.add(annotation.getQualifiedName().toString());\n"
            + "      annotations.add(ta);\n"
            + "      everyAnnotation.add(ta);\n"
            + "    }\n"
            + "    sb.append(element.getSimpleName());\n"
            + "    return sb.toString();\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitError(ErrorType t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitTypeVariable(TypeVariable t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    /*\n"
            + "     * Types can be recursive so we have to check if we have already done this type.\n"
            + "     */\n"
            + "    final String previous = typeVariables.get(t);\n"
            + "\n"
            + "    if (previous != null) {\n"
            + "      p.append(previous);\n"
            + "      return p;\n"
            + "    }\n"
            + "\n"
            + "    final StringBuilder sb = new StringBuilder();\n"
            + "\n"
            + "    /*\n"
            + "     * We do not have to print the upper and lower bound as those are defined usually\n"
            + "     * on the method.\n"
            + "     */\n"
            + "    if (includeAnnotations) {\n"
            + "      for (final var ta : t.getAnnotationMirrors()) {\n"
            + "        p.append(ta.toString()).append(\" \");\n"
            + "        sb.append(ta.toString()).append(\" \");\n"
            + "      }\n"
            + "    }\n"
            + "    var name = t.asElement().getSimpleName().toString();\n"
            + "    if (mainType == null) {\n"
            + "      mainType = name;\n"
            + "    }\n"
            + "\n"
            + "    p.append(name);\n"
            + "    sb.append(name);\n"
            + "    typeVariables.put(t, sb.toString());\n"
            + "    var upperBound = t.getUpperBound();\n"
            + "    if (upperBound != null) {\n"
            + "      child(upperBound, new StringBuilder());\n"
            + "    }\n"
            + "\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitWildcard(WildcardType t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    final var extendsBound = t.getExtendsBound();\n"
            + "    final var superBound = t.getSuperBound();\n"
            + "    kind = t.getKind();\n"
            + "    for (final var ta : t.getAnnotationMirrors()) {\n"
            + "      p.append(ta.toString()).append(\" \");\n"
            + "    }\n"
            + "    if (extendsBound != null) {\n"
            + "      p.append(\"? extends \");\n"
            + "      child(extendsBound, p);\n"
            + "    } else if (superBound != null) {\n"
            + "      p.append(\"? super \");\n"
            + "      child(superBound, p);\n"
            + "    } else {\n"
            + "      p.append(\"?\");\n"
            + "    }\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitExecutable(ExecutableType t, StringBuilder p) {\n"
            + "    throw new UnsupportedOperationException(\"don't support executables\");\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitNoType(NoType t, StringBuilder p) {\n"
            + "    throw new UnsupportedOperationException(\"don't support NoType\");\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitIntersection(IntersectionType t, StringBuilder p) {\n"
            + "    kind = t.getKind();\n"
            + "    boolean first = true;\n"
            + "    for (final var b : t.getBounds()) {\n"
            + "      if (first) {\n"
            + "        first = false;\n"
            + "      } else {\n"
            + "        p.append(\" & \");\n"
            + "      }\n"
            + "      child(b, p);\n"
            + "    }\n"
            + "    return p;\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public StringBuilder visitUnion(UnionType t, StringBuilder p) {\n"
            + "    throw new UnsupportedOperationException();\n"
            + "  }\n"
            + "}\n");
  }
}
