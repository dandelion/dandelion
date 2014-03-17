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

import java.util.Properties;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * Entry point for all Dandelion configuration properties.
 * 
 * <p>
 * The configuration is loaded only once using the configured instance of
 * {@link ConfigurationLoader}.
 * 
 * <p>
 * Custom configuration properties can still be accessed using the
 * {@link #get(String)} method.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class Configuration {

	private static Properties configuration;

	public static final String DEFAULT_ASSET_LOCATION_STRATEGY = "webjar,webapp,cdn";
	public static final String DEFAULT_ASSET_PROCESSORS = "";
	public static final boolean DEFAULT_ASSET_PROCESSORS_ENABLED = false;
	public static final String DEFAULT_ASSET_PROCESSORS_ENCODING = "UTF-8";
	public static final String DEFAULT_ASSET_EXCLUDES = "";
	public static final String DEFAULT_ASSET_CACHE_MANAGER_NAME = "";
	public static final String DEFAULT_ASSET_CACHE_CONFIGURATION_LOCATION = "";
	public static final String DEFAULT_BUNDLE_INCLUDES = "";
	public static final String DEFAULT_BUNDLE_EXCLUDES = "";

	public static final String PROP_ASSET_LOCATION_STRATEGY = "asset.locations.encoding";
	public static final String PROP_ASSET_PROCESSORS_ENABLED = "asset.processors.enabled";
	public static final String PROP_ASSET_PROCESSORS = "asset.processors";
	public static final String PROP_ASSET_PROCESSORS_ENCODING = "asset.processors.encoding";
	public static final String PROP_ASSET_EXCLUDES = "asset.excludes";
	public static final String PROP_ASSET_CACHE_MANAGER_NAME = "asset.cache.manager";
	public static final String PROP_ASSET_CACHE_CONFIGURATION_LOCATION = "asset.cache.configuration.location";
	public static final String PROP_BUNDLE_INCLUDES = "bundle.includes";
	public static final String PROP_BUNDLE_EXCLUDES = "bundle.excludes";

	private static String assetLocationStrategy = DEFAULT_ASSET_LOCATION_STRATEGY;
	private static String assetProcessors = DEFAULT_ASSET_PROCESSORS;
	private static boolean assetProcessorsEnabled = DEFAULT_ASSET_PROCESSORS_ENABLED;
	private static String assetProcessorEncoding = DEFAULT_ASSET_PROCESSORS_ENCODING;
	private static String assetExcludes = DEFAULT_ASSET_EXCLUDES;
	private static String assetCacheManagerName = DEFAULT_ASSET_CACHE_MANAGER_NAME;
	private static String assetCacheConfigurationLocation = DEFAULT_ASSET_CACHE_CONFIGURATION_LOCATION;
	private static String bundleIncludes = DEFAULT_BUNDLE_INCLUDES;
	private static String bundleExcludes = DEFAULT_BUNDLE_EXCLUDES;

	public static void initializeIfNeeded() {
		if (configuration == null || DevMode.isEnabled()) {
			loadConfiguration();
		}
	}

	public static String get(String key) {
		initializeIfNeeded();
		return configuration.getProperty(key);
	}

	public static String get(String key, String defaultValue) {
		initializeIfNeeded();
		return configuration.getProperty(key, defaultValue);
	}

	/**
	 * <p>
	 * Load the Dandelion configuration using the following strategy:
	 * <ul>
	 * <li>All default properties files are loaded (dandelion, webanalytics,
	 * ...)</li>
	 * <li>If it exists, the user properties are loaded using the bundle
	 * mechanism and override the default configuration</li>
	 * </ul>
	 */
	private static synchronized void loadConfiguration() {
		if (configuration == null) {

			ConfigurationLoader confLoader = DandelionConfigurator.getConfigurationLoader();
			Properties properties = new Properties();
			properties.putAll(confLoader.loadDefaultConfiguration());
			properties.putAll(confLoader.loadUserConfiguration());
			configuration = properties;
		}
	}

	public static String getAssetLocationStrategy() {
		String value = get(PROP_ASSET_LOCATION_STRATEGY);
		if (StringUtils.isNotBlank(value)) {
			assetLocationStrategy = value;
		}
		return assetLocationStrategy;
	}
	
	public static boolean isAssetProcessorsEnabled() {
		return assetProcessorsEnabled;
	}

	public static String getAssetProcessors() {
		String value = get(PROP_ASSET_PROCESSORS);
		if (StringUtils.isNotBlank(value)) {
			assetProcessors = value;
		}
		return assetProcessors;
	}

	public static String getAssetProcessorEncoding() {
		String value = get(PROP_ASSET_PROCESSORS_ENCODING);
		if (StringUtils.isNotBlank(value)) {
			assetProcessorEncoding = value;
		}
		return assetProcessorEncoding;
	}

	public static String getAssetExcludes() {
		String value = get(PROP_ASSET_EXCLUDES);
		if (StringUtils.isNotBlank(value)) {
			assetExcludes = value;
		}
		return assetExcludes;
	}

	public static String getAssetCacheManagerName() {
		String value = get(PROP_ASSET_CACHE_MANAGER_NAME);
		if (StringUtils.isNotBlank(value)) {
			assetCacheManagerName = value;
		}
		return assetCacheManagerName;
	}

	public static String getAssetCacheConfigurationLocation() {
		String value = get(PROP_ASSET_CACHE_CONFIGURATION_LOCATION);
		if (StringUtils.isNotBlank(value)) {
			assetCacheConfigurationLocation = value;
		}
		return assetCacheConfigurationLocation;
	}

	public static String getBundleIncludes() {
		String value = get(PROP_BUNDLE_INCLUDES);
		if (StringUtils.isNotBlank(value)) {
			bundleIncludes = value;
		}
		return bundleIncludes;
	}

	public static String getBundleExcludes() {
		String value = get(PROP_BUNDLE_EXCLUDES);
		if (StringUtils.isNotBlank(value)) {
			bundleExcludes = value;
		}
		return bundleExcludes;
	}
}