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
package com.github.dandelion.core.asset.generator.js;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.generator.AssetContentGenerator;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.web.WebConstants;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractJavascriptContentGeneratorTest {

   private AssetContentGenerator javascriptGenerator;
   private MockHttpServletRequest request;
   private MockFilterConfig filterConfig;
   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() {
      javascriptGenerator = new FakeJavascriptContentGenerator();
   }

   @Test
   public void should_pretty_print_in_devMode() {
      filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.TOOL_ASSET_PRETTY_PRINTING.getName(), "true");
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);

      String generatedAsset = javascriptGenerator.getAssetContent(request);
      assertThat(generatedAsset).isEqualTo("function() {\n    var o = new Object();\n}");
   }

   @Test
   public void should_not_pretty_print_in_prodMode() {
      filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.TOOL_ASSET_PRETTY_PRINTING.getName(), "false");
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);

      String generatedAsset = javascriptGenerator.getAssetContent(request);
      assertThat(generatedAsset).isEqualTo("function(){var o = new Object();}");
   }
}
