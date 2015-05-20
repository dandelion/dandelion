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

/**
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public interface Cache<K, V> {

   /**
    * <p>
    * Insert a new value into the cache.
    * </p>
    * 
    * @param key
    *           the key of the new entry.
    * @param value
    *           the value to be cached.
    */
   void put(K key, V value);

   /**
    * <p>
    * Retrieve a value from the cache.
    * </p>
    * 
    * @param key
    *           the key of the value to be retrieved.
    * @return the retrieved value, or null if no value exists for the specified
    *         key.
    */
   V get(K key);

   /**
    * <p>
    * Clear the entire cache.
    * </p>
    */
   void clear();

   /**
    * <p>
    * Clears a specific entry in the cache.
    * </p>
    * 
    * @param key
    *           the key of the entry to be cleared.
    */
   void clearKey(K key);

   /**
    * <p>
    * Returns all the keys contained in this cache.
    * </p>
    *
    * @return the complete set of cache keys.
    */
   Set<K> keySet();

   /**
    * @return the number of cached entries.
    */
   int size();
}