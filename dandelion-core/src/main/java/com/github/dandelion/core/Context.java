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
package com.github.dandelion.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.servlet.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.cache.AssetCacheManager;
import com.github.dandelion.core.asset.cache.impl.MemoryAssetCache;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.asset.locator.spi.AssetLocator;
import com.github.dandelion.core.asset.processor.AssetProcessorManager;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.bundle.loader.impl.DandelionBundleLoader;
import com.github.dandelion.core.bundle.loader.impl.VendorBundleLoader;
import com.github.dandelion.core.bundle.loader.spi.BundleLoader;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.config.ConfigurationLoader;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.config.StandardConfigurationLoader;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.utils.ClassUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * The {@link Context} is used to pick up different classes in charge of the
 * configuration loading, instantiate them and cache them.
 * 
 * <ul>
 * <li>The configuration loader, in charge of loading default and user
 * properties</li>
 * </ul>
 * <p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class Context {

	private static Logger LOG = LoggerFactory.getLogger(Context.class);

	private ServiceLoader<AssetCache> assetCacheServiceLoader = ServiceLoader.load(AssetCache.class);
	private ServiceLoader<AssetLocator> alServiceLoader = ServiceLoader.load(AssetLocator.class);
	private ServiceLoader<AssetProcessor> apServiceLoader = ServiceLoader.load(AssetProcessor.class);
	private ServiceLoader<BundleLoader> blServiceLoader = ServiceLoader.load(BundleLoader.class);

	private AssetCache assetCache;
	private Map<String, AssetProcessor> processorsMap;
	private List<AssetProcessor> activeProcessors;
	private List<BundleLoader> bundleLoaders;
	private AssetProcessorManager assetProcessorManager;
	private AssetCacheManager assetCacheManager;

	private ConfigurationLoader configurationLoader;
	private static Map<String, AssetLocator> assetLocatorsMap;
	private static BundleStorage bundleStorage;
	private Configuration configuration;

	public Context() {
		initialize();
	}

	public Context(FilterConfig filterConfig) {
		initialize();
	}

	public synchronized void initialize() {

		initializeConfiguration();
		initializeBundleLoaders();
		initializeAssetLocators();
		initializeAssetCache();
		initializeAssetProcessors();

		assetProcessorManager = new AssetProcessorManager(this);
		assetCacheManager = new AssetCacheManager(this);

		bundleStorage = new BundleStorage();
		for (BundleLoader bundleLoader : getBundleLoaders()) {
			bundleStorage.storeBundles(bundleLoader.loadBundles(this));
		}
		bundleStorage.checkBundleDag();
	}

	/**
	 * <p>
	 * Returns an implementation of {@link ConfigurationLoader} using the
	 * following strategy:
	 * <ol>
	 * <li>Check first if the <code>dandelion.confloader.class</code> system
	 * property is set and tries to instantiate it</li>
	 * <li>Otherwise, instantiate the {@link StandardConfigurationLoader} based
	 * on property files</li>
	 * </ol>
	 * 
	 * @return an implementation of {@link ConfigurationLoader}.
	 */
	public void initializeConfiguration() {

		configuration = new Configuration();

		LOG.debug("Initializing the configuration loader...");

		if (StringUtils.isNotBlank(System.getProperty(ConfigurationLoader.DANDELION_CONFLOADER_CLASS))) {
			Class<?> clazz;
			try {
				clazz = ClassUtils.getClass(System.getProperty(ConfigurationLoader.DANDELION_CONFLOADER_CLASS));
				configurationLoader = (ConfigurationLoader) ClassUtils.getNewInstance(clazz);
			}
			catch (Exception e) {
				LOG.warn(
						"Unable to instantiate the configured {} due to a {} exception. Falling back to the default one.",
						ConfigurationLoader.DANDELION_CONFLOADER_CLASS, e.getClass().getName(), e);
			}
		}

		if (configurationLoader == null) {
			configurationLoader = new StandardConfigurationLoader();
		}

		Properties properties = new Properties();
		properties.putAll(configurationLoader.loadUserConfiguration());
		configuration.putAll(properties);
	}

	public void initializeBundleLoaders() {
		VendorBundleLoader vendorLoader = new VendorBundleLoader();
		DandelionBundleLoader dandelionLoader = new DandelionBundleLoader();

		bundleLoaders = new ArrayList<BundleLoader>();

		// All vendor bundles must be loaded in first
		bundleLoaders.add(vendorLoader);

		// Then all bundles of the components present in the classpath
		for (BundleLoader bl : blServiceLoader) {
			bundleLoaders.add(bl);
			LOG.info("Active bundle loader found: {}", bl.getClass().getSimpleName());
		}

		// Finally all bundles created by users
		bundleLoaders.add(dandelionLoader);
	}
	
	public void initializeAssetCache() {
		Map<String, AssetCache> caches = new HashMap<String, AssetCache>();
		for (AssetCache ac : assetCacheServiceLoader) {
			caches.put(ac.getCacheName(), ac);
			LOG.info("Asset cache found: {}", ac.getClass().getSimpleName());
		}

		String cacheName = configuration.get(DandelionConfig.CACHE_MANAGER_NAME);
		if (!caches.isEmpty()) {
			if (caches.containsKey(cacheName)) {
				assetCache = caches.get(cacheName);
			}
			else if (cacheName == null && caches.size() == 1) {
				assetCache = caches.values().iterator().next();
			}
			else {
				LOG.warn("Asset Cache Strategy is set with {}, but only caches with names {} have been found.",
						cacheName, caches.keySet());
			}
		}
		else if (cacheName != null) {
			LOG.warn("Asset Cache Strategy is set with {}, but we don't find any cache", cacheName);
		}

		if (assetCache == null) {
			assetCache = new MemoryAssetCache(this);
		}

		// assetCache.setContext(this);
		LOG.info("Selected asset cache system: {} (based on {})", assetCache.getCacheName(), assetCache.getClass()
				.getSimpleName());
	}

	public void initializeAssetLocators() {
		assetLocatorsMap = new HashMap<String, AssetLocator>();
		for (AssetLocator al : alServiceLoader) {
			assetLocatorsMap.put(al.getLocationKey(), al);
			LOG.info("Asset locator found: {} ({})", al.getLocationKey(), al.isActive() ? "active" : "inactive");
		}
	}

	public void initializeAssetProcessors() {

		processorsMap = new HashMap<String, AssetProcessor>();
		activeProcessors = new ArrayList<AssetProcessor>();

		for (AssetProcessor ape : apServiceLoader) {
			processorsMap.put(ape.getProcessorKey().toLowerCase(), ape);
			LOG.info("Asset processor found: {}", ape.getClass().getSimpleName());
		}

		if (configuration.isMinificationOn()) {
			LOG.info("Asset processors enabled.");

			for (String assetProcessorKey : configuration.getAssetProcessors()) {
				if (processorsMap.containsKey(assetProcessorKey)) {
					activeProcessors.add(processorsMap.get(assetProcessorKey));
				}
			}
			LOG.info("The following processors are active: {}", activeProcessors);
		}
		else {
			LOG.info("Asset processors disabled. All assets will be left untouched.");
		}
	}

	//
	// static void clear() {
	// configurationLoader = null;
	// }

	public AssetCache getAssetCache() {
		return assetCache;
	}

	public Map<String, AssetProcessor> getProcessorsMap() {
		return processorsMap;
	}

	public List<BundleLoader> getBundleLoaders() {
		return bundleLoaders;
	}

	public Map<String, AssetLocator> getAssetLocatorsMap() {
		return assetLocatorsMap;
	}

	public List<AssetProcessor> getActiveProcessors() {
		return activeProcessors;
	}

	public BundleStorage getBundleStorage() {
		return bundleStorage;
	}

	public AssetProcessorManager getProcessorManager() {
		return assetProcessorManager;
	}

	public AssetCacheManager getCacheManager() {
		return assetCacheManager;
	}

	/**
	 * @return the {@link Configuration} store associated to the Dandelion
	 *         {@link Context} .
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
}