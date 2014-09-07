/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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
package com.github.dandelion.core.asset.locator.impl;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.web.WebConstants;

public class CdnLocatorTest {

	private CdnLocator locator = new CdnLocator();
	private String absolute_url = "http://domain/folder/sub/assets/css/my.css";
	private String protocol_relative_url = "//domain/folder/sub/assets/css/my.css";
	private MockHttpServletRequest request;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		request = new MockHttpServletRequest();
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, new Context(new MockFilterConfig()));
	}

	@Test
	public void should_return_the_same_absolute_url() {
		AssetStorageUnit asu = new AssetStorageUnit("my.js", singletonMap("cdn", absolute_url));
		String location = locator.getLocation(asu, request);
		assertThat(location).isEqualTo(absolute_url);
	}

	@Test
	public void should_return_the_same_protocol_relative_url() {
		AssetStorageUnit asu = new AssetStorageUnit("my.js", singletonMap("cdn", protocol_relative_url));
		String location = locator.getLocation(asu, request);
		assertThat(location).isEqualTo(protocol_relative_url);
	}

	@Test
	public void should_return_the_content() {
		String filePath = new File("src/test/resources/locator/asset.js").toURI().toString();
		AssetStorageUnit asu = new AssetStorageUnit("my.js", singletonMap("cdn", filePath));
		String content = locator.getContent(asu, request);
		assertThat(content).isEqualTo("/* content */");
	}
}
