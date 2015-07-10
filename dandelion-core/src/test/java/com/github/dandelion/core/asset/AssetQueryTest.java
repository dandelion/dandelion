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
package com.github.dandelion.core.asset;

import java.io.File;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.config.StandardConfigurationLoader;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.WebConstants;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetQueryTest {

   private MockHttpServletRequest request;
   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() {
      System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
      String path = new File("src/test/resources/asset-query/json/dandelion/".replace("/", File.separator))
            .getAbsolutePath();
      System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);

      context = new Context(new MockFilterConfig());
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
   }

   @After
   public void teardown() {
      System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
   }

   @Test
   public void should_return_only_head_assets() {

      AssetRequestContext.get(request).addBundles("bundle5");
      Set<Asset> assets = new AssetQuery(request, context).atPosition(AssetDomPosition.head).perform();
      assertThat(assets).extracting("name").containsExactly("a5_4", "a5_5");
   }

   @Test
   public void should_return_only_head_assets_with_js_in_last() {

      AssetRequestContext.get(request).addBundles("bundle5,bundle6");
      Set<Asset> assets = new AssetQuery(request, context).atPosition(AssetDomPosition.head).perform();
      assertThat(assets).extracting("name").containsExactly("a5_4", "a5_5", "a6_2", "a6_1");
   }

   @Test
   public void should_return_only_body_assets() {

      AssetRequestContext.get(request).addBundles("bundle5");
      Set<Asset> assets = new AssetQuery(request, context).atPosition(AssetDomPosition.body).perform();
      assertThat(assets).extracting("name").containsExactly("a5_1", "a5_2", "a5_3");
   }

   @Test
   public void should_return_only_body_assets_with_1_js_excluded() {

      AssetRequestContext.get(request).addBundles("bundle5").excludeJs("a5_1");
      Set<Asset> assets = new AssetQuery(request, context).atPosition(AssetDomPosition.body).perform();
      assertThat(assets).extracting("name").containsExactly("a5_2", "a5_3");
   }

   @Test
   public void should_return_all_assets_with_1_js_and_1_css_excluded() {

      AssetRequestContext.get(request).addBundles("bundle5").excludeJs("a5_1").excludeCss("a5_5");
      Set<Asset> assets = new AssetQuery(request, context).perform();
      assertThat(assets).extracting("name").containsExactly("a5_2", "a5_3", "a5_4");
   }

   @Test
   public void should_return_all_assets_with_1_css_excluded() {

      AssetRequestContext.get(request).addBundles("bundle5").excludeCss("a5_5");
      Set<Asset> assets = new AssetQuery(request, context).perform();
      assertThat(assets).extracting("name").containsExactly("a5_1", "a5_2", "a5_3", "a5_4");
   }

   @Test
   public void should_return_all_assets_with_1_css_with_malformatted_name_excluded() {

      AssetRequestContext.get(request).addBundles("bundle5").excludeCss("  a5_5 ");
      Set<Asset> assets = new AssetQuery(request, context).perform();
      assertThat(assets).extracting("name").containsExactly("a5_1", "a5_2", "a5_3", "a5_4");
   }

   @Test
   public void should_return_all_assets() {

      AssetRequestContext.get(request).addBundles("bundle5").excludeCss("unknown_css");
      Set<Asset> assets = new AssetQuery(request, context).perform();
      assertThat(assets).extracting("name").containsExactly("a5_1", "a5_2", "a5_3", "a5_4", "a5_5");
   }

   @Test
   public void should_return_all_assets_with_name_and_type() {

      AssetRequestContext.get(request).addBundles("bundle7");
      Set<Asset> assets = new AssetQuery(request, context).perform();
      assertThat(assets).extracting("name").containsExactly("a7_1", "a7_2", "a7_3");
      assertThat(assets).extracting("type").containsExactly(AssetType.js, AssetType.css, AssetType.js);
   }
}
