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
package com.github.dandelion.core.asset.merging;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.storage.AssetStorage;

/**
 * <p>
 * SPI for an asset merging strategy.
 * </p>
 * <p>
 * It is recommended for extensions to extend
 * {@link AbstractAssetMergingStrategy} instead of this interface.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 2.0.0
 * @see DandelionConfig#ASSET_MERGING_STRATEGY
 */
public interface AssetMergingStrategy {

   /**
    * @return the name of the strategy.
    */
   String getName();

   /**
    * <p>
    * Initiliazes the merging strategy by injecting the Dandelion context.
    * </p>
    * 
    * @param context
    *           The Dandelion context.
    */
   void init(Context context);

   /**
    * <p>
    * Actually performs 2 steps:
    * </p>
    * <ol>
    * <li>Prepare the {@link AssetStorage} with all required assets. The set of
    * assets will change depending on the actual implementation of the strategy
    * </li>
    * <li>Build the set of assets that will be displayed in the HTML source code
    * </li>
    * </ol>
    * 
    * @param assets
    *           All assets needed in the HTML page.
    * @param request
    *           The current request.
    * @return the set of assets that will be displayed client-side.
    */
   public Set<Asset> prepareStorageAndGet(Set<Asset> assets, HttpServletRequest request);
}
