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
import com.github.dandelion.core.storage.SingleAssetEntry;
import com.github.dandelion.core.util.DigestUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Abstract base class for all merging strategies.
 * </p>
 * <p>
 * It is recommended for extensions to extend this class instead of
 * {@link AssetMergingStrategy}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 2.0.0
 */
public abstract class AbstractAssetMergingStrategy implements AssetMergingStrategy {

   protected static final String MERGED_ASSET_NAME = "merged";
   
   /**
    * The Dandelion context.
    */
   protected Context context;

   @Override
   public void init(Context context) {
      this.context = context;
   }

   /**
    * @return the name of the merged asset.
    */
   protected String getMergedAssetName() {
      return MERGED_ASSET_NAME;
   }

   /**
    * <p>
    * Computes the version to be used in the final location of the merged
    * assets.
    * </p>
    * 
    * @param assets
    *           The assets to be merged.
    * @param request
    *           The current requets.
    * @return The version hash corresponding to all provided assets.
    */
   public String getVersion(Set<Asset> assets, HttpServletRequest request) {

      StringBuilder sb = new StringBuilder();
      for (Asset asset : assets) {
         SingleAssetEntry entry = (SingleAssetEntry) this.context.getAssetStorage().get(asset.getStorageKey());
         String contents = entry.resolveContents(request);
         if (StringUtils.isNotBlank(contents)) {
            sb.append(contents);
         }
      }

      return DigestUtils.md5Digest(sb.toString());
   }
}
