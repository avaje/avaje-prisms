package io.avaje.prisms.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProcessorUtilsTest {

  @Test
  void trimmedAnnotation() {

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
}
