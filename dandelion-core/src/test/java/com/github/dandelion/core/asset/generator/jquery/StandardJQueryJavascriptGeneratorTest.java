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
package com.github.dandelion.core.asset.generator.jquery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class StandardJQueryJavascriptGeneratorTest {

	private StandardJQueryJavascriptGenerator javascriptGenerator;
	private JQueryAssetBuffer jab;

	@Before
	public void setup() {
		javascriptGenerator = new StandardJQueryJavascriptGenerator();
		jab = new JQueryAssetBuffer();
	}

	@Test
	public void should_not_fill_anything_if_empty() {
		jab.appendToBeforeAll("");

		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeAll()).isNull();
	}

	@Test
	public void should_not_fill_anything_if_null() {
		jab.appendToBeforeAll(null);
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeAll()).isNull();
	}

	@Test
	public void should_fill_the_beforeAll_placeholder() {
		jab.appendToBeforeAll("js code");
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeAll().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeAll().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("js codejs code$(document).ready(function(){});");
	}

	@Test
	public void should_fill_the_beforeStartDocumentReady_placeholder() {
		jab.appendToBeforeStartDocumentReady("js code");
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeStartDocumentReady().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeStartDocumentReady().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("js codejs code$(document).ready(function(){});");
	}

	@Test
	public void should_fill_the_afterStartDocumentReady_placeholder() {
		jab.appendToAfterStartDocumentReady("js code");
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getAfterStartDocumentReady().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getAfterStartDocumentReady().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("$(document).ready(function(){js codejs code});");
	}

	@Test
	public void should_fill_the_componentConf_placeholder() {
		jab.appendToComponentConf("js code");
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getComponentConfiguration().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getComponentConfiguration().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("$(document).ready(function(){js codejs code});");
	}

	@Test
	public void should_fill_the_beforeEndDocumentReady_placeholder() {
		jab.appendToBeforeEndDocumentReady("js code");

		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeEndDocumentReady().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getBeforeEndDocumentReady().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("$(document).ready(function(){js codejs code});");
	}

	@Test
	public void should_fill_the_afterEndDocumentReady_placeholder() {
		jab.appendToAfterEndDocumentReady("js code");

		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getAfterEndDocumentReady().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getAfterEndDocumentReady().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getAfterAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("$(document).ready(function(){});js codejs code");

	}

	@Test
	public void should_fill_the_afterAll_placeholder() {
		jab.appendToAfterAll("js code");

		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getAfterAll().toString()).isEqualTo("js code");
		assertThat(javascriptGenerator.getBeforeAll()).isNull();
		
		javascriptGenerator.fillBuffer(jab);
		assertThat(javascriptGenerator.getAfterAll().toString()).isEqualTo("js codejs code");
		assertThat(javascriptGenerator.getBeforeAll()).isNull();
		
		assertThat(javascriptGenerator.getGeneratedAsset(null)).isEqualTo("$(document).ready(function(){});js codejs code");
	}
}
