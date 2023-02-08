package io.avaje.prisms.test;

import io.avaje.jsonb.spi.Generated;
import io.avaje.prism.GeneratePrism;

@io.avaje.prism.GeneratePrisms({
  @GeneratePrism(value = io.avaje.jsonb.Json.MixIn.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.class),
  @GeneratePrism(value = io.avaje.jsonb.Json.JsonAlias.class),
  @GeneratePrism(value = Generated.class),
})
public class TestClass {}
