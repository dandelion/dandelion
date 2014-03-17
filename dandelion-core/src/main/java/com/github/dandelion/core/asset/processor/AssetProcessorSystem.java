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
package com.github.dandelion.core.asset.processor;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.AssetCacheSystem;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.asset.web.AssetServlet;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.StringUtils;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * <p>
 * System in charge of discovering and manipulating all implementations of
 * {@link AssetProcessor} available in the classpath.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public final class AssetProcessorSystem {

	private static final Logger LOG = LoggerFactory.getLogger(AssetProcessorSystem.class);

	private static ServiceLoader<AssetProcessor> apServiceLoader = ServiceLoader.load(AssetProcessor.class);
	private static Map<String, AssetProcessor> processorsMap;
	private static List<AssetProcessor> activeProcessors;

	private static void initializeIfNeeded() {
		if (processorsMap == null || DevMode.isEnabled()) {
			initializeAssetProcessors();
		}
	}

	/**
	 * <p>
	 * The initialization is performed in 2 steps.
	 * <p>
	 * First, all available implementations are stored in the
	 * {@link #processorsMap}.
	 * <p>
	 * Then, if the asset processing is enabled, the {@link #activeProcessors}
	 * list is updated with all active processors.
	 */
	private static synchronized void initializeAssetProcessors() {

		processorsMap = new HashMap<String, AssetProcessor>();
		activeProcessors = new ArrayList<AssetProcessor>();

		for (AssetProcessor ape : apServiceLoader) {
			processorsMap.put(ape.getProcessorKey().toLowerCase(), ape);
			LOG.info("Asset processor found: {}", ape.getClass().getSimpleName());
		}

		if (Configuration.isAssetProcessorsEnabled()) {
			LOG.info("Asset processors enabled.");

			// User-defined active processors
			String assetProcessorString = Configuration.getAssetProcessors();
			if (StringUtils.isNotBlank(assetProcessorString)) {
				for (String assetProcessorKey : assetProcessorString.trim().toLowerCase().split(",")) {
					if (processorsMap.containsKey(assetProcessorKey)) {
						activeProcessors.add(processorsMap.get(assetProcessorKey));
					}
				}
			}
			// Default active processors
			else {
				activeProcessors.add(processorsMap.get("jsmin"));
			}
			LOG.info("The following processors are active: {}", activeProcessors);
		}
		else {
			LOG.info("Asset processors disabled.");
		}
	}

	public static Set<Asset> process(Set<Asset> assets, HttpServletRequest request) {
		initializeIfNeeded();

		if (!activeProcessors.isEmpty()) {
			LOG.debug("Processing assets with the following processors: {}", activeProcessors);
			for (Asset asset : assets) {

				if (anyProcessorCanBeAppliedFor(asset)) {

					String content = AssetCacheSystem.getContent(asset.getCacheKey());

					Reader assetReader = new StringReader(content);
					Writer assetWriter = new StringWriter();

					List<AssetProcessor> compatibleAssetProcessors = getCompatibleProcessorFor(asset);
					for (AssetProcessor assetProcessor : compatibleAssetProcessors) {
						LOG.trace("Applying processor {} on {}", assetProcessor.getProcessorKey(), asset.toLog());
						assetWriter = new StringWriter();
						assetProcessor.process(asset, assetReader, assetWriter);
						assetReader = new StringReader(assetWriter.toString());
					}

					// The old asset is removed from cache
					AssetCacheSystem.remove(asset.getCacheKey());

					// The new cache key is built, with ".min" applied before
					// the extension
					String context = UrlUtils.getCurrentUrl(request, true).toString();
					context = context.replaceAll("\\?", "_").replaceAll("&", "_");
					String newCacheKey = AssetCacheSystem.generateCacheKey(context, asset.getConfigLocation(),
							asset.getName() + ".min", asset.getType());

					// The final asset location is overriden
					asset.setFinalLocation(UrlUtils.getProcessedUrl(AssetServlet.DANDELION_ASSETS_URL + newCacheKey,
							request, null));

					// The cache system is updated with the new key/content pair
					AssetCacheSystem.storeContent(newCacheKey, assetWriter.toString());
				}
			}
		}
		else {
			LOG.debug("No asset processor active. All asset will be left untouched.");
		}

		return assets;
	}

	private static List<AssetProcessor> getCompatibleProcessorFor(Asset asset) {

		List<AssetProcessor> compatibleProcessors = new ArrayList<AssetProcessor>();

		for (AssetProcessor assetProcessor : activeProcessors) {
			Annotation annotation = assetProcessor.getClass().getAnnotation(CompatibleAssetType.class);
			CompatibleAssetType compatibleAssetType = (CompatibleAssetType) annotation;
			List<AssetType> compatibleAssetTypes = Arrays.asList(compatibleAssetType.types());
			if (compatibleAssetTypes.contains(asset.getType())) {
				compatibleProcessors.add(assetProcessor);
			}
		}
		return compatibleProcessors;
	}

	public static boolean anyProcessorCanBeAppliedFor(Asset asset) {

		for (AssetProcessor assetProcessor : activeProcessors) {
			Annotation annotation = assetProcessor.getClass().getAnnotation(CompatibleAssetType.class);
			CompatibleAssetType compatibleAssetType = (CompatibleAssetType) annotation;
			List<AssetType> compatibleAssetTypes = Arrays.asList(compatibleAssetType.types());
			if (compatibleAssetTypes.contains(asset.getType())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Prevents instantiation.
	 */
	private AssetProcessorSystem() {
	}
}
