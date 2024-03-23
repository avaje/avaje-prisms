package io.avaje.prism;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Dedicated annotation for signaling the prism generator to create an entry in {@code META-INF/services/javax.annotation.processing.Processor}.
 * The generator will automatically try to write META-INF files if the following annotations are
 * detected on a concrete processor class.
 *
 * <ul>
 *   <li>Any avaje prism annotation
 *   <li>{@link javax.annotation.processing.SupportedAnnotationTypes @SupportedAnnotationTypes}
 *   <li>{@link javax.annotation.processing.SupportedOptions @SupportedOptions}
 *   <li>{@link javax.annotation.processing.SupportedSourceVersion @SupportedSourceVersion}
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface AnnotationProcessor {}
