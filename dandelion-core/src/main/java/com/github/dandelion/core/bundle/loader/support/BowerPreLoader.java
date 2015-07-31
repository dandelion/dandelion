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
package com.github.dandelion.core.bundle.loader.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.locator.impl.ClasspathLocator;
import com.github.dandelion.core.asset.locator.impl.FileLocator;
import com.github.dandelion.core.asset.locator.impl.WebappLocator;
import com.github.dandelion.core.bundle.loader.AbstractBundlePreLoader;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.storage.support.BundleUtils;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.util.ClassUtils;
import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.util.scanner.ClasspathResourceScanner;
import com.github.dandelion.core.util.scanner.FileSystemResourceScanner;
import com.github.dandelion.core.util.scanner.WebResourceScanner;

/**
 * <p>
 * Extra loader intended to scan for Bower components and convert the Bower
 * manifests into {@link BundleStorageUnit}.
 * </p>
 * <p>
 * Note that bower.json files that do not contain a {@code main} parameter won't
 * be loaded.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 * @see DandelionConfig#BOWER_COMPONENTS_LOCATION
 */
public class BowerPreLoader extends AbstractBundlePreLoader {

   private static final Logger LOG = LoggerFactory.getLogger(BowerPreLoader.class);
   private static final String BOWER_COMPONENTS_FOLDER = "bower_components";
   private static final String BOWER_MANIFEST_FILENAME = "bower.json";

   public static final String PRELOADER_NAME = "bower";

   /**
    * The mapper used to read Bower manifests (bower.json).
    */
   private final ObjectMapper mapper;

   public BowerPreLoader() {
      this.mapper = new ObjectMapper();
      this.mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
   }

   @Override
   public String getName() {
      return PRELOADER_NAME;
   }

   @Override
   public List<BundleStorageUnit> getExtraBundles() {

      List<BundleStorageUnit> extraBundles = new ArrayList<BundleStorageUnit>();

      String bowerComponentsLocation = this.context.getConfiguration().getBowerComponentsLocation();

      // First scan at the desired location, if specified
      if (StringUtils.isNotBlank(bowerComponentsLocation)) {
         LocationType locationType = resolveLocationType(bowerComponentsLocation);
         LOG.debug("Scanning for Bower components within \"{}\"", bowerComponentsLocation);

         switch (locationType) {
         case webapp:
            extraBundles.addAll(getBundlesFromWebapp(bowerComponentsLocation));
            break;
         case classpath:
            extraBundles.addAll(getBundlesFromClasspath(bowerComponentsLocation));
            break;
         case file:
            extraBundles.addAll(getBundlesFromFilesystem(bowerComponentsLocation));
            break;
         }
      }
      // If no location is specified, first scan for the "bower_components"
      // folder within the root directory of the web application
      else {
         LOG.debug("Searching for a \"{}\" directory within the web application...", BOWER_COMPONENTS_FOLDER);
         String bowerFolder = WebResourceScanner.findResourcePath(this.context.getFilterConfig().getServletContext(),
               "/", BOWER_COMPONENTS_FOLDER);
         if (StringUtils.isNotBlank(bowerFolder)) {
            LOG.debug("Scanning for Bower components within \"{}\"", bowerFolder);
            extraBundles.addAll(getBundlesFromWebapp(bowerFolder));
         }
         // If no "bower_components" folder is found within the web app, try
         // within the classpath
         else {
            LOG.debug("No \"{}\" directory found within the web application directory. Trying in the classpath...",
                  BOWER_COMPONENTS_FOLDER);
            bowerFolder = ClasspathResourceScanner.findResourcePath("", BOWER_COMPONENTS_FOLDER);
            extraBundles.addAll(getBundlesFromClasspath(bowerFolder));
         }
      }

      return extraBundles;
   }

   public List<BundleStorageUnit> getBundlesFromFilesystem(String rootLocation) {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();
      Set<String> resourcePaths = null;
      resourcePaths = FileSystemResourceScanner
            .findResourcePaths(rootLocation.replace(FileSystemResourceScanner.PREFIX, ""), BOWER_MANIFEST_FILENAME);

      for (String bowerManifest : resourcePaths) {
         try {
            URL bowerManifestUrl = new URL(FileSystemResourceScanner.PREFIX + bowerManifest);
            BowerManifest bowerConf = mapper.readValue(bowerManifestUrl, BowerManifest.class);

            if (bowerConf.getMain() != null) {
               LOG.debug("Bower component found: \"{}\"", bowerConf.getName());
               BundleStorageUnit bsu = mapToBundle(bowerConf, rootLocation);
               BundleUtils.finalize(bsu, this.context);
               bsu.setRelativePath(bowerManifestUrl.toString());
               LOG.trace("Parsed bundle \"{}\" ({})", bsu.getName(), bsu);
               bundles.add(bsu);
            }
            else {
               LOG.debug("No \"main\" parameter was found in the \"{}\" file", bowerManifest);
            }
         }
         catch (IOException e) {
            LOG.warn("Unable to convert the \"{}\" Bower component to a bundle", e);
         }
      }
      return bundles;
   }

