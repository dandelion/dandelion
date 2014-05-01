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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.asset.locator.spi.AssetLocator;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.utils.UrlUtils;
import com.github.dandelion.core.web.DandelionServlet;

/**
 * <p>
 * Used to map an {@link AssetStorageUnit} to an {@link Asset}.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetMapper {

	private static final Logger LOG = LoggerFactory.getLogger(AssetMapper.class);

	private HttpServletRequest request;
	private Context context;

	public AssetMapper(HttpServletRequest request, Context context) {
		this.request = request;
		this.context = context;
	}

	/**
	 * The same as {@link #mapToAssets(Set)} but for a set of
	 * {@link AssetStorageUnit}s.
	 * 
	 * @param asus
	 *            The set of {@link AssetStorageUnit}s to map to a set of
	 *            {@link Asset}s.
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
	 * 
	 * <p>
	 * Depending on how the {@link AssetStorageUnit} is configured, the
	 * {@link Asset} will contains resolved locations and its content will be
	 * cached in the configured {@link AssetCache}.
	 * 
	 * @param asu
	 *            The {@link AssetStorageUnit} to map to an {@link Asset}.
	 * @return the mapped {@link Asset}.
	 * @throws DandelionException
	 *             if the {@link AssetStorageUnit} is not configured properly.
	 */
	public Asset mapToAsset(AssetStorageUnit asu) {
		Asset asset = new Asset(asu);

		LOG.trace("Resolving location for the asset {}", asset.toLog());

		// no available locations = no locations
		if (asu.getLocations() == null || asu.getLocations().isEmpty()) {
			StringBuilder msg = new StringBuilder("No location is configured for the asset ");
			msg.append(asu.toLog());
			msg.append(". Please add at least one location in the corresponding JSON file.");
			throw new DandelionException(msg.toString());
		}

		// Selecting location key
		String locationKey = null;

		if (asu.getLocations().size() == 1) {
			// use the unique location if needed
			locationKey = asu.getLocations().entrySet().iterator().next().getKey();
		}
		else {
			// otherwise search for the first matching location key among the
			// configured ones
			for (String searchedLocationKey : context.getConfiguration().getAssetLocationsResolutionStrategy()) {
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
		
		Map<String, AssetLocator> locators = context.getAssetLocatorsMap();
		if (!locators.containsKey(locationKey)) {
			StringBuilder msg = new StringBuilder("The location key '");
			msg.append(locationKey);
			msg.append("' is not valid. Please choose a valid one among ");
			msg.append(locators.keySet());
			msg.append(".");
			throw new DandelionException(msg.toString());
		}

		// Otherwise check for the locator
		String location = null;
		AssetLocator locator = locators.get(locationKey);
		if (locators.containsKey(locationKey) && locators.get(locationKey).isActive()) {
			LOG.trace("Locator '{}' will be applied on the asset {}.", locator.getClass().getSimpleName(), asu.toLog());
			location = locators.get(locationKey).getLocation(asu, request);
		}

		if (location == null) {
			LOG.warn("No location found for {} on {}", locationKey, asu.toString());
		}

		asset.setConfigLocationKey(locationKey);
		asset.setConfigLocation(asu.getLocations().get(locationKey));

		if (locator.isCachingForced()) {
			String context = UrlUtils.getCurrentUrl(request, true).toString();
			context = context.replaceAll("\\?", "_").replaceAll("&", "_");
			String cacheKey = this.context.getCacheManager().generateCacheKey(context, asset);
			asu.setCacheKey(cacheKey);
			asset.setCacheKey(cacheKey);
			asset.setFinalLocation(UrlUtils.getProcessedUrl(DandelionServlet.DANDELION_ASSETS_URL + cacheKey, request,
					null));
			
			// First try to access the asset content in order to see if it must be
			// cached
			String content = this.context.getCacheManager().getContent(asset.getCacheKey());

			if (content == null || this.context.isDevModeEnabled()) {
				Map<String, AssetLocator> assetLocatorsMap = this.context.getAssetLocatorsMap();
				if (assetLocatorsMap.containsKey(asset.getConfigLocationKey())
						&& assetLocatorsMap.get(asset.getConfigLocationKey()).isActive()) {
					// LOG.debug("use location wrapper for {} on {}.", locationKey,
					// asu);
					// location = wrappers.get(locationKey).getWrappedLocation(asu,
					// request);

					content = assetLocatorsMap.get(asset.getConfigLocationKey()).getContent(asu, request);

					// Finally store the final content in cache
					this.context.getCacheManager().storeContent(asset.getCacheKey(), content);
				}
			}
		}
		else {
			asset.setFinalLocation(location);
		}

		return asset;
	}
}
