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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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

public class GZipCompressionPostHandlerTest {

   private GzipCompressionPostHandler handler;
   private MockHttpServletRequest request;
   private MockHttpServletResponse response;
   private MockFilterConfig filterConfig;
   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() throws Exception {
      handler = new GzipCompressionPostHandler();
      filterConfig = new MockFilterConfig();
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }

   @Test
   public void should_not_apply_if_gzip_is_not_accepted_by_the_browser() {
      request.addHeader(HttpHeader.ACCEPT_ENCODING.getName(), "compress");
      HandlerContext handlerContext = new HandlerContext(context, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_not_apply_if_the_response_has_an_incompatible_mimeType() {
      response.setContentType("incompatible-content-type");
      HandlerContext handlerContext = new HandlerContext(context, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_return_gzipped_content() throws Exception {

      String content = "response-to-compress";

      HandlerContext handlerContext = new HandlerContext(context, request, response, content.getBytes());
      handler.handle(handlerContext);

      InputStream ungzippedStream = new GZIPInputStream(new ByteArrayInputStream(handlerContext.getResponseAsBytes()));

      assertThat(IOUtils.toString(ungzippedStream)).isEqualTo(content);
      assertThat(response.getHeader(HttpHeader.CONTENT_ENCODING.getName())).isEqualTo("gzip");
   }

   @Test
   public void should_return_false_if_the_response_is_already_commited() throws Exception {

      response.setCommitted(true);
      HandlerContext handlerContext = new HandlerContext(context, request, response, "".getBytes());
      assertThat(handler.handle(handlerContext)).isFalse();
   }

   @Test
   public void should_return_false_if_204() throws Exception {

      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      HandlerContext handlerContext = new HandlerContext(context, request, response, "".getBytes());
      assertThat(handler.handle(handlerContext)).isFalse();
   }

   @Test
   public void should_return_false_if_205() throws Exception {

      response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
      HandlerContext handlerContext = new HandlerContext(context, request, response, "".getBytes());
      assertThat(handler.handle(handlerContext)).isFalse();
   }

   @Test
   public void should_return_false_if_304() throws Exception {

      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      HandlerContext handlerContext = new HandlerContext(context, request, response, "".getBytes());
      assertThat(handler.handle(handlerContext)).isFalse();
   }
}
