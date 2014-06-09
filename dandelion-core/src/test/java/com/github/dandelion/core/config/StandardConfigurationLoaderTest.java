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
package com.github.dandelion.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StandardConfigurationLoaderTest {

	private static StandardConfigurationLoader loader;

	@BeforeClass
	public static void setup() {
		loader = new StandardConfigurationLoader();
	}

	@Before
	public void before() throws Exception {
		System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
	}

	@Test
	public void should_load_properties_from_classpath() throws Exception {
		Properties userProperties = loader.loadUserConfiguration();

		assertThat(userProperties).hasSize(1);
		assertThat(userProperties.getProperty("asset.locations.resolution.strategy")).isEqualTo("webapp,cdn");
	}

	@Test
	public void should_load_user_properties_from_system_property() throws Exception {
		String path = new File("src/test/resources/dandelion-test/configuration-loader/".replace("/", File.separator))
				.getAbsolutePath();
		System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);

		Properties userProperties = loader.loadUserConfiguration();

		assertThat(userProperties).hasSize(1);
		assertThat(userProperties.getProperty("assets.locations")).isEqualTo("other,remote,local");
	}
	
	@AfterClass
	public static void teardown() {
		System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
	}
}
