package io.avaje.prisms;

import java.lang.annotation.Target;

import io.avaje.prism.GeneratePrism;
import io.avaje.prism.GeneratePrisms;

@GeneratePrism(GeneratePrism.class)
@GeneratePrism(GeneratePrisms.class)
@GeneratePrism(io.avaje.jsonb.Json.class)
@GeneratePrism(io.avaje.jsonb.Json.Import.class)
@GeneratePrism(io.avaje.jsonb.Json.JsonAlias.class)
@GeneratePrism(io.avaje.jsonb.Json.Ignore.class)
@GeneratePrism(io.avaje.jsonb.Json.Property.class)
@GeneratePrism(io.avaje.jsonb.Json.MixIn.class)
@GeneratePrism(io.avaje.jsonb.Json.Raw.class)
@GeneratePrism(io.avaje.jsonb.Json.SubTypes.class)
@GeneratePrism(io.avaje.jsonb.Json.SubType.class)
@GeneratePrism(value = javax.inject.Inject.class, name = "javaxInjectPrism")
@GeneratePrism(value = jakarta.inject.Inject.class, name = "jakartaInjectPrism")
@GeneratePrism(io.avaje.jsonb.Json.Value.class)
@GeneratePrism(io.avaje.jsonb.spi.MetaData.class)
// test nested annotation generation
@GeneratePrism(value = io.swagger.v3.oas.annotations.OpenAPIDefinition.class, publicAccess = true)
@GeneratePrism(value = io.swagger.v3.oas.annotations.info.Info.class, name = "INfoefPrism")
@GeneratePrism(io.avaje.jsonb.spi.MetaData.Factory.class)
@GeneratePrism(Target.class)
public class TestClass {}
