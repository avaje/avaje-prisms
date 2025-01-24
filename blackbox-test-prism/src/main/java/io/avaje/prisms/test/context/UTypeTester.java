package io.avaje.prisms.test.context;

import java.util.List;
import java.util.Map;

import io.avaje.prisms.test.CommonInterface;
import io.avaje.prisms.test.CommonInterface2;
import io.avaje.prisms.test.TestAnnotation;
import io.avaje.prisms.test.context.V4Rusty.Weapon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@TestAnnotation
public class UTypeTester<T extends CommonInterface & CommonInterface2> {

  @NotEmpty @NotBlank
  Map<
          @NotBlank(groups = Weapon.class) String,
          @NotEmpty(groups = Weapon.class) Map<
              @NotBlank(groups = Weapon.class) Weapon, @NotBlank V4Rusty>>
      map;

  List<T> list;
  List<? super Weapon> superWild;
  List<? extends Object> extendWild;
  List<?> wild;

  @NotEmpty
  @NotEmpty
  List<@NotBlank.List({@NotBlank, @NotBlank}) String> repeatable;

  void testVoid(@NotEmpty(groups = int[].class) int[] param) {}
}
