package io.avaje.prism;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Dedicated annotation for signaling the prism generator to create an entry in META-INF/services.
 * The generator will automatically try to write META-INF files if the following annotations are
 * detected on a concrete processor class.
 *
 * <ul>
 *   <li>Any Avaje Prism annotations
 *   <li>{@link javax.annotation.processing.SupportedAnnotationTypes @SupportedAnnotationTypes}
 *   <li>{@link javax.annotation.processing.SupportedOptions @SupportedOptions}
 *   <li>{@link javax.annotation.processing.SupportedSourceVersion @SupportedSourceVersion}
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface AnnotationProcessor {}
