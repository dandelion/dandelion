/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
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
package com.github.dandelion.core.web.handler.debug;

import java.io.IOException;
import java.util.Map;

import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Contract for all debug pages.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public interface DebugPage {

   /**
    * <p>
    * Initializes the debug page with the current context.
    * </p>
    * 
    * @param context
    *           The wrapper object holding the context.
    */
   void initWith(HandlerContext context);

   /**
    * <p>
    * Returns the identifier of the debug page, used in its URI.
    * </p>
    * <p>
    * For example "assets" in
    * <code>http://domain/context/?ddl-debug&ddl-debug-page=assets</code>.
    * </p>
    * 
    * @return the idenfier of the debug page, used its URI.
    */
   String getId();

   /**
    * @return the name of the debug page, used in the sidebar menu.
    */
   String getName();

   /**
    * <p>
    * Loads the template of the debug page and returns it as a String in order
    * to be processed.
    * </p>
    * 
    * @param context
    *           The wrapper object holding the context.
    * @return the template as a String
    * @throws IOException
    *            if something goes wrong while loading the template.
    */
   String getTemplate(HandlerContext context) throws IOException;

   Map<String, String> getExtraParams();

   String getContext();
}
