package io.avaje.prisms.test.context;

import java.util.List;
import java.util.Map;

import io.avaje.prisms.test.CommonInterface;
import io.avaje.prisms.test.CommonInterface2;
import io.avaje.prisms.test.TestAnnotation;
import io.avaje.prisms.test.context.V4Rusty.Weapon;
import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.NotEmpty;

@TestAnnotation
public class TypeUse<T extends CommonInterface & CommonInterface2> {

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

  <T2 extends CommonInterface> T2 test() {
    return null;
  }
}
