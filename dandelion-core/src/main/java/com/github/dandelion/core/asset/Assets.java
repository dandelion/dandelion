/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.dandelion.core.utils.DandelionUtils.devModeOverride;

/**
 * Assets API
 */
public final class Assets {
    static AssetsConfigurator assetsConfigurator;
    static AssetsStorage assetsStorage;

    /**
     * Initialize Assets only if needed
     */
    static void initializeIfNeeded() {
        if(devModeOverride(assetsConfigurator == null)) {
            if(devModeOverride(assetsStorage == null)) {
                initializeStorageIfNeeded();
            }
            initializeConfiguratorIfNeeded();
        }
    }

    /**
     * Initialize Assets Configurator only if needed
     */
    synchronized private static void initializeConfiguratorIfNeeded() {
        if(devModeOverride(assetsConfigurator == null)) {
            assetsConfigurator = new AssetsConfigurator(assetsStorage);
            assetsConfigurator.initialize();
        }
    }

    /**
     * Initialize Assets Storage only if needed
     */
    synchronized private static void initializeStorageIfNeeded() {
        if(devModeOverride(assetsStorage == null)) {
            assetsStorage = new AssetsStorage();
        }
    }

    /**
     * Get Configured Locations of Assets<br/>
     *
     * Configured by assetsLocations in 'dandelion/dandelion.properties'
     *
     * @return locations of Assets
     */
    public static List<String> getAssetsLocations() {
        initializeIfNeeded();
        return assetsConfigurator.assetsLocations;
    }

    /**
     * Find Assets for Scopes
     * @param scopes scopes of assets
     * @return Assets of scopes
     */
    public static List<Asset> assetsFor(String ... scopes) {
        initializeIfNeeded();
        return assetsStorage.assetsFor(scopes);
    }

    /**
     * @param asset asset for extract location
     * @return the location of this asset by the configured assets locations.
     */
    public static String getAssetLocation(Asset asset) {
        for(String locationKey:getAssetsLocations()) {
            if(asset.getLocations().containsKey(locationKey)) {
                String location = asset.getLocations().get(locationKey);
                if(assetsConfigurator.assetsLocationWrappers.containsKey(locationKey)) {
                    location = assetsConfigurator.assetsLocationWrappers.get(locationKey).customize(location);
                }
                if(location != null && !location.isEmpty())
                    return location;
            }
        }
        return "";
    }

    /**
     * @param assets assets to filter
     * @param filters exclude assets names
     * @return a filtered list of assets
     */
    public static List<Asset> excludeByName(List<Asset> assets, String... filters) {
        List<Asset> _assets = new ArrayList<Asset>();
        List<String> _filters = new ArrayList<String>(Arrays.asList(filters));
        for(Asset _asset:assets) {
            if(!_filters.contains(_asset.getName())) {
                _assets.add(_asset);
            }
        }
        return _assets;
    }

    /**
     * @param assets assets to filter
     * @param filters filtered assets type
     * @return a filtered list of assets
     */
    public static List<Asset> filterByType(List<Asset> assets, AssetType... filters) {
        List<Asset> _assets = new ArrayList<Asset>();
        List<AssetType> _filters = new ArrayList<AssetType>(Arrays.asList(filters));
        for(Asset _asset:assets) {
            if(_filters.contains(_asset.getType())) {
                _assets.add(_asset);
            }
        }
        return _assets;
    }

    /**
     * @param asset template asset
     * @return the location of this asset
     */
    public static String getAssetTemplateLocation(Asset asset) {
        return asset.getLocations().get("template");
    }
}