   public List<BundleStorageUnit> getBundlesFromClasspath(String rootLocation) {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();
      Set<String> resourcePaths = null;

      if (rootLocation == null) {
         return bundles;
      }

      resourcePaths = ClasspathResourceScanner.findResourcePaths(
            rootLocation.replace(ClasspathResourceScanner.PREFIX, ""), null, BOWER_MANIFEST_FILENAME);

      ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

      for (String bowerManifest : resourcePaths) {
         try {
            URL bowerManifestUrl = classLoader.getResource(bowerManifest);
            BowerManifest bowerConf = mapper.readValue(bowerManifestUrl, BowerManifest.class);

            if (bowerConf.getMain() != null) {
               LOG.debug("Bower component found: \"{}\"", bowerConf.getName());
               BundleStorageUnit bsu = mapToBundle(bowerConf, rootLocation);
               BundleUtils.finalize(bsu, this.context);
               bsu.setRelativePath(bowerManifestUrl.toString());
               LOG.trace("Parsed bundle \"{}\" ({})", bsu.getName(), bsu);
               bundles.add(bsu);
            }
            else {
               LOG.debug("No \"main\" parameter was found in the \"{}\" file", bowerManifest);
            }
         }
         catch (IOException e) {
            LOG.warn("Unable to convert the \"{}\" Bower component to a bundle", e);
         }
      }

      return bundles;
   }

   public List<BundleStorageUnit> getBundlesFromWebapp(String rootLocation) {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();
      Set<String> resourcePaths = null;
      resourcePaths = WebResourceScanner.findResourcePaths(this.context.getFilterConfig().getServletContext(),
            rootLocation, BOWER_MANIFEST_FILENAME);
      for (String bowerManifest : resourcePaths) {
         try {
            URL bowerManifestUrl = this.context.getFilterConfig().getServletContext().getResource(bowerManifest);
            BowerManifest bowerConf = mapper.readValue(bowerManifestUrl, BowerManifest.class);

            if (bowerConf.getMain() != null) {
               LOG.debug("Bower component found: \"{}\"", bowerConf.getName());
               BundleStorageUnit bsu = mapToBundle(bowerConf, rootLocation);
               bsu.setRelativePath(bowerManifestUrl.toString());
               BundleUtils.finalize(bsu, this.context);
               LOG.trace("Parsed bundle \"{}\" ({})", bsu.getName(), bsu);
               bundles.add(bsu);
            }
            else {
               LOG.debug("No \"main\" parameter was found in the \"{}\" file", bowerManifest);
            }
         }
         catch (IOException e) {
            LOG.warn("Unable to convert the \"{}\" Bower component to a bundle", e);
         }
      }

      return bundles;
   }

   public BundleStorageUnit mapToBundle(BowerManifest bowerConf, String bowerComponentsLocation) {

      Set<AssetStorageUnit> asus = new HashSet<AssetStorageUnit>();
      LocationType locationType = resolveLocationType(bowerComponentsLocation);
      BundleStorageUnit bsu = new BundleStorageUnit();
      bsu.setBundleLoaderOrigin(getName());
      bsu.setVendor(true);
      bsu.setName(bowerConf.getName());
      
      if (bowerConf.getDependencies() != null) {
         bsu.setDependencies(new ArrayList<String>(bowerConf.getDependencies().keySet()));
      }
      for (String mainAsset : bowerConf.getMain()) {

         String extension = AssetUtils.getExtension(mainAsset.toLowerCase());

         if (AssetType.getCompatibleExtensions().contains(extension)) {

            AssetStorageUnit asu = new AssetStorageUnit();
            asu.setName(PathUtils.extractLowerCasedName(mainAsset));
            asu.setVersion(bowerConf.getVersion());
            asu.setBundle(bsu.getName());
            asu.setVendor(true);

            Map<String, String> locations = new HashMap<String, String>();
            switch (locationType) {
            case classpath:
               locations.put(ClasspathLocator.LOCATION_KEY,
                     bowerComponentsLocation.replace(ClasspathResourceScanner.PREFIX, "") + bowerConf.getName() + "/"
                           + mainAsset);
               break;
            case file:
               locations.put(FileLocator.LOCATION_KEY,
                     bowerComponentsLocation.replace(FileSystemResourceScanner.PREFIX, "") + bowerConf.getName() + "/"
                           + mainAsset);
               break;
            case webapp:
               String processedLocation = !bowerComponentsLocation.startsWith("/") ? "/" + bowerComponentsLocation
                     : bowerComponentsLocation;
               locations.put(WebappLocator.LOCATION_KEY, processedLocation + bowerConf.getName() + "/" + mainAsset);
               break;
            default:
               break;

            }
            asu.setLocations(locations);
            asus.add(asu);
         }
         else {
            LOG.debug("The asset type is not supported yet (\"{}\"", extension);
         }
      }

      bsu.setAssetStorageUnits(asus);
      return bsu;
   }

   /**
    * <p>
    * Resolves the type of location of the bower_components directory using the
    * prefix of the {@link DandelionConfig#BOWER_COMPONENTS_LOCATION} option.
    * </p>
    * <p>
    * Always fallback to {@link LocationType#webapp}.
    * </p>
    * 
    * @param bowerComponentsLocation
    *           The location of the bower_components set in the configuration
    *           option.
    * @return the location type.
    */
   public LocationType resolveLocationType(String bowerComponentsLocation) {
      if (bowerComponentsLocation.startsWith(ClasspathResourceScanner.PREFIX)) {
         return LocationType.classpath;
      }
      else if (bowerComponentsLocation.startsWith(FileSystemResourceScanner.PREFIX)) {
         return LocationType.file;
      }
      else {
         return LocationType.webapp;
      }
   }

   private enum LocationType {
      classpath, file, webapp;
   }
}
