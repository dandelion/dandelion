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
package com.github.dandelion.core.asset.processor;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.processor.impl.CssMinProcessor;
import com.github.dandelion.core.asset.processor.impl.CssUrlRewritingProcessor;
import com.github.dandelion.core.asset.processor.impl.JsMinProcessor;
import com.github.dandelion.core.asset.processor.impl.JsSemiColonProcessor;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetProcessorPipelineFactoryTest {

   private Context context;
   private AssetProcessorPipelineFactory pipelineFactory;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Before
   public void setup() {
      context = new Context(new MockFilterConfig());
      pipelineFactory = new AssetProcessorPipelineFactory(context);
   }

   @Test
   public void should_resolve_an_empty_pipeline() {
      Asset asset = new Asset();
      asset.setType(AssetType.js);
      asset.setProcessors(new String[] {});
      List<AssetProcessor> processors = pipelineFactory.resolveProcessorPipeline(asset);
      assertThat(processors).hasSize(0);
   }

   @Test
   public void should_resolve_the_default_js_pipeline() {
      Asset asset = new Asset();
      asset.setType(AssetType.js);
      List<AssetProcessor> processors = pipelineFactory.resolveProcessorPipeline(asset);
      assertThat(processors).hasSize(2);
      assertThat(processors.get(0)).isInstanceOf(JsMinProcessor.class);
      assertThat(processors.get(1)).isInstanceOf(JsSemiColonProcessor.class);
   }

   @Test
   public void should_resolve_a_custom_js_pipeline() {
      Asset asset = new Asset();
      asset.setType(AssetType.js);
      asset.setProcessors(new String[] { new JsMinProcessor().getName() });
      List<AssetProcessor> processors = pipelineFactory.resolveProcessorPipeline(asset);
      assertThat(processors).hasSize(1);
      assertThat(processors.get(0)).isInstanceOf(JsMinProcessor.class);
   }

   @Test
   public void should_resolve_a_custom_js_pipeline_with_an_incompatible_processor() {
      Asset asset = new Asset();
      asset.setType(AssetType.js);
      asset.setProcessors(new String[] { new JsMinProcessor().getName(), new CssMinProcessor().getName() });
      List<AssetProcessor> processors = pipelineFactory.resolveProcessorPipeline(asset);
      assertThat(processors).hasSize(1);
      assertThat(processors.get(0)).isInstanceOf(JsMinProcessor.class);
   }

   @Test
   public void should_resolve_the_default_css_pipeline() {
      Asset asset = new Asset();
      asset.setType(AssetType.css);
      List<AssetProcessor> processors = pipelineFactory.resolveProcessorPipeline(asset);
      assertThat(processors).hasSize(2);
      assertThat(processors.get(0)).isInstanceOf(CssUrlRewritingProcessor.class);
      assertThat(processors.get(1)).isInstanceOf(CssMinProcessor.class);
   }

   @Test
   public void should_resolve_a_custom_css_pipeline() {
      Asset asset = new Asset();
      asset.setProcessors(new String[] { new CssMinProcessor().getName() });
      asset.setType(AssetType.css);
      List<AssetProcessor> processors = pipelineFactory.resolveProcessorPipeline(asset);
      assertThat(processors).hasSize(1);
      assertThat(processors.get(0)).isInstanceOf(CssMinProcessor.class);
   }

   @Test
   public void should_throw_an_exception_when_requesting_an_unexisting_processor() {
      exception.expect(DandelionException.class);
      exception.expectMessage(
            "\"foo\" doesn't exist among the available processors. Please correct the asset \"name\" of the bundle \"bundle\" before continuing.");

      Asset asset = new Asset("name", "whatever", AssetType.css);
      asset.setBundle("bundle");
      asset.setProcessors(new String[] { "foo" });
      pipelineFactory.resolveProcessorPipeline(asset);
   }
}
