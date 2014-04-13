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

package com.github.dandelion.core.asset.locator.impl;

import static java.util.Collections.singletonMap;
import static org.fest.assertions.Assertions.assertThat;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.WebConstants;

public class DelegatedLocatorTest {

	private DelegateLocator locator = new DelegateLocator();
	private MockHttpServletRequest request;

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		request = new MockHttpServletRequest();
		request.setRequestURI("/context/page.html");
		request.setContextPath("/context");
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, new Context(new MockFilterConfig()));
	}

	@Test
	public void should_throw_an_exception_when_using_an_empty_location(){
		exception.expect(DandelionException.class);
		exception
				.expectMessage("The asset 'my.js' (js, v1.0.0) configured with a 'delegate' location key has a blank location. Please correct this location in the corresponding JSON file.");
		
		AssetStorageUnit asu = new AssetStorageUnit("my.js", "1.0.0", AssetType.js, singletonMap("delegate", ""));
		locator.getLocation(asu, null);
	}
	
	@Test
	public void should_return_the_same_absolute_url() {
		AssetStorageUnit asu = new AssetStorageUnit("my-js", singletonMap("delegate", "my.js"));
		String location = locator.getLocation(asu, request);
		assertThat(location).isNull();
	}

	@Test
	public void should_return_the_content() {

		AssetRequestContext.get(request).addParameter("my-js", DelegateLocator.DELEGATED_CONTENT_PARAM,
				new DelegatedContent() {
					@Override
					public String getContent(HttpServletRequest request) {
						return "/* delegated content */";
					}
				});

		AssetStorageUnit asset = new AssetStorageUnit("my-js", "1.0", AssetType.js, singletonMap(
				locator.getLocationKey(), "my.js"));
		String content = locator.getContent(asset, request);
		assertThat(content).isEqualTo("/* delegated content */");
	}
}
