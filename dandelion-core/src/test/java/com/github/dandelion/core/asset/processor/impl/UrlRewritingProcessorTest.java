/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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

import static org.fest.assertions.Assertions.assertThat;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.utils.ResourceUtils;

public class UrlRewritingProcessorTest {

	private AssetProcessor assetProcessor = new CssUrlRewritingProcessor();
	private Asset processedAsset;
	private Context context;

	@Before
	public void setup(){
		context = new Context(new MockFilterConfig());
		processedAsset = new Asset();
		processedAsset.setFinalLocation("http://domain/folder/sub/assets/css/asset.css");
	}
	
	@Test
	public void should_not_process_anything() {
		Writer writer = new StringWriter();
		assetProcessor.process(processedAsset, new StringReader(ResourceUtils.getFileContentFromClasspath("locator/test1.css", true)), writer, context);
		assertThat(writer.toString()).contains("images/my-image.png");
	}

	@Test
	public void should_process_one_relative_path_1_lvl() {
		Writer writer = new StringWriter();
		assetProcessor.process(processedAsset, new StringReader(ResourceUtils.getFileContentFromClasspath("locator/test2.css", true)), writer, context);
		assertThat(writer.toString()).contains("http://domain/folder/sub/assets/images/my-image.png");
	}

	@Test
	public void should_process_one_relative_path_2_lvl() {
		Writer writer = new StringWriter();
		assetProcessor.process(processedAsset, new StringReader(ResourceUtils.getFileContentFromClasspath("locator/test3.css", true)), writer, context);
		assertThat(writer.toString()).contains("http://domain/folder/sub/images/my-image.png");
	}

	@Test
	public void should_process_multiple_relative_paths_2_lvl() {
		Writer writer = new StringWriter();
		assetProcessor.process(processedAsset, new StringReader(ResourceUtils.getFileContentFromClasspath("locator/test4.css", true)), writer, context);
		Scanner scanner = new Scanner(writer.toString());
		while (scanner.hasNextLine()) {
			assertThat(scanner.nextLine()).contains("http://domain/folder/sub/assets/images/my-image.png");
		}
	}
}
