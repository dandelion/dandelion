/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.ResourceScanner;
import com.github.dandelion.core.utils.StringUtils;

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
 * {@code assets.cache.manager} configuration property.</li>
 * <li>If no {@link CacheManager} is configured, a default one is created.</li>
 * </ul>
 * 
 * <p>
 * Once the {@link CacheManager} obtained, then the configuration file is loaded
 * using the following strategy:
 * <ul>
 * <li>First checks if the {@code assets.cache.configuration} configuration
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
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class EhCacheAssetCache implements AssetCache {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(EhCacheAssetCache.class);

	private Cache cache;

	public EhCacheAssetCache() {

		CacheManager cacheManager = null;
		String cacheManagerName = Configuration.getProperty("assets.cache.manager");

		// First try to get an existing CacheManager
		if (StringUtils.isNotBlank(cacheManagerName)) {
			cacheManager = CacheManager.getCacheManager(cacheManagerName);
		}
		// Or create one
		else {
			InputStream stream = null;

			String cacheConfigurationPath = Configuration.getProperty("assets.cache.configuration");

			if (StringUtils.isBlank(cacheConfigurationPath)) {
				try {
					cacheConfigurationPath = ResourceScanner.findResourcePath("", "ehcache.xml");
				}
				catch (IOException e) {
					LOG.warn("No ehcache.xml configuration file has been found. Dandelion will let EhCache use the default configuration.");
				}
			}

			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(cacheConfigurationPath);
			cacheManager = stream == null ? CacheManager.create() : CacheManager.create(stream);
		}

		if (!cacheManager.cacheExists(DANDELION_CACHE_NAME)) {
			cacheManager.addCache(DANDELION_CACHE_NAME);
		}

		cache = cacheManager.getCache(DANDELION_CACHE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCacheName() {
		return "Ehcache";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContent(String cacheKey) {
		Element element = cache.get(cacheKey);
		return element == null ? null : (String) element.getObjectValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeContent(String cacheKey, String cacheContent) {
		cache.put(new Element(cacheKey, cacheContent));
	}
}