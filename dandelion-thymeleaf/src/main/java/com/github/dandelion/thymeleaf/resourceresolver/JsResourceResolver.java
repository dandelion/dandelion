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
package com.github.dandelion.thymeleaf.resourceresolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.resourceresolver.IResourceResolver;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.storage.AssetStorage;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.web.WebConstants;

/**
 * <p>
 * Custom Thymeleaf {@link IResourceResolver} in charge of resolving Javascript
 * assets managed by Dandelion.
 * </p>
 * <p>
 * This resolver performs the following action:
 * </p>
 * <ol>
 * <li>Get the cache key from the current request URL in order to fetch the
 * asset contents (Javascript) from the {@link AssetStorage}</li>
 * <li>Wrap the contents with prototype-only comment blocks so that Thymeleaf
 * will consider it as proper inlined Javascript</li>
 * <li>Wrap the processed contents in a buffer so that Thymeleaf can properly
 * read it</li>
 * </ol>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class JsResourceResolver implements IResourceResolver {

   public static final String NAME = "DANDELIONASSET";
   private static final String BLOCK_WRAP_START = "<!--/*/ <th:block th:inline=\"javascript\"> /*/-->";
   private static final String BLOCK_WRAP_END = "<!--/*/ </th:block> /*/-->";
   public static final String BLOCK_WRAP_CDATA_START = "/*<![CDATA[*/\n";
   public static final String BLOCK_WRAP_CDATA_END = "\n/*]]>*/";

   @Override
   public String getName() {
      return NAME;
   }

   @Override
   public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters, String resourceName) {

      IContext context = templateProcessingParameters.getContext();
      if (!(context instanceof IWebContext)) {
         throw new TemplateProcessingException("Resource resolution by ServletContext with "
               + this.getClass().getName() + " can only be performed " + "when context implements "
               + IWebContext.class.getName() + " [current context: " + context.getClass().getName() + "]");
      }

      HttpServletRequest request = ((IWebContext) context).getHttpServletRequest();

      Context dandelionContext = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);

      // Get the asset content thanks to the cache key
      String cacheKey = AssetUtils.extractCacheKeyFromRequest(request);
      String contents = dandelionContext.getAssetStorage().get(cacheKey).getContents();

      // Wrap the contents with prototype-only comment blocks
      StringBuilder adaptedContents = new StringBuilder(BLOCK_WRAP_START);
      adaptedContents.append(BLOCK_WRAP_CDATA_START);
      adaptedContents.append(contents);
      adaptedContents.append(BLOCK_WRAP_CDATA_END);
      adaptedContents.append(BLOCK_WRAP_END);

      String configuredEncoding = dandelionContext.getConfiguration().getEncoding();

      InputStream is = null;
      try {
         is = new ByteArrayInputStream(adaptedContents.toString().getBytes(configuredEncoding));
      }
      catch (UnsupportedEncodingException e) {
         throw new DandelionException("Unable to encode the Javascript asset using the '" + configuredEncoding
               + "', which doesn't seem to be supported", e);
      }

      return is;
   }

}
