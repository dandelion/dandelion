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

import java.util.Collection;

/**
 * <p>
 * Interface for an asset storage.
 * </p>
 * <p>
 * Custom implementations should extends {@link AbstractAssetStorage} instead of
 * implementing this interface.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public interface AssetStorage {

   /**
    * @return the name of the implementation of {@link AssetStorage}.
    */
   String getName();

   /**
    * <p>
    * Gets the asset contents from the storage using the provided
    * {@code storageKey}.
    * </p>
    * 
    * @param storageKey
    *           The key under which the asset contents is stored in the storage.
    * @return the contents associated with the storage key.
    */
   StorageEntry get(String storageKey);

   Collection<StorageEntry> getAll();

   /**
    * <p>
    * Puts the provided {@code contents} into the storage.
    * </p>
    * 
    * @param storageKey
    *           The key used to puts the contents to the storage.
    * @param contents
    *           The asset contents to store in the storage.
    */
   void put(String storageKey, StorageEntry element);

   /**
    * <p>
    * Removes the entry in the storage corresponding to the provided cache key.
    * </p>
    * 
    * @param storageKey
    *           The key to use to lookup the entry to remove.
    */
   void remove(String storageKey);

   boolean contains(String storageKey);

   int size();

   /**
    * <p>
    * Clears the underlying store.
    * </p>
    */
   void clear();
}
