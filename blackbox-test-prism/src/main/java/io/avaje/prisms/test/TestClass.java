package io.avaje.prisms.test;

import java.lang.annotation.Target;

import io.avaje.prism.GeneratePrism;
import io.avaje.prism.GeneratePrisms;

@GeneratePrism(GeneratePrism.class)
@GeneratePrism(GeneratePrisms.class)
@GeneratePrism(io.avaje.jsonb.Json.class)
@GeneratePrism(io.avaje.jsonb.Json.Import.class)
@GeneratePrism(io.avaje.jsonb.Json.Alias.class)
@GeneratePrism(io.avaje.jsonb.Json.Ignore.class)
@GeneratePrism(io.avaje.jsonb.Json.Property.class)
@GeneratePrism(io.avaje.jsonb.Json.MixIn.class)
@GeneratePrism(io.avaje.jsonb.Json.Raw.class)
@GeneratePrism(value = io.avaje.jsonb.Json.SubTypes.class, superClass = CommonClass.class)
@GeneratePrism(value = io.avaje.jsonb.Json.SubType.class, superClass = CommonClass.class)
@GeneratePrism(value = javax.inject.Inject.class, name = "javaxInjectPrism")
@GeneratePrism(value = jakarta.inject.Inject.class, name = "jakartaInjectPrism")
@GeneratePrism(io.avaje.jsonb.Json.Value.class)
@GeneratePrism(io.avaje.jsonb.spi.MetaData.class)
// test nested annotation generation
@GeneratePrism(
    value = io.swagger.v3.oas.annotations.OpenAPIDefinition.class,
    publicAccess = true,
    superInterfaces = {CommonInterface.class, CommonInterface2.class})
@GeneratePrism(
    value = io.swagger.v3.oas.annotations.info.Info.class,
    name = "INfoefPrism",
    superInterfaces = {CommonInterface.class, CommonInterface2.class})
@GeneratePrism(io.avaje.jsonb.spi.MetaData.JsonFactory.class)
@GeneratePrism(Target.class)
@GeneratePrism(GeneratePrisms.class)
@GeneratePrism(GeneratePrism.class)
public class TestClass {}
