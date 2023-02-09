module io.avaje.prism {

  exports io.avaje.prism;
  requires java.compiler;
  provides javax.annotation.processing.Processor with io.avaje.prism.internal.PrismGenerator;

}
