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
package com.github.dandelion.core.asset.processor.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Rule;
import org.junit.Test;

import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.processor.AssetProcessor;
import com.github.dandelion.core.util.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class CssUrlRewritingProcessorTest extends AbstractProcessorTest {

   private AssetProcessor assetProcessor = new CssUrlRewritingProcessor();

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Test
   public void should_rewrite_image_url_in_css() {
      Writer writer = new StringWriter();
      String oldCss = ResourceUtils.getFileContentFromClasspath("processor/css-url-rewriter/source.css");
      String newCss = ResourceUtils.getFileContentFromClasspath("processor/css-url-rewriter/result.css");
      assetProcessor.process(new StringReader(oldCss), writer, processingContext);
      assertThat(writer.toString().replaceAll("\n", "").replaceAll("\r", "")).isEqualTo(
            newCss.replaceAll("\n", "").replaceAll("\r", ""));
   }
}
