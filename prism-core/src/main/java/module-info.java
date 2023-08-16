module io.avaje.prism.core {

	  requires java.compiler;
	  requires io.avaje.spi;
  provides javax.annotation.processing.Processor with io.avaje.prism.internal.PrismGenerator;

}
