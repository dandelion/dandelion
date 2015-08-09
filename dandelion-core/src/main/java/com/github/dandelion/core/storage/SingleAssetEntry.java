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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.locator.AssetLocator;
import com.github.dandelion.core.asset.locator.impl.ApiLocator;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.WebConstants;

/**
 * <p>
 * Wrapper entry for a single asset.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 2.0.0
 */
public class SingleAssetEntry implements StorageEntry {

   private static final Logger LOG = LoggerFactory.getLogger(SingleAssetEntry.class);

   /**
    * The asset and all its metadata.
    */
   private final Asset asset;

   /**
    * The asset contents.
    */
   private final String contents;

   /**
    * <p>
    * Creates a new storage entry.
    * </p>
    * 
    * @param asset
    *           The asset metadata.
    * @param contents
    *           The asset contents.
    */
   public SingleAssetEntry(Asset asset, String contents) {
      super();
      this.asset = asset;
      this.contents = contents;
   }

   /**
    * <p>
    * Retrieves the asset stored in this storage entry.
    * </p>
    * 
    * @return all the asset metadata.
    */
   public Asset getAsset() {
      return asset;
   }

   /**
    * <p>
    * Retrieves the asset contents.
    * </p>
    * 
    * @return the asset contents.
    */
   public String getContents() {
      return contents;
   }

   @Override
   public void processContents(HttpServletRequest request) {
      LOG.debug("Processing asset: {}", asset);

      if (asset.isProcessing()) {
         Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);

         String contents = resolveContents(request);

         context.getProcessorManager().process(asset, contents, request);
      }
      else {
         LOG.debug("Processing is disabled for the asset: {}", asset.toLog());
      }
   }

   @Override
   public String resolveContents(HttpServletRequest request) {
      Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);

      String contents = null;
      if (StringUtils.isNotBlank(this.contents)) {
         contents = this.contents;
      }
      else {

         AssetLocator assetLocator = AssetUtils.getAssetLocator(asset, context);

         if (ApiLocator.LOCATION_KEY.equals(assetLocator.getLocationKey())) {
            contents = getContents();
         }
         else {
            contents = assetLocator.getContent(asset, request);
         }
      }

      return contents;
   }
}
