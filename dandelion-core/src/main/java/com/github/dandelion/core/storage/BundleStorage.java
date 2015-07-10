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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.reporting.Alert;
import com.github.dandelion.core.reporting.Alert.AlertType;
import com.github.dandelion.core.reporting.Suggestion;
import com.github.dandelion.core.storage.support.BundleDag;
import com.github.dandelion.core.storage.support.BundleUtils;
import com.github.dandelion.core.storage.support.TopologicalSorter;
import com.github.dandelion.core.util.JsonUtils;
import com.github.dandelion.core.util.ResourceUtils;
import com.github.dandelion.core.util.scanner.ClasspathResourceScanner;

/**
 * <p>
 * Storage for all bundles, backed by a {@link BundleDag} instance.
 * </p>
 * <p>
 * This facade provides several utilities intented to
 * </p>
 * <ul>
 * <li>check the consistency of {@link BundleStorageUnit}</li>
 * <li>build and fill the {@link BundleDag}</li>
 * <li>query the {@link BundleDag} on the lookout for bundles</li>
 * <li>query the {@link BundleDag} on the lookout for alerts</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class BundleStorage {

   private static final Logger LOG = LoggerFactory.getLogger(BundleStorage.class);

   private final BundleDag bundleDag;

   public BundleStorage() {
      this.bundleDag = new BundleDag();
   }

   /**
    * <p>
    * Load all given {@link BundleStorageUnit}s into the {@link BundleDag}.
    * </p>
    * 
    * @param bundleStorageUnits
    *           All bundle storage units to load into the dag.
    * @return the {@link BundleDag} updated with the new
    *         {@link BundleStorageUnit} and {@link AssetStorageUnit}.
    * @throws DandelionException
    *            as soon as a cycle is detected in the bundle DAG.
    */
   public BundleDag storeBundles(List<BundleStorageUnit> bundleStorageUnits) {

      for (BundleStorageUnit bsu : bundleStorageUnits) {

         BundleStorageUnit bsuToAdd = bundleDag.addVertexIfNeeded(bsu);

         // DAG updating and dependencies handling
         if (bsu.getDependencies() != null && !bsu.getDependencies().isEmpty()) {
            for (String dependency : bsu.getDependencies()) {

               BundleStorageUnit to = bundleDag.addVertexIfNeeded(dependency);
               bundleDag.addEdge(bsuToAdd, to);
            }
         }
         else {
            bsuToAdd = bundleDag.addVertexIfNeeded(bsu);
         }

         // Asset updating

         // Let's see if each asset already exists in any bundle
         for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {

            boolean assetAlreadyExists = false;
            for (BundleStorageUnit existingBundle : bundleDag.getVerticies()) {
               for (AssetStorageUnit existingAsu : existingBundle.getAssetStorageUnits()) {

                  // If the asset has both the same name
                  // (case-insensitive) and type, the old one is simply
                  // overriden
                  if (existingAsu.getName().equalsIgnoreCase(asu.getName())
                        && existingAsu.getType().equals(asu.getType())) {

                     LOG.trace(
                           "Replacing asset '{}' ({}) from the bundle '{}' by the asset {} ({}) from the bundle {}.",
                           existingAsu.getName(), existingAsu.getVersion(), existingBundle.getName(), asu.getName(),
                           asu.getVersion(), bsuToAdd.getName());

                     existingAsu.setVersion(asu.getVersion());
                     existingAsu.setLocations(asu.getLocations());
                     existingAsu.setDom(asu.getDom());
                     existingAsu.setBundle(existingBundle.getName());
                     existingAsu.setType(asu.getType());
                     existingAsu.setAttributes(asu.getAttributes());
                     existingAsu.setAttributesOnlyName(asu.getAttributesOnlyName());
                     existingAsu.setCondition(asu.getCondition());
                     existingAsu.setGeneratorUid(asu.getGeneratorUid());
                     assetAlreadyExists = true;
                     break;
                  }
               }

               if (assetAlreadyExists) {
                  break;
               }
            }

            // If the asset doesn't already exist, we just add it to the
            // current bundle
            if (!assetAlreadyExists) {

               LOG.trace("Adding {} '{}' ({}) to the bundle '{}'", asu.getType(), asu.getName(), asu.getVersion(),
                     bsuToAdd.getName());
               bsuToAdd.getAssetStorageUnits().add(asu);
            }
         }
      }

      return bundleDag;
   }

   /**
    * Return the list of labels of bundles according to the topological sort.
    * 
    * @param bundleName
    *           The name of the bundle.
    * 
    * @return The list of bundle names sorted by a topological order. The list
    *         also contains the given bundle name, always in last.
    */
   public Set<BundleStorageUnit> bundlesFor(String bundleName) {
      BundleStorageUnit bsu = bundleDag.getVertex(bundleName);

      if (bsu != null) {
         Set<BundleStorageUnit> retval = null;

         if (bsu.isLeaf()) {
            retval = new HashSet<BundleStorageUnit>(1);
            retval.add(bsu);
         }
         else {
            retval = new LinkedHashSet<BundleStorageUnit>(TopologicalSorter.sort(bsu));
         }

         return retval;
      }

      return Collections.emptySet();
   }

   public Set<Alert> alertsFor(String... requestedBundleNames) {

      Set<Alert> errors = new HashSet<Alert>();

      for (String requestedBundleName : requestedBundleNames) {
         errors.addAll(alertsFor(requestedBundleName, requestedBundleNames));
      }

      return errors;
   }

   public Set<Alert> alertsFor(String requestedBundleName, String... requestedBundleNames) {
      BundleStorageUnit bsu = bundleDag.getVertex(requestedBundleName);
      Set<Alert> alerts = new HashSet<Alert>();
      Alert alert = null;

      // An alert is reported as "missing bundle" whether the requested bundle
      // does not actually exist or if it has been added to the bundle storage
      // via a dependency
      if (bsu == null || bsu.getAssetStorageUnitNames() == null || bsu.getAssetStorageUnitNames().isEmpty()) {
         alert = new Alert(requestedBundleName);
         alert.setAlertType(AlertType.MISSING_BUNDLE);

         alerts.addAll(findSuggestion(alert, alerts, requestedBundleName, requestedBundleNames));
      }

      if (alert != null) {
         alerts.add(alert);
      }

      return alerts;
   }

   public Set<Alert> findSuggestion(Alert alert, Set<Alert> existingAlerts, String bundleName,
         String... requestedBundleNames) {

      LOG.trace("Scanning classpath for any suggestions with the name \"{}\" (exact match)", bundleName + ".json");
      Set<String> suggestions = ClasspathResourceScanner.findResourcePaths("dandelion", null, bundleName + ".json");

      if (suggestions != null) {
         for (String suggestion : suggestions) {

            try {
               Suggestion sug = new Suggestion();
               String json = ResourceUtils.getContentFromInputStream(Thread.currentThread().getContextClassLoader()
                     .getResourceAsStream(suggestion));

               sug.setSuggestedRawBundle(json);

               ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
               InputStream configFileStream = classLoader.getResourceAsStream(suggestion);
               BundleStorageUnit suggestedBsu = null;
               suggestedBsu = JsonUtils.read(configFileStream, BundleStorageUnit.class);
               suggestedBsu.setRelativePath(suggestion);
               BundleUtils.finalize(suggestedBsu, null);

               sug.setSuggestedBundle(suggestedBsu);
               alert.addSuggestion(sug);

               if (suggestedBsu != null && suggestedBsu.getDependencies() != null) {
                  for (String dependency : suggestedBsu.getDependencies()) {
                     if (!Arrays.asList(requestedBundleNames).contains(dependency)) {
                        existingAlerts.addAll(alertsFor(dependency, requestedBundleNames));
                     }
                  }
               }
            }
            catch (IOException e) {
               throw new DandelionException("Unable to read JSON file at " + suggestion, e);
            }
         }
      }
      else {
         LOG.trace("No suggestion found");
      }

      return existingAlerts;
   }

   public Set<BundleStorageUnit> bundlesFor(String... bundleNames) {

      Set<BundleStorageUnit> retval = new LinkedHashSet<BundleStorageUnit>();
      for (String bundleName : bundleNames) {
         retval.addAll(bundlesFor(bundleName.trim()));
      }

      return retval;
   }

   /**
    * @return the internal {@link BundleDag} used to store the bundle graph.
    */
   public BundleDag getBundleDag() {
      return bundleDag;
   }

   public void consolidateBundles(List<BundleStorageUnit> allBundles) {

      for (BundleStorageUnit bsu : bundleDag.getVerticies()) {

         for (BundleStorageUnit rawBsu : allBundles) {
            if (rawBsu.getName().equalsIgnoreCase(bsu.getName())) {

               bsu.setDependencies(rawBsu.getDependencies());
               bsu.setAssetStorageUnits(rawBsu.getAssetStorageUnits());
               bsu.setRelativePath(rawBsu.getRelativePath());
               bsu.setBundleLoaderOrigin(rawBsu.getBundleLoaderOrigin());
               bsu.setVendor(rawBsu.isVendor());
               break;
            }
         }
      }
   }
}
