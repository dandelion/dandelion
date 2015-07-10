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

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.web.handler.HandlerContext;
import com.github.dandelion.core.web.handler.cache.HttpHeader;

import static org.assertj.core.api.Assertions.assertThat;

public class ETagPostHandlerTest {

   private static String RESOURCE_RAW = "some-resource";
   private static String RESOURCE_MD5 = "e6b6c052bdb0a50cf2bc0e4aa9c7d921";
   private ETagPostHandler handler;
   private MockHttpServletRequest request;
   private MockHttpServletResponse response;
   private MockFilterConfig filterConfig;
   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() throws Exception {
      handler = new ETagPostHandler();
      filterConfig = new MockFilterConfig();
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }

   @Test
   public void should_not_apply_on_html() {
      response.setContentType("text/html");
      HandlerContext handlerContext = new HandlerContext(context, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_apply_on_everything_else_but_html() {
      response.setContentType("text/javascript");
      HandlerContext handlerContext = new HandlerContext(context, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isTrue();

      response.setContentType("application/javascript");
      handlerContext = new HandlerContext(context, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isTrue();

      response.setContentType("text/css");
      handlerContext = new HandlerContext(context, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isTrue();
   }

   @Test
   public void should_stop_chaining_when_ifnonematch_equals_etag() {

      HandlerContext handlerContext = new HandlerContext(context, request, response, RESOURCE_RAW.getBytes());
      request.addHeader(HttpHeader.IFNONEMATCH.getName(), "\"" + RESOURCE_MD5 + "\"");

      boolean shouldContinue = handler.handle(handlerContext);

      assertThat(shouldContinue).isFalse();
      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_MODIFIED);
   }

   @Test
   public void should_continue_chaining_when_ifnonematch_ne_etag() {

      HandlerContext handlerContext = new HandlerContext(context, request, response, "content-has-changed".getBytes());
      request.addHeader(HttpHeader.IFNONEMATCH.getName(), "\"" + RESOURCE_MD5 + "\"");

      boolean shouldContinue = handler.handle(handlerContext);

      assertThat(shouldContinue).isTrue();
   }
}
