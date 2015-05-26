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

package com.github.dandelion.core.asset.locator;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.AssetProcessor;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.AssetRequestContext;

/**
 * <p>
 * Abstract base class for all {@link AssetLocator}.
 * 
 * <p>
 * By default, all {@link AssetLocator} are active and caching of assets is not
 * forced. It's up to the implementation to adapt this behaviour.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractAssetLocator implements AssetLocator {

   protected boolean active = true;
   protected Context context;

   @Override
   public boolean isCachingForced() {
      return false;
   }

   @Override
   public void initLocator(Context context) {
      this.context = context;
   }

   /**
    * <p>
    * Checks that the asset if properly configured before computing its
    * location.
    * 
    * @param AssetStorageUnit
    *           The asset storage unit from which the location should be
    *           extracted.
    * @param request
    *           The current HTTP request.
    * @throws DandelionException
    *            if the location associated with the selected location key is
    *            null or empty.
    */
   @Override
   public String getLocation(AssetStorageUnit asu, HttpServletRequest request) {
      String location = asu.getLocations().get(getLocationKey());
      if (StringUtils.isBlank(location)) {
         StringBuilder sb = new StringBuilder("The asset ");
         sb.append(asu.toLog());
         sb.append(" configured with a '");
         sb.append(getLocationKey());
         sb.append("' location key has a blank location. Please correct this location in the corresponding JSON file.");
         throw new DandelionException(sb.toString());
      }

      return doGetLocation(asu, request);
   }

   public abstract String doGetLocation(AssetStorageUnit asu, HttpServletRequest request);

   /**
    * <p>
    * Returns the content of the given {@link Asset}, using its
    * {@link Asset#getFinalLocation()} value.
    * 
    * <p>
    * Note that this method can be used to access the asset's content after the
    * {@link AssetProcessor}'s execution.
    * 
    * @param asset
    *           The asset from which the content should be extracted.
    * @param request
    *           The current HTTP request.
    * @return the raw content (unprocessed) of the passed {@link Asset}.
    */
   public String getContent(Asset asset, HttpServletRequest request) {

      Map<String, Object> parameters = AssetRequestContext.get(request).getParameters(asset.getName());
      String content = doGetContent(asset, parameters, request);

      // Apply variable replacement
      if (!parameters.isEmpty()) {
         for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue().toString());
         }
      }

      return content;
   }

   protected abstract String doGetContent(Asset asset, Map<String, Object> parameters,
         HttpServletRequest request);
}
