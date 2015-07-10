/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.FilterConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.locator.AssetLocator;
import com.github.dandelion.core.asset.processor.AssetProcessor;
import com.github.dandelion.core.asset.processor.AssetProcessorManager;
import com.github.dandelion.core.asset.versioning.AssetVersioningStrategy;
import com.github.dandelion.core.bundle.loader.BundleLoader;
import com.github.dandelion.core.bundle.loader.PreLoader;
import com.github.dandelion.core.bundle.loader.impl.DandelionBundleLoader;
import com.github.dandelion.core.cache.Cache;
import com.github.dandelion.core.cache.CacheManager;
import com.github.dandelion.core.cache.RequestCache;
import com.github.dandelion.core.cache.StandardCache;
import com.github.dandelion.core.cache.impl.MemoryRequestCache;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.config.ConfigurationLoader;
import com.github.dandelion.core.config.Profile;
import com.github.dandelion.core.config.StandardConfigurationLoader;
import com.github.dandelion.core.jmx.DandelionRuntime;
import com.github.dandelion.core.storage.AssetStorage;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.storage.impl.MemoryAssetStorage;
import com.github.dandelion.core.util.ClassUtils;
import com.github.dandelion.core.util.LibraryDetector;
import com.github.dandelion.core.util.ServiceLoaderUtils;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.DandelionFilter;
import com.github.dandelion.core.web.RequestFlashData;
import com.github.dandelion.core.web.handler.HandlerChain;
import com.github.dandelion.core.web.handler.debug.DebugMenu;
import com.github.dandelion.core.web.handler.debug.DebugPage;

