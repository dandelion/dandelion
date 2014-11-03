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
package com.github.dandelion.core.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.config.StandardConfigurationLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class BundleStorageInitTest {
	
	private BundleStorage bundleStorage = new BundleStorage();
	private Context context;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		context = new Context(new MockFilterConfig());
	}

	@After
	public void teardown() {
		System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
	}

	@Test
	public void should_substitute_variables_in_locations() {

		Properties variables = new Properties();
		variables.put("SERVER", "https://some.media.server");
		context.getConfiguration().setProperties(variables);
		
		Map<String, String> locations = new HashMap<String, String>();
		locations.put("cdn", "%SERVER%/foo/bar/a.js");
		
		Set<AssetStorageUnit> asus = new HashSet<AssetStorageUnit>();
		asus.add(new AssetStorageUnit("a1", locations));
		
		BundleStorageUnit bsu1 = new BundleStorageUnit();
		bsu1.setName("bundle1");
		bsu1.setAssetStorageUnits(asus);
		
		List<BundleStorageUnit> bsus = new ArrayList<BundleStorageUnit>();
		bsus.add(bsu1);
		
		bundleStorage.finalizeBundleConfiguration(bsus, context);
		
		assertThat(bsus.get(0).getAssetStorageUnits().iterator().next().getLocations().get("cdn")).isEqualTo(
				"https://some.media.server/foo/bar/a.js");
	}
	
	@Test
	public void should_throw_an_exception_if_the_variable_does_not_exist() {
		
		Map<String, String> locations = new HashMap<String, String>();
		locations.put("cdn", "%SERVER%/foo/bar/a.js");
		
		Set<AssetStorageUnit> asus = new HashSet<AssetStorageUnit>();
		asus.add(new AssetStorageUnit("a1", locations));
		
		BundleStorageUnit bsu1 = new BundleStorageUnit();
		bsu1.setName("bundle1");
		bsu1.setAssetStorageUnits(asus);
		
		List<BundleStorageUnit> bsus = new ArrayList<BundleStorageUnit>();
		bsus.add(bsu1);
		
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("The supplied set of variables doesn't contain a variable named \"SERVER\"");
		
		bundleStorage.finalizeBundleConfiguration(bsus, context);
	}
}
