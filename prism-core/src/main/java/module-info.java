module io.avaje.prism.core {

  requires java.compiler;
  provides javax.annotation.processing.Processor with io.avaje.prism.internal.PrismGenerator;

}
