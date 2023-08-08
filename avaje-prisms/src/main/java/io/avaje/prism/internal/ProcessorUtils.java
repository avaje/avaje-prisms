package io.avaje.prism.internal;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

@Generated("avaje-prism-generator")
final class ProcessorUtils {

  private static final Pattern WHITE_SPACE_REGEX =
      Pattern.compile("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
  private static final Pattern COMMA_PATTERN =
      Pattern.compile(", (?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
  private static final Pattern PARENTHESIS_CONTENT = Pattern.compile("\\((.*?)\\)");

  private ProcessorUtils() {}

  private static final Map<String, String> BOX_MAP =
      Map.of(
          "char", "Character",
          "byte", "Byte",
          "int", "Integer",
          "long", "Long",
          "short", "Short",
          "double", "Double",
          "float", "Float",
          "boolean", "Boolean");

  /**
   * Returns boxed type if type string is primitive, otherwise return the input unchanged
   *
   * @param type type string
   * @return boxed type string if type is primitive
   */
  public static String getBoxedPrimitive(String type) {
    final var wrapped = BOX_MAP.get(type);
    return wrapped != null ? "java.lang." + wrapped : null;
  }

  /**
   * Return true if type string is of a primitive type
   *
   * @param type
   * @return true if type represents a primitive
   */
  public static boolean isPrimitive(String type) {
    return BOX_MAP.containsKey(type);
  }

  /**
   * Get Package from a given fqn string
   *
   * @param fqn the fully qualified type string
   * @return the package of the type
   */
  public static String packageOf(String fqn) {

    return fqn.replace("." + shortType(fqn), "");
  }

  /**
   * Get short type from a given fqn string. Nested Classes will have parent classes as part of the
   * short name
   *
   * @param fqn the fully qualified type string
   * @return the short type
   */
  public static String shortType(String fqn) {
    final int p = fqn.lastIndexOf('.');
    if (p == -1) {
      return fqn;
    }
    var result = "";
    var foundClass = false;
    for (final String part : fqn.split("\\.")) {
      if (foundClass || Character.isUpperCase(part.charAt(0))) {
        foundClass = true;
        result += (result.isEmpty() ? "" : ".") + part;
      }
    }
    return result;
  }

  /**
   * Remove all annotations and their values from a string.
   *
   * @param input string to remove annotations from
   * @return input free of annotations
   */
  public static String trimAnnotations(String input) {
    input = COMMA_PATTERN.matcher(input).replaceAll(",");
    return cutAnnotations(input);
  }

  private static String cutAnnotations(String input) {
    final int pos = input.indexOf("@");
    if (pos == -1) {
      return input;
    }

    final Matcher matcher = WHITE_SPACE_REGEX.matcher(input);

    int currentIndex = 0;
    if (matcher.find()) {
      currentIndex = matcher.start();
    }
    final var result = input.substring(0, pos) + input.substring(currentIndex + 1);
    return cutAnnotations(result);
  }

  /**
   * Return the common parent package between two classes/packages.
   *
   * @param firstPkg first class/package string
   * @param secondPkg second class/package string
   * @return the common package between the two classes
   */
  public static String commonParent(String firstPkg, String secondPkg) {
    if (secondPkg == null) return firstPkg;
    if (firstPkg == null) return packageOf(secondPkg);
    if (secondPkg.startsWith(firstPkg)) {
      return firstPkg;
    }
    int next;
    do {
      next = firstPkg.lastIndexOf('.');
      if (next > -1) {
        firstPkg = firstPkg.substring(0, next);
        if (secondPkg.startsWith(firstPkg)) {
          return firstPkg;
        }
      }
    } while (next > -1);

    return firstPkg;
  }

  /**
   * Determine if a VariableElement is a varargs parameter
   *
   * @param element the parameter element
   * @param position the position of the parameter in the signature
   * @return true if element is a varargs parameter, false otherwise
   */
  public static boolean isVarArg(VariableElement element, int position) {
    final var methodString = trimAnnotations(element.getEnclosingElement().toString());
    final var typeString = trimAnnotations(element.asType().toString()).replace("[]", "");
    final Matcher matcher = PARENTHESIS_CONTENT.matcher(methodString);

    if (matcher.find()) {
      final var param = matcher.group(1).split(",")[position];

      return param.replace("[]", "").contains(typeString) && param.endsWith("...");
    }
    return false;
  }

  /**
   * Check if element has an annotation with a simple name that matches the given short name
   *
   * @param element element to check
   * @param simpleName the simple name of the target annotation
   * @return true if a matching annotation is present
   */
  public static boolean hasAnnotationWithName(Element element, String simpleName) {
    for (final var mirror : element.getAnnotationMirrors()) {
      if (simpleName.equals(mirror.getAnnotationType().asElement().getSimpleName().toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sanitize an import string to remove invalid characters
   *
   * @param input input to sanitize
   * @return sanitized import statement
   */
  public static String sanitizeImports(String input) {
    final int pos = input.indexOf("@");
    if (pos == -1) {
      return removeInvalidChars(input);
    }
    final var start = pos == 0 ? input.substring(0, pos) : "";
    return start + removeInvalidChars(input.substring(input.lastIndexOf(' ') + 1));
  }

  private static String removeInvalidChars(String type) {
    return type.replaceAll("[^\\n\\r\\t $;\\w.]", "");
  }
}
