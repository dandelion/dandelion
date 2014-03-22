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

import static java.util.Collections.singletonMap;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.web.WebConstants;
import com.github.dandelion.core.storage.AssetStorageUnit;

public class AssetMapperTest {

	private AssetMapper assetMapper;
	private MockHttpServletRequest request;
	private Context context;

	@Before
	public void setup() {
		context = new Context();
		request = new MockHttpServletRequest();
		request.setContextPath("/context");
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
		assetMapper = new AssetMapper(request, context);
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void should_map_an_AssetStorageUnit_to_an_Asset() {

		AssetStorageUnit asu = new AssetStorageUnit();
		asu.setName("asset-name");
		asu.setType(AssetType.js);
		asu.setVersion("1.0.0");
		asu.setLocations(singletonMap("webapp", "/assets/js/asset-name.js"));
		Asset asset = assetMapper.mapToAsset(asu);
		assertThat(asset.getName()).isEqualTo("asset-name");
		assertThat(asset.getType()).isEqualTo(AssetType.js);
		assertThat(asset.getVersion()).isEqualTo("1.0.0");
		assertThat(asset.getConfigLocation()).isEqualTo("/assets/js/asset-name.js");
		assertThat(asset.getConfigLocationKey()).isEqualTo("webapp");
		assertThat(asset.getFinalLocation()).isEqualTo("/context/assets/js/asset-name.js");
	}

	@Test
	public void should_throw_an_exception_when_the_asset_has_no_location() {
		exception.expect(DandelionException.class);
		exception
				.expectMessage("No location is configured for the asset 'asset-name' (js, v1.0.0). Please add at least one location in the corresponding JSON file.");

		AssetStorageUnit asu = new AssetStorageUnit();
		asu.setName("asset-name");
		asu.setType(AssetType.js);
		asu.setVersion("1.0.0");
		assetMapper.mapToAsset(asu);
	}

	@Test
	public void should_throw_an_exception_when_the_asset_has_empty_locations() {
		exception.expect(DandelionException.class);
		exception
				.expectMessage("No location is configured for the asset 'asset-name' (js, v1.0.0). Please add at least one location in the corresponding JSON file.");

		AssetStorageUnit asu = new AssetStorageUnit();
		asu.setName("asset-name");
		asu.setType(AssetType.js);
		asu.setVersion("1.0.0");
		asu.setLocations(Collections.<String, String>emptyMap());
		assetMapper.mapToAsset(asu);
	}

	@Test
	public void should_throw_an_exception_when_the_asset_has_an_unknown_location() {
		exception.expect(DandelionException.class);
		exception.expectMessage("The location key 'foo' is not valid. Please choose a valid one among "
				+ context.getAssetLocatorsMap().keySet() + ".");

		AssetStorageUnit asu = new AssetStorageUnit();
		asu.setName("asset-name");
		asu.setType(AssetType.js);
		asu.setVersion("1.0.0");
		asu.setLocations(singletonMap("foo", "/assets/js/asset-name.js"));
		assetMapper.mapToAsset(asu);
	}

	@Test
	public void should_select_webapp_as_a_first_authorized_location() {

		AssetStorageUnit asu = new AssetStorageUnit();
		asu.setName("asset-name");
		asu.setType(AssetType.js);
		asu.setVersion("1.0.0");

		Map<String, String> locations = new HashMap<String, String>();
		locations.put("cdn", "//asset-name.js");
		locations.put("webapp", "/assets/js/asset-name.js");
		locations.put("classpath", "/foo/bar/asset-name.js");
		asu.setLocations(locations);
		Asset asset = assetMapper.mapToAsset(asu);
		assertThat(asset.getConfigLocation()).isEqualTo("/assets/js/asset-name.js");
		assertThat(asset.getConfigLocationKey()).isEqualTo("webapp");
		assertThat(asset.getFinalLocation()).isEqualTo("/context/assets/js/asset-name.js");
	}

	@Test
	public void should_select_cdn_as_a_second_authorized_location() {

		AssetStorageUnit asu = new AssetStorageUnit();
		asu.setName("asset-name");
		asu.setType(AssetType.js);
		asu.setVersion("1.0.0");

		Map<String, String> locations = new HashMap<String, String>();
		locations.put("classpath", "foo/bar/asset-name.js");
		locations.put("cdn", "//my.domain/asset-name.js");
		asu.setLocations(locations);
		Asset asset = assetMapper.mapToAsset(asu);
		assertThat(asset.getConfigLocation()).isEqualTo("//my.domain/asset-name.js");
		assertThat(asset.getConfigLocationKey()).isEqualTo("cdn");
		assertThat(asset.getFinalLocation()).isEqualTo("http://my.domain/asset-name.js");
	}
}
