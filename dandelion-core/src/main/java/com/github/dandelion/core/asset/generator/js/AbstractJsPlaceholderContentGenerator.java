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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.generator.AbstractAssetPlaceholderContent;
import com.github.dandelion.core.asset.generator.AbstractAssetPlaceholderContentGenerator;
import com.github.dandelion.core.asset.generator.AssetPlaceholder;
import com.github.dandelion.core.scripting.ScriptingUtils;

/**
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public abstract class AbstractJsPlaceholderContentGenerator<P extends AssetPlaceholder, C extends AbstractAssetPlaceholderContent<P>>
      extends AbstractAssetPlaceholderContentGenerator<P, C> {

   private static final Logger logger = LoggerFactory.getLogger(AbstractJsPlaceholderContentGenerator.class);

   protected AbstractJsPlaceholderContentGenerator(C content) {
      super(content);
   }

   @Override
   public String getPlaceholderContent(HttpServletRequest request, Map<P, StringBuilder> contents) {

      logger.debug("Generating asset...");
      String generatedContent = getPlaceholderJavascriptContent(request, contents);
      logger.debug("Asset generated successfully");

      return ScriptingUtils.prettyPrintJs(generatedContent);
   }

   protected abstract String getPlaceholderJavascriptContent(HttpServletRequest request, Map<P, StringBuilder> contents);
}
