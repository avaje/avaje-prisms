module io.avaje.prism.core {

  requires java.compiler;
  requires static io.avaje.spi;
  provides javax.annotation.processing.Processor with io.avaje.prism.internal.PrismGenerator;

}
