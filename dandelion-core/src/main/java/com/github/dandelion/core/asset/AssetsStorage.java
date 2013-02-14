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
 * 3. Neither the name of DataTables4j nor the names of its contributors
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

import com.github.dandelion.core.DandelionException;

import java.util.*;

/**
 * Tree Storage Units for Assets<br/>
 * <p/>
 * An asset is store by his scope.<br/>
 * All scopes have a parent except for ROOT parent (aka Root Scope).<br/>
 * An asset can be access by his scope.<br/>
 */
public final class AssetsStorage {
    static final int ASSET_SCOPE_STORAGE_POSITION = 1000;
    /**
     * Assets Storage Units
     */
    private Map<String, AssetsScopeStorageUnit> storage;

    /**
     * Define the Root Scope string representation
     */
    public static final String MASTER_SCOPE = "master_scope_" + System.currentTimeMillis();

    /**
     * Define the Root Scope string representation
     */
    public static final String ROOT_SCOPE = "default";

    /**
     * Define the Detached Scope string representation
     */
    public static final String DETACHED_PARENT_SCOPE = "none";

    /**
     * Assets Storage Utility
     */
    AssetsStorage() {
        // initialize storage
        storage = new HashMap<String, AssetsScopeStorageUnit>();
        // initialize root storage unit
        AssetsScopeStorageUnit rootUnit = new AssetsScopeStorageUnit(ROOT_SCOPE, MASTER_SCOPE);
        rootUnit.rootParentScope = ROOT_SCOPE;
        rootUnit.storagePosition = 0;
        storage.put(ROOT_SCOPE, rootUnit);
        // initialize detached storage unit
        AssetsScopeStorageUnit detachedUnit = new AssetsScopeStorageUnit(DETACHED_PARENT_SCOPE, MASTER_SCOPE);
        detachedUnit.rootParentScope = DETACHED_PARENT_SCOPE;
        detachedUnit.storagePosition = 0;
        storage.put(DETACHED_PARENT_SCOPE, detachedUnit);
    }

    /**
     * Store an Asset in Root Scope as his scope
     *
     * @param asset asset to store
     */
    public void store(Asset asset) {
        store(asset, ROOT_SCOPE, MASTER_SCOPE);
    }

    /**
     * Store an Asset in his scope (with Root Scope as parent)
     *
     * @param asset asset to store
     * @param scope scope of this asset
     */
    public void store(Asset asset, String scope) {
        store(asset, scope, ROOT_SCOPE);
    }

    /**
     * Store an Asset in his scope
     *
     * @param asset       asset to store
     * @param scope       scope of this asset
     * @param parentScope parent of the scope
     */
    public void store(Asset asset, String scope, String parentScope) {
        if (DETACHED_PARENT_SCOPE.equalsIgnoreCase(scope)) {
            throw new DandelionException(AssetsStorageError.DETACHED_SCOPE_NOT_ALLOWED)
                    .set("detachedScope", DETACHED_PARENT_SCOPE);
        }
        AssetsScopeStorageUnit scopeUnit = getOrCreateStorageUnit(asset, scope, parentScope);
        store(asset, scopeUnit);
    }

    /**
     * Retrieve the assets for a groups of scopes.
     *
     * @param scopes scopes of needed assets
     * @return the list of assets for scopes
     */
    public List<Asset> assetsFor(String... scopes) {
        List<Asset> assets = new ArrayList(assetsMapFor(scopes).values());
        Collections.sort(assets, assetStoragePositionComparator);
        return assets;
    }

    /**
     * Store an asset in the Storage Unit of this scope
     * @param asset asset to store
     * @param scopeUnit storage unit of the asset's scope
     */
    private void store(Asset asset, AssetsScopeStorageUnit scopeUnit) {
        // don't store if we found invalid asset or invalid storage unit
        if (scopeUnit == null || asset == null || !asset.isValid()) return;
        // set up position in the storage and the storage unit
        asset.storagePosition = scopeUnit.storagePosition * ASSET_SCOPE_STORAGE_POSITION + scopeUnit.assets.size();
        scopeUnit.assets.add(asset);
    }

    private AssetsScopeStorageUnit getOrCreateStorageUnit(Asset asset, String scope, String parentScope) {
        AssetsScopeStorageUnit scopeUnit;
        if (storage.containsKey(scope)) {
            AssetsScopeStorageUnit storedScopeUnit = storage.get(scope);
            checkParentScopeIncompatibility(parentScope, storedScopeUnit);
            try {
                checkAssetAlreadyExists(asset, storedScopeUnit);
            } catch (DandelionException e) {
                // TODO this code isn't in the right place
                Asset originalAsset = storedScopeUnit.assets.get(
                        storedScopeUnit.assets.indexOf(e.get("originalAsset"))
                );
                checkAssetsLocationAlreadyExists(asset, originalAsset, e);
                // merge the asset locations to the original asset
                originalAsset.getLocations().putAll(asset.getLocations());
                // TODO add an DandelionException here
                return null;
            }
            scopeUnit = storedScopeUnit;
        } else {
            // create a new empty scope
            checkUnknownParentScope(parentScope);
            scopeUnit = new AssetsScopeStorageUnit(scope, parentScope);
            AssetsScopeStorageUnit parentScopeUnit = storage.get(parentScope);
            scopeUnit.rootParentScope = parentScopeUnit.rootParentScope;
            scopeUnit.storagePosition = parentScopeUnit.storagePosition + 1;
            storage.put(scope, scopeUnit);
        }
        return scopeUnit;
    }

