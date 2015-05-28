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
package com.github.dandelion.core.storage.support;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.locator.impl.ApiLocator;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.BundleStorageLogBuilder;
import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * TODO
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class BundleUtils {

   private static final Logger LOG = LoggerFactory.getLogger(BundleUtils.class);

   /**
    * <p>
    * Performs several checks on the passed {@link BundleStorageUnit} and
    * buffers errors in the {@link BundleStorageLogBuilder}.
    * </p>
    * 
    * @param bsu
    *           The bundle to check.
    * @param log
    *           The builder in which errors are buffered.
    * @return {@code true} is the bundle is valid, {@code false} otherwise.
    */
   public static boolean isValid(BundleStorageUnit bsu, BundleStorageLogBuilder log) {

      boolean isValid = true;

      // Check that the bundle contains at least one asset
      if (bsu.getAssetStorageUnits() == null || bsu.getAssetStorageUnits().isEmpty()) {
         log.error("- Empty bundle", "[" + bsu.getName() + "] The bundle \"" + bsu.getName()
               + "\" is empty. You would better remove it.");
         isValid = false;
      }

      // Check that every asset of every bundle contains at least one
      // locationKey/location pair because both name and type will be deducted
      // from it
      for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {

         // Check locations
         if (asu.getLocations().isEmpty()) {
            log.error("- Missing asset location(s)", "[" + bsu.getName()
                  + "] The bundle contain asset with no location whereas it is required.");
            isValid = false;
         }
         else {
            for (String locationKey : asu.getLocations().keySet()) {

               String rawLocation = asu.getLocations().get(locationKey);

               if (StringUtils.isBlank(locationKey)) {
                  log.error(
                        "- Missing location key",
                        "["
                              + bsu.getName()
                              + "] One of the assets contained in this bundle has a location with no location key. Please correct it before continuing.");
                  isValid = false;
               }

               if (StringUtils.isBlank(rawLocation)) {
                  log.error("- Missing asset location", "[" + bsu.getName()
                        + "] One of the assets contained in the bundle \"" + bsu.getName()
                        + "\" has an empty location. Please correct it before continuing.");
                  isValid = false;
               }

               // For all locations except api
               if (!locationKey.toLowerCase().trim().equals(ApiLocator.LOCATION_KEY)) {
                  // If the type is not explicitely declared, it must be
                  // inferred from the location
                  if (asu.getType() == null) {
                     boolean extensionNotFound = true;
                     for (AssetType assetType : AssetType.values()) {
                        if (rawLocation.toLowerCase().endsWith("." + assetType.toString())) {
                           extensionNotFound = false;
                           break;
                        }
                     }
                     if (extensionNotFound) {
                        log.error("- Missing extension", "[" + bsu.getName()
                              + "] The extension is required in all locations.");
                        isValid = false;
                     }
                  }
               }

               // Special checks for api locations
               if (locationKey.toLowerCase().trim().equals(ApiLocator.LOCATION_KEY)) {

                  // The asset name is required
                  if (StringUtils.isBlank(asu.getName())) {
                     log.error("- Missing name", "[" + bsu.getName() + "] Assets configured with the \""
                           + ApiLocator.LOCATION_KEY + "\" location key must have an explicit name");
                     isValid = false;
                  }

                  // The asset type is required
                  if (asu.getType() == null) {
                     log.error("- Missing type", "[" + bsu.getName() + "] Assets configured with the \""
                           + ApiLocator.LOCATION_KEY + "\" location key must have an explicit type");
                     isValid = false;
                  }
               }
            }
         }
      }

      return isValid;
   }

   /**
    * <p>
    * Performs several initializations on {@link BundleStorageUnit} in order for
    * them to be consistent before feeding the {@link BundleDag}.
    * </p>
    * 
    * @param loadedBundles
    * @param context
    */
   public static void finalize(BundleStorageUnit bsu, Context context) {

      LOG.trace("Finalizing configuration of bundle \"{}\"", bsu);

      // The name of the bundle is extracted from its path if not
      // specified
      if (StringUtils.isBlank(bsu.getName())) {
         String extractedName = PathUtils.extractLowerCasedName(bsu.getRelativePath());
         bsu.setName(extractedName);
         LOG.trace("Name of the bundle extracted from its path: \"{}\"", extractedName);
      }

      if (bsu.getAssetStorageUnits() != null) {

         for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {

            String firstFoundLocation = asu.getLocations().values().iterator().next();
            if (StringUtils.isBlank(asu.getName())) {
               String extractedName = PathUtils.extractLowerCasedName(firstFoundLocation);
               asu.setName(extractedName);
               LOG.trace("Name of the asset extracted from its first location: \"{}\"", extractedName);
            }
            if (asu.getType() == null) {
               AssetType extractedType = AssetType.extractFromAssetLocation(firstFoundLocation);
               asu.setType(extractedType);
               LOG.trace("Type of the asset extracted from its first location: \"{}\"", extractedType);
            }
            
            for(Entry<String, String> entry : asu.getLocations().entrySet()) {
               if(entry.getKey().toLowerCase().trim().equals(ApiLocator.LOCATION_KEY)) {
                  asu.setGeneratorUid(entry.getValue().toLowerCase().trim());
               }
            }
         }

         // Perform variable substitutions only if the user uses a
         // configuration file
         if (context != null && context.getConfiguration().getProperties() != null) {
            for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
               Map<String, String> locations = asu.getLocations();
               for (Entry<String, String> locationEntry : asu.getLocations().entrySet()) {
                  locations.put(locationEntry.getKey(),
                        StringUtils.substitute(locationEntry.getValue(), context.getConfiguration().getProperties()));
               }
            }
         }
      }
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private BundleUtils() {
      throw new AssertionError();
   }
}
