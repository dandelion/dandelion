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
package com.github.dandelion.core.web.handler.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Debug page focused on global options.
 * </p>
 * <p>
 * This page displays all options currently enabled in the application.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public class OptionsDebugPage extends AbstractDebugPage {

	public static final String PAGE_ID = "options";
	public static final String PAGE_NAME = "Options";
	private static final String PAGE_LOCATION = "META-INF/resources/ddl-debugger/html/core-options.html";

	@Override
	public String getId() {
		return PAGE_ID;
	}

	@Override
	public String getName() {
		return PAGE_NAME;
	}

	@Override
	public String getTemplate(HandlerContext context) throws IOException {
		return ResourceUtils.getContentFromInputStream(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(PAGE_LOCATION));
	}

	@Override
	public Map<String, String> getCustomParameters(HandlerContext context) {

		Configuration conf = context.getContext().getConfiguration();
		Map<String, String> params = new HashMap<String, String>();
		StringBuilder table = new StringBuilder("<table class='table table-striped table-hover table-bordered' style='margin-top:15px;'><thead>");
		table.append("<tr><th>Option</th><th>Active value</th></tr></thead><tbody>");

		table.append(tr("Active profile", conf.getActiveProfile()));
		
		// Asset-related options
		table.append("<tr class='header-tr'><td colspan='3'>Asset-related options</td></tr>");
		table.append(tr(DandelionConfig.ASSET_MINIFICATION, conf.isAssetMinificationEnabled()));
		table.append(tr(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY, conf.getAssetLocationsResolutionStrategy()));
		table.append(tr(DandelionConfig.ASSET_PROCESSORS, conf.getAssetProcessors()));
		table.append(tr(DandelionConfig.ASSET_JS_EXCLUDES, conf.getAssetJsExcludes()));
		table.append(tr(DandelionConfig.ASSET_CSS_EXCLUDES, conf.getAssetCssExcludes()));
		table.append(tr(DandelionConfig.ASSET_URL_PATTERN, conf.getAssetUrlPattern()));
		table.append(tr(DandelionConfig.ASSET_STORAGE, conf.getAssetStorage()));

		// Versioning-related options
		table.append("<tr class='header-tr'><td colspan='3'>Versioning-related options</td></tr>");
		table.append(tr(DandelionConfig.ASSET_VERSIONING_MODE, conf.getAssetVersioningMode()));
		table.append(tr(DandelionConfig.ASSET_VERSIONING_STRATEGY, conf.getAssetVersioningStrategy()));
		table.append(tr(DandelionConfig.ASSET_FIXED_VERSION_TYPE, conf.getAssetFixedVersionType()));
		table.append(tr(DandelionConfig.ASSET_FIXED_VERSION_VALUE, conf.getAssetFixedVersionValue()));
		table.append(tr(DandelionConfig.ASSET_FIXED_VERSION_DATEPATTERN, conf.getAssetFixedVersionDatePattern()));
		table.append(tr(DandelionConfig.ASSET_FIXED_VERSION_TYPE, conf.getAssetFixedVersionType()));

		// Caching-related options
		table.append("<tr class='header-tr'><td colspan='3'>Caching-related options</td></tr>");
		table.append(tr(DandelionConfig.CACHE, conf.isCachingEnabled()));
		table.append(tr(DandelionConfig.CACHE_NAME, conf.getCacheName()));
		table.append(tr(DandelionConfig.CACHE_MAX_SIZE, conf.getCacheMaxSize()));
		table.append(tr(DandelionConfig.CACHE_MANAGER_NAME, conf.getCacheManagerName()));
		table.append(tr(DandelionConfig.CACHE_CONFIGURATION_LOCATION, conf.getCacheConfigurationLocation()));

		// Bundle-related options
		table.append("<tr class='header-tr'><td colspan='3'>Bundle-related options</td></tr>");
		table.append(tr(DandelionConfig.BUNDLE_LOCATION, conf.getBundleLocation()));
		table.append(tr(DandelionConfig.BUNDLE_INCLUDES, conf.getBundleIncludes()));
		table.append(tr(DandelionConfig.BUNDLE_EXCLUDES, conf.getBundleExcludes()));

		// Tooling-related options
		table.append("<tr class='header-tr'><td colspan='3'>Tooling-related options</td></tr>");
		table.append(tr(DandelionConfig.TOOL_GZIP, conf.isToolGzipEnabled()));
		table.append(tr(DandelionConfig.TOOL_GZIP_MIME_TYPES, conf.getToolGzipMimeTypes()));
		table.append(tr(DandelionConfig.TOOL_DEBUGGER, conf.isToolDebuggerEnabled()));
		table.append(tr(DandelionConfig.TOOL_BUNDLE_RELOADING, conf.isToolBundleReloadingEnabled()));
		
		// Monitoring-related options
		table.append(tr(DandelionConfig.MONITORING_JMX, conf.isMonitoringJmxEnabled()));

		// Misc options
		table.append("<tr class='header-tr'><td colspan='3'>Misc options</td></tr>");
		table.append(tr(DandelionConfig.ENCODING, conf.getEncoding()));

		table.append("</tbody></table>");
		params.put("%TABLE_OPTIONS%", table.toString());
		return params;
	}
}
