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
package com.github.dandelion.core.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.web.handler.HandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Main Dandelion filter. This filter is used to:
 * </p>
 * <ul>
 * <li>bootstraps the Dandelion {@link Context}. It occurs only once in the
 * {@link #init(FilterConfig)} method.</li>
 * <li>injects into all requests this {@link Context}, so that it can be
 * accessed later in the web application</li>
 * <li>invoke two handler chains: a first one that pre-processes requests and
 * another one that post-processes responses. These chains are used, e.g. to
 * injects assets into HTML source code or to GZIP responses.</li>
 * </ul>
 * 
 * <p>
 * This filter needs to be registered in your {@code web.xml} file:
 * </p>
 * 
 * <pre>
 * &lt;!-- Dandelion filter definition and mapping --&gt;
 * &lt;filter&gt;
 *    &lt;filter-name&gt;dandelionFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;com.github.dandelion.core.web.DandelionFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;dandelionFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </pre>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class DandelionFilter implements Filter {

   private static final Logger LOG = LoggerFactory.getLogger(DandelionFilter.class);

   /**
    * The Dandelion context.
    */
   private Context context;

   @Override
   public void init(FilterConfig filterConfig) throws ServletException {
      LOG.info("Initializing the Dandelion context");
      context = new Context(filterConfig);
      LOG.info("Dandelion context initialized");
   }

   @Override
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
         throws IOException, ServletException {

      // Only filter HTTP requests
      if (!(servletRequest instanceof HttpServletRequest)) {
         LOG.warn("The DandelionFilter only applies to HTTP requests");
         filterChain.doFilter(servletRequest, servletResponse);
         return;
      }

      HttpServletRequest request = (HttpServletRequest) servletRequest;
      HttpServletResponse response = (HttpServletResponse) servletResponse;
      LOG.trace("Request URL: \"{}\" is about to be processed", request.getRequestURL());

      // Make the Dandelion context available through a request attribute for
      // potential use by end-users or components
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);

      // Pre-filtering handlers processing
      HandlerChain preHandlerChain = context.getPreHandlerChain();
      if (preHandlerChain != null) {
         HandlerContext preHandlerContext = new HandlerContext(context, request, response, null);
         preHandlerChain.doHandle(preHandlerContext);
      }

      // Wraps the response before applying the filter chain
      ByteArrayResponseWrapper wrappedResponse = new ByteArrayResponseWrapper(response);
      filterChain.doFilter(request, wrappedResponse);

      // In case of a redirect, no need to process the response. Moreover,
      // getWritter may have allready been called.
      if (wrappedResponse.isRedirect()) {
         return;
      }
      
      // Extracts the response as a byte array so that it can be passed to the
      // post-handlers chain
      byte[] finalResponse = wrappedResponse.toByteArray();

      // Post-filtering handlers processing
      HandlerChain postHandlerChain = context.getPostHandlerChain();
      HandlerContext postHandlerContext = null;
      if (postHandlerChain != null) {
         postHandlerContext = new HandlerContext(context, request, response, finalResponse);
         postHandlerChain.doHandle(postHandlerContext);
      }

      // The response may have been set to null by one of the handlers
      if (postHandlerContext != null && postHandlerContext.getResponseAsBytes() == null) {
         return;
      }

      response.setContentLength(postHandlerContext.getResponseAsBytes().length);
      response.getOutputStream().write(postHandlerContext.getResponseAsBytes());
   }

   @Override
   public void destroy() {
      context.destroy();
   }
}