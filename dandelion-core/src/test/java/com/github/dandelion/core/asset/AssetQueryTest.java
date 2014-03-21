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
package com.github.dandelion.core.asset;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.web.AssetFilter;
import com.github.dandelion.core.asset.web.AssetRequestContext;
import com.github.dandelion.core.config.StandardConfigurationLoader;

public class AssetQueryTest {

	private MockHttpServletRequest request;
	private Context context;

	@Before
	public void setup() {
		System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
		String path = new File("src/test/resources/dandelion-test/asset-query/".replace("/", File.separator))
		.getAbsolutePath();
		System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);
		context = new Context();
		
		request = new MockHttpServletRequest();
		request.setContextPath("/context");
		request.setAttribute(AssetFilter.DANDELION_CONTEXT_ATTRIBUTE, context);

	}

	@Test
	public void should_return_1_processed_asset() {

		AssetRequestContext.get(request).addBundles("bundle4");
		Set<Asset> assets = new AssetQuery(request, context).perform();
		assertThat(assets).hasSize(1);
		Asset asset = assets.iterator().next();
		assertThat(context.getCacheManager().getContent(asset.getCacheKey())).isEqualTo("\nvar v={};");
	}
	
	@Test
	public void should_return_only_head_assets() {

		AssetRequestContext.get(request).addBundles("bundle5");
		Set<Asset> assets = new AssetQuery(request, context).withPosition(AssetDomPosition.head).perform();
		assertThat(assets).onProperty("name").contains("a5_4", "a5_5");
	}	
	
	@Test
	public void should_return_only_body_assets() {

		AssetRequestContext.get(request).addBundles("bundle5");
		Set<Asset> assets = new AssetQuery(request, context).withPosition(AssetDomPosition.body).perform();
		assertThat(assets).onProperty("name").contains("a5_1", "a5_2", "a5_3");
	}
	
	@Test
	public void should_exclude_1_asset() {

		AssetRequestContext.get(request).addBundles("bundle5").excludeAssets("a5_1");
		Set<Asset> assets = new AssetQuery(request, context).withPosition(AssetDomPosition.body).perform();
		assertThat(assets).onProperty("name").contains("a5_2", "a5_3");
	}
}
