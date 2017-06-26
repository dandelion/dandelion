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
package com.github.dandelion.core.cache;

import java.util.Set;

import com.github.dandelion.core.cache.support.ConcurrentLruCache;

/**
 * <p>
 * Standard implementation of the {@link Cache} interface based on a in-memory
 * LRU cache.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class StandardCache<K, V> implements Cache<K, V> {

   private final ConcurrentLruCache<K, V> dataContainer;

   public StandardCache(int maxSize) {
      this.dataContainer = new ConcurrentLruCache<K, V>(maxSize);
   }

   @Override
   public void put(K key, V value) {
      this.dataContainer.put(key, value);
   }

   @Override
   public V get(K key) {

      V resultEntry = this.dataContainer.get(key);

      return resultEntry;
   }

   @Override
   public Set<K> keySet() {
      return this.dataContainer.keySet();
   }

   @Override
   public void clear() {
      this.dataContainer.clear();
   }

   @Override
   public void clearKey(K key) {
      this.dataContainer.remove(key);
   }

   @Override
   public int size() {
      return this.dataContainer.size();
   }
}
