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
package com.github.dandelion.thymeleaf.web.handler.impl;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.cache.Cache;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.RequestFlashData;
import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;
import com.github.dandelion.core.web.handler.cache.HttpHeader;
import com.github.dandelion.core.web.handler.cache.HttpHeaderUtils;
import com.github.dandelion.thymeleaf.resourceresolver.JsResourceResolver;
import com.github.dandelion.thymeleaf.templatemode.DandelionTemplateModeHandlers;
import com.github.dandelion.thymeleaf.templateresolver.JsTemplateResolver;

/**
 * <p>
 * Post-filtering request handler in charge of processing external Javascript
 * assets with Thymeleaf expressions.
 * </p>
 * <p>
 * Only applies on "user assets" (i.e. non vendor).
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class ProcessJsPostHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(ProcessJsPostHandler.class);

   /**
    * Thymeleaf template engine.
    */
   private static TemplateEngine templateEngine;

   private static Set<String> compatibleMimeTypes;

   /**
    * Initialisation of the template engine with the custom template mode.
    */
   static {
      JsTemplateResolver templateResolver = new JsTemplateResolver();
      templateResolver.setTemplateMode(DandelionTemplateModeHandlers.TEMPLATEMODE_DANDELION_JS);
      templateResolver.setCacheable(false);

      templateEngine = new TemplateEngine();
      templateEngine.addTemplateModeHandler(DandelionTemplateModeHandlers.DANDELION_JS);
      templateEngine.setTemplateResolver(templateResolver);

      compatibleMimeTypes = new HashSet<String>();
      compatibleMimeTypes.add("application/x-javascript");
      compatibleMimeTypes.add("application/javascript");
      compatibleMimeTypes.add("text/javascript");
      compatibleMimeTypes.add("text/ecmascript");
      compatibleMimeTypes.add("application/ecmascript");
      compatibleMimeTypes.add("text/jscript");
   }

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public boolean isAfterChaining() {
      return true;
   }

   @Override
   public int getRank() {
      return 2;
   }

   @Override
   public boolean isApplicable(HandlerContext handlerContext) {

      // Retrieves the content type from the filtered response and remove
      // charset if present
      String mimeType = handlerContext.getResponse().getContentType();
      boolean compatibleMimeType = StringUtils.isNotBlank(mimeType) && compatibleMimeTypes.contains(mimeType.split(";")[0]);

      String servletUrlPattern = handlerContext.getContext().getConfiguration().getAssetUrlPattern()
            .replaceAll("/\\*", "");
      boolean matchesServletName = handlerContext.getRequest().getRequestURL().toString().contains(servletUrlPattern);
      LOG.trace("compatibleMimeType: {}, matchesServletName: {}", compatibleMimeType, matchesServletName);

      boolean isJsProcessingEnabled = handlerContext.getContext().getConfiguration().isAssetJsProcessingEnabled();

      return isJsProcessingEnabled && compatibleMimeType && matchesServletName;
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {

      // Get the asset content thanks to the cache key
      String cacheKey = AssetUtils.extractCacheKeyFromRequest(handlerContext.getRequest());
      Asset asset = handlerContext.getContext().getAssetStorage().get(cacheKey).getAsset();

      // Update the current context with additional attributes
      WebContext ctx = new WebContext(handlerContext.getRequest(), handlerContext.getResponse(), handlerContext
            .getRequest().getServletContext(), handlerContext.getRequest().getLocale());

      // Retrieve the cached request attributes from cache
      String requestKey = AssetUtils.extractRequestKeyFromRequest(handlerContext.getRequest());
      RequestFlashData requestData = handlerContext.getContext().getRequestFlashDataCache().get(requestKey);

      if (requestData != null) {
         ctx.setVariables(requestData.getAttributes());
      }

      // Process the Javascript asset
      String processed = templateEngine.process(asset.getName(), ctx);

      // Housecleaning
      processed = processed.replace(JsResourceResolver.BLOCK_WRAP_CDATA_START, "");
      processed = processed.replace(JsResourceResolver.BLOCK_WRAP_CDATA_END, "");

      String configuredEncoding = handlerContext.getContext().getConfiguration().getEncoding();

      // Update HTTP headers so that this asset is never cached
      handlerContext.getResponse().setHeader(HttpHeader.CACHE_CONTROL.getName(), "no-cache, no-store");
      handlerContext.getResponse().setHeader(HttpHeader.ETAG.getName(),
            HttpHeaderUtils.computeETag(handlerContext.getResponseAsBytes(), handlerContext));
      Calendar past = Calendar.getInstance();
      past.add(Calendar.YEAR, -1);
      handlerContext.getResponse().setDateHeader(HttpHeader.EXPIRES.getName(), past.getTimeInMillis());
      handlerContext.getResponse().setHeader(HttpHeader.VARY.getName(), "Accept-Encoding");

      // Clean flash data cache
      Cache<String, RequestFlashData> cache = handlerContext.getContext().getRequestFlashDataCache();
      Iterator<String> iterator = cache.keySet().iterator();
      while (iterator.hasNext()) {
         String key = iterator.next();
         if (cache.get(key).isExpired()) {
            iterator.remove();
         }
      }
      
      // Override the response with the processed Javascript
      try {
         handlerContext.setResponseAsBytes(processed.getBytes(configuredEncoding));
      }
      catch (UnsupportedEncodingException e) {
         throw new DandelionException("Unable to encode the HTML page using the '" + configuredEncoding
               + "', which doesn't seem to be supported", e);
      }

      return false;
   }
}