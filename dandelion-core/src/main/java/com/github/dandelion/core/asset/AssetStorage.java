package com.github.dandelion.core.asset;

import com.github.dandelion.core.api.asset.Asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree Storage Units for Asset<br/>
 *
 * An asset is store by his scope.<br/>
 * All scopes have a parent except for ROOT parent (aka Root Scope).<br/>
 * An asset can be access by his scope.<br/>
 */
public final class AssetStorage {
    /**
     * Storage Units
     */
    private static Map<String, StorageUnit> storage;

    /**
     * Define the Root Scope string representation
     */
    public static final String ROOT_SCOPE = "default";

    /**
     * Define the Detach Scope string representation
     */
    public static final String DETACH_PARENT_SCOPE = "none";

    static {
        // A empty storage is a scope 'default' with no assets
        clearAll();
    }

    /**
     * Storage Unit Utility Class
     */
    private AssetStorage() {}

    /**
     * Store an Asset in Root Scope as his scope
     *
     * @param asset asset to store
     * @throws ParentScopeIncompatibilityException
     *         An asset can't have a couple of Scope/Parent Scope when his scope is already associated to another parent scope
     * @throws AssetAlreadyExistsInScopeException
     *         An asset can't be add twice in the same scope (same name)
     * @throws UndefinedParentScopeException
     *         An asset can't have a parent scope who don't already exists
     */
    public static void store(Asset asset) {
        store(asset, ROOT_SCOPE, ROOT_SCOPE);
    }

    /**
     * Store an Asset in his scope (with Root Scope as parent)
     *
     * @param asset asset to store
     * @param scope scope of this asset
     * @throws ParentScopeIncompatibilityException
     *         An asset can't have a couple of Scope/Parent Scope when his scope is already associated to another parent scope
     * @throws AssetAlreadyExistsInScopeException
     *         An asset can't be add twice in the same scope (same name)
     * @throws UndefinedParentScopeException
     *         An asset can't have a parent scope who don't already exists
     */
    public static void store(Asset asset, String scope) {
        store(asset, scope, ROOT_SCOPE);
    }

    /**
     * Store an Asset in his scope
     *
     * @param asset asset to store
     * @param scope scope of this asset
     * @param parentScope parent of the scope
     * @throws ParentScopeIncompatibilityException
     *         An asset can't have a couple of Scope/Parent Scope when his scope is already associated to another parent scope
     * @throws AssetAlreadyExistsInScopeException
     *         An asset can't be add twice in the same scope (same name)
     * @throws UndefinedParentScopeException
     *         An asset can't have a parent scope who don't already exists
     */
    public static void store(Asset asset, String scope, String parentScope) {
        if(scope.equalsIgnoreCase(DETACH_PARENT_SCOPE)) {
            throw new DetachScopeNotAllowedException(DETACH_PARENT_SCOPE);
        }
        StorageUnit storageUnit;
        if(storage.containsKey(scope)) {
            StorageUnit storedStorageUnit = storage.get(scope);
            checkParentScopeIncompatibility(parentScope, storedStorageUnit);
            checkAssetAlreadyExists(asset, storedStorageUnit);
            storageUnit = storedStorageUnit;
        } else {
            // create a new empty scope
            checkUnknownParentScope(parentScope);
            storageUnit = new StorageUnit(scope, parentScope);
            storage.put(scope, storageUnit);
        }

        // don't add to the scope a null or invalid asset
        if(asset != null && asset.isValid()) {
            storageUnit.assets.add(asset);
        }
    }

    /**
     * Check if an asset have a known parent scope
     * 
     * @param parentScope parent scope to check
     */
    private static void checkUnknownParentScope(String parentScope) {
        if(!storage.containsKey(parentScope) && !DETACH_PARENT_SCOPE.equalsIgnoreCase(parentScope)) {
            throw new UndefinedParentScopeException();
        }
    }

    /**
     * Check if an asset is already in this scope (same name)
     * 
     * @param asset asset to check
     * @param storedStorageUnit stored storage unit
     */
    private static void checkAssetAlreadyExists(Asset asset, StorageUnit storedStorageUnit) {
        if(storedStorageUnit.assets.contains(asset)) {
            throw new AssetAlreadyExistsInScopeException(asset);
        }
    }

    /**
     * Check if an asset don't have a couple of Scope/Parent Scope identical to the couple Scope/Another parent scope
     *
     * @param parentScope parent scope to check
     * @param storedStorageUnit stored storage unit
     */
    private static void checkParentScopeIncompatibility(String parentScope, StorageUnit storedStorageUnit) {
        if(!storedStorageUnit.parentScope.equalsIgnoreCase(parentScope)) {
            throw new ParentScopeIncompatibilityException(storedStorageUnit.scope, storedStorageUnit.parentScope);
        }
    }

    /**
     * Retrieve the assets for a groups of scopes.
     *
     * @param scopes scopes of needed assets
     * @return the list of assets for scopes
     */
    public static List<Asset> assetsFor(String ... scopes) {
        if(scopes.length == 0
                || (scopes.length == 1 && ROOT_SCOPE.equalsIgnoreCase(scopes[0])))
            return storage.get(ROOT_SCOPE).assets;
        List<Asset> assets = new ArrayList<Asset>();
        for(String scope:scopes) {
            List<Asset> scopedAssets = new ArrayList<Asset>();
            StorageUnit assetScope = storage.get(scope);
            if(assetScope != null) {
                scopedAssets.addAll(assetScope.assets);
                if(!DETACH_PARENT_SCOPE.equalsIgnoreCase(assetScope.parentScope)) {
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

    /**
     * Clear the storage unit from all stored assets
     */
    public static void clearAll() {
        storage = new HashMap<String, StorageUnit>();
        storage.put(ROOT_SCOPE, new StorageUnit(ROOT_SCOPE, ROOT_SCOPE));
    }
}
