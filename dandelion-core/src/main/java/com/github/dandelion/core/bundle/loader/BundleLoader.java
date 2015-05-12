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
package com.github.dandelion.core.bundle.loader;

import java.util.List;

import com.github.dandelion.core.storage.BundleStorageUnit;

/**
 * <p>
 * Interface for all bundle loaders.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public interface BundleLoader {

   /**
    * @return the name of the loader, mainly used for logging.
    */
   String getName();

   /**
    * @return the path used by the loader to scan for bundles in the classpath.
    */
   String getScanningPath();

   /**
    * <p>
    * Load the vendor bundles by scanning the classpath starting from the
    * configured scanning path.
    * </p>
    * 
    * @return a list of {@link BundleStorageUnit} deserialized from the JSON
    *         files.
    */
   List<BundleStorageUnit> getVendorBundles();

   /**
    * <p>
    * Load the regular bundles by scanning the classpath starting from the
    * configured scanning path.
    * </p>
    * 
    * @return a list of {@link BundleStorageUnit} deserialized from the JSON
    *         files.
    */
   List<BundleStorageUnit> getRegularBundles();
}
