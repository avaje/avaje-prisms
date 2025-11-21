package io.avaje.prisms.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProcessorUtilsTest {

  @Test
  void trimmedAnnotation() {
    assertEquals(
        "java.lang.String",
        ProcessorUtils.trimAnnotations(
            "@io.avaje.validation.constraints.Length(min = 5) java.lang.String"));

    assertEquals(
        "java.ProcessorUtils.Map<java.lang.String,java.lang.String>",
        ProcessorUtils.trimAnnotations(
            "java.ProcessorUtils.@io.avaje.validation.constraints.NotEmpty(message=\"sus \", groups={io.avaje.validation.generator.models.valid.Ship.class}) Map<java.lang.@io.avaje.validation.constraints.NotEmpty(groups={io.avaje.validation.generator.models.valid.Ship.class}),@io.avaje.validation.constraints.NotBlank String,java.lang.@io.avaje.validation.constraints.NotBlank(groups={io.avaje.validation.generator.models.valid.Ship.class}),@io.avaje.validation.Valid String>"));

    assertEquals(
        "java.ProcessorUtils.List<java.lang.String>",
        ProcessorUtils.trimAnnotations(
            "java.ProcessorUtils.@jakarta.validation.constraints.NotEmpty(\"message(); ,\") List<java.lang.@jakarta.validation.constraints.NotNull String>"));
    assertEquals(
        "int", ProcessorUtils.trimAnnotations("@jakarta.validation.constraints.Positive int"));
    assertEquals(
        "java.ProcessorUtils.Map<java.lang.String,java.lang.String>",
        ProcessorUtils.trimAnnotations(
            "java.ProcessorUtils.Map<@jakarta.validation.constraints.Positive(message=\"sus \", groups=1) java.lang.String,java.lang.String>"));
    assertEquals(
        "java.ProcessorUtils.Map<java.lang.String,java.lang.String>",
        ProcessorUtils.trimAnnotations(
            "java.ProcessorUtils.Map<java.lang.@jakarta.validation.constraints.Positive(message=\\\"sus \\\", groups=1) String,java.lang.String>"));
  }

  // --- Tests for boxedPrimitive ---

  @Test
  void boxedPrimitive_primitiveType() {
    assertEquals("java.lang.Integer", ProcessorUtils.boxedPrimitive("int"));
    assertEquals("java.lang.Character", ProcessorUtils.boxedPrimitive("char"));
    assertEquals("java.lang.Boolean", ProcessorUtils.boxedPrimitive("boolean"));
    assertEquals("java.lang.Double", ProcessorUtils.boxedPrimitive("double"));
  }

  @Test
  void boxedPrimitive_nonPrimitiveType() {
    assertNull(ProcessorUtils.boxedPrimitive("String"));
    assertNull(ProcessorUtils.boxedPrimitive("java.util.List"));
  }

  @Test
  void boxedPrimitive_emptyString() {
    assertNull(ProcessorUtils.boxedPrimitive(""));
  }

  // --- Tests for isPrimitive ---

  @Test
  void isPrimitive_isPrimitive() {
    assertTrue(ProcessorUtils.isPrimitive("byte"));
    assertTrue(ProcessorUtils.isPrimitive("long"));
    assertTrue(ProcessorUtils.isPrimitive("float"));
  }

  @Test
  void isPrimitive_isNotPrimitive() {
    assertFalse(ProcessorUtils.isPrimitive("Byte"));
    assertFalse(ProcessorUtils.isPrimitive("String"));
    assertFalse(ProcessorUtils.isPrimitive("void")); // void is not in BOX_MAP
  }

  // --- Tests for packageOf ---

  @Test
  void packageOf_normalFQN() {
    assertEquals("java.util", ProcessorUtils.packageOf("java.util.List"));
  }

  @Test
  void packageOf_nestedClass() {
    // shortType handles nested classes, e.g., "Outer.Inner"
    assertEquals("io.avaje.test", ProcessorUtils.packageOf("io.avaje.test.Outer.Inner"));
  }

  @Test
  void packageOf_emptyString() {
    assertEquals("", ProcessorUtils.packageOf(""));
  }

  // --- Tests for shortType ---

  @Test
  void shortType_simpleClass() {
    assertEquals("List", ProcessorUtils.shortType("java.util.List"));
  }

  @Test
  void shortType_nestedClass() {
    assertEquals("Outer.Inner", ProcessorUtils.shortType("io.avaje.test.Outer.Inner"));
  }

  @Test
  void shortType_nestedClassWithPackagePrefix() {
    assertEquals("Outer.Inner", ProcessorUtils.shortType("mypkg.Outer.Inner"));
  }

  @Test
  void shortType_classInDefaultPackage() {
    assertEquals("MyClass", ProcessorUtils.shortType("MyClass"));
  }

  @Test
  void shortType_packageOnly() {
    assertEquals("util", ProcessorUtils.shortType("java.util"));
  }

  @Test
  void shortType_emptyString() {
    assertEquals("", ProcessorUtils.shortType(""));
  }

  // --- Tests for commonParent ---

  @Test
  void commonParent_identical() {
    assertEquals(
        "java.util.concurrent",
        ProcessorUtils.commonParent("java.util.concurrent", "java.util.concurrent"));
  }

  @Test
  void commonParent_firstIsParent() {
    assertEquals("java.util", ProcessorUtils.commonParent("java.util", "java.util.List"));
  }

  @Test
  void commonParent_secondIsParent() {
    assertEquals("java.util", ProcessorUtils.commonParent("java.util.List", "java.util"));
  }

  @Test
  void commonParent_defaultPackage() {
    assertEquals("MyClass", ProcessorUtils.commonParent("MyClass", "java.lang"));
  }

  @Test
  void commonParent_nullSecond() {
    assertEquals("java.util.List", ProcessorUtils.commonParent("java.util.List", null));
  }

  @Test
  void commonParent_nullFirst() {
    // commonParent calls packageOf(secondPkg) if first is null
    assertEquals("java.util", ProcessorUtils.commonParent(null, "java.util.List"));
  }

  @Test
  void commonParent_bothNull() {
    assertNull(ProcessorUtils.commonParent(null, null));
  }

  // --- Tests for sanitizeImports ---

  @Test
  void sanitizeImports_cleanInput() {
    String input = "import java.util.List;";
    assertEquals("import java.util.List;", ProcessorUtils.sanitizeImports(input));
  }

  @Test
  void sanitizeImports_withAnnotation() {
    String input = "@SuppressWarning(\"a\") import java.util.List;";
    assertEquals("java.util.List;", ProcessorUtils.sanitizeImports(input));
  }

  @Test
  void sanitizeImports_invalidCharacters() {
    String input = "import java.util.L<>ist$;";
    assertEquals("import java.util.List$;", ProcessorUtils.sanitizeImports(input));
  }

  // --- Tests for extractEnclosingFQN ---

  @Test
  void extractEnclosingFQN_notNested() {
    assertEquals("java.util.List", ProcessorUtils.extractEnclosingFQN("java.util.List"));
  }

  @Test
  void extractEnclosingFQN_singleNested() {
    assertEquals("io.package.Top", ProcessorUtils.extractEnclosingFQN("io.package.Top.Nested"));
  }

  @Test
  void extractEnclosingFQN_doubleNested() {
    assertEquals(
        "io.package.Top.Nested", ProcessorUtils.extractEnclosingFQN("io.package.Top.Nested.Inner"));
  }
  @Test
  void extractEnclosingFQN_tripleNested() {
    assertEquals(
        "io.package.Top.Nested.Inner", ProcessorUtils.extractEnclosingFQN("io.package.Top.Nested.Inner.Inner"));
  }

  @Test
  void extractEnclosingFQN_defaultPackage() {
    assertEquals("Top", ProcessorUtils.extractEnclosingFQN("Top.Nested"));
  }

  @Test
  void extractEnclosingFQN_topLevelClassOnly() {
    assertEquals("Top", ProcessorUtils.extractEnclosingFQN("Top"));
  }

  @Test
  void extractEnclosingFQN_packageOnly() {
    assertEquals("java.util", ProcessorUtils.extractEnclosingFQN("java.util"));
  }
}
