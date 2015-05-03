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
package com.github.dandelion.core.storage.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.storage.AbstractAssetStorage;
import com.github.dandelion.core.storage.AssetStorage;
import com.github.dandelion.core.storage.StorageEntry;

/**
 * <p>
 * Standard implementation of {@link AssetStorage} that stores asset contents in
 * memory.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class MemoryAssetStorage extends AbstractAssetStorage {

   private static final Logger LOG = LoggerFactory.getLogger(MemoryAssetStorage.class);

   /**
    * The actual store.
    */
   private ConcurrentHashMap<String, StorageEntry> contentStore;

   public MemoryAssetStorage() {
      this.contentStore = new ConcurrentHashMap<String, StorageEntry>();
   }

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public String getName() {
      return "memory";
   }

   @Override
   public StorageEntry doGet(String storageKey) {
      return this.contentStore.get(storageKey);
   }

   @Override
   public int doPut(String storageKey, StorageEntry element) {
      this.contentStore.put(storageKey, element);
      return this.contentStore.size();
   }

   @Override
   public void doRemove(String storageKey) {
      this.contentStore.remove(storageKey);
   }

   @Override
   public boolean contains(String storageKey) {
      return this.contentStore.containsKey(storageKey);
   }

   @Override
   public int size() {
      return this.contentStore.size();
   }

   @Override
   public void doClear() {
      this.contentStore.clear();
   }

   @Override
   public Collection<StorageEntry> getAll() {
      return this.contentStore.values();
   }
}