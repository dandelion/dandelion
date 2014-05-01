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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.cache.AssetCacheManager;
import com.github.dandelion.core.asset.cache.impl.MemoryAssetCache;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.asset.locator.Servlet2Compatible;
import com.github.dandelion.core.asset.locator.Servlet3Compatible;
import com.github.dandelion.core.asset.locator.spi.AssetLocator;
import com.github.dandelion.core.asset.processor.AssetProcessorManager;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.bundle.loader.impl.DandelionBundleLoader;
import com.github.dandelion.core.bundle.loader.impl.VendorBundleLoader;
import com.github.dandelion.core.bundle.loader.spi.BundleLoader;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.config.ConfigurationLoader;
import com.github.dandelion.core.config.StandardConfigurationLoader;
import com.github.dandelion.core.jmx.DandelionRuntime;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.utils.ClassUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * This class is in charge of discovering and storing several configuration
 * points, such as the configured {@link AssetCache} implementation or the
 * active {@link AssetProcessor}s.
 * 
 * <p>
 * There should be only one instance of this class in the application.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class Context {

	private static Logger LOG = LoggerFactory.getLogger(Context.class);

	private AssetCache assetCache;
	private Map<String, AssetProcessor> processorsMap;
	private List<AssetProcessor> activeProcessors;
	private List<BundleLoader> bundleLoaders;
	private AssetProcessorManager assetProcessorManager;
	private AssetCacheManager assetCacheManager;

	private Map<String, AssetLocator> assetLocatorsMap;
	private BundleStorage bundleStorage;
	private Configuration configuration;

	/**
	 * Public constructor.
	 * 
	 * @param filterConfig
	 *            The servlet filter configuration.
	 */
	public Context(FilterConfig filterConfig) {
		init(filterConfig);
	}

	/**
	 * <p>
	 * Performs all the required initializations of the Dandelion context.
	 * 
	 * @param filterConfig
	 *            The servlet filter configuration.
	 */
	public void init(FilterConfig filterConfig) {

		initConfiguration(filterConfig);
		initBundleLoaders();
		initAssetLocators();
		initAssetCache();
		initAssetProcessors();

		assetProcessorManager = new AssetProcessorManager(this);
		assetCacheManager = new AssetCacheManager(this);

		initBundleStorage();
		initMBean(filterConfig);
	}

	/**
	 * <p>
	 * Returns an implementation of {@link ConfigurationLoader} using the
	 * following strategy:
	 * <ol>
	 * <li>Check first if the <code>dandelion.confloader.class</code> system
	 * property is set and tries to instantiate it</li>
	 * <li>Otherwise, instantiate the {@link StandardConfigurationLoader} which
	 * reads properties files</li>
	 * </ol>
	 * 
	 * @return an implementation of {@link ConfigurationLoader}.
	 */
	public void initConfiguration(FilterConfig filterConfig) {

		ConfigurationLoader configurationLoader = null;

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

		Properties userProperties = new Properties();
		userProperties.putAll(configurationLoader.loadUserConfiguration());
		configuration = new Configuration(filterConfig, userProperties);
	}

	/**
	 * <p>
	 * Initializes the {@link BundleLoader}s in a particular order:
	 * <ol>
	 * <li>First, the {@link VendorBundleLoader} which loads all
	 * "vendor bundles"</li>
	 * <li>Then, all service providers of the {@link BundleLoader} SPI present
	 * in the classpath</li>
	 * <li>Finally the {@link DandelionBundleLoader} which loads "user bundles"</li>
	 * </ol>
	 */
	public void initBundleLoaders() {
		ServiceLoader<BundleLoader> blServiceLoader = ServiceLoader.load(BundleLoader.class);
		
		VendorBundleLoader vendorLoader = new VendorBundleLoader();
		vendorLoader.initLoader(this);
		DandelionBundleLoader dandelionLoader = new DandelionBundleLoader();
		dandelionLoader.initLoader(this);

		bundleLoaders = new ArrayList<BundleLoader>();

		// All vendor bundles must be loaded in first
		bundleLoaders.add(vendorLoader);

		// Then all bundles of the components present in the classpath
		for (BundleLoader bl : blServiceLoader) {
			bl.initLoader(this);
			bundleLoaders.add(bl);
			LOG.info("Active bundle loader found: {}", bl.getClass().getSimpleName());
		}

		// Finally all bundles created by users
		bundleLoaders.add(dandelionLoader);
	}

	/**
	 * Initializes the service provider of {@link AssetCache} to use for
	 * caching.
	 */
	public void initAssetCache() {
		ServiceLoader<AssetCache> assetCacheServiceLoader = ServiceLoader.load(AssetCache.class);
		
		Map<String, AssetCache> caches = new HashMap<String, AssetCache>();
		for (AssetCache ac : assetCacheServiceLoader) {
			caches.put(ac.getCacheName().toLowerCase().trim(), ac);
			LOG.info("Asset caching system found: {}", ac.getClass().getSimpleName());
		}

		String desiredCacheName = configuration.getCacheName();
		if(StringUtils.isNotBlank(desiredCacheName)) {
			if(caches.containsKey(desiredCacheName)) {
				assetCache = caches.get(desiredCacheName);
			}
			else {
				LOG.warn(
						"The desired caching system ({}) hasn't been found in the classpath. Did you forget to add a dependency? The default one will be used.",
						desiredCacheName);
			}
		}

		// If no caching system is detected, it defaults to memory caching
		if (assetCache == null) {
			assetCache = new MemoryAssetCache();
		}

		assetCache.initCache(this);

		LOG.info("Selected asset cache system: {} (based on {})", assetCache.getCacheName(), assetCache.getClass()
				.getSimpleName());
	}

	/**
	 * Initializes all service providers of the {@link AssetLocator} SPI. The
	 * order doesn't matter.
	 */
	public void initAssetLocators() {
		ServiceLoader<AssetLocator> alServiceLoader = ServiceLoader.load(AssetLocator.class);
		
		assetLocatorsMap = new HashMap<String, AssetLocator>();
		for (AssetLocator al : alServiceLoader) {

			// Only register Servlet3-compatible asset locators if Servlet 3.x
			// is being used
			if (this.getConfiguration().isServlet3Enabled()) {
				if (Servlet3Compatible.class.isAssignableFrom(al.getClass())) {
					al.initLocator(this);
					assetLocatorsMap.put(al.getLocationKey(), al);
					LOG.info("Asset locator found: {} ({})", al.getLocationKey(), al.getClass().getSimpleName());
				}
			}
			// Otherwise register all Servlet2-compatible asset locators
			else {
				if (Servlet2Compatible.class.isAssignableFrom(al.getClass())) {
					al.initLocator(this);
					assetLocatorsMap.put(al.getLocationKey(), al);
					LOG.info("Asset locator found: {} ({})", al.getLocationKey(), al.getClass().getSimpleName());
				}
			}
		}
	}

	/**
	 * <p>
	 * Initializes all service providers of the {@link AssetProcessor} SPI and
	 * stores them all in the {@link #processorsMap}.
	 * 
	 * <p>
	 * If minification is enabled, the {@link #activeProcessors} is filled with
	 * default service providers.
	 */
	public void initAssetProcessors() {

		ServiceLoader<AssetProcessor> apServiceLoader = ServiceLoader.load(AssetProcessor.class);
		
		processorsMap = new HashMap<String, AssetProcessor>();
		activeProcessors = new ArrayList<AssetProcessor>();

		for (AssetProcessor ape : apServiceLoader) {
			ape.initProcessor(this);
			processorsMap.put(ape.getProcessorKey().toLowerCase(), ape);
			LOG.info("Asset processor found: {}", ape.getClass().getSimpleName());
		}

		if (configuration.isMinificationEnabled()) {
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

	/**
	 * <p>
	 * Initializes the {@link BundleStorage} by using all configured
	 * {@link BundleLoader}s.
	 * 
	 * <p>
	 * Once loader, some checks are performed on the {@link BundleStorage}.
	 */
	public void initBundleStorage() {
		bundleStorage = new BundleStorage();
		for (BundleLoader bundleLoader : getBundleLoaders()) {
			bundleStorage.storeBundles(bundleLoader.loadBundles());
		}
		bundleStorage.checkBundleDag();
	}

	/**
	 * If JMX is enabled, initializes a MBean allowing to reload bundles and
	 * access cache.
	 * 
	 * @param filterConfig
	 *            The servlet filter configuration.
	 */
	public void initMBean(FilterConfig filterConfig) {
		if (configuration.isJmxEnabled()) {
			try {
				MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
				ObjectName name = new ObjectName("com.github.dandelion", "type", DandelionRuntime.class.getSimpleName());
				if (!mbeanServer.isRegistered(name)) {
					mbeanServer.registerMBean(new DandelionRuntime(this, filterConfig), name);
				}
			}
			catch (final JMException e) {
				LOG.error("An exception occured while registering the DandelionRuntimeMBean", e);
			}
		}
	}
	
	public void destroy(){
		if (configuration.isJmxEnabled()) {
			try {
				MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
				ObjectName name = new ObjectName("com.github.dandelion", "type", DandelionRuntime.class.getSimpleName());
				if (mbeanServer.isRegistered(name)) {
					mbeanServer.unregisterMBean(name);
				}
			}
			catch (final JMException e) {
				LOG.error("An exception occured while unregistering the DandelionRuntimeMBean", e);
			}
		}
	}
	
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

	/**
	 * @return {@code true} if the {@code dandelion.mode} configuration property
	 *         is set to "development" or any other value than "production". If
	 *         the mode is not explicitely configured, it returns {@code true}.
	 */
	public boolean isDevModeEnabled() {
		return "development".equalsIgnoreCase(configuration.getDandelionMode().toString());
	}
}