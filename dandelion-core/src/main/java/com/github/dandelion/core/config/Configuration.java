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

import java.util.List;
import java.util.Properties;

import javax.servlet.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionMode;
import com.github.dandelion.core.utils.PropertiesUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * This class holds the Dandelion configuration initialized at server startup
 * and must be accessed through the Dandelion {@link Context}.
 * 
 * <p>
 * All configuration present in the {@link DandelionConfig} enum are read using
 * a particular strategy. See {@link #readConfig(DandelionConfig)}.
 * 
 * <p>
 * There should be only one instance of this class in the application.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class Configuration {

	private static Logger LOG = LoggerFactory.getLogger(Configuration.class);

	private FilterConfig filterConfig;
	private Properties userProperties;
	private boolean servlet3Enabled;
	private boolean jmxEnabled;

	private DandelionMode dandelionMode;
	private boolean minificationEnabled;
	private List<String> assetLocationsResolutionStrategy;
	private List<String> assetProcessors;
	private String assetProcessorEncoding;
	private List<String> assetJsExcludes;
	private List<String> assetCssExcludes;
	private String cacheName;
	private int cacheAssetMaxSize;
	private int cacheRequestMaxSize;
	private String cacheManagerName;
	private String cacheConfigurationLocation;
	private List<String> bundleIncludes;
	private List<String> bundleExcludes;

	public Configuration(FilterConfig filterConfig, Properties userProperties) {
		this.filterConfig = filterConfig;
		this.userProperties = userProperties;

		// Dandelion mode
		try {
			this.dandelionMode = DandelionMode.valueOf(readConfig(DandelionConfig.DANDELION_MODE).toUpperCase());
		}
		catch (IllegalArgumentException e) {
			LOG.warn("The '{}' property is incorrectly configured. Falling back to the default value ({})",
					DandelionConfig.DANDELION_MODE.getName(), DandelionConfig.DANDELION_MODE.getDefaultDevValue());
			this.dandelionMode = DandelionMode.DEVELOPMENT;
		}
		if (dandelionMode.equals(DandelionMode.DEVELOPMENT)) {
			LOG.info("===========================================");
			LOG.info("");
			LOG.info("Dandelion development mode enabled.");
			LOG.info("");
			LOG.info("===========================================");
		}

		// Main properties
		this.minificationEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.MINIFICATION));
		this.jmxEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.JMX_ENABLED));

		// Bundles-related properties
		this.bundleIncludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.BUNDLE_INCLUDES), ",");
		this.bundleExcludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.BUNDLE_EXCLUDES), ",");

		// Assets-related properties
		this.assetLocationsResolutionStrategy = PropertiesUtils.propertyAsList(
				readConfig(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY), ",");
		this.assetProcessors = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.ASSET_PROCESSORS), ",");
		this.assetProcessorEncoding = readConfig(DandelionConfig.ASSET_PROCESSORS_ENCODING);
		this.assetJsExcludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.ASSET_JS_EXCLUDES), ",");
		this.assetCssExcludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.ASSET_CSS_EXCLUDES), ",");

		// Caching-related properties
		this.cacheName = readConfig(DandelionConfig.CACHE_NAME);
		try {
			this.cacheAssetMaxSize = Integer.parseInt(readConfig(DandelionConfig.CACHE_ASSET_MAX_SIZE));
		}
		catch (NumberFormatException e) {
			LOG.warn("The '{}' property is incorrectly configured. Falling back to the default value ({})",
					DandelionConfig.CACHE_ASSET_MAX_SIZE.getName(),
					DandelionConfig.CACHE_ASSET_MAX_SIZE.getDefaultDevValue());
			this.cacheAssetMaxSize = Integer.parseInt(DandelionConfig.CACHE_ASSET_MAX_SIZE.getDefaultDevValue());
		}
		try {
			this.cacheRequestMaxSize = Integer.parseInt(readConfig(DandelionConfig.CACHE_REQUEST_MAX_SIZE));
		}
		catch (NumberFormatException e) {
			LOG.warn("The '{}' property is incorrectly configured. Falling back to the default value ({})",
					DandelionConfig.CACHE_REQUEST_MAX_SIZE.getName(),
					DandelionConfig.CACHE_REQUEST_MAX_SIZE.getDefaultDevValue());
			this.cacheRequestMaxSize = Integer.parseInt(DandelionConfig.CACHE_REQUEST_MAX_SIZE.getDefaultDevValue());
		}
		this.cacheManagerName = readConfig(DandelionConfig.CACHE_MANAGER_NAME);
		this.cacheConfigurationLocation = readConfig(DandelionConfig.CACHE_CONFIGURATION_LOCATION);

		// Configure Servlet3 flag
		String overrideServlet3 = readConfig(DandelionConfig.OVERRIDE_SERVLET3);
		if (StringUtils.isBlank(overrideServlet3) && filterConfig != null) {
			this.servlet3Enabled = filterConfig.getServletContext().getMajorVersion() == 3;
		}
		else {
			this.servlet3Enabled = Boolean.parseBoolean(overrideServlet3);
		}
	}

	public DandelionMode getDandelionMode() {
		return dandelionMode;
	}

	public boolean isMinificationEnabled() {
		return minificationEnabled;
	}

	public List<String> getAssetLocationsResolutionStrategy() {
		return assetLocationsResolutionStrategy;
	}

	public List<String> getAssetProcessors() {
		return assetProcessors;
	}

	public String getAssetProcessorEncoding() {
		return assetProcessorEncoding;
	}

	public List<String> getAssetJsExcludes() {
		return assetJsExcludes;
	}

	public List<String> getAssetCssExcludes() {
		return assetCssExcludes;
	}

	public int getCacheAssetMaxSize() {
		return cacheAssetMaxSize;
	}

	public int getCacheRequestMaxSize() {
		return cacheRequestMaxSize;
	}

	public String getCacheManagerName() {
		return cacheManagerName;
	}

	public String getCacheConfigurationLocation() {
		return cacheConfigurationLocation;
	}

	public List<String> getBundleIncludes() {
		return bundleIncludes;
	}

	public List<String> getBundleExcludes() {
		return bundleExcludes;
	}

	public Properties getProperties() {
		return userProperties;
	}

	public void setProperties(Properties properties) {
		this.userProperties = properties;
	}

	public String get(String key, String defaultValue) {
		return userProperties.getProperty(key, defaultValue);
	}

	public boolean isServlet3Enabled() {
		return servlet3Enabled;
	}

	public void setServlet3Enabled(boolean servlet3Enabled) {
		this.servlet3Enabled = servlet3Enabled;
	}

	public boolean isJmxEnabled() {
		return jmxEnabled;
	}

	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	/**
	 * <p>
	 * Reads the given {@link DandelionConfig} in order of priority:
	 * <ol>
	 * <li>System properties have the highest precedence and thus override
	 * everything else</li>
	 * <li>Then the filter initialization parameters, coming from the
	 * {@code web.xml} file</li>
	 * <li>Then the user-defined properties, coming from the
	 * {@code dandelion.properties} file, if it exists</li>
	 * <li>Finally the default "dev" value of the given {@link DandelionConfig}
	 * if the {@link DandelionMode#DEVELOPMENT} is enabled, otherwise the
	 * default "prod" value.</li>
	 * </ol>
	 * 
	 * @param config
	 *            The config to read.
	 * @return the value of the given {@link DandelionConfig}.
	 */
	public String readConfig(DandelionConfig config) {

		String retval = null;
		if (System.getProperty(config.getName()) != null) {
			retval = System.getProperty(config.getName());
		}

		if (retval == null && filterConfig != null) {
			retval = filterConfig.getInitParameter(config.getName());
		}

		if (retval == null && userProperties != null) {
			retval = userProperties.getProperty(config.getName());
		}

		if (retval == null) {
			if (dandelionMode != null && dandelionMode.equals(DandelionMode.PRODUCTION)) {
				retval = config.getDefaultProdValue();
			}
			else {
				retval = config.getDefaultDevValue();
			}
		}

		return retval.trim();
	}

	public void setDandelionMode(DandelionMode dandelionMode) {
		this.dandelionMode = dandelionMode;
	}

	public void setMinificationEnabled(boolean minificationEnabled) {
		this.minificationEnabled = minificationEnabled;
	}

	public void setAssetLocationsResolutionStrategy(List<String> assetLocationsResolutionStrategy) {
		this.assetLocationsResolutionStrategy = assetLocationsResolutionStrategy;
	}

	public void setAssetProcessors(List<String> assetProcessors) {
		this.assetProcessors = assetProcessors;
	}

	public void setAssetProcessorEncoding(String assetProcessorEncoding) {
		this.assetProcessorEncoding = assetProcessorEncoding;
	}

	public void setAssetJsExcludes(List<String> assetJsExcludes) {
		this.assetJsExcludes = assetJsExcludes;
	}

	public void setAssetCssExcludes(List<String> assetCssExcludes) {
		this.assetCssExcludes = assetCssExcludes;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public void setCacheAssetMaxSize(int cacheAssetMaxSize) {
		this.cacheAssetMaxSize = cacheAssetMaxSize;
	}

	public void setCacheRequestMaxSize(int cacheRequestMaxSize) {
		this.cacheRequestMaxSize = cacheRequestMaxSize;
	}

	public void setCacheManagerName(String cacheManagerName) {
		this.cacheManagerName = cacheManagerName;
	}

	public void setCacheConfigurationLocation(String cacheConfigurationLocation) {
		this.cacheConfigurationLocation = cacheConfigurationLocation;
	}

	public void setBundleIncludes(List<String> bundleIncludes) {
		this.bundleIncludes = bundleIncludes;
	}

	public void setBundleExcludes(List<String> bundleExcludes) {
		this.bundleExcludes = bundleExcludes;
	}
}