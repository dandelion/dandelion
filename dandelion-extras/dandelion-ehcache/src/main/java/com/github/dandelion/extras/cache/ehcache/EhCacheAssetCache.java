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
package com.github.dandelion.extras.cache.ehcache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.spi.AbstractAssetCache;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.utils.ResourceScanner;
import com.github.dandelion.core.utils.StringUtils;
import com.github.dandelion.core.web.DandelionServlet;

/**
 * <p>
 * EhCache implementation of the {@link AssetCache}.
 * 
 * <p>
 * The initialization of this cache has two prerequisites: obtain a
 * {@link CacheManager} instance and a {@code ehcache.xml} configuration file.
 * 
 * <p>
 * The {@link CacheManager} instance is obtained using the following strategy:
 * <ul>
 * <li>First tries to get an existing {@link CacheManager} using the
 * {@code cache.manager.name} configuration property.</li>
 * <li>If no {@link CacheManager} is configured, a default one is created.</li>
 * </ul>
 * 
 * <p>
 * Once the {@link CacheManager} obtained, then the configuration file is loaded
 * using the following strategy:
 * <ul>
 * <li>First checks if the {@code cache.configuration.location} configuration
 * property exists and uses this path to load the file.</li>
 * <li>If the above configuration property is not used, scans the classpath
 * (starting from the root) for the {@code ehcache.xml} configuration file.</li>
 * </li>
 * </ul>
 * 
 * <p>
 * Note that Dandelion uses a cache called {@code dandelionCache}. If it doesn't
 * exist in your {@code ehcache.xml} configuration file, Dandelion will
 * automatically add and use it.
 * 
 * <p>
 * Note finally that the same cache is used for two purposes:
 * <ul>
 * <li>Store the content of {@link Asset}s that are served by the
 * {@link DandelionServlet}</li>
 * <li>Store the set of {@link Asset}s to be displayed on a page, for a given
 * request, thus avoiding location resolution and processing</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class EhCacheAssetCache extends AbstractAssetCache {

	private static final Logger LOG = LoggerFactory.getLogger(EhCacheAssetCache.class);

	private Cache cache;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCacheName() {
		return "ehcache";
	}
	
	@Override
	public void initCache(Context context) {
		super.initCache(context);

		CacheManager cacheManager = null;
		String cacheManagerName = context.getConfiguration().getCacheManagerName();

		// First try to get an existing CacheManager
		if (StringUtils.isNotBlank(cacheManagerName)) {
			cacheManager = CacheManager.getCacheManager(cacheManagerName);
			LOG.warn("No cache manager found with the name '{}'. Dandelion will create one.", cacheManagerName);
		}
		
		if(cacheManager == null){
			InputStream stream = null;

			String cacheConfigurationPath = context.getConfiguration().getCacheConfigurationLocation();

			if (StringUtils.isBlank(cacheConfigurationPath)) {
				LOG.warn("The 'cache.configuration.location' configuration is not set. Dandelion will scan for any ehcache.xml file inside the classpath.");
				try {
					cacheConfigurationPath = ResourceScanner.findResourcePath("", "ehcache.xml");
					LOG.debug("ehcache.xml file found: {}", cacheConfigurationPath);
				}
				catch (IOException e) {
					LOG.warn("No ehcache.xml configuration file has been found. Dandelion will let EhCache use the default configuration.");
				}
			}

			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(cacheConfigurationPath);
			cacheManager = stream == null ? CacheManager.create() : CacheManager.create(stream);
		}

		if(!cacheManager.cacheExists(DANDELION_CACHE_NAME)){
			cacheManager.addCache(DANDELION_CACHE_NAME);
			LOG.debug("Added cache called '{}' to the cache manager", DANDELION_CACHE_NAME);
		}
		cache = cacheManager.getCache(DANDELION_CACHE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAssetContent(String cacheKey) {
		Element element = cache.get(cacheKey);
		return element == null ? null : (String) element.getObjectValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeAssetContent(String cacheKey, String cacheContent) {
		cache.put(new Element(cacheKey, cacheContent));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(String cacheKey) {
		cache.remove(cacheKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Set<Asset> getRequestAssets(String cacheKey) {
		Element element = cache.get(cacheKey);
		return element == null ? null : (Set<Asset>) element.getObjectValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeRequestAssets(String cacheKey, Set<Asset> assets) {
		cache.put(new Element(cacheKey, assets));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAll() {
		cache.removeAll();
	}
}
