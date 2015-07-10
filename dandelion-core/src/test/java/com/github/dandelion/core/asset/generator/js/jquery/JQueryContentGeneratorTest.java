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
package com.github.dandelion.core.asset.generator.js.jquery;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.WebConstants;

import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.AFTER_ALL;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.AFTER_END_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.AFTER_START_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.BEFORE_ALL;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.BEFORE_END_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.BEFORE_START_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.COMPONENT_CONFIGURATION;

import static org.assertj.core.api.Assertions.assertThat;

public class JQueryContentGeneratorTest {

   private JQueryJsContentGenerator jQueryContentGenerator;
   private JQueryContent jQueryContent;
   private MockHttpServletRequest request;
   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() {
      jQueryContent = new JQueryContent();
      jQueryContentGenerator = new JQueryJsContentGenerator(jQueryContent);
      context = new Context(new MockFilterConfig());
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
   }

   @Test
   public void should_not_fill_anything_if_empty() {
      jQueryContent.appendToBeforeAll("");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_ALL)).isNull();
   }

   @Test
   public void should_not_fill_anything_if_null() {
      jQueryContent.appendToBeforeAll(null);
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_ALL)).isNull();
   }

   @Test
   public void should_fill_the_beforeAll_placeholder() {
      jQueryContent.appendToBeforeAll("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_ALL).toString()).isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      jQueryContent.appendToBeforeAll("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_ALL).toString()).isEqualTo("js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      assertThat(jQueryContentGenerator.getAssetContent(request)).isEqualTo(
            "js codejs code$(document).ready(function() {});");
   }

   @Test
   public void should_fill_the_beforeStartDocumentReady_placeholder() {
      jQueryContent.appendToBeforeStartDocumentReady("js code");

      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_START_DOCUMENT_READY).toString())
            .isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      jQueryContent.appendToBeforeStartDocumentReady("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_START_DOCUMENT_READY).toString()).isEqualTo(
            "js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      assertThat(jQueryContentGenerator.getAssetContent(request)).isEqualTo(
            "js codejs code$(document).ready(function() {});");
   }

   @Test
   public void should_fill_the_afterStartDocumentReady_placeholder() {
      jQueryContent.appendToAfterStartDocumentReady("js code");

      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_START_DOCUMENT_READY).toString()).isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      jQueryContent.appendToAfterStartDocumentReady("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_START_DOCUMENT_READY).toString()).isEqualTo(
            "js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      assertThat(StringUtils.getTestString(jQueryContentGenerator.getAssetContent(request))).isEqualTo(
            "$(document).ready(function(){jscodejscode});");
   }

   @Test
   public void should_fill_the_componentConf_placeholder() {
      jQueryContent.appendToComponentConfiguration("js code");

      assertThat(jQueryContent.getPlaceholderContent().get(COMPONENT_CONFIGURATION).toString()).isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      jQueryContent.appendToComponentConfiguration("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(COMPONENT_CONFIGURATION).toString()).isEqualTo(
            "js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      assertThat(StringUtils.getTestString(jQueryContentGenerator.getAssetContent(request))).isEqualTo(
            "$(document).ready(function(){jscodejscode});");
   }

   @Test
   public void should_fill_the_beforeEndDocumentReady_placeholder() {
      jQueryContent.appendToBeforeEndDocumentReady("js code");

      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_END_DOCUMENT_READY).toString()).isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      jQueryContent.appendToBeforeEndDocumentReady("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_END_DOCUMENT_READY).toString()).isEqualTo(
            "js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      assertThat(StringUtils.getTestString(jQueryContentGenerator.getAssetContent(request))).isEqualTo(
            "$(document).ready(function(){jscodejscode});");
   }

   @Test
   public void should_fill_the_afterEndDocumentReady_placeholder() {
      jQueryContent.appendToAfterEndDocumentReady("js code");

      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_END_DOCUMENT_READY).toString()).isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      jQueryContent.appendToAfterEndDocumentReady("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_END_DOCUMENT_READY).toString()).isEqualTo(
            "js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL)).isNull();

      assertThat(StringUtils.getTestString(jQueryContentGenerator.getAssetContent(request))).isEqualTo(
            "$(document).ready(function(){});jscodejscode");

   }

   @Test
   public void should_fill_the_afterAll_placeholder() {
      jQueryContent.appendToAfterAll("js code");

      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL).toString()).isEqualTo("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_ALL)).isNull();

      jQueryContent.appendToAfterAll("js code");
      assertThat(jQueryContent.getPlaceholderContent().get(AFTER_ALL).toString()).isEqualTo("js codejs code");
      assertThat(jQueryContent.getPlaceholderContent().get(BEFORE_ALL)).isNull();

      assertThat(StringUtils.getTestString(jQueryContentGenerator.getAssetContent(request))).isEqualTo(
            "$(document).ready(function(){});jscodejscode");
   }
}
