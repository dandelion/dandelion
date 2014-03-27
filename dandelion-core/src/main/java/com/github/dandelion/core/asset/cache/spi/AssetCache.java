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
package com.github.dandelion.core.asset.cache.spi;

import java.util.HashMap;
import java.util.Set;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.impl.MemoryAssetCache;
import com.github.dandelion.core.asset.web.AssetFilter;

/**
 * <p>
 * SPI for all implementation of {@link AssetCache}.
 * 
 * <p>
 * Dandelion provides one out-of-the-box service provider:
 * <ul>
 * <li>{@link MemoryAssetCache} that uses a simple {@link HashMap} for in-memory
 * caching.</li>
 * </ul>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public interface AssetCache {

	public static final String DANDELION_CACHE_NAME = "dandelionCache";

	/**
	 * <p>
	 * Initializes the configured service provider of the {@link AssetCache} SPI
	 * by using the {@link Context}.
	 * 
	 * @param context
	 *            The {@link Context} initialized in the {@link AssetFilter}.
	 */
	void initCache(Context context);

	/**
	 * @return the name of the asset cache.
	 */
	String getCacheName();

	/**
	 * Gets the content from the cache stored under the passed {@code cacheKey}.
	 * 
	 * @param cacheKey
	 *            The cache key under which the asset content is stored in the
	 *            cache.
	 * @return the content associated with the cache key.
	 */
	String getAssetContent(String cacheKey);

	/**
	 * Gets the set of assets to be displayed for a request stored under the
	 * passed {@code cacheKey}.
	 * 
	 * @param cacheKey
	 *            The cache key under which the set of assets is stored in the
	 *            cache.
	 * @return the assets associated with the cache key.
	 */
	Set<Asset> getRequestAssets(String cacheKey);

	/**
	 * Puts the passed {@code assetContent} to the cache.
	 * 
	 * @param cacheKey
	 *            The key used to puts the content to the cache.
	 * @param assetContent
	 *            The content to store in the cache.
	 */
	void storeAssetContent(String cacheKey, String assetContent);

	/**
	 * Puts the passed {@code assets} to the cache.
	 * 
	 * @param cacheKey
	 *            The key used to puts the assets to the cache.
	 * @param assets
	 *            The assets to store in the cache.
	 */
	void storeRequestAssets(String cacheKey, Set<Asset> assets);

	/**
	 * Removes the content stored under the passed {@code cacheKey}.
	 * 
	 * @param cacheKey
	 *            The key under which the content will be removed.
	 */
	void remove(String cacheKey);
}
