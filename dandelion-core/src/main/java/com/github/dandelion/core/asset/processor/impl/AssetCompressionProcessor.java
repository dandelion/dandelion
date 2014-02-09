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
package com.github.dandelion.core.asset.processor.impl;

import static com.github.dandelion.core.asset.web.AssetServlet.DANDELION_ASSETS_URL;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.cache.AssetCacheSystem;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.RequestUtils;
import com.github.dandelion.core.utils.ResourceUtils;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * <p>
 * Processor entry in charge of compressing all assets present in the
 * {@link AssetStack}.
 * 
 * <p>
 * This processor entry is based on YUI Compressor.
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class AssetCompressionProcessor extends AssetProcessor {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(AssetCompressionProcessor.class);

	public static final String COMPRESSION = "compression";
	public static final String COMPRESSION_ENABLED_KEY = "dandelion.compression.enabled";
	public static final String COMPRESSION_JS_MUNGE = "dandelion.compression.js.munge";
	public static final String COMPRESSION_JS_PRESERVE_SEMICOLONS = "dandelion.compression.js.preserveSemiColons";
	public static final String COMPRESSION_JS_DISABLE_OPTIMIZATIONS = "dandelion.compression.js.disableOptimizations";

	private boolean compressionEnabled = false;
	private boolean jsMunge = true;
	private boolean jsPreserveSemiColons = true;
	private boolean jsDisableOptimizations = true;

	public AssetCompressionProcessor() {
		this.compressionEnabled = Boolean.TRUE.toString().equals(
				Configuration.getProperty(COMPRESSION_ENABLED_KEY, Boolean.toString(compressionEnabled)));
		this.jsMunge = Boolean.TRUE.toString().equals(
				Configuration.getProperty(COMPRESSION_JS_MUNGE, Boolean.toString(compressionEnabled)));
		this.jsPreserveSemiColons = Boolean.TRUE.toString().equals(
				Configuration.getProperty(COMPRESSION_JS_PRESERVE_SEMICOLONS, Boolean.toString(compressionEnabled)));
		this.jsDisableOptimizations = Boolean.TRUE.toString().equals(
				Configuration.getProperty(COMPRESSION_JS_DISABLE_OPTIMIZATIONS, Boolean.toString(compressionEnabled)));

		LOG.info("Dandelion Asset Compression is {}", compressionEnabled ? "enabled" : "disabled");
		if (compressionEnabled) {
			LOG.debug("Dandelion Asset Compression JS munge is {}", jsMunge ? "enabled" : "disabled");
			LOG.debug("Dandelion Asset Compression JS preserve semicolons is {}", jsPreserveSemiColons ? "enabled"
					: "disabled");
			LOG.debug("Dandelion Asset Compression JS disable optimizations is {}", jsDisableOptimizations ? "enabled"
					: "disabled");
		}
	}

	@Override
	public String getProcessorKey() {
		return COMPRESSION;
	}

	@Override
	public int getRank() {
		return 2000;
	}

	@Override
	public List<Asset> process(List<Asset> assets, HttpServletRequest request) {
		if (!compressionEnabled) {
			return assets;
		}

		String context = RequestUtils.getCurrentUrl(request, true);
		context = context.replaceAll("\\?", "_").replaceAll("&", "_");

		String baseUrl = RequestUtils.getBaseUrl(request);
		List<Asset> compressedAssets = new ArrayList<Asset>();
		for (Asset asset : assets) {
			for (String location : asset.getLocations().values()) {
				String cacheKey = AssetCacheSystem.generateCacheKey(context, location, COMPRESSION, asset.getType());

				// Updates the cache in order for the compressed content to be
				// retrieved by the servlet
				LOG.debug("Cache updated with compressed assets (key={})", asset.getAssetKey());
				cacheCompressedContent(request, context, location, asset, cacheKey);

				String accessLocation = baseUrl + DANDELION_ASSETS_URL + cacheKey;

				Map<String, String> locations = new HashMap<String, String>();
				locations.put(COMPRESSION, accessLocation);

				compressedAssets.add(new Asset(cacheKey, COMPRESSION, asset.getType(), locations));
				LOG.debug("New asset created with name {}, version {}, type {}, locations [{}={}]", cacheKey,
						COMPRESSION, asset.getType(), COMPRESSION, accessLocation);
			}
		}
		return compressedAssets;
	}

	private void cacheCompressedContent(HttpServletRequest request, String context, String location, Asset asset,
			String cacheKey) {
		String content = compress(asset, request);
		AssetCacheSystem.storeContent(context, location, COMPRESSION, asset.getType(), content);
	}

	private String compress(Asset asset, HttpServletRequest request) {
		String content = extractContent(asset, request);
		switch (asset.getType()) {
		case css:
			LOG.debug("CSS compression for asset {}", asset.getAssetKey());
			return compressCss(asset.getAssetKey(), content);
		case js:
			LOG.debug("JS compression for asset {}", asset.getAssetKey());
			return compressJs(asset.getAssetKey(), content);
		default:
			LOG.debug("No compression for asset {}", asset.getAssetKey());
			return content;
		}
	}

	private String compressJs(String assetKey, String content) {
		LOG.debug("JS compression with YUI compressor");
		Writer output = new StringWriter();

		try {
			JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(content),
					new YuiCompressorErrorReporter());
			compressor.compress(output, -1, jsMunge, false, jsPreserveSemiColons, jsDisableOptimizations);
		}
		catch (EvaluatorException e) {
			LOG.error("YUI compressor can't evaluate the content of {}", assetKey);
			LOG.debug("YUI compressor can't evaluate the content [{}]", content);
			throw DandelionException.wrap(e, null).set("assetKey", assetKey).set("content", content);
		}
		catch (IOException e) {
			LOG.error("YUI compressor can't access to the content of {}", assetKey);
			throw DandelionException.wrap(e, null).set("assetKey", assetKey);
		}

		return output.toString();
	}

	private String compressCss(String assetKey, String content) {
		LOG.debug("CSS compression with YUI compressor");
		Writer output = new StringWriter();

		try {
			CssCompressor compressor = new CssCompressor(new StringReader(content));
			compressor.compress(output, -1);
		}
		catch (IOException e) {
			LOG.error("YUI compressor can't access to the content of {}", assetKey);
			throw DandelionException.wrap(e, null).set("assetKey", assetKey);
		}

		return output.toString();
	}

	private String extractContent(Asset asset, HttpServletRequest request) {
		Map<String, AssetLocationWrapper> wrappers = AssetStack.getAssetLocationWrappers();
		StringBuilder groupContent = new StringBuilder();

		for (Map.Entry<String, String> location : asset.getLocations().entrySet()) {
			AssetLocationWrapper wrapper = wrappers.get(location.getKey());
			String content;
			if (wrapper != null && wrapper.isActive()) {
				content = wrapper.getWrappedContent(asset, request);
			}
			else {
				content = ResourceUtils.getContentFromUrl(request, location.getValue(), true);
			}
			if (content != null) {
				groupContent.append(content).append("\n");
			}
		}
		return groupContent.toString();
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}
}
