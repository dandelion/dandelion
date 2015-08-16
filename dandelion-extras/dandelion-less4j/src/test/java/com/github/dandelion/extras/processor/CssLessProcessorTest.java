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
package com.github.dandelion.extras.processor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.ProcessingContext;
import com.github.dandelion.core.util.IOUtils;
import com.github.dandelion.core.web.WebConstants;

import static org.assertj.core.api.Assertions.assertThat;

public class CssLessProcessorTest {

   private CssLessProcessor processor = new CssLessProcessor();
   private Writer writer;

   protected ProcessingContext processingContext;
   protected Context context;
   protected MockHttpServletRequest request;

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Before
   public void setup() {
      context = new Context(new MockFilterConfig());

      request = new MockHttpServletRequest();
      request.setContextPath("/context-path");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);

      Asset asset = new Asset();
      asset.setConfigLocation("//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap.css");
      asset.setFinalLocation("/context-path/dandelion-assets/sha1/bootstrap2-2.3.2.css");
      processingContext = new ProcessingContext(context, asset, request);

      writer = new StringWriter();
   }

   @Test
   public void should_compile_basic_expressions() {
      processor.process(new StringReader(".class { width: (1 + 1) }"), writer, null);
      assertThat(writer.toString().replaceAll("\n", "")).isEqualTo(".class {  width: 2;}");
   }

   @Test
   public void should_compile_less_with_variables() throws DandelionException, IOException {
      processor.process(new FileReader("src/test/resources/style1-input.less"), writer, null);
      assertThat(writer.toString()).isEqualTo(IOUtils.toString(new FileReader("src/test/resources/style1-output.css")));
   }

   @Test
   public void should_compile_less_with_mixins() throws DandelionException, IOException {
      processor.process(new FileReader("src/test/resources/style2-input.less"), writer, null);
      assertThat(writer.toString()).isEqualTo(IOUtils.toString(new FileReader("src/test/resources/style2-output.css")));
   }

   @Test
   public void should_compile_less_with_nested_rules() throws DandelionException, IOException {
      processor.process(new FileReader("src/test/resources/style3-input.less"), writer, null);
      assertThat(writer.toString()).isEqualTo(IOUtils.toString(new FileReader("src/test/resources/style3-output.css")));
   }

   @Test
   public void should_compile_less_with_nested_directives() throws DandelionException, IOException {
      processor.process(new FileReader("src/test/resources/style4-input.less"), writer, null);
      assertThat(writer.toString()).isEqualTo(IOUtils.toString(new FileReader("src/test/resources/style4-output.css")));
   }

   @Test
   public void should_compile_less_with_import() throws DandelionException, IOException {

      Asset asset = new Asset();
      asset.setUrl(new File("src/test/resources/style5-input.less").toURI().toURL());

      processingContext = new ProcessingContext(context, asset, request);

      processor.process(new FileReader("src/test/resources/style5-input.less"), writer, processingContext);
      assertThat(writer.toString()).doesNotContain("@import");
      assertThat(writer.toString()).isEqualTo(IOUtils.toString(new FileReader("src/test/resources/style5-output.css")));
   }
}
