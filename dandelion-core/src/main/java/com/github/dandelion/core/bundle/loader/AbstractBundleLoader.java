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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.bundle.loader.strategy.JsonBundleLoadingStrategy;
import com.github.dandelion.core.bundle.loader.strategy.LoadingStrategy;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.StringBuilderUtils;

/**
 * <p>
 * Abstract bundle loader in charge of loading JSON definitions of bundle.
 * </p>
 * <p>
 * The JSON definitions are scanned in the folder specified by the
 * {@link #getScanningPath()} method.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractBundleLoader implements BundleLoader {

   /**
    * The Dandelion context.
    */
   protected final Context context;

   /**
    * Whether the bundle loader used in a standalone component.
    */
   protected final boolean usedStandalone;

   public AbstractBundleLoader(Context context, boolean usedStandalone) {
      this.context = context;
      this.usedStandalone = usedStandalone;
   }

   /**
    * @return the {@link Logger} bound to the actual implementation.
    */
   protected abstract Logger getLogger();

   @Override
   public List<BundleStorageUnit> getVendorBundles() {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();

      if (!this.usedStandalone) {
         bundles.addAll(loadVendorBundles());
      }

      // Bundle/asset post-processing
      for (BundleStorageUnit bsu : bundles) {
         bsu.setBundleLoaderOrigin(getName());
         bsu.setVendor(true);
         for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
            asu.setBundle(bsu.getName());
            asu.setVendor(true);
         }
      }

      return bundles;
   }

   @Override
   public List<BundleStorageUnit> getRegularBundles() {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();
      bundles.addAll(loadRegularBundles());

      // Bundle/asset post-processing
      for (BundleStorageUnit bsu : bundles) {
         bsu.setBundleLoaderOrigin(getName());
         for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
            asu.setBundle(bsu.getName());
         }
      }

      return bundles;
   }

   private List<BundleStorageUnit> loadVendorBundles() {
      getLogger().debug("Scanning vendor bundles in {}", getVendorBundlesPath());
      return loadBundles(getVendorBundlesPath(), null);
   }

   private List<BundleStorageUnit> loadRegularBundles() {
      Set<String> excludedPaths = new HashSet<String>();
      excludedPaths.add(getVendorBundlesPath());
      for (BundleLoader loader : context.getBundleLoaders()) {
         if (loader instanceof AbstractBundleLoader) {
            String path = ((AbstractBundleLoader) loader).getVendorBundlesPath();
            if (!path.equalsIgnoreCase(getScanningPath())) {
               excludedPaths.add(path);
            }
         }
      }

      getLogger().debug("Scanning vendor bundles in {}, by excluding {}", getRegularBundlesPath(), excludedPaths);
      return loadBundles(getRegularBundlesPath(), excludedPaths);
   }

   private List<BundleStorageUnit> loadBundles(String bundleLocation, Set<String> excludedPaths) {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();
      LoadingStrategy jsonLoadingStrategy = new JsonBundleLoadingStrategy(context);

      getLogger().debug("Scanning \"{}\" for JSON-formatted bundles...", bundleLocation);
      Set<String> resourcePaths = jsonLoadingStrategy.getResourcePaths(bundleLocation, excludedPaths);
      getLogger().debug("{} bundles selected", resourcePaths.size());

      if (!resourcePaths.isEmpty()) {
         List<BundleStorageUnit> bsus = jsonLoadingStrategy.mapToBundles(resourcePaths);
         bundles.addAll(bsus);
      }

      if (resourcePaths.isEmpty()) {
         getLogger().debug("No bundle found in {}", bundleLocation);
      }

      return bundles;
   }

   private String getRegularBundlesPath() {
      StringBuilder regularBundlesPath = new StringBuilder(getBundleBaseLocation());
      regularBundlesPath.append(getScanningPath());
      return regularBundlesPath.toString();
   }

   private String getVendorBundlesPath() {
      StringBuilder vendorBundlesPath = new StringBuilder(getBundleBaseLocation());
      vendorBundlesPath.append(getScanningPath());
      vendorBundlesPath.append("/vendor");
      return vendorBundlesPath.toString();
   }

   private StringBuilder getBundleBaseLocation() {

      StringBuilder bundleBaseLocation = new StringBuilder(context.getConfiguration().getBundleLocation());
      if (StringBuilderUtils.isNotBlank(bundleBaseLocation)) {
         bundleBaseLocation.append('/');
      }

      return bundleBaseLocation;
   }
}
