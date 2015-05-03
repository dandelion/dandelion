/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.thymeleaf.templatemode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.templatemode.ITemplateModeHandler;
import org.thymeleaf.templatemode.TemplateModeHandler;

import com.github.dandelion.thymeleaf.templateparser.JsTemplateParser;
import com.github.dandelion.thymeleaf.templatewriter.JsTemplateWriter;

/**
 * <p>
 * Utility class that defines the custom set of {@link ITemplateModeHandler}
 * objects.
 * </p>
 * <p>
 * For now, only one custom template mode exists:
 * </p>
 * <ul>
 * <li>DANDELION-JS (for Javascript assets)</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class DandelionTemplateModeHandlers {

   // We have to set a maximum pool size. Some environments might set too high
   // numbers for Runtime.availableProcessors (for example, Google App Engine
   // sets this to 1337).
   private static final int MAX_PARSERS_POOL_SIZE = 24;

   public static final ITemplateModeHandler DANDELION_JS;
   public static final String TEMPLATEMODE_DANDELION_JS = "DANDELION-JS";

   public static final Set<ITemplateModeHandler> ALL_TEMPLATE_MODE_HANDLERS;

   static {

      final int availableProcessors = Runtime.getRuntime().availableProcessors();
      final int poolSize = Math.min((availableProcessors <= 2 ? availableProcessors : availableProcessors - 1),
            MAX_PARSERS_POOL_SIZE);

      DANDELION_JS = new TemplateModeHandler(TEMPLATEMODE_DANDELION_JS, new JsTemplateParser(poolSize),
            new JsTemplateWriter());

      ALL_TEMPLATE_MODE_HANDLERS = new HashSet<ITemplateModeHandler>(
            Arrays.asList(new ITemplateModeHandler[] { DANDELION_JS }));
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private DandelionTemplateModeHandlers() {
      throw new AssertionError();
   }
}
