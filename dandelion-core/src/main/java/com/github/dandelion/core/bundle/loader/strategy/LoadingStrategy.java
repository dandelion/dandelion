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
package com.github.dandelion.core.bundle.loader.strategy;

import java.util.List;
import java.util.Set;

import com.github.dandelion.core.storage.BundleStorageUnit;

/**
 * <p>
 * Describes a loading strategy for {@link BundleStorageUnit}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public interface LoadingStrategy {

   /**
    * <p>
    * Scans for bundles in the classpath using different filters.
    * </p>
    * 
    * @param bundleLocation
    *           The root folder in the classpath where the scanning starts.
    * 
    * @param excludedPaths
    *           A set of paths to exclude from the search.
    * @return a set of paths corresponding to bundles.
    */
   Set<String> getResourcePaths(String bundleLocation, Set<String> excludedPaths);

   /**
    * <p>
    * Loads the resources pointed by the provided resource paths and map them
    * into {@link BundleStorageUnit} instances.
    * </p>
    * 
    * @param resourcePaths
    *           A set of paths corresponding to bundles.
    * @return a collection of {@link BundleStorageUnit}s.
    */
   List<BundleStorageUnit> mapToBundles(Set<String> resourcePaths);
}
