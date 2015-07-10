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
package com.github.dandelion.extras.cache.ehcache;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.cache.AbstractRequestCache;
import com.github.dandelion.core.cache.CacheEntry;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.util.scanner.ClasspathResourceScanner;

/**
 * <p>
 * EhCache implementation of {@link Cache}.
 * </p>
 * <p>
 * The configuration file is loaded using the following strategy:
 * </p>
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
 * </p>
 * <p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class EhCacheRequestCache extends AbstractRequestCache {

   private static final Logger LOG = LoggerFactory.getLogger(EhCacheRequestCache.class);

   private Cache cache;

   @Override
   public String getCacheName() {
      return "ehcache";
   }

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public void initCache(Context context) {
      super.initCache(context);

      InputStream stream = null;

      String cacheConfigurationPath = context.getConfiguration().getCacheConfigurationLocation();

      if (StringUtils.isBlank(cacheConfigurationPath)) {
         LOG.warn("The 'cache.configuration.location' configuration is not set. Dandelion will scan for any ehcache.xml file inside the classpath.");
         cacheConfigurationPath = ClasspathResourceScanner.findResourcePath("", "ehcache.xml");
         LOG.debug("ehcache.xml file found: {}", cacheConfigurationPath);
      }

      stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(cacheConfigurationPath);
      CacheManager cacheManager = stream == null ? CacheManager.create() : CacheManager.create(stream);

      if (!cacheManager.cacheExists(DANDELION_CACHE_NAME)) {
         cacheManager.addCache(DANDELION_CACHE_NAME);
         LOG.debug("Added cache called '{}' to the cache manager", DANDELION_CACHE_NAME);
      }
      cache = cacheManager.getCache(DANDELION_CACHE_NAME);
   }

   @Override
   public CacheEntry doGet(String cacheKey) {
      Element element = cache.get(cacheKey);
      return (element == null ? null : (CacheEntry) element.getObjectValue());
   }

   @Override
   protected int doPut(String cacheKey, CacheEntry cacheElement) {
      cache.put(new Element(cacheKey, cacheElement));
      return cache.getKeysNoDuplicateCheck().size();
   }

   @Override
   public void doClear() {
      cache.removeAll();
   }

   @Override
   protected Collection<CacheEntry> doGetAll() {
      Collection<Element> elements = cache.getAll(cache.getKeysNoDuplicateCheck()).values();
      Collection<CacheEntry> cacheElements = new ArrayList<CacheEntry>();
      for (Element e : elements) {
         cacheElements.add((CacheEntry) e.getObjectValue());
      }
      return cacheElements;
   }
}
