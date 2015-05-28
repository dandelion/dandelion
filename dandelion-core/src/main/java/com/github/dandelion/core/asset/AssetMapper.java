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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.locator.AssetLocator;
import com.github.dandelion.core.asset.versioning.AssetVersioningStrategy;
import com.github.dandelion.core.cache.RequestCache;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.StorageEntry;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Mapper that converts {@link AssetStorageUnit}s to {@link Asset}s.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetMapper {

   private static final Logger LOG = LoggerFactory.getLogger(AssetMapper.class);

   /**
    * The Dandelion context.
    */
   private final Context context;

   /**
    * The current HTTP request.
    */
   private final HttpServletRequest request;

   public AssetMapper(Context context, HttpServletRequest request) {
      this.context = context;
      this.request = request;
   }

   /**
    * <p>
    * The same as {@link #mapToAssets(Set)} but for a set of
    * {@link AssetStorageUnit}s.
    * </p>
    * 
    * @param asus
    *           The set of {@link AssetStorageUnit}s to map to a set of
    *           {@link Asset}s.
    * @return a set of mapped {@link Asset}s.
    */
   public Set<Asset> mapToAssets(Set<AssetStorageUnit> asus) {
      Set<Asset> retval = new LinkedHashSet<Asset>();

      for (AssetStorageUnit asu : asus) {
         retval.add(mapToAsset(asu));
      }

      return retval;
   }

   /**
    * <p>
    * Maps an {@link AssetStorageUnit} to an {@link Asset}.
    * </p>
    * <p>
    * Depending on how the {@link AssetStorageUnit} is configured, the
    * {@link Asset} will contains resolved locations and its content will be
    * cached in the configured {@link RequestCache}.
    * </p>
    * 
    * @param asu
    *           The {@link AssetStorageUnit} to map to an {@link Asset}.
    * @return the mapped {@link Asset}.
    * @throws DandelionException
    *            if the {@link AssetStorageUnit} is not configured properly.
    */
   public Asset mapToAsset(AssetStorageUnit asu) {

      Asset asset = new Asset(asu);

      LOG.trace("Resolving location for the asset {}", asset.toLog());

      // Resolve location key and location
      String locationKey = getLocationKey(asu);
      asset.setConfigLocationKey(locationKey);

      AssetLocator assetLocator = AssetUtils.getAssetLocator(asset, context);

      String location = assetLocator.getLocation(asu, request);
      LOG.trace("Locator '{}' will be applied on the asset {}.", assetLocator.getClass().getSimpleName(), asu.toLog());
      asset.setProcessedConfigLocation(location);
      asset.setConfigLocation(asu.getLocations().get(locationKey));
      asset.setGeneratorUid(asu.getGeneratorUid());
      asset.setStorageKey(AssetUtils.generateStorageKey(asset, request));

      // Vendor assets are served as-is, no need to store them
      if (asset.isNotVendor()) {

         // Update the asset storage with minified contents
         if (context.getConfiguration().isAssetMinificationEnabled()) {
            asset = this.context.getProcessorManager().process(asset, request);
         }
         // Update the asset storage with normal contents
         else if (context.getConfiguration().isAssetAutoVersioningEnabled() || assetLocator.isCachingForced()) {
            String contents = assetLocator.getContent(asset, request);
            this.context.getAssetStorage().put(asset.getStorageKey(), new StorageEntry(asset, contents));
         }
      }

      asset.setName(getName(asu, location));
      asset.setType(getType(asu, location));
      asset.setVersion(getVersion(asset));
      asset.setFinalLocation(getFinalLocation(asset, assetLocator));

      return asset;
   }

   private String getLocationKey(AssetStorageUnit asu) {

      if (asu.getLocations() == null || asu.getLocations().isEmpty()) {
         StringBuilder msg = new StringBuilder("No location is configured for the asset ");
         msg.append(asu.toLog());
         msg.append(". Please add at least one location in the corresponding JSON file.");
         throw new DandelionException(msg.toString());
      }

      String locationKey = null;

      if (asu.getLocations().size() == 1) {
         // use the unique location if needed
         locationKey = asu.getLocations().entrySet().iterator().next().getKey();
      }
      else {
         // otherwise search for the first matching location key among the
         // configured ones
         for (String searchedLocationKey : this.context.getConfiguration().getAssetLocationsResolutionStrategy()) {
            if (asu.getLocations().containsKey(searchedLocationKey)) {
               String location = asu.getLocations().get(searchedLocationKey);
               if (location != null && !location.isEmpty()) {
                  locationKey = searchedLocationKey;
                  break;
               }
            }
         }
      }
      LOG.trace("Location key '{}' selected for the asset {}", locationKey, asu.toString());

      return locationKey;
   }

   /**
    * <p>
    * Computes the final location of the provided {@link Asset}. This location
    * is the one used in the HTML source code.
    * </p>
    * 
    * @param asset
    *           The asset for which the final location is to be computed.
    * @param locator
    *           The selected asset locator.
    * @return The final location of the asset.
    */
   private String getFinalLocation(Asset asset, AssetLocator locator) {

      if (asset.isNotVendor()
            && (this.context.getConfiguration().isAssetAutoVersioningEnabled() || locator.isCachingForced() || this.context
                  .getConfiguration().isAssetMinificationEnabled())) {
         return AssetUtils.getAssetFinalLocation(request, asset, null);
      }
      else {
         return asset.getProcessedConfigLocation();
      }
   }

   /**
    * <p>
    * Returns the version of the provided asset:
    * </p>
    * <ol>
    * <li>first by selecting the version specified in the bundle definition</li>
    * <li>or by applying the active {@link AssetVersioningStrategy} if automatic
    * versioning is enabled</li>
    * <li>or finally a version called <code>UNDEFINED_VERSION</code> that
    * indicates the versioning information is missing</li>
    * </ol>
    *
    * @param asset
    *           The asset to extract the version from.
    * @return the version of the asset.
    */
   private String getVersion(Asset asset) {

      // First: manual versioning if specified, coming from the
      // AssetStorageUnit
      if (StringUtils.isNotBlank(asset.getVersion())) {
         return asset.getVersion();
      }

      // If enabled, auto versioning takes precedence over manual one
      if (this.context.getConfiguration().isAssetAutoVersioningEnabled()) {
         AssetVersioningStrategy avs = this.context.getActiveVersioningStrategy();
         return avs.getAssetVersion(asset);
      }

      // Finally, a clear version indicating some configuration is missing
      return "UNDEFINED_VERSION";
   }

   /**
    * <p>
    * Computes the final asset name:
    * <ol>
    * <li>First by reading the asset storage unit if the name if specified</li>
    * <li>Otherwise by extracting the asset name from its first location</li>
    * </ol>
    * </p>
    * 
    * @param asu
    *           The asset storage unit definition coming from the bundle
    *           definition.
    * @param location
    *           The selected location.
    * 
    * @return the name of the asset.
    */
   private String getName(AssetStorageUnit asu, String location) {
      if (StringUtils.isNotBlank(asu.getName())) {
         return asu.getName();
      }
      else {
         return PathUtils.extractLowerCasedName(location);
      }
   }

   /**
    * <p>
    * Computes the asset type:
    * <ol>
    * <li>First by reading the asset storage unit definition, if the type is
    * manually specified</li>
    * <li>Otherwise by extracting the asset type from its first location</li>
    * </ol>
    * </p>
    * 
    * @param asu
    *           The asset storage unit definition coming from the bundle
    *           definition.
    * @param location
    *           The selected location.
    * @return the type of the asset.
    */
   private AssetType getType(AssetStorageUnit asu, String location) {
      if (asu.getType() != null) {
         return asu.getType();
      }
      else {
         return AssetType.extractFromAssetLocation(location);
      }
   }
}
