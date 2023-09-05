package io.avaje.prisms.test.context;

import java.util.List;

import io.avaje.prisms.test.TestAnnotation;

@TestAnnotation
public class V4Rusty {
  int ap;
  List<String> buddies = List.of("Vespers", "Raven");

  public class Weapon {
    int cost;
    String name;
  }
}
