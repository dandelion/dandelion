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

/**
 * <p>
 * Enum containing all configuration properties and their associated default
 * value.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public enum DandelionConfig {

	// Main configurations
	MINIFICATION_ON("minification.on", "false"),
	
	// Asset configurations
	ASSET_LOCATIONS_RESOLUTION_STRATEGY("asset.locations.resolution.strategy", "webjar,webapp,cdn"),
	ASSET_PROCESSORS_ENABLED("asset.processors.enabled", "false"),
	ASSET_PROCESSORS("asset.processors", "cssurlrewriting,jsmin,cssmin"),
	ASSET_PROCESSORS_ENCODING("asset.processors.encoding", "UTF-8"),
	ASSET_JS_EXCLUDES("asset.js.excludes", ""),
	ASSET_CSS_EXCLUDES("asset.css.excludes", ""),
	
	// Cache configurations
	CACHE_ASSET_MAX_SIZE("cache.asset.max.size", "50"),
	CACHE_REQUEST_MAX_SIZE("cache.request.max.size", "50"),
	CACHE_MANAGER_NAME("cache.manager.name", ""),
	CACHE_CONFIGURATION_LOCATION("cache.configuration.location", ""),
	
	// Bundle configurations
	BUNDLE_INCLUDES("bundle.includes", ""),
	BUNDLE_EXCLUDES("bundle.excludes", "");
	
	private String propertyName;
	private String defaultValue;
	
	private DandelionConfig(String propertyName, String defaultValue){
		this.propertyName = propertyName;
		this.defaultValue = defaultValue;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
