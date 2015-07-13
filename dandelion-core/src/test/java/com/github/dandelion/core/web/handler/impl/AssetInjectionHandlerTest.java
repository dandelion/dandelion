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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.HandlerContext;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AssetInjectionPostHandler.class, Context.class })
public class AssetInjectionHandlerTest {

   private static final String JS_PLACEHOLDER_NAME = "placeholderjs";
   private static final String CSS_PLACEHOLDER_NAME = "placeholdercss";
   private static final String HTML_DEFAULT_PLACEHOLDER = "<html><head></head><body></body></html>";
   private static final String HTML_CUSTOM_JS_PLACEHOLDER = "<html><head>" + JS_PLACEHOLDER_NAME
         + "</head><body></body></html>";
   private static final String HTML_CUSTOM_CSS_PLACEHOLDER = "<html><head></head><body></body>" + CSS_PLACEHOLDER_NAME
         + "</html>";
   private AssetInjectionPostHandler handler;
   private MockHttpServletRequest request;
   private HttpServletResponse response;

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Before
   public void setup() {
      handler = new AssetInjectionPostHandler();
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }

   @Test
   public void should_not_apply_on_javascript() {
      response.setContentType("text/javascript");
      HandlerContext handlerContext = new HandlerContext(null, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_not_apply_on_css() {
      response.setContentType("text/css");
      HandlerContext handlerContext = new HandlerContext(null, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_not_apply_if_explicitely_disabled_by_a_request_parameter() {
      request.addParameter(WebConstants.DANDELION_ASSET_FILTER_STATE, "false");
      response.setContentType("text/html");
      HandlerContext handlerContext = new HandlerContext(null, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_not_apply_if_explicitely_disabled_by_a_request_attribute() {
      request.setAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE, "false");
      response.setContentType("text/html");
      HandlerContext handlerContext = new HandlerContext(null, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isFalse();
   }

   @Test
   public void should_apply_on_html() {
      response.setContentType("text/html");
      HandlerContext handlerContext = new HandlerContext(null, request, response, null);
      assertThat(handler.isApplicable(handlerContext)).isTrue();
   }

   @Test
   public void should_insert_html_tags_in_the_default_placeholder() throws Exception {

      Set<Asset> js = new LinkedHashSet<Asset>();
      js.add(new Asset("a1", "1.0.0", AssetType.js, "final-location/a1.js"));
      js.add(new Asset("a2", "1.0.0", AssetType.js, "final-location/a2.js"));
      Set<Asset> css = new LinkedHashSet<Asset>();
      css.add(new Asset("a1", "1.0.0", AssetType.css, "final-location/a1.css"));
      css.add(new Asset("a2", "1.0.0", AssetType.css, "final-location/a2.css"));

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);

      when(context.getConfiguration().getEncoding()).thenReturn("UTF-8");

      AssetQuery aq = mock(AssetQuery.class, Mockito.RETURNS_DEEP_STUBS);
      whenNew(AssetQuery.class).withAnyArguments().thenReturn(aq);
      when(aq.atPosition(AssetDomPosition.head).perform()).thenReturn(css);
      when(aq.atPosition(AssetDomPosition.body).perform()).thenReturn(js);

      AssetRequestContext arc = mock(AssetRequestContext.class);
      request.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      HandlerContext handlerContext = new HandlerContext(context, request, response,
            HTML_DEFAULT_PLACEHOLDER.getBytes());

      handler.handle(handlerContext);

      byte[] processedResponse = handlerContext.getResponseAsBytes();
      String processedResponseAsString = new String(processedResponse);
      assertThat(processedResponseAsString).contains(
            "<link rel=\"stylesheet\" href=\"final-location/a1.css\"></link>\n<link rel=\"stylesheet\" href=\"final-location/a2.css\"></link>\n</head>");
      assertThat(processedResponseAsString).contains(
            "<script src=\"final-location/a1.js\"></script>\n<script src=\"final-location/a2.js\"></script>\n</body>");
   }

   @Test
   public void should_insert_script_tags_in_the_configured_placeholder() throws Exception {

      Set<Asset> js = new LinkedHashSet<Asset>();
      js.add(new Asset("a1", "1.0.0", AssetType.js, "final-location/a1.js"));
      js.add(new Asset("a2", "1.0.0", AssetType.js, "final-location/a2.js"));
      Set<Asset> css = new LinkedHashSet<Asset>();
      css.add(new Asset("a1", "1.0.0", AssetType.css, "final-location/a1.css"));
      css.add(new Asset("a2", "1.0.0", AssetType.css, "final-location/a2.css"));

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
      when(context.getConfiguration().getEncoding()).thenReturn("UTF-8");

      AssetQuery aq = mock(AssetQuery.class, Mockito.RETURNS_DEEP_STUBS);
      whenNew(AssetQuery.class).withAnyArguments().thenReturn(aq);
      when(aq.atPosition(AssetDomPosition.head).perform()).thenReturn(css);
      when(aq.atPosition(AssetDomPosition.body).perform()).thenReturn(js);

      AssetRequestContext arc = mock(AssetRequestContext.class);
      when(arc.getJsPlaceholder()).thenReturn(JS_PLACEHOLDER_NAME);
      request.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      HandlerContext handlerContext = new HandlerContext(context, request, response,
            HTML_CUSTOM_JS_PLACEHOLDER.getBytes());

      handler.handle(handlerContext);
      byte[] processedResponse = handlerContext.getResponseAsBytes();
      String processedResponseAsString = new String(processedResponse);
      assertThat(processedResponseAsString).contains(
            "<link rel=\"stylesheet\" href=\"final-location/a1.css\"></link>\n<link rel=\"stylesheet\" href=\"final-location/a2.css\"></link>\n</head>");
      assertThat(processedResponseAsString).contains(
            "<head><script src=\"final-location/a1.js\"></script>\n<script src=\"final-location/a2.js\"></script>");
   }

   @Test
   public void should_insert_link_tags_in_the_configured_placeholder() throws Exception {

      Set<Asset> js = new LinkedHashSet<Asset>();
      js.add(new Asset("a1", "1.0.0", AssetType.js, "final-location/a1.js"));
      js.add(new Asset("a2", "1.0.0", AssetType.js, "final-location/a2.js"));
      Set<Asset> css = new LinkedHashSet<Asset>();
      css.add(new Asset("a1", "1.0.0", AssetType.css, "final-location/a1.css"));
      css.add(new Asset("a2", "1.0.0", AssetType.css, "final-location/a2.css"));

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
      when(context.getConfiguration().getEncoding()).thenReturn("UTF-8");

      AssetQuery aq = mock(AssetQuery.class, Mockito.RETURNS_DEEP_STUBS);
      whenNew(AssetQuery.class).withAnyArguments().thenReturn(aq);
      when(aq.atPosition(AssetDomPosition.head).perform()).thenReturn(css);
      when(aq.atPosition(AssetDomPosition.body).perform()).thenReturn(js);

      AssetRequestContext arc = mock(AssetRequestContext.class);
      when(arc.getCssPlaceholder()).thenReturn(CSS_PLACEHOLDER_NAME);
      request.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      HandlerContext handlerContext = new HandlerContext(context, request, response,
            HTML_CUSTOM_CSS_PLACEHOLDER.getBytes());

      handler.handle(handlerContext);
      byte[] processedResponse = handlerContext.getResponseAsBytes();
      String processedResponseAsString = new String(processedResponse);
      assertThat(processedResponseAsString).contains(
            "<link rel=\"stylesheet\" href=\"final-location/a1.css\"></link>\n<link rel=\"stylesheet\" href=\"final-location/a2.css\"></link>\n</html>");
      assertThat(processedResponseAsString).contains(
            "<script src=\"final-location/a1.js\"></script>\n<script src=\"final-location/a2.js\"></script>\n</body>");
   }

   @Test
   public void should_throw_an_exception_if_a_wrong_encoding_is_configured() throws Exception {

      String wrongEncoding = "WRONG-ENCODING";

      AssetQuery aq = mock(AssetQuery.class, Mockito.RETURNS_DEEP_STUBS);
      whenNew(AssetQuery.class).withAnyArguments().thenReturn(aq);

      exception.expect(DandelionException.class);
      exception.expectMessage(
            "Unable to encode the HTML page using the '" + wrongEncoding + "', which doesn't seem to be supported");

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
      when(context.getConfiguration().getEncoding()).thenReturn(wrongEncoding);

      AssetRequestContext arc = mock(AssetRequestContext.class);
      request.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      HandlerContext handlerContext = new HandlerContext(context, request, response,
            HTML_DEFAULT_PLACEHOLDER.getBytes());

      handler.handle(handlerContext);
   }
}
