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
package com.github.dandelion.core.bundle.loader.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.bundle.loader.strategy.LoadingStrategy;
import com.github.dandelion.core.bundle.loader.strategy.XmlBundleLoadingStrategy;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlBundleLoadingStrategyTest {

	private LoadingStrategy loadingStrategy = new XmlBundleLoadingStrategy();

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void should_return_empty_set() {
		Set<String> resourcePaths = loadingStrategy.getResourcePaths("unkown-folder", null);
		assertThat(resourcePaths).isEmpty();
	}

	@Test
	public void should_find_xml_resource_paths() {
		Set<String> resourcePaths = loadingStrategy.getResourcePaths("bundle-loading/xml/xml-strategy", null);
		assertThat(resourcePaths).hasSize(4);
	}

	@Test
	public void should_map_to_bundles_using_resource_paths() {
		List<BundleStorageUnit> bundles = loadingStrategy.mapToBundles(new HashSet<String>(Arrays
				.asList("bundle-loading/xml/xml-strategy/bundle2.xml")));

		BundleStorageUnit bundle = bundles.get(0);
		System.out.println(bundle);
		assertThat(bundle.getName()).isEqualTo("bundle2");
		assertThat(bundle.getDependencies()).isNull();
		assertThat(bundle.getAssetStorageUnitNames()).hasSize(1);
		assertThat(bundle.getAssetStorageUnits()).hasSize(1);

		AssetStorageUnit asu = bundle.getAssetStorageUnits().iterator().next();
		assertThat(asu.getLocations()).hasSize(1);
		assertThat(asu.getLocations().get("cdn")).isEqualTo("//domain.com/js/asset2_1.js");
	}

	@Test
	public void should_throw_an_exception_when_the_bundle_is_badly_formatted() {

		expectedEx.expect(DandelionException.class);
		expectedEx.expectMessage(CoreMatchers
				.containsString("Invalid content was found starting with element 'unknown'."));
		loadingStrategy.mapToBundles(new HashSet<String>(Arrays
				.asList("bundle-loading/xml/xml-strategy/wrong-bundle.xml")));
	}
}
