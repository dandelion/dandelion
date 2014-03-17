/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.locator.spi.AssetLocator;
import com.github.dandelion.core.asset.processor.AssetProcessorSystem;
import com.github.dandelion.core.asset.web.AssetRequestContext;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

/**
 * <p>
 * Main developer-side entry point for accessing the assets managed by
 * Dandelion.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class Assets {

	private static AssetConfigurator assetConfigurator;

	public static void initializeIfNeeded() {
		if (assetConfigurator == null || DevMode.isEnabled()) {
			initializeAssetConfigurator();
		}
	}

	private static synchronized void initializeAssetConfigurator() {
		if (assetConfigurator == null) {
			assetConfigurator = new AssetConfigurator();
			assetConfigurator.initialize();
		}
	}

	public static AssetConfigurator configurator() {
		initializeIfNeeded();
		return assetConfigurator;
	}

	/**
	 * Get Configured Locations of Assets<br/>
	 * 
	 * Configured by assets.locations in 'dandelion/*.properties'
	 * 
	 * @return locations of Assets
	 */
	public static List<String> getAssetLocations() {
		initializeIfNeeded();
		return assetConfigurator.getAssetLocations();
	}

	/**
	 * Get Configured Wrappers for Locations of Assets<br/>
	 * 
	 * Configured by assets.locations in 'dandelion/*.properties'
	 * 
	 * @return wrappers for locations of Assets
	 */
	public static Map<String, AssetLocator> getAssetLocatorsMap() {
		initializeIfNeeded();
		return assetConfigurator.getAssetLocatorsMap();
	}

	/**
	 * Returns the implementation of {@link AssetLocator} corresponding to the
	 * given location key.
	 * 
	 * @return the location wrapper.
	 */
	public static AssetLocator getAssetLocator(String locationKey) {
		initializeIfNeeded();
		return assetConfigurator.getAssetLocatorsMap().get(locationKey);
	}

	/**
	 * 
	 * 
	 * <p>
	 * For better performance, all filtering tasks are performed before
	 * converting asset storage units into assets
	 * 
	 * @param request
	 * @param excludedAssets
	 * @param desiredPosition
	 * @param bundleNames
	 * @return
	 */
	public static Set<Asset> assetsFor(HttpServletRequest request, String[] excludedAssets,
			Temp desiredPosition, boolean skipCaching, boolean applyProcessors, String... bundleNames) {
		initializeIfNeeded();

		// Gathers all asset storage units in an ordered set
		Set<AssetStorageUnit> assetStorageUnits = new LinkedHashSet<AssetStorageUnit>();

		for (BundleStorageUnit bsu : configurator().getStorage().bundlesFor(bundleNames)) {
			assetStorageUnits.addAll(bsu.getAssetStorageUnits());
		}

		// Filters by asset name
		if (excludedAssets != null && excludedAssets.length > 0) {
			assetStorageUnits = AssetUtils.filtersByName(assetStorageUnits, excludedAssets);
		}

		// Filters by DOM position if requested
		if (desiredPosition != null) {
			assetStorageUnits = AssetUtils.filtersByDomPosition(assetStorageUnits, desiredPosition);
		}

		// Convert all asset storage units into assets
		AssetMapper assetMapper = new AssetMapper(request, skipCaching);
		Set<Asset> assets = assetMapper.mapToAssets(assetStorageUnits);

		// Applying the processor chain
		if (applyProcessors) {
			assets = AssetProcessorSystem.process(assets, request);
		}

		return assets;
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, String bundleName) {
		initializeIfNeeded();
		String[] bundleNames = new String[] { bundleName };
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, null, true, true, bundleNames);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, String bundleName, boolean applyProcessors) {
		initializeIfNeeded();
		String[] bundleNames = new String[] { bundleName };
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, null, false, applyProcessors, bundleNames);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, String bundleName, boolean skipCaching,
			boolean applyProcessors) {
		initializeIfNeeded();
		String[] bundleNames = new String[] { bundleName };
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, null, skipCaching, applyProcessors, bundleNames);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, Temp desiredPosition) {
		initializeIfNeeded();
		String[] bundleNames = AssetRequestContext.get(request).getBundles(true);
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, desiredPosition, false, true, bundleNames);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request, Temp desiredPosition,
			boolean skipCaching, boolean applyProcessors) {
		initializeIfNeeded();
		String[] bundleNames = AssetRequestContext.get(request).getBundles(true);
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, desiredPosition, skipCaching, applyProcessors, bundleNames);
	}

	public static Set<Asset> assetsFor(HttpServletRequest request) {
		initializeIfNeeded();
		String[] bundleNames = AssetRequestContext.get(request).getBundles(true);
		String[] excludedAssets = AssetRequestContext.get(request).getExcludedAssets();
		return assetsFor(request, excludedAssets, null, false, true, bundleNames);
	}

	// public static BundleStorage getStorage() {
	// initializeIfNeeded();
	// return bundleStorage;
	// }

	public static Set<BundleStorageUnit> bundlesFor(HttpServletRequest request) {
		initializeIfNeeded();
		String[] bundleNames = AssetRequestContext.get(request).getBundles(true);
		Set<BundleStorageUnit> retval = configurator().getStorage().bundlesFor(bundleNames);
		return retval;
	}

	/**
	 * <p>
	 * Returns {@code true} if at least one asset needs to be display for the
	 * current request.
	 * <p>
	 * Note that asset locations are not processed in order to improve
	 * performance.
	 * 
	 * @param request
	 * @return
	 */
	public static boolean existsAssetsFor(HttpServletRequest request) {
		initializeIfNeeded();
		Set<Asset> assets = assetsFor(request, AssetRequestContext.get(request).getExcludedAssets(), null, false,
				false, AssetRequestContext.get(request).getBundles(false));
		return !assets.isEmpty();
	}

	/**
	 * Prevents instantiation.
	 */
	private Assets() {
	}
}