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
package com.github.dandelion.core.web.handler.impl;

import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;
import com.github.dandelion.core.web.handler.cache.HttpHeader;
import com.github.dandelion.core.web.handler.cache.HttpHeaderUtils;

/**
 * <p>
 * Post-filtering response handler intended to set various HTTP cache headers on
 * the response.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class CacheHeaderPostHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(CacheHeaderPostHandler.class);

   private static final long ONE_YEAR_IN_SECONDS = 365 * 24 * 60 * 60;
   private static final long ONE_YEAR_IN_MILLISECONDS = ONE_YEAR_IN_SECONDS * 1000L;
   private final static long LAST_MODIFIED = System.currentTimeMillis();

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
      return 40;
   }

   @Override
   public boolean isApplicable(HandlerContext handlerContext) {
      return handlerContext.getResponse().getContentType() != null
            && !handlerContext.getResponse().getContentType().contains("text/html");
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {

      HttpServletResponse httpResponse = handlerContext.getResponse();

      if (handlerContext.getContext().getConfiguration().isCachingEnabled()) {

         httpResponse.setHeader(HttpHeader.CACHE_CONTROL.getName(), "public, max-age=" + ONE_YEAR_IN_SECONDS);

         httpResponse.setHeader(HttpHeader.ETAG.getName(),
               HttpHeaderUtils.computeETag(handlerContext.getResponseAsBytes(), handlerContext));

         httpResponse
               .setDateHeader(HttpHeader.EXPIRES.getName(), System.currentTimeMillis() + ONE_YEAR_IN_MILLISECONDS);

         // Considered the last modified date as the start up time of the
         // server
         httpResponse.setDateHeader(HttpHeader.LAST_MODIFIED.getName(), LAST_MODIFIED);
      }
      // Headers are set in order to disable cache and force new resource
      // updates to fetched (default in dev profile)
      else {

         httpResponse.setHeader(HttpHeader.CACHE_CONTROL.getName(), "no-cache, no-store");

         Calendar past = Calendar.getInstance();
         past.add(Calendar.YEAR, -1);
         httpResponse.setDateHeader(HttpHeader.EXPIRES.getName(), past.getTimeInMillis());
      }

      httpResponse.setHeader(HttpHeader.VARY.getName(), "Accept-Encoding");

      return true;
   }
}
