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
package com.github.dandelion.core.asset.processor.impl;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;

/**
 * <p>
 * Processor entry in charge of resolving asset locations.
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class AssetLocationProcessor extends AssetProcessor {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(AssetLocationProcessor.class);

	@Override
	public String getProcessorKey() {
		return "location";
	}

	@Override
	public int getRank() {
		return 0;
	}

	@Override
	public Set<Asset> process(Set<Asset> assets, HttpServletRequest request) {
		Set<Asset> _assets = new LinkedHashSet<Asset>();

		for (Asset asset : assets) {
			// no available locations = no locations
			if (asset.getLocations().isEmpty()) {
				LOG.warn("No available location for {}", asset.toString());
				continue;
			}

			String locationKey = null;
			if (asset.getLocations().size() == 1) {
				// use the unique location if needed
				LOG.debug("Only one location found for {}, automatically used", asset.toString());
				for (String _locationKey : asset.getLocations().keySet()) {
					locationKey = _locationKey;
				}
			}
			else {
				// otherwise search for the first match in authorized locations
				LOG.debug("Search for the right location for {}", asset.toString());
				for (String searchedLocationKey : Assets.getAssetLocations()) {
					if (asset.getLocations().containsKey(searchedLocationKey)) {
						String location = asset.getLocations().get(searchedLocationKey);
						if (location != null && !location.isEmpty()) {
							locationKey = searchedLocationKey;
							break;
						}
					}
				}

			}

			// And if any location was found = no locations
			if (locationKey == null) {
				LOG.warn("No location matches the requested location ({}) for the asset {} among {}.", locationKey,
						asset.toString(), Assets.getAssetLocations());
				continue;
			}

			// Otherwise check for wrapper
			String location;
			Map<String, AssetLocationWrapper> wrappers = Assets.getAssetLocationWrappers();
			if (wrappers.containsKey(locationKey) && wrappers.get(locationKey).isActive()) {
				LOG.debug("use location wrapper for {} on {}.", locationKey, asset);
				location = wrappers.get(locationKey).getWrappedLocation(asset, request);
			}
			else {
				location = asset.getLocations().get(locationKey);
			}

			if (location == null) {
				LOG.warn("No location found for {} on {}", locationKey, asset.toString());
				continue;
			}

			asset.setLocation(location);
			Asset wrappedAsset = asset.clone();
			wrappedAsset.setLocation(location);
			wrappedAsset.getLocations().put(locationKey, location);
			if (DevMode.isEnabled()) {
				debugAttributes(asset, locationKey, false);
			}
			_assets.add(wrappedAsset);

		}
		return _assets;
	}

	private void debugAttributes(Asset asset, String locationKey, Boolean wrapped) {
		asset.addAttribute("asset-name", asset.getName());
		asset.addAttribute("asset-type", asset.getType().name());
		asset.addAttribute("asset-version", asset.getVersion());
		if (asset.getDom() != null) {
			asset.addAttribute("asset-dom-position", asset.getDom().name());
		}
		asset.addAttribute("asset-location-key", locationKey);
		asset.addAttribute("asset-location-wrap", wrapped.toString());
	}
}
