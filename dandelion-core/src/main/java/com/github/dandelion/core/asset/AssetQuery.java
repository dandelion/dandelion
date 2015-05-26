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
package com.github.dandelion.core.asset;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.cache.CacheEntry;
import com.github.dandelion.core.cache.RequestCache;
import com.github.dandelion.core.reporting.Alert;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.util.UrlUtils;
import com.github.dandelion.core.web.AssetRequestContext;

/**
 * <p>
 * Builder that allows to create and execute queries in order to retrieve all
 * {@link Asset} to be loaded for the current {@link HttpServletRequest}.
 * </p>
 * <p>
 * If caching is enabled, the result of the query is cached in the configured
 * {@link RequestCache} system to be returned faster.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.9.0
 */
public class AssetQuery {

   private static final Logger LOG = LoggerFactory.getLogger(AssetQuery.class);

   /**
    * The Dandelion context.
    */
   private final Context context;

   /**
    * The current request.
    */
   private final HttpServletRequest request;

   /**
    * The desired position in the DOM.
    */
   private AssetDomPosition assetDomPosition;

   public AssetQuery(HttpServletRequest request, Context context) {
      this.context = context;
      this.request = request;
   }

   /**
    * <p>
    * Filters the desired {@link Asset}s on their {@link AssetDomPosition}.
    * </p>
    * <p>
    * If no DOM position is provided, all assets required in the current page
    * will be returned.
    * </p>
    * 
    * @param assetDomPosition
    *           the DOM position used to filter.
    * @return the current instance of {@link AssetQuery}.
    */
   public AssetQuery atPosition(AssetDomPosition assetDomPosition) {
      this.assetDomPosition = assetDomPosition;
      return this;
   }

   /**
    * <p>
    * Actually performs the query to retrieve the desired {@link Asset}s.
    * </p>
    * 
    * @return an ordered set of {@link Asset} to be used in the current
    *         {@link HttpServletRequest}.
    */
   public Set<Asset> perform() {

      Set<Asset> requestedAssets = null;
      String requestCacheKey = null;
      String currentUri = UrlUtils.getCurrentUri(request).toString();

      LOG.debug("Performing query for the request \"{}\"", currentUri);

      if (this.context.getConfiguration().isCachingEnabled()) {
         requestCacheKey = this.context.getCacheManager().generateRequestCacheKey(this.request);
         CacheEntry cacheElement = this.context.getCacheManager().getAssets(requestCacheKey);
         if (cacheElement != null) {
            requestedAssets = cacheElement.getAssets();
         }
      }

      if (requestedAssets == null) {

         // All asset storage units are gathered in an ordered set
         Set<AssetStorageUnit> assetStorageUnits = collectAssetStorageUnits();

         // Convert all asset storage units into assets
         AssetMapper assetMapper = new AssetMapper(context, request);
         requestedAssets = assetMapper.mapToAssets(assetStorageUnits);

         // If caching is enabled, the assocation request<=>assets is cached
         // for
         // quicker future access
         if (this.context.getConfiguration().isCachingEnabled()) {
            requestedAssets = context.getCacheManager()
                  .storeAssets(requestCacheKey, new CacheEntry(currentUri, requestedAssets)).getAssets();
         }
      }

      Set<Asset> filteredAssets = getFilteredAssets(requestedAssets);
      LOG.debug("-> Query returned {} assets: {}", filteredAssets.size(), filteredAssets);
      return filteredAssets;
   }

   public Set<Alert> alerts() {

      Set<Alert> errors = new HashSet<Alert>();
      String currentUri = UrlUtils.getCurrentUri(request).toString();

      LOG.debug("Checking for alerts for the request \"{}\"", currentUri);

      String[] bundleNames = AssetRequestContext.get(this.request).getBundles(true);
      errors.addAll(this.context.getBundleStorage().alertsFor(bundleNames));
      
      LOG.debug("-> Errors found: {}", errors.size());
      return errors;
   }
   
   private Set<Asset> getFilteredAssets(Set<Asset> requestedAssets) {

      // First collect JS from the excluded bundles
      Set<String> excludedJsNames = new HashSet<String>();
      for (String bundleToExclude : AssetRequestContext.get(this.request).getExcludedBundles()) {
         Set<BundleStorageUnit> bsus = this.context.getBundleStorage().bundlesFor(bundleToExclude);
         for (BundleStorageUnit bsu : bsus) {
            excludedJsNames.addAll(bsu.getJsAssetStorageUnitNames());
         }
      }

      // Then add JS "manually" excluded
      for (String assetToExclude : AssetRequestContext.get(this.request).getExcludedJs()) {
         excludedJsNames.add(assetToExclude);
      }

      // Then collect CSS from the excluded bundles
      Set<String> excludedCssNames = new HashSet<String>();
      for (String bundleToExclude : AssetRequestContext.get(this.request).getExcludedBundles()) {
         Set<BundleStorageUnit> bsus = this.context.getBundleStorage().bundlesFor(bundleToExclude);
         for (BundleStorageUnit bsu : bsus) {
            excludedCssNames.addAll(bsu.getCssAssetStorageUnitNames());
         }
      }

      // Then add CSS "manually" excluded
      for (String assetToExclude : AssetRequestContext.get(this.request).getExcludedCss()) {
         excludedCssNames.add(assetToExclude);
      }

      if (this.assetDomPosition != null) {
         requestedAssets = AssetUtils.filtersByDomPosition(requestedAssets, this.assetDomPosition);
      }

      if (!excludedJsNames.isEmpty()) {
         requestedAssets = AssetUtils.filtersByNameAndType(requestedAssets, excludedJsNames, AssetType.js);
      }

      if (!excludedCssNames.isEmpty()) {
         requestedAssets = AssetUtils.filtersByNameAndType(requestedAssets, excludedCssNames, AssetType.css);
      }

      return requestedAssets;
   }

   private Set<AssetStorageUnit> collectAssetStorageUnits() {

      Set<AssetStorageUnit> asus = new LinkedHashSet<AssetStorageUnit>();

      String[] bundleNames = AssetRequestContext.get(this.request).getBundles(true);
      for (BundleStorageUnit bsu : this.context.getBundleStorage().bundlesFor(bundleNames)) {
         asus.addAll(bsu.getAssetStorageUnits());
      }
      return asus;
   }
}