    /**
     * Check if an asset have a known parent scope
     *
     * @param parentScope parent scope to check
     */
    private void checkUnknownParentScope(String parentScope) {
        if (!storage.containsKey(parentScope) && !DETACHED_PARENT_SCOPE.equalsIgnoreCase(parentScope)) {
            throw new DandelionException(AssetsStorageError.UNDEFINED_PARENT_SCOPE)
                    .set("parentScope", parentScope);
        }
    }

    /**
     * Check if an asset is already in this scope (same name/type)
     *
     * @param asset                        asset to check
     * @param storedAssetsScopeStorageUnit stored storage unit
     */
    private void checkAssetAlreadyExists(Asset asset, AssetsScopeStorageUnit storedAssetsScopeStorageUnit) {
        if (storedAssetsScopeStorageUnit.assets.contains(asset)) {
            throw new DandelionException(AssetsStorageError.ASSET_ALREADY_EXISTS_IN_SCOPE)
                    .set("originalAsset", asset);
        }
    }

    /**
     * Check if an asset location is already in this scope (same location key)
     *
     * @param asset         asset to check
     * @param originalAsset original asset
     * @param e
     */
    private void checkAssetsLocationAlreadyExists(Asset asset, Asset originalAsset, DandelionException e) {
        List<String> locations = new ArrayList<String>();
        for (String assetLocationKey : asset.getLocations().keySet()) {
            if (originalAsset.getLocations().containsKey(assetLocationKey)) {
                locations.add(assetLocationKey);
            }
        }
        if (locations.size() == originalAsset.getLocations().keySet().size()) {
            throw e;
        } else if (!locations.isEmpty()) {
            throw new DandelionException(AssetsStorageError.ASSET_LOCATION_ALREADY_EXISTS_IN_SCOPE)
                    .set("locations", locations)
                    .set("asset", asset);
        }
    }

    /**
     * Check if an asset don't have a couple of Scope/Parent Scope identical to the couple Scope/Another parent scope
     *
     * @param parentScope                  parent scope to check
     * @param storedAssetsScopeStorageUnit stored storage unit
     */
    private void checkParentScopeIncompatibility(String parentScope, AssetsScopeStorageUnit storedAssetsScopeStorageUnit) {
        if (!storedAssetsScopeStorageUnit.parentScope.equalsIgnoreCase(parentScope)) {
            throw new DandelionException(AssetsStorageError.PARENT_SCOPE_INCOMPATIBILITY)
                    .set("scope", storedAssetsScopeStorageUnit.scope)
                    .set("parentScope", storedAssetsScopeStorageUnit.parentScope);
        }
    }

    /**
     * Retrieve the assets (as map) for a groups of scopes.
     *
     * @param scopes scopes of needed assets
     * @return the map of assets for scopes (key is the Asset#equalsKey)
     */
    private Map<String, Asset> assetsMapFor(String... scopes) {
        if (scopes.length == 0) {
            scopes = new String[]{ROOT_SCOPE};
        }
        Map<String, Asset> assetsMap = new HashMap<String, Asset>();
        for (String scope : scopes) {
            Map<String, Asset> scopedAssetsMap = new HashMap<String, Asset>();
            AssetsScopeStorageUnit assetScope = storage.get(scope);
            if (assetScope != null) {
                for (Asset asset : assetScope.assets) {
                    String key = asset.equalsKey() + "_" + assetScope.rootParentScope;
                    scopedAssetsMap.put(key, Asset.class.cast(asset.clone()));
                }

                Map<String, Asset> parentAssets = assetsMapFor(assetScope.parentScope);
                mergeAssets(scopedAssetsMap, parentAssets);
                mergeAssets(assetsMap, scopedAssetsMap);
            }
        }
        return assetsMap;
    }

    /**
     * Merge 2 maps of assets
     * @param container current container of assets
     * @param others the others assets for add
     */
    private void mergeAssets(Map<String, Asset> container, Map<String, Asset> others) {
        for (Map.Entry<String, Asset> other : others.entrySet()) {
            if (container.containsKey(other.getKey())) {
                Asset asset = container.get(other.getKey());
                asset.storagePosition = other.getValue().storagePosition;
                if (asset.getVersion().equalsIgnoreCase(other.getValue().getVersion())) {
                    for (Map.Entry<String, String> location : other.getValue().getLocations().entrySet()) {
                        if (!asset.getLocations().containsKey(location.getKey())) {
                            asset.getLocations().put(location.getKey(), location.getValue());
                        }
                    }
                }
            } else {
                container.put(other.getKey(), other.getValue());
            }
        }
    }

    /**
     * Comparator of storage position for internal use
     */
    private static final Comparator<Asset> assetStoragePositionComparator = new Comparator<Asset>() {
        @Override
        public int compare(Asset asset, Asset asset2) {
            return asset.storagePosition - asset2.storagePosition;
        }
    };
}
