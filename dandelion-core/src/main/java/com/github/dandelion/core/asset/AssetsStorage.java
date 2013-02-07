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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree Storage Units for Assets<br/>
 *
 * An asset is store by his scope.<br/>
 * All scopes have a parent except for ROOT parent (aka Root Scope).<br/>
 * An asset can be access by his scope.<br/>
 */
public final class AssetsStorage {
    /**
     * Assets Storage Units
     */
    private Map<String, AssetsScopeStorageUnit> storage;

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
        storage = new HashMap<String, AssetsScopeStorageUnit>();
        storage.put(ROOT_SCOPE, new AssetsScopeStorageUnit(ROOT_SCOPE, ROOT_SCOPE));
    }

    /**
     * Store an Asset in Root Scope as his scope
     *
     * @param asset asset to store
     */
    public void store(Asset asset) {
        store(asset, ROOT_SCOPE, ROOT_SCOPE);
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
     * @param asset asset to store
     * @param scope scope of this asset
     * @param parentScope parent of the scope
     */
    public void store(Asset asset, String scope, String parentScope) {
        if(DETACHED_PARENT_SCOPE.equalsIgnoreCase(scope)) {
            throw new DandelionException(AssetsStorageError.DETACHED_SCOPE_NOT_ALLOWED)
                    .set("detachedScope", DETACHED_PARENT_SCOPE);
        }
        AssetsScopeStorageUnit assetsScopeStorageUnit;
        if(storage.containsKey(scope)) {
            AssetsScopeStorageUnit storedAssetsScopeStorageUnit = storage.get(scope);
            checkParentScopeIncompatibility(parentScope, storedAssetsScopeStorageUnit);
            checkAssetAlreadyExists(asset, storedAssetsScopeStorageUnit);
            assetsScopeStorageUnit = storedAssetsScopeStorageUnit;
        } else {
            // create a new empty scope
            checkUnknownParentScope(parentScope);
            assetsScopeStorageUnit = new AssetsScopeStorageUnit(scope, parentScope);
            storage.put(scope, assetsScopeStorageUnit);
        }

        // don't add to the scope a null or invalid asset
        if(asset != null && asset.isValid()) {
            assetsScopeStorageUnit.assets.add(asset);
        }
    }

    /**
     * Check if an asset have a known parent scope
     * 
     * @param parentScope parent scope to check
     */
    private void checkUnknownParentScope(String parentScope) {
        if(!storage.containsKey(parentScope) && !DETACHED_PARENT_SCOPE.equalsIgnoreCase(parentScope)) {
            throw new DandelionException(AssetsStorageError.UNDEFINED_PARENT_SCOPE)
                    .set("parentScope", parentScope);
        }
    }

    /**
     * Check if an asset is already in this scope (same name)
     * 
     * @param asset asset to check
     * @param storedAssetsScopeStorageUnit stored storage unit
     */
    private void checkAssetAlreadyExists(Asset asset, AssetsScopeStorageUnit storedAssetsScopeStorageUnit) {
        if(storedAssetsScopeStorageUnit.assets.contains(asset)) {
            throw new DandelionException(AssetsStorageError.ASSET_ALREADY_EXISTS_IN_SCOPE)
                    .set("originalAsset", asset);
        }
    }

    /**
     * Check if an asset don't have a couple of Scope/Parent Scope identical to the couple Scope/Another parent scope
     *
     * @param parentScope parent scope to check
     * @param storedAssetsScopeStorageUnit stored storage unit
     */
    private void checkParentScopeIncompatibility(String parentScope, AssetsScopeStorageUnit storedAssetsScopeStorageUnit) {
        if(!storedAssetsScopeStorageUnit.parentScope.equalsIgnoreCase(parentScope)) {
            throw new DandelionException(AssetsStorageError.PARENT_SCOPE_INCOMPATIBILITY)
                    .set("scope", storedAssetsScopeStorageUnit.scope)
                    .set("parentScope", storedAssetsScopeStorageUnit.parentScope);
        }
    }

    /**
     * Retrieve the assets for a groups of scopes.
     *
     * @param scopes scopes of needed assets
     * @return the list of assets for scopes
     */
    public List<Asset> assetsFor(String ... scopes) {
        if(scopes.length == 0
                || (scopes.length == 1 && ROOT_SCOPE.equalsIgnoreCase(scopes[0])))
            return storage.get(ROOT_SCOPE).assets;
        List<Asset> assets = new ArrayList<Asset>();
        for(String scope:scopes) {
            List<Asset> scopedAssets = new ArrayList<Asset>();
            AssetsScopeStorageUnit assetScope = storage.get(scope);
            if(assetScope != null) {
                scopedAssets.addAll(assetScope.assets);
                if(!DETACHED_PARENT_SCOPE.equalsIgnoreCase(assetScope.parentScope)) {
                    List<Asset> parentAssets = assetsFor(assetScope.parentScope);
                    parentAssets.removeAll(scopedAssets);
                    scopedAssets.addAll(parentAssets);
                    scopedAssets.removeAll(assets);
                }
                assets.addAll(scopedAssets);
            }
        }
        return assets;
    }
}