/**
 * <p>
 * Holds the whole Dandelion context.
 * </p>
 * <p>
 * This class is in charge of discovering and storing several configuration
 * points, such as the configured {@link RequestCache} implementation or the
 * active {@link AssetProcessor}s.
 * </p>
 * <p>
 * There should be only one instance of this class per JVM.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class Context {

   private static final Logger LOG = LoggerFactory.getLogger(Context.class);

   private final FilterConfig filterConfig;
   private List<Component> components;
   private RequestCache requestCache;
   private Cache<String, RequestFlashData> requestFlashDataCache;
   private Map<String, AssetProcessor> processorsMap;
   private Map<String, AssetVersioningStrategy> versioningStrategyMap;
   private AssetVersioningStrategy activeVersioningStrategy;
   private List<AssetProcessor> activeProcessors;
   private List<BundleLoader> bundleLoaders;
   private List<PreLoader> extraLoaders;
   private AssetProcessorManager assetProcessorManager;
   private CacheManager assetCacheManager;
   private Map<String, AssetLocator> assetLocatorsMap;
   private BundleStorage bundleStorage;
   private AssetStorage assetStorage;
   private Configuration configuration;
   private HandlerChain preHandlerChain;
   private HandlerChain postHandlerChain;
   private Map<String, DebugMenu> debugMenuMap;
   private Map<String, DebugPage> debugPageMap;

   /**
    * <p>
    * Public constructor.
    * </p>
    * 
    * @param filterConfig
    *           The servlet filter configuration.
    */
   public Context(FilterConfig filterConfig) {
      this.filterConfig = filterConfig;
      init();
   }

   /**
    * <p>
    * Performs all the required initializations of the Dandelion context.
    * </p>
    */
   public void init() {

      initConfiguration(this.filterConfig);
      initComponents();
      initBundleLoaders();
      initExtraLoaders();
      initAssetLocators();
      initRequestCache();
      initRequestFlashDataCache();
      initAssetProcessors();
      initAssetVersioning();

      assetProcessorManager = new AssetProcessorManager(this);
      assetCacheManager = new CacheManager(this);

      initBundleStorage();
      initAssetStorage();
      initMBean(this.filterConfig);
      initHandlers();
      initDebugMenus();
   }

   public void initComponents() {
      LOG.info("Scanning for components");

      components = ServiceLoaderUtils.getProvidersAsList(Component.class);

      Iterator<Component> i = components.iterator();
      StringBuilder log = new StringBuilder(i.next().getName());
      while (i.hasNext()) {
         log.append(", ");
         log.append(i.next().getName());
      }
      LOG.info("Found component(s): {}", log.toString());
   }

   /**
    * <p>
    * Returns an implementation of {@link ConfigurationLoader} using the
    * following strategy:
    * </p>
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
      LOG.info("Initializing configuration loader");

      ConfigurationLoader configurationLoader = null;

      if (StringUtils.isNotBlank(System.getProperty(ConfigurationLoader.DANDELION_CONFLOADER_CLASS))) {
         Class<?> clazz;
         try {
            clazz = ClassUtils.getClass(System.getProperty(ConfigurationLoader.DANDELION_CONFLOADER_CLASS));
            configurationLoader = (ConfigurationLoader) ClassUtils.getNewInstance(clazz);
         }
         catch (Exception e) {
            LOG.warn("Unable to instantiate the configured {} due to a {} exception. Falling back to the default one.",
                  ConfigurationLoader.DANDELION_CONFLOADER_CLASS, e.getClass().getName(), e);
         }
      }

      if (configurationLoader == null) {
         configurationLoader = new StandardConfigurationLoader();
      }

      configuration = new Configuration(filterConfig, configurationLoader.loadUserConfiguration(), this);
   }

   /**
    * <p>
    * Initializes the asset versioning for the whole application.
    * </p>
    */
   public void initAssetVersioning() {
      LOG.info("Initializing asset versioning");

      List<AssetVersioningStrategy> availableStrategies = ServiceLoaderUtils
            .getProvidersAsList(AssetVersioningStrategy.class);

      versioningStrategyMap = new HashMap<String, AssetVersioningStrategy>();

      for (AssetVersioningStrategy strategy : availableStrategies) {
         LOG.info("Found asset versioning strategy: {}", strategy.getName());
         versioningStrategyMap.put(strategy.getName(), strategy);
      }

      String desiredVersioningStrategy = configuration.getAssetVersioningStrategy().toLowerCase().trim();
      if (StringUtils.isNotBlank(desiredVersioningStrategy)) {
         if (versioningStrategyMap.containsKey(desiredVersioningStrategy)) {
            activeVersioningStrategy = versioningStrategyMap.get(desiredVersioningStrategy);
            LOG.info("Selected asset versioning strategy: {}", activeVersioningStrategy.getName());
            activeVersioningStrategy.init(this);
         }
         else {
            throw new DandelionException("The desired asset versioning strategy (" + desiredVersioningStrategy
                  + ") hasn't been found among the available ones: " + versioningStrategyMap.keySet());
         }
      }
   }

   /**
    * <p>
    * Initializes the {@link BundleLoader}s in a particular order:
    * </p>
    * 
    * <ol>
    * <li>First, the {@link VendorBundleLoader} which loads all "vendor bundles"
    * </li>
    * <li>Then, all service providers of the {@link BundleLoader} SPI present in
    * the classpath</li>
    * <li>Finally the {@link DandelionBundleLoader} which loads all
    * "user bundles"</li>
    * </ol>
    */
   public void initBundleLoaders() {
      LOG.info("Initializing bundle loaders");

      bundleLoaders = new ArrayList<BundleLoader>();

      // First register all bundle loaders except DandelionBundleLoader
      for (Component component : components) {
         if (!component.getName().equalsIgnoreCase(CoreComponent.COMPONENT_NAME)) {
            BundleLoader bundleLoader = component.getBundleLoader(this);
            bundleLoaders.add(bundleLoader);
            LOG.info("Found bundle loader: {}", bundleLoader.getName());
         }
      }

      // Finally register the DandelionBundleLoader
      for (Component component : components) {
         if (component.getName().equalsIgnoreCase(CoreComponent.COMPONENT_NAME)) {
            BundleLoader bundleLoader = component.getBundleLoader(this);
            bundleLoaders.add(bundleLoader);
            LOG.info("Found bundle loader: {}", bundleLoader.getName());
         }
      }

      Iterator<BundleLoader> i = bundleLoaders.iterator();
      StringBuilder log = new StringBuilder(i.next().getName());
      while (i.hasNext()) {
         log.append(", ");
         log.append(i.next().getName());
      }
      LOG.info("Bundle loaders initialized: {}", log.toString());
   }

   /**
    * <p>
    * Initializes all {@link PreLoader}s, intended to feed the bundle graph
    * using generated bundles.
    * </p>
    */
   public void initExtraLoaders() {
      LOG.info("Initializing extra loaders");

      this.extraLoaders = new ArrayList<PreLoader>();

      ServiceLoader<PreLoader> extraLoaders = ServiceLoader.load(PreLoader.class);

      for (PreLoader extraLoader : extraLoaders) {
         extraLoader.init(this);
         this.extraLoaders.add(extraLoader);
      }

      Iterator<PreLoader> i = this.extraLoaders.iterator();
      StringBuilder log = new StringBuilder(i.next().getName());
      while (i.hasNext()) {
         log.append(", ");
         log.append(i.next().getName());
      }
      LOG.info("Extra loaders initialized: {}", log.toString());
   }

   /**
    * <p>
    * Initialize the request flash data cache only if Thymeleaf is present in
    * the classpath.
    * </p>
    */
   public void initRequestFlashDataCache() {

      if (LibraryDetector.isThymeleafAvailable()) {
         requestFlashDataCache = new StandardCache<String, RequestFlashData>(100);
      }
   }

   /**
    * <p>
    * Initialize the service provider of {@link RequestCache} to use for
    * caching.
    * </p>
    */
   public void initRequestCache() {
      LOG.info("Initializing asset caching system");

      ServiceLoader<RequestCache> assetCacheServiceLoader = ServiceLoader.load(RequestCache.class);

      Map<String, RequestCache> caches = new HashMap<String, RequestCache>();
      for (RequestCache ac : assetCacheServiceLoader) {
         caches.put(ac.getCacheName().toLowerCase().trim(), ac);
         LOG.info("Found asset caching system: {}", ac.getCacheName());
      }

      String desiredCacheName = configuration.getCacheName().toLowerCase().trim();
      if (StringUtils.isNotBlank(desiredCacheName)) {
         if (caches.containsKey(desiredCacheName)) {
            requestCache = caches.get(desiredCacheName);
         }
         else {
            LOG.warn(
                  "The desired caching system ({}) hasn't been found in the classpath. Did you forget to add a dependency? The default one will be used.",
                  desiredCacheName);
         }
      }

      // If no caching system is detected, it defaults to memory caching
      if (requestCache == null) {
         requestCache = caches.get(MemoryRequestCache.CACHE_NAME);
      }

      requestCache.initCache(this);

      LOG.info("Asset cache system initialized: {}", requestCache.getCacheName());
   }

   /**
    * <p>
    * Initializes all service providers of the {@link AssetLocator} SPI. The
    * order doesn't matter.
    * </p>
    */
   public void initAssetLocators() {
      LOG.info("Initializing asset locators");

      ServiceLoader<AssetLocator> alServiceLoader = ServiceLoader.load(AssetLocator.class);

      assetLocatorsMap = new HashMap<String, AssetLocator>();

      for (AssetLocator al : alServiceLoader) {

         al.initLocator(this);
         assetLocatorsMap.put(al.getLocationKey(), al);
         LOG.info("Found asset locator: {}", al.getLocationKey());
      }
   }

   /**
    * <p>
    * Initializes all service providers of the {@link AssetProcessor} SPI and
    * stores them all in the {@link #processorsMap}.
    * </p>
    * 
    * <p>
    * If minification is enabled, the {@link #activeProcessors} is filled with
    * default service providers.
    * </p>
    */
   public void initAssetProcessors() {
      LOG.info("Initializing asset processors");

      ServiceLoader<AssetProcessor> apServiceLoader = ServiceLoader.load(AssetProcessor.class);

      processorsMap = new HashMap<String, AssetProcessor>();
      activeProcessors = new ArrayList<AssetProcessor>();

      for (AssetProcessor ape : apServiceLoader) {
         processorsMap.put(ape.getProcessorKey().toLowerCase().trim(), ape);
         LOG.info("Found asset processor: {}", ape.getClass().getSimpleName());
      }

      if (configuration.isAssetMinificationEnabled()) {
         LOG.info("Asset processors enabled.");

         for (String assetProcessorKey : configuration.getAssetProcessors()) {
            if (processorsMap.containsKey(assetProcessorKey)) {
               activeProcessors.add(processorsMap.get(assetProcessorKey));
               LOG.info("Processor enabled: {}", processorsMap.get(assetProcessorKey).getProcessorKey());
            }
         }
      }
      else {
         LOG.info("Asset processors disabled. All assets will be served as-is.");
      }
   }

   /**
    * <p>
    * Initializes the {@link BundleStorage} by using all configured
    * {@link BundleLoader}s.
    * </p>
    * 
    * <p>
    * Once loader, some checks are performed on the {@link BundleStorage}.
    * </p>
    */
   public void initBundleStorage() {
      LOG.info("Initializing bundle storage");

      bundleStorage = new BundleStorage();
      List<BundleStorageUnit> allBundles = new ArrayList<BundleStorageUnit>();

      // Extra vendor bundles
      if (this.getConfiguration().isBundlePreLoaderEnabled()) {
         for (PreLoader extraLoader : this.extraLoaders) {
            LOG.debug("Loading bundles using the {}", extraLoader.getClass().getSimpleName());

            List<BundleStorageUnit> loadedBundles = extraLoader.getExtraBundles();
            allBundles.addAll(loadedBundles);

            LOG.debug("Found {} bundle{}: {}", loadedBundles.size(), loadedBundles.size() <= 1 ? "" : "s",
                  loadedBundles);
         }
      }
      else {
         LOG.debug("Bundle extra loader is disabled");
      }

      // Vendor bundles
      for (BundleLoader bundleLoader : getBundleLoaders()) {
         LOG.debug("Loading bundles using the {}", bundleLoader.getClass().getSimpleName());

         // Load all bundles using the current BundleLoader
         List<BundleStorageUnit> loadedBundles = bundleLoader.getVendorBundles();
         allBundles.addAll(loadedBundles);

         LOG.debug("Found {} bundle{}: {}", loadedBundles.size(), loadedBundles.size() <= 1 ? "" : "s", loadedBundles);

         bundleStorage.storeBundles(loadedBundles);
      }

      // Regular bundles
      for (BundleLoader bundleLoader : getBundleLoaders()) {
         LOG.debug("Loading bundles using the {}", bundleLoader.getClass().getSimpleName());

         // Load all bundles using the current BundleLoader
         List<BundleStorageUnit> loadedBundles = bundleLoader.getRegularBundles();
         allBundles.addAll(loadedBundles);

         LOG.debug("Found {} bundle{}: {}", loadedBundles.size(), loadedBundles.size() <= 1 ? "" : "s", loadedBundles);

         bundleStorage.storeBundles(loadedBundles);
      }

      bundleStorage.consolidateBundles(allBundles);

      LOG.info("Bundle storage initialized with {} bundles", bundleStorage.getBundleDag().getVertexMap().size());
   }

   public void initAssetStorage() {
      LOG.info("Initializing asset storage");

      ServiceLoader<AssetStorage> asServiceLoader = ServiceLoader.load(AssetStorage.class);

      String desiredAssetStorage = configuration.getAssetStorage();
      if (StringUtils.isNotBlank(desiredAssetStorage)) {
         for (AssetStorage assetStorage : asServiceLoader) {
            LOG.info("Found asset storage: {}", assetStorage.getClass().getSimpleName());

            if (assetStorage.getName().equalsIgnoreCase(desiredAssetStorage.trim())) {
               this.assetStorage = assetStorage;
            }
         }
      }

      // If no caching system is detected, it defaults to memory caching
      if (assetStorage == null) {
         assetStorage = new MemoryAssetStorage();
      }

      requestCache.initCache(this);

      LOG.info("Asset storage initialized with: {}", assetStorage.getName());
   }

   /**
    * <p>
    * If JMX is enabled, initializes a MBean allowing to reload bundles and
    * access cache.
    * </p>
    * 
    * @param filterConfig
    *           The servlet filter configuration.
    */
   public void initMBean(FilterConfig filterConfig) {
      if (configuration.isMonitoringJmxEnabled()) {
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

   /**
    * <p>
    * Initializes the handler chains to be invoked in the
    * {@link DandelionFilter} to preprocess requests and postprocess responses.
    * </p>
    */
   public void initHandlers() {
      LOG.info("Initializing handlers");

      List<HandlerChain> preHandlers = new ArrayList<HandlerChain>();
      List<HandlerChain> postHandlers = new ArrayList<HandlerChain>();

      List<HandlerChain> allHandlers = ServiceLoaderUtils.getProvidersAsList(HandlerChain.class);
      for (HandlerChain handler : allHandlers) {
         if (handler.isAfterChaining()) {
            postHandlers.add(handler);
         }
         else {
            preHandlers.add(handler);
         }
      }

      // Sort all handlers using their rank
      Collections.sort(preHandlers);
      Collections.sort(postHandlers);

      // Build the pre-handlers chain
      Iterator<HandlerChain> preHandlerIterator = preHandlers.iterator();
      HandlerChain preHandler = preHandlerIterator.next();
      int index = 1;
      do {

         LOG.info("Pre-handler ({}/{}) {} (rank: {})", index, preHandlers.size(),
               preHandler.getClass().getSimpleName(), preHandler.getRank());

         if (preHandlerIterator.hasNext()) {
            HandlerChain next = preHandlerIterator.next();
            preHandler.setNext(next);
            preHandler = next;
         }

         index++;
      }
      while (index <= preHandlers.size());
      this.preHandlerChain = preHandlers.get(0);

      // Build the post-handlers chain
      Iterator<HandlerChain> postHandlerIterator = postHandlers.iterator();
      HandlerChain postHandler = postHandlerIterator.next();
      index = 1;
      do {

         LOG.info("Post-handler ({}/{}) {} (rank: {})", index, postHandlers.size(), postHandler.getClass()
               .getSimpleName(), postHandler.getRank());

         if (postHandlerIterator.hasNext()) {
            HandlerChain next = postHandlerIterator.next();
            postHandler.setNext(next);
            postHandler = next;
         }

         index++;
      }
      while (index <= postHandlers.size());

      this.postHandlerChain = postHandlers.get(0);
   }

   public void initDebugMenus() {

      debugMenuMap = new HashMap<String, DebugMenu>();
      debugPageMap = new HashMap<String, DebugPage>();

      for (Component component : components) {
         DebugMenu componentDebugMenu = component.getDebugMenu();
         if (componentDebugMenu != null) {
            debugMenuMap.put(componentDebugMenu.getDisplayName().trim().toLowerCase(), componentDebugMenu);
            for (DebugPage debugPage : componentDebugMenu.getPages()) {
               debugPageMap.put(debugPage.getId(), debugPage);
            }
         }
      }
   }

   public void destroy() {
      if (configuration.isMonitoringJmxEnabled()) {
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

   public FilterConfig getFilterConfig() {
      return filterConfig;
   }

   /**
    * @return the selected asset caching system.
    */
   public RequestCache getCache() {
      return requestCache;
   }

   /**
    * @return the map of all available {@link AssetProcessor}.
    */
   public Map<String, AssetProcessor> getProcessorsMap() {
      return processorsMap;
   }

   public List<BundleLoader> getBundleLoaders() {
      return bundleLoaders;
   }

   /**
    * @return the map of all available {@link AssetLocator}.
    */
   public Map<String, AssetLocator> getAssetLocatorsMap() {
      return assetLocatorsMap;
   }

   public List<AssetProcessor> getActiveProcessors() {
      return activeProcessors;
   }

   public BundleStorage getBundleStorage() {
      return bundleStorage;
   }

   public AssetStorage getAssetStorage() {
      return assetStorage;
   }

   public AssetProcessorManager getProcessorManager() {
      return assetProcessorManager;
   }

   public CacheManager getCacheManager() {
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
    * @return {@code true} if the current {@link Profile} is set to "dev" or any
    *         other aliases present in {@link Profile#DEV_ALIASES},
    *         {@code false} otherwise.
    */
   public boolean isDevProfileEnabled() {
      return Profile.DEFAULT_DEV_PROFILE.equals(this.configuration.getActiveProfile());
   }

   /**
    * @return {@code true} if the current {@link Profile} is set to "prod" or
    *         any other aliases present in {@link Profile#PROD_ALIASES},
    *         {@code false} otherwise.
    */
   public boolean isProdProfileEnabled() {
      return Profile.DEFAULT_PROD_PROFILE.equals(this.configuration.getActiveProfile());
   }

   /**
    * @return the active {@link AssetVersioningStrategy}.
    */
   public AssetVersioningStrategy getActiveVersioningStrategy() {
      return this.activeVersioningStrategy;
   }

   /**
    * @return the map of all available {@link AssetVersioningStrategy}.
    */
   public Map<String, AssetVersioningStrategy> getVersioningStrategyMap() {
      return versioningStrategyMap;
   }

   /**
    * @return the {@link HandlerChain} to be invoked in the
    *         {@link DandelionFilter} to preprocess requests.
    */
   public HandlerChain getPreHandlerChain() {
      return preHandlerChain;
   }

   /**
    * @return the {@link HandlerChain} to be invoked in the
    *         {@link DandelionFilter} to postprocess server responses.
    */
   public HandlerChain getPostHandlerChain() {
      return postHandlerChain;
   }

   public Map<String, DebugMenu> getDebugMenuMap() {
      return debugMenuMap;
   }

   public Map<String, DebugPage> getDebugPageMap() {
      return debugPageMap;
   }

   public Cache<String, RequestFlashData> getRequestFlashDataCache() {
      return requestFlashDataCache;
   }
}