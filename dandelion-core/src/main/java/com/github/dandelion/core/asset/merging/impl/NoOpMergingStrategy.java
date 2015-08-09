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
package com.github.dandelion.core.asset.merging.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.locator.impl.ApiLocator;
import com.github.dandelion.core.asset.merging.AbstractAssetMergingStrategy;
import com.github.dandelion.core.config.Profile;
import com.github.dandelion.core.storage.SingleAssetEntry;
import com.github.dandelion.core.storage.StorageEntry;
import com.github.dandelion.core.util.AssetUtils;

/**
 * <p>
 * Default strategy used with the "dev" {@link Profile}.
 * </p>
 * <p>
 * This strategy actually performs no merging. All assets are returned as is.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 2.0.0
 */
public class NoOpMergingStrategy extends AbstractAssetMergingStrategy {

   public static final String NAME = "noop";

   @Override
   public String getName() {
      return NAME;
   }

   @Override
   public Set<Asset> prepareStorageAndGet(Set<Asset> rawAssets, HttpServletRequest request) {

      Set<Asset> retval = new LinkedHashSet<Asset>();

      StorageEntry entry = null;
      for (Asset asset : rawAssets) {
         if (ApiLocator.LOCATION_KEY.equalsIgnoreCase(asset.getConfigLocationKey())) {
            String contents = AssetUtils.getAssetLocator(asset, context).getContent(asset, request);
            entry = new SingleAssetEntry(asset, contents);
         }
         else {
            entry = new SingleAssetEntry(asset, null);
         }
         this.context.getAssetStorage().put(asset.getStorageKey(), entry);
         retval.add(asset);
      }

      return retval;
   }
}
