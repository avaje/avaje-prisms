package io.avaje.prism;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Place on a Type/Package to have the Prism Generator create a helper class */
@Retention(SOURCE)
@Target({TYPE, PACKAGE})
public @interface GenerateUtils {}
