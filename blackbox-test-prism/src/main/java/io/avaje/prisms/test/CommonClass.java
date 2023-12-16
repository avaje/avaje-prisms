package io.avaje.prisms.test;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateModuleInfoReader;
import io.avaje.prism.GenerateUtils;

@GenerateUtils
@GenerateAPContext
@GenerateModuleInfoReader
public class CommonClass {

  public void common() {
    System.out.println("some string idk");
  }
}
