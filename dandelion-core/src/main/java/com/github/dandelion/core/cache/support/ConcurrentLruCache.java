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
package com.github.dandelion.core.cache.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentLruCache<K, V> extends LinkedHashMap<K, V> {

   private static final long serialVersionUID = 4555114766903087183L;
   private int maxEntries;
   private ReadWriteLock lock = new ReentrantReadWriteLock();

   public ConcurrentLruCache(int maxEntries) {
      super(maxEntries + 1);
      this.maxEntries = maxEntries;
   }

   @Override
   protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return super.size() > maxEntries && isRemovable(eldest);
   }

   protected boolean isRemovable(Map.Entry<K, V> eldest) {
      return true;
   }

   @Override
   public V get(Object key) {
      try {
         lock.readLock().lock();
         return super.get(key);
      }
      finally {
         lock.readLock().unlock();
      }
   }

   @Override
   public V put(K key, V value) {
      try {
         lock.writeLock().lock();
         return super.put(key, value);
      }
      finally {
         lock.writeLock().unlock();
      }
   }
}