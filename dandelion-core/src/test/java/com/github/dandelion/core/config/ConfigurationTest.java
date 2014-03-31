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

import static org.fest.assertions.Assertions.assertThat;

import java.util.Properties;

import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.DandelionMode;

public class ConfigurationTest {

	@Test
	public void should_load_configuration_from_default() {
		Configuration config = new Configuration(new MockFilterConfig(), new Properties());

		assertThat(config.getDandelionMode()).isEqualTo(DandelionMode.DEVELOPMENT);
		assertThat(config.getAssetLocationsResolutionStrategy()).containsSequence("webjar", "webapp", "cdn");
		assertThat(config.getCacheAssetMaxSize()).isEqualTo(50);
	}

	@Test
	public void should_load_configuration_from_properties() {
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.DANDELION_MODE.getName(), "production");
		userProperties.put(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "foo,bar");
		userProperties.put(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "40");

		Configuration config = new Configuration(new MockFilterConfig(), userProperties);

		assertThat(config.getDandelionMode()).isEqualTo(DandelionMode.PRODUCTION);
		assertThat(config.getAssetLocationsResolutionStrategy()).containsSequence("foo", "bar");
		assertThat(config.getCacheAssetMaxSize()).isEqualTo(40);
	}

	@Test
	public void should_load_configuration_from_initparams() {
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.DANDELION_MODE.getName(), "production");
		userProperties.put(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "foo,bar");
		userProperties.put(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "40");

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.addInitParameter(DandelionConfig.DANDELION_MODE.getName(), "production");
		filterConfig.addInitParameter(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(),
				"foo,bar,baz");
		filterConfig.addInitParameter(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "30");

		Configuration config = new Configuration(filterConfig, userProperties);

		assertThat(config.getDandelionMode()).isEqualTo(DandelionMode.PRODUCTION);
		assertThat(config.getAssetLocationsResolutionStrategy()).containsSequence("foo", "bar", "baz");
		assertThat(config.getCacheAssetMaxSize()).isEqualTo(30);
	}

	@Test
	public void should_load_configuration_from_system() {
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.DANDELION_MODE.getName(), "production");
		userProperties.put(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "foo,bar");
		userProperties.put(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "40");

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.addInitParameter(DandelionConfig.DANDELION_MODE.getName(), "production");
		filterConfig.addInitParameter(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(),
				"foo,bar,baz");
		filterConfig.addInitParameter(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "30");

		System.setProperty(DandelionConfig.DANDELION_MODE.getName(), "production");
		System.setProperty(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "bar,foo,baz,qux");
		System.setProperty(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "20");

		Configuration config = new Configuration(new MockFilterConfig(), userProperties);

		assertThat(config.getDandelionMode()).isEqualTo(DandelionMode.PRODUCTION);
		assertThat(config.getAssetLocationsResolutionStrategy()).containsSequence("bar", "foo", "baz", "qux");
		assertThat(config.getCacheAssetMaxSize()).isEqualTo(20);

		System.clearProperty(DandelionConfig.DANDELION_MODE.getName());
		System.clearProperty(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName());
		System.clearProperty(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName());
	}

	@Test
	public void should_override_default_prod_configuration(){
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.DANDELION_MODE.getName(), "production");
		userProperties.put(DandelionConfig.MINIFICATION.getName(), "false");
		
		Configuration config = new Configuration(new MockFilterConfig(), userProperties);

		assertThat(config.getDandelionMode()).isEqualTo(DandelionMode.PRODUCTION);
		assertThat(config.isMinificationEnabled()).isFalse();
	}
	
	@Test
	public void should_set_mode_to_development_if_unknown_value() {
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.DANDELION_MODE.getName(), "unknown");
		Configuration config = new Configuration(new MockFilterConfig(), userProperties);

		assertThat(config.getDandelionMode()).isEqualTo(DandelionMode.DEVELOPMENT);
	}

	@Test
	public void should_set_default_cacheAssetMaxSize_if_wrong_value() {
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(), "text");
		Configuration config = new Configuration(new MockFilterConfig(), userProperties);

		assertThat(config.getCacheAssetMaxSize()).isEqualTo(50);
	}

	@Test
	public void should_set_default_cacheRequestMaxSize_if_wrong_value() {
		Properties userProperties = new Properties();
		userProperties.put(DandelionConfig.CACHE_REQUEST_MAX_SIZE.getName(), "text");
		Configuration config = new Configuration(new MockFilterConfig(), userProperties);

		assertThat(config.getCacheRequestMaxSize()).isEqualTo(50);
	}
}
