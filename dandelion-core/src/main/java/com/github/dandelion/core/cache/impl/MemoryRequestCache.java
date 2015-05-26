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
package com.github.dandelion.core.cache.impl;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.cache.AbstractRequestCache;
import com.github.dandelion.core.cache.RequestCache;
import com.github.dandelion.core.cache.CacheEntry;
import com.github.dandelion.core.cache.support.ConcurrentLruCache;

/**
 * <p>
 * Service provider for {@link RequestCache} that uses
 * {@link ConcurrentLruCache}s as stores.
 * </p>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class MemoryRequestCache extends AbstractRequestCache {

   private static final Logger LOG = LoggerFactory.getLogger(MemoryRequestCache.class);
   public static final String CACHE_NAME = "default";

   /**
    * The backend used to stored cache entries, based on the LRU policy.
    */
   private ConcurrentLruCache<String, CacheEntry> mapRequestAssets;

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public void initCache(Context context) {
      super.initCache(context);
      mapRequestAssets = new ConcurrentLruCache<String, CacheEntry>(context.getConfiguration().getCacheMaxSize());
   }

   @Override
   public String getCacheName() {
      return CACHE_NAME;
   }

   @Override
   public CacheEntry doGet(String cacheKey) {
      return mapRequestAssets.get(cacheKey);
   }

   @Override
   protected Collection<CacheEntry> doGetAll() {
      return mapRequestAssets.values();
   }

   @Override
   public int doPut(String cacheKey, CacheEntry cacheElement) {
      mapRequestAssets.put(cacheKey, cacheElement);
      return this.mapRequestAssets.size();
   }

   @Override
   public void doClear() {
      mapRequestAssets.clear();
   }

   public Map<String, CacheEntry> getMapRequestAssets() {
      return this.mapRequestAssets;
   }
}
