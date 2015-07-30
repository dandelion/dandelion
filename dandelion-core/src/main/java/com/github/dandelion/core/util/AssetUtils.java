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
package com.github.dandelion.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.locator.AssetLocator;
import com.github.dandelion.core.asset.locator.impl.ApiLocator;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.web.WebConstants;

/**
 * <p>
 * Some utilities to deal with {@link Asset}s.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class AssetUtils {

   /**
    * <p>
    * Filters the given set of {@link Asset}s using the given array of
    * {@link AssetType}.
    * </p>
    * 
    * @param assets
    *           The set of {@link Asset}s to filter.
    * @param filters
    *           Types of asset used to filter.
    * @return a filtered collection of {@link Asset}s.
    */
   public static Set<Asset> filtersByType(Set<Asset> assets, AssetType... filters) {
      Set<Asset> retval = new LinkedHashSet<Asset>();
      List<AssetType> types = new ArrayList<AssetType>(Arrays.asList(filters));
      for (Asset asset : assets) {
         if (types.contains(asset.getType())) {
            retval.add(asset);
         }
      }
      return retval;
   }

   /**
    * <p>
    * Filters the given set of {@link Asset}s by removing all elements whose
    * name is present in the given array of excluded asset names.
    * </p>
    * 
    * @param assets
    *           The collection of {@link Asset}s to filter.
    * @param excludedAssetNames
    *           The collection of asset names to exclude from the collection.
    * @return a filtered collection of {@link Asset}s.
    */
   public static Set<Asset> filtersByName(Set<Asset> assets, String[] excludedAssetNames) {

      List<String> excludedAssetNameList = Arrays.asList(excludedAssetNames);

      Set<Asset> filteredAsus = new LinkedHashSet<Asset>();
      for (Asset asset : assets) {

         if (!excludedAssetNameList.contains(asset.getName().trim().toLowerCase())) {
            filteredAsus.add(asset);
         }
      }

      return filteredAsus;
   }

   /**
    * <p>
    * Filters the given set of {@link Asset}s by removing all elements whose
    * name is present in the given array of excluded asset names and whose type
    * if given as parameter.
    * </p>
    * 
    * @param assets
    *           The collection of {@link Asset}s to filter.
    * @param excludedAssetNames
    *           The collection of asset names to exclude from the collection.
    * @param type
    *           The type of asset to exclude.
    * @return a filtered collection of {@link Asset}s.
    */
   public static Set<Asset> filtersByNameAndType(Set<Asset> assets, Set<String> excludedAssetNames, AssetType type) {

      List<String> excludedAssetNameList = new ArrayList<String>(excludedAssetNames);

      Set<Asset> filteredAsus = new LinkedHashSet<Asset>();
      for (Asset asu : assets) {

         if (!asu.getType().equals(type) || !excludedAssetNameList.contains(asu.getName().trim().toLowerCase())) {
            filteredAsus.add(asu);
         }
      }

      return filteredAsus;
   }

   /**
    * <p>
    * Filters the given set of {@link Asset}s using the given
    * {@link AssetDomPosition}.
    * </p>
    * 
    * @param assets
    *           The set of {@link Asset}s to filter.
    * @param desiredPosition
    *           The DOM position used to filter.
    * @return a filtered collection of {@link Asset}s.
    */
   public static Set<Asset> filtersByDomPosition(Set<Asset> assets, AssetDomPosition desiredPosition) {
      Set<Asset> filteredAsus = new LinkedHashSet<Asset>();
      Set<Asset> filteredJsAsus = new LinkedHashSet<Asset>();
      for (Asset asu : assets) {

         AssetDomPosition assetPosition = asu.getDom() == null ? asu.getType().getDefaultDom() : asu.getDom();
         if (assetPosition.equals(desiredPosition) && asu.getType().equals(AssetType.js)) {
            filteredJsAsus.add(asu);
         }
         else if (assetPosition.equals(desiredPosition) && !asu.getType().equals(AssetType.js)) {
            filteredAsus.add(asu);
         }
      }

      filteredAsus.addAll(filteredJsAsus);

      return filteredAsus;
   }

   /**
    * <p>
    * Filters the given set of {@link Asset}s by removing all vendor assets.
    * </p>
    * 
    * @param assets
    *           The set of {@link Asset}s to filter.
    * @return a filtered collection of {@link Asset}s.
    */
   public static Set<Asset> filtersNotVendor(Set<Asset> assets) {
      Set<Asset> retval = new LinkedHashSet<Asset>();
      for (Asset asset : assets) {
         if (asset.isNotVendor()) {
            retval.add(asset);
         }
      }
      return retval;
   }

   public static String getAssetFinalLocation(HttpServletRequest request, Asset asset, String suffix) {

      String requestKey = (String) request.getAttribute(WebConstants.DANDELION_REQUEST_KEY);

      Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);

      StringBuilder finalLocation = new StringBuilder();
      finalLocation.append(UrlUtils.getProcessedUrl(context.getConfiguration().getAssetUrlPattern(), request, null));
      if (finalLocation.charAt(finalLocation.length() - 1) != '/') {
         finalLocation.append("/");
      }
      if (requestKey != null) {
         finalLocation.append(requestKey);
         finalLocation.append("/");
      }
      finalLocation.append(asset.getStorageKey());
      finalLocation.append("/");
      finalLocation.append(asset.getType().name());
      finalLocation.append("/");
      finalLocation.append(asset.getName());
      finalLocation.append("-");
      finalLocation.append(asset.getVersion());
      if (StringUtils.isNotBlank(suffix)) {
         finalLocation.append(".");
         finalLocation.append(suffix);
      }
      finalLocation.append(".");
      finalLocation.append(asset.getType().name());

      return finalLocation.toString();
   }

   /**
    * <p>
    * Extracts the asset cache key from the provided request using the
    * configured {@link DandelionConfig#ASSET_URL_PATTERN}.
    * </p>
    * <p>
    * For example, using {@code my-pattern} as a custom URL pattern and the
    * following request URL:
    * </p>
    * <p>
    * {@code /context/my-pattern/7c267aa805f44ef61a273afbe4d26f2a/css/application-1.0.css}
    * </p>
    * <p>
    * This method will extract {@code 7c267aa805f44ef61a273afbe4d26f2a}.
    * </p>
    * 
    * @param request
    *           The {@link HttpServletRequest} made against the server to load
    *           the asset.
    * @return the cache key present in the request URL.
    */
   public static String extractCacheKeyFromRequest(HttpServletRequest request) {

      Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);
      String processedUrlPattern = context.getConfiguration().getAssetUrlPattern().startsWith("/")
            ? context.getConfiguration().getAssetUrlPattern().substring(1)
            : context.getConfiguration().getAssetUrlPattern();

      Pattern patternWithRequestKey = Pattern.compile(".*" + processedUrlPattern + "[a-f0-9]{32}/[a-f0-9]{32}/.*");
      Pattern patternWithoutRequestKey = Pattern.compile(".*" + processedUrlPattern + "[a-f0-9]{32}/.*");
      Matcher matcherWithRequestKey = patternWithRequestKey.matcher(request.getRequestURI());
      Matcher matcherWithoutRequestKey = patternWithoutRequestKey.matcher(request.getRequestURI());

      Pattern capturingPattern = null;
      Matcher capturingMatcher = null;
      if (matcherWithRequestKey.matches()) {
         capturingPattern = Pattern.compile(processedUrlPattern + "[a-f0-9]{32}/([a-f0-9]{32})/");
         capturingMatcher = capturingPattern.matcher(request.getRequestURI());

      }
      else if (matcherWithoutRequestKey.matches()) {
         capturingPattern = Pattern.compile(processedUrlPattern + "([a-f0-9]{32})/");
         capturingMatcher = capturingPattern.matcher(request.getRequestURI());
      }

      String cacheKey = null;
      if (capturingMatcher.find()) {
         cacheKey = capturingMatcher.group(1);
      }

      return cacheKey;
   }

   public static String extractRequestKeyFromRequest(HttpServletRequest request) {

      Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);
      String processedUrlPattern = context.getConfiguration().getAssetUrlPattern().startsWith("/")
            ? context.getConfiguration().getAssetUrlPattern().substring(1)
            : context.getConfiguration().getAssetUrlPattern();
      Pattern p = Pattern.compile(processedUrlPattern + "([a-f0-9]{32})/[a-f0-9]{32}/");
      Matcher m = p.matcher(request.getRequestURI());

      String cacheKey = null;
      if (m.find()) {
         cacheKey = m.group(1);
      }

      return cacheKey;
   }

   /**
    * <p>
    * Generates a MD5 hash using information of the provided {@link Asset}.
    * </p>
    * <p>
    * The set of information used to generate the hash depends on the location
    * key.
    * </p>
    * <ul>
    * <li>{@code webapp}, {@code jar}, {@code webjar}, {@code classpath} and
    * {@code remote}: bundle name + asset name + asset type</li>
    * <li>{@code api}: bundle name + asset name + asset type + current URI</li>
    * </ul>
    * 
    * @param asset
    *           The asset holding information used to generate the hash.
    * @return a MD5 hash.
    */
   public static String generateStorageKey(Asset asset, HttpServletRequest request) {

      Validate.notNull(asset.getConfigLocationKey(), "The location key of the provided asset cannot be null");

      StringBuilder keyContext = new StringBuilder();
      keyContext.append(asset.getBundle());
      keyContext.append(asset.getName());
      keyContext.append(asset.getType().name());

      if (asset.getConfigLocationKey().equalsIgnoreCase(ApiLocator.LOCATION_KEY)) {
         keyContext.append(UrlUtils.getCurrentUri(request));
      }

      return DigestUtils.md5Digest(keyContext.toString());
   }

   public static AssetLocator getAssetLocator(Asset asset, Context context) {

      String locationKey = asset.getConfigLocationKey();

      Map<String, AssetLocator> locators = context.getAssetLocatorsMap();
      if (!locators.containsKey(locationKey)) {
         StringBuilder msg = new StringBuilder("The location key '");
         msg.append(locationKey);
         msg.append("' is not valid. Please choose a valid one among ");
         msg.append(locators.keySet());
         msg.append(".");
         throw new DandelionException(msg.toString());
      }

      AssetLocator locator = locators.get(locationKey);
      return locator;
   }

   /**
    * <p>
    * Extracts the extension from the provided asset name.
    * </p>
    * <p>
    * 
    * <pre>
    * AssetUtils.getExtension("jquery.js")            = "js"
    * AssetUtils.getExtension("dataTables.jquery.js") = "js"
    * AssetUtils.getExtension(null)                   = null
    * AssetUtils.getExtension("")                     = null
    * AssetUtils.getExtension("jquery")               = null
    * </pre>
    * </p>
    * 
    * @param assetName
    *           The name of the asset to extract the extension from.
    * @return the extension.
    */
   public static String getExtension(String assetName) {
      if (StringUtils.isNotBlank(assetName) && assetName.lastIndexOf('.') != -1) {
         return assetName.substring(assetName.lastIndexOf('.') + 1);
      }
      return null;
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private AssetUtils() {
      throw new AssertionError();
   }
}
