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

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.util.DigestUtils;
import com.github.dandelion.core.web.RequestFlashData;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Post-filtering request handler in charge of storing request attributes for
 * later use inside external JavaScript assets.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class SaveRequestPostHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(SaveRequestPostHandler.class);

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
      return 1;
   }

   @Override
   public boolean isApplicable(HandlerContext handlerContext) {

      HttpServletResponse wrappedResponse = handlerContext.getResponse();
      HttpServletRequest request = handlerContext.getRequest();

      boolean isJsProcessingEnabled = handlerContext.getContext().getConfiguration().isAssetJsProcessingEnabled();
      boolean isHtmlRequest = wrappedResponse.getContentType() != null
            && wrappedResponse.getContentType().contains("text/html");
      boolean notBrowsingDebugger = !request.getRequestURI().contains("ddl-debugger");

      return isJsProcessingEnabled && isHtmlRequest && notBrowsingDebugger;
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {

      // Generate a brand new request key
      String requestKey = DigestUtils.md5Digest(UUID.randomUUID().toString());

      // Create a new instance intended to be stored in session
      RequestFlashData requestData = new RequestFlashData(handlerContext.getRequest());

      // Store request data in cache for later use
      handlerContext.getContext().getRequestFlashDataCache().put(requestKey, requestData);

      // Store the generated request key as a request attribute for later use
      handlerContext.getRequest().setAttribute(WebConstants.DANDELION_REQUEST_KEY, requestKey);

      return true;
   }
}