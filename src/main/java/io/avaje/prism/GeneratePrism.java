/*
Copyright (c) 2006,2007, Bruce Chapman

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation and/or
      other materials provided with the distribution.
    * Neither the name of the Hickory project nor the names of its contributors
      may be used to endorse or promote products derived from this software without
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
 * GeneratePrism.java
 *
 * Created on 27 June 2006, 21:58
 *
 */
package io.avaje.prism;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates a Prism for the specified annotation, in the same package as the target.
 *
 * <p>If multiple @Prism annotations specifying the same value() (and name()) are present within one
 * package, only one Prism will be generated and no error will be raised.
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface GeneratePrism {

  /** The annotation to generate a prism for. */
  Class<? extends java.lang.annotation.Annotation> value();

  /**
   * The name of the generated prism class. Defaults to XXPrism where XX is the simple name of the
   * annotation specified by value().
   */
  String name() default "";

  /**
   * Set to true for the prism to have public access, otherwise the generated prism and its members
   * will be package visible. The default is sufficient if the prism is generated in the same
   * package as the AnnotationProcessor which uses them.
   */
  boolean publicAccess() default false;
}
