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
package com.github.dandelion.core.asset.generator.js;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.generator.AssetContentGenerator;
import com.github.dandelion.core.scripting.ScriptingUtils;
import com.github.dandelion.core.web.WebConstants;

/**
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public abstract class AbstractJsContentGenerator implements AssetContentGenerator {

   private static Logger logger = LoggerFactory.getLogger(AbstractJsContentGenerator.class);

   @Override
   public String getAssetContent(HttpServletRequest request) {

      Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);

      logger.debug("Generating asset...");
      String generatedContent = getJavascriptContent(request);
      logger.debug("Asset generated successfully");

      if (context.getConfiguration().isToolAssetPrettyPrintingEnabled()) {
         return ScriptingUtils.prettyPrintJs(generatedContent);
      }

      return generatedContent;
   }

   protected abstract String getJavascriptContent(HttpServletRequest request);
}
