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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.github.dandelion.core.utils.PropertiesUtils;
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

	/**
	 * The properties instance used as a configuration storage.
	 */
	private Properties properties;

	public Configuration() {
		this.properties = new Properties();
		setDefaultConfiguration();
	}

	public String get(DandelionConfig config) {
		String retval = this.properties.getProperty(config.getPropertyName());
		if (StringUtils.isNotBlank(retval)) {
			return retval;
		}
		else {
			return config.getDefaultValue();
		}
	}

	public void put(DandelionConfig config, Object value) {
		this.properties.put(config.getPropertyName(), value);
	}

	public void putDefault(DandelionConfig config) {
		this.properties.put(config.getPropertyName(), config.getDefaultValue());
	}

	public void putAll(Properties properties) {
		this.properties.putAll(properties);
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public boolean isMinificationOn() {
		String retval = get(DandelionConfig.MINIFICATION_ON);
		if (StringUtils.isNotBlank(retval)) {
			return Boolean.parseBoolean(retval);
		}
		return false;
	}

	public void setMinificationOn(boolean value) {
		put(DandelionConfig.MINIFICATION_ON, value);
	}

	public List<String> getAssetLocationsResolutionStrategy() {
		String retval = get(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY);
		if (StringUtils.isNotBlank(retval)) {
			return PropertiesUtils.propertyAsList(retval, ",");
		}
		return Collections.emptyList();
	}

	public void setAssetLocationsResolutionStrategy(String value) {
		put(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY, value);
	}

	public List<String> getAssetProcessors() {
		String value = get(DandelionConfig.ASSET_PROCESSORS);
		if (StringUtils.isNotBlank(value)) {
			return PropertiesUtils.propertyAsList(value, ",");
		}
		return Collections.emptyList();
	}

	public void setAssetProcessors(String... processors) {
		put(DandelionConfig.ASSET_PROCESSORS, processors);
	}

	public String getAssetProcessorEncoding() {
		return get(DandelionConfig.ASSET_PROCESSORS_ENCODING);
	}

	public void setAssetProcessorEncoding(String value) {
		put(DandelionConfig.ASSET_PROCESSORS_ENCODING, value);
	}

	public List<String> getAssetJsExcludes() {
		String value = get(DandelionConfig.ASSET_JS_EXCLUDES);
		if (StringUtils.isNotBlank(value)) {
			return PropertiesUtils.propertyAsList(value, ",");
		}
		return Collections.emptyList();
	}

	public List<String> getAssetCssExcludes() {
		String value = get(DandelionConfig.ASSET_CSS_EXCLUDES);
		if (StringUtils.isNotBlank(value)) {
			return PropertiesUtils.propertyAsList(value, ",");
		}
		return Collections.emptyList();
	}
	
	public String getCacheManagerName() {
		return get(DandelionConfig.CACHE_MANAGER_NAME);
	}

	public String getCacheConfigurationLocation() {
		return get(DandelionConfig.CACHE_CONFIGURATION_LOCATION);
	}

	public int getCacheAssetMaxSize() {
		String value = get(DandelionConfig.CACHE_ASSET_MAX_SIZE);
		if (StringUtils.isNotBlank(value)) {
			return Integer.parseInt(value);
		}
		return Integer.parseInt(DandelionConfig.CACHE_ASSET_MAX_SIZE.getDefaultValue());
	}

	public int getCacheRequestMaxSize() {
		String value = get(DandelionConfig.CACHE_REQUEST_MAX_SIZE);
		if (StringUtils.isNotBlank(value)) {
			return Integer.parseInt(value);
		}
		return Integer.parseInt(DandelionConfig.CACHE_REQUEST_MAX_SIZE.getDefaultValue());
	}

	public List<String> getBundleIncludes() {
		String value = get(DandelionConfig.BUNDLE_INCLUDES);
		if (StringUtils.isNotBlank(value)) {
			return PropertiesUtils.propertyAsList(value, ",");
		}
		return Collections.emptyList();
	}

	public List<String> getBundleExcludes() {
		String value = get(DandelionConfig.BUNDLE_EXCLUDES);
		if (StringUtils.isNotBlank(value)) {
			return PropertiesUtils.propertyAsList(value, ",");
		}
		return Collections.emptyList();
	}

	public void setDefaultConfiguration() {
		putDefault(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY);
		putDefault(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY);
		putDefault(DandelionConfig.ASSET_PROCESSORS);
		putDefault(DandelionConfig.ASSET_PROCESSORS_ENABLED);
		putDefault(DandelionConfig.ASSET_PROCESSORS_ENCODING);
		putDefault(DandelionConfig.ASSET_JS_EXCLUDES);
		putDefault(DandelionConfig.ASSET_CSS_EXCLUDES);
		putDefault(DandelionConfig.CACHE_ASSET_MAX_SIZE);
		putDefault(DandelionConfig.CACHE_REQUEST_MAX_SIZE);
		putDefault(DandelionConfig.CACHE_MANAGER_NAME);
		putDefault(DandelionConfig.CACHE_MANAGER_NAME);
		putDefault(DandelionConfig.CACHE_CONFIGURATION_LOCATION);
		putDefault(DandelionConfig.BUNDLE_INCLUDES);
		putDefault(DandelionConfig.BUNDLE_EXCLUDES);
	}
}