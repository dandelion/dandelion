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
package com.github.dandelion.core.asset.cache.spi;

import com.github.dandelion.core.asset.cache.impl.HashMapAssetCache;

/**
 * <p>
 * SPI for caching any asset content.
 * 
 * <p>
 * Dandelion provides one out-of-the-box service provider:
 * <ul>
 * <li>{@link HashMapAssetCache} based on memory caching</li>
 * </ul>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public interface AssetCache {

	public static final String DANDELION_CACHE_NAME = "dandelionCache";

	/**
	 * @return the name of the asset cache system.
	 */
	String getCacheName();

	/**
	 * Gets the content from the cache stored under the passed {@code cacheKey}.
	 * 
	 * @param cacheKey
	 *            The cache key under which the content is stored in the cache.
	 * @return the content associated with the cacheKey.
	 */
	String getContent(String cacheKey);

	/**
	 * Puts the passed {@code cacheContent} to the cache.
	 * 
	 * @param cacheKey
	 *            The key used to puts the content to the cache.
	 * @param cacheContent
	 *            The content to store in the cache.
	 */
	void storeContent(String cacheKey, String cacheContent);
}