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
package com.github.dandelion.core.storage;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

/**
 * <p>
 * Abstract asset storage in charge of manipulating actual implementations of
 * {@link AssetStorage}.
 * </p>
 * <p>
 * This class also handles logging and access statistics.
 * </p>
 * <p>
 * Custom implementations should extends this class instead of implementing
 * {@link AssetStorage}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public abstract class AbstractAssetStorage implements AssetStorage {

   private AtomicLong getCount;
   private AtomicLong putCount;
   private AtomicLong hitCount;
   private AtomicLong missCount;

   public AbstractAssetStorage() {
      super();
      this.getCount = new AtomicLong(0);
      this.putCount = new AtomicLong(0);
      this.hitCount = new AtomicLong(0);
      this.missCount = new AtomicLong(0);
   }

   protected abstract Logger getLogger();

   @Override
   public StorageEntry get(String cacheKey) {

      this.getCount.incrementAndGet();
      StorageEntry element = doGet(cacheKey);

      if (element == null) {
         this.missCount.incrementAndGet();
         getLogger().trace("Storage miss for key \"{}\"", cacheKey);
         return null;
      }

      this.hitCount.incrementAndGet();
      getLogger().trace("Storage hit for key \"{}\"", cacheKey);
      return element;
   }

   @Override
   public void put(String cacheKey, StorageEntry element) {
      this.putCount.incrementAndGet();
      int newSize = doPut(cacheKey, element);
      getLogger().trace("Added storage entry for key \"{}\". New size is {}.", cacheKey, newSize);
   }

   @Override
   public void remove(String cacheKey) {

      doRemove(cacheKey);
      getLogger().trace("Removed storage entry for key \"{}\"", cacheKey);
   }

   @Override
   public void clear() {
      doClear();
      getLogger().trace("Cleared storage");
   }

   protected abstract StorageEntry doGet(String cacheKey);

   protected abstract int doPut(String cacheKey, StorageEntry element);

   protected abstract void doRemove(String cacheKey);

   protected abstract void doClear();
}
