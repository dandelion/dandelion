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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.util.UrlUtils;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;
import com.github.dandelion.core.web.handler.debug.AssetsDebugPage;
import com.github.dandelion.core.web.handler.debug.DebugPage;

/**
 * <p>
 * Post-filtering request handler intended to display the debugger when it is
 * requested by the user using the following request parameter:
 * {@code ddl-debug}.
 * </p>
 * <p>
 * If so, the {@link HttpServletResponse} is simply overriden with a new page
 * containing the debugger.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class DebuggerPostHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(DebuggerPostHandler.class);

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
      return 50;
   }

   /**
    * The debugger is only accessible from a HTML page.
    */
   @Override
   public boolean isApplicable(HandlerContext handlerContext) {
      return handlerContext.getContext().getConfiguration().isToolDebuggerEnabled()
            && handlerContext.getResponse().getContentType() != null
            && handlerContext.getResponse().getContentType().contains("text/html")
            && handlerContext.getRequest().getParameter(WebConstants.DANDELION_DEBUGGER) != null;
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {

      byte[] newResponse;

      String debugPage = handlerContext.getRequest().getParameter(WebConstants.DANDELION_DEBUGGER_PAGE);

      try {
         String responseAsString = getView(debugPage, handlerContext);
         newResponse = responseAsString.getBytes(handlerContext.getContext().getConfiguration().getEncoding());
      }
      catch (Exception e) {
         throw new DandelionException("An error occured when generating the \"" + debugPage + "\" debug page.", e);
      }

      // The response is overriden with a new one containing the debug page
      handlerContext.setResponseAsBytes(newResponse);

      return false;
   }

   private String getView(String pageName, HandlerContext context) throws IOException {

      DebugPage page;

      // If no page is specified or if the page does not exist, let's redirect
      // to the "assets" page by default
      if (StringUtils.isBlank(pageName)
            || !context.getContext().getDebugPageMap().containsKey(pageName.trim().toLowerCase())) {
         page = context.getContext().getDebugPageMap().get(AssetsDebugPage.PAGE_ID);
      }
      else {
         page = context.getContext().getDebugPageMap().get(pageName.trim().toLowerCase());
      }

      return getPage(page, context);
   }

   private String getPage(DebugPage page, HandlerContext context) throws IOException {

      page.initWith(context);

      // Get the template
      String template = page.getTemplate(context);

      // Inject Mustache context
      template = template.replace("%CONTEXT%", UrlUtils.getContext(context.getRequest()).toString());

      Map<String, String> variables = page.getExtraParams();
      if (variables != null) {
         for (Entry<String, String> variable : variables.entrySet()) {
            template = template.replace(variable.getKey(), variable.getValue());
         }
      }

      template = template.replace("%MUSTACHE_CTX%", page.getContext());

      return template;
   }
}
