package io.avaje.prisms.test;

import io.avaje.prism.GeneratePrism;

@io.avaje.prism.GeneratePrisms({
  @GeneratePrism(value = io.avaje.jsonb.Json.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.Import.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.JsonAlias.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.Ignore.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.Property.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.MixIn.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.Raw.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.SubTypes.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.SubType.class),
  @GeneratePrism(value = javax.inject.Inject.class, name = "javaxInjectPrism"),
  @GeneratePrism(value = jakarta.inject.Inject.class, name = "jakartaInjectPrism"),
  @GeneratePrism(value = io.avaje.jsonb.Json.Value.class),
  @GeneratePrism(value = io.avaje.jsonb.spi.MetaData.class),
  // test nested annotation generation
  @GeneratePrism(value = io.swagger.v3.oas.annotations.OpenAPIDefinition.class, publicAccess = true),
  @GeneratePrism(value = io.swagger.v3.oas.annotations.info.Info.class, name = "INfoefPrism"),
  @GeneratePrism(value = io.avaje.jsonb.spi.MetaData.Factory.class),
})
public class TestClass {}
