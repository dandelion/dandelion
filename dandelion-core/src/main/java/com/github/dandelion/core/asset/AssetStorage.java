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

import java.util.*;

import com.github.dandelion.core.DandelionException;

/**
 * Tree Storage Units for Assets<br/>
 * <p/>
 * An asset is stored by its scope.<br/>
 * All scopes have a parent except for ROOT parent (aka Root Scope).<br/>
 * An asset can be accessed by its scope.<br/>
 */
public final class AssetStorage {
	static final int ASSET_SCOPE_STORAGE_POSITION = 1000;

	private enum StorageCommand {
		INSERT, MERGE
	};

	/**
	 * Assets Storage Units
	 */
	private Map<String, AssetScopeStorageUnit> storage;

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
	AssetStorage() {
		// initialize storage
		storage = new HashMap<String, AssetScopeStorageUnit>();
		// initialize root storage unit
		AssetScopeStorageUnit rootUnit = new AssetScopeStorageUnit(ROOT_SCOPE, MASTER_SCOPE);
		rootUnit.rootParentScope = ROOT_SCOPE;
		rootUnit.storagePosition = 0;
		storage.put(ROOT_SCOPE, rootUnit);
		// initialize detached storage unit
		AssetScopeStorageUnit detachedUnit = new AssetScopeStorageUnit(DETACHED_PARENT_SCOPE, MASTER_SCOPE);
		detachedUnit.rootParentScope = DETACHED_PARENT_SCOPE;
		detachedUnit.storagePosition = 0;
		storage.put(DETACHED_PARENT_SCOPE, detachedUnit);
	}

	/**
	 * Store an Asset in Root Scope as his scope
	 * 
	 * @param asset
	 *            asset to store
	 */
	public void store(Asset asset) {
		store(asset, ROOT_SCOPE, MASTER_SCOPE);
	}

	/**
	 * Store an Asset in his scope (with Root Scope as parent)
	 * 
	 * @param asset
	 *            asset to store
	 * @param scope
	 *            scope of this asset
	 */
	public void store(Asset asset, String scope) {
		store(asset, scope, ROOT_SCOPE);
	}

	/**
	 * Store an Asset in his scope
	 * 
	 * @param asset
	 *            asset to store
	 * @param scope
	 *            scope of this asset
	 * @param parentScope
	 *            parent of the scope
	 */
	public void store(Asset asset, String scope, String parentScope) {
		// don't store if we found invalid asset
		if (asset == null || !asset.isValid())
			return;

		if (DETACHED_PARENT_SCOPE.equalsIgnoreCase(scope)) {
			throw new DandelionException(AssetStorageError.DETACHED_SCOPE_NOT_ALLOWED).set("detachedScope",
                    DETACHED_PARENT_SCOPE);
		}
		AssetScopeStorageUnit scopeUnit = getStorageUnit(scope, parentScope);
		insertAsset(asset, scopeUnit);
	}

	/**
	 * Retrieve the assets for a groups of scopes.
	 * 
	 * @param scopes
	 *            scopes of needed assets
	 * @return the list of assets for scopes
	 */
	public List<Asset> assetsFor(String... scopes) {
		List<Asset> assets = new ArrayList<Asset>(assetsMapFor(scopes).values());
		Collections.sort(assets, assetStoragePositionComparator);
		return assets;
	}

	/**
	 * Store an asset in the Storage Unit of this scope
	 * 
	 * @param asset
	 *            asset to store
	 * @param scopeUnit
	 *            storage unit of the asset's scope
	 */
	private void insertAsset(Asset asset, AssetScopeStorageUnit scopeUnit) {
		StorageCommand command = checkAssetStorageIncompatibility(asset, scopeUnit);
		switch (command) {
		case INSERT:
			// set up position in the storage and the storage unit
			asset.storagePosition = scopeUnit.storagePosition * ASSET_SCOPE_STORAGE_POSITION + scopeUnit.assets.size();
			scopeUnit.assets.add(asset);
			break;
		case MERGE:
			Asset originalAsset = scopeUnit.assets.get(scopeUnit.assets.indexOf(asset));
			combineAsset(originalAsset, asset);
			break;
		default:
			return;
		}
	}

	private StorageCommand checkAssetStorageIncompatibility(Asset asset, AssetScopeStorageUnit scopeUnit) {
		// the asset can be store if he isn't store already
		if (!scopeUnit.assets.contains(asset)) {
			return StorageCommand.INSERT;
		}
		// if not, we check if he can be merge
		Asset originalAsset = scopeUnit.assets.get(scopeUnit.assets.indexOf(asset));

		// he can be merge if the versions are equals
		if (!originalAsset.getVersion().equals(asset.getVersion())) {
			throw new DandelionException(AssetStorageError.ASSET_ALREADY_EXISTS_IN_SCOPE).set("originalAsset", asset);
		}

		// he can be merge if the locations can be merge
		List<String> locationsInError = new ArrayList<String>();
		for (Map.Entry<String, String> locationEntry : asset.getLocations().entrySet()) {
			if (originalAsset.getLocations().containsKey(locationEntry.getKey())
					&& !originalAsset.getLocations().get(locationEntry.getKey()).equals(locationEntry.getValue())) {
				locationsInError.add(locationEntry.getKey());
			}
		}
		if (!locationsInError.isEmpty()) {
			throw new DandelionException(AssetStorageError.ASSET_LOCATION_ALREADY_EXISTS_IN_SCOPE).set("locations",
                    locationsInError).set("asset", asset);
		}

		// he can be merge if the attributes can be merge
		List<String> attributesInError = new ArrayList<String>();
		for (Map.Entry<String, String> attributeEntry : asset.getAttributes().entrySet()) {
			if (originalAsset.getAttributes().containsKey(attributeEntry.getKey())
					&& !originalAsset.getAttributes().get(attributeEntry.getKey()).equals(attributeEntry.getValue())) {
				attributesInError.add(attributeEntry.getKey());
			}
		}
		if (!attributesInError.isEmpty()) {
			throw new DandelionException(AssetStorageError.ASSET_ATTRIBUTE_ALREADY_EXISTS_IN_SCOPE).set("attributes",
                    attributesInError).set("asset", asset);
		}

		// he can be merge if the DOM position can be merge
		if (originalAsset.getDom() != null && asset.getDom() != null && originalAsset.getDom() != asset.getDom()) {
			throw new DandelionException(AssetStorageError.ASSET_DOM_POSITION_ALREADY_EXISTS_IN_SCOPE).set(
                    "domPosition", originalAsset.getDom()).set("asset", asset);
		}
		return StorageCommand.MERGE;
	}

	private void combineAsset(Asset originalAsset, Asset asset) {
		// merge the asset attributes only name to the original asset
		List<String> attributesOnlyName = Arrays.asList(originalAsset.getAttributesOnlyName());
		for (String attributeOnlyName : asset.getAttributesOnlyName()) {
			if (!attributesOnlyName.contains(attributeOnlyName)) {
				originalAsset.addAttribute(attributeOnlyName);
			}
		}

		// merge the asset attributes to the original asset
		for (Map.Entry<String, String> attributeEntry : asset.getAttributes().entrySet()) {
			if (!originalAsset.getAttributes().containsKey(attributeEntry.getKey())) {
				originalAsset.getAttributes().put(attributeEntry.getKey(), attributeEntry.getValue());
			}
		}

		// merge the asset locations to the original asset
		for (Map.Entry<String, String> locationEntry : asset.getLocations().entrySet()) {
			if (!originalAsset.getLocations().containsKey(locationEntry.getKey())) {
				originalAsset.getLocations().put(locationEntry.getKey(), locationEntry.getValue());
			}
		}

		// merge the asset dom position to the original asset if needed
		if (originalAsset.getDom() == null) {
			originalAsset.setDom(asset.getDom());
		}
	}

	void setupEmptyParentScope(String scope) {
		getStorageUnit(scope, ROOT_SCOPE);
	}

	void setupEmptyScope(String scope, String parentScope) {
		getStorageUnit(scope, parentScope);
	}

	private AssetScopeStorageUnit getStorageUnit(String scope, String parentScope) {
		scope = scope.toLowerCase();
		parentScope = parentScope.toLowerCase();
		AssetScopeStorageUnit scopeUnit;
		if (storage.containsKey(scope)) {
			AssetScopeStorageUnit storedScopeUnit = storage.get(scope);
			checkParentScopeIncompatibility(parentScope, storedScopeUnit);
			scopeUnit = storedScopeUnit;
		}
		else {
			// create a new empty scope
			checkUnknownParentScope(parentScope);
			scopeUnit = new AssetScopeStorageUnit(scope, parentScope);
			AssetScopeStorageUnit parentScopeUnit = storage.get(parentScope);
			scopeUnit.rootParentScope = parentScopeUnit.rootParentScope;
			scopeUnit.storagePosition = parentScopeUnit.storagePosition + 1;
			storage.put(scope, scopeUnit);
		}
		return scopeUnit;
	}

	/**
	 * Check if an asset have a known parent scope
	 * 
	 * @param parentScope
	 *            parent scope to check
	 */
	private void checkUnknownParentScope(String parentScope) {
		if (!storage.containsKey(parentScope) && !DETACHED_PARENT_SCOPE.equalsIgnoreCase(parentScope)) {
			throw new DandelionException(AssetStorageError.UNDEFINED_PARENT_SCOPE).set("parentScope", parentScope);
		}
	}

	/**
	 * Check if an asset don't have a couple of Scope/Parent Scope identical to
	 * the couple Scope/Another parent scope
	 * 
	 * @param parentScope
	 *            parent scope to check
	 * @param storedAssetScopeStorageUnit
	 *            stored storage unit
	 */
	private void checkParentScopeIncompatibility(String parentScope, AssetScopeStorageUnit storedAssetScopeStorageUnit) {
		if (!storedAssetScopeStorageUnit.parentScope.equalsIgnoreCase(parentScope)) {
			throw new DandelionException(AssetStorageError.PARENT_SCOPE_INCOMPATIBILITY).set("scope",
                    storedAssetScopeStorageUnit.scope).set("parentScope", storedAssetScopeStorageUnit.parentScope);
		}
	}

	/**
	 * Retrieve the assets (as map) for a groups of scopes.
	 * 
	 * @param scopes
	 *            scopes of needed assets
	 * @return the map of assets for scopes (key is the Asset#getAssetKey)
	 */
	private Map<String, Asset> assetsMapFor(String... scopes) {
		if (scopes.length == 0) {
			scopes = new String[] { ROOT_SCOPE };
		}
		Map<String, Asset> assetsMap = new HashMap<String, Asset>();
		for (String scope : scopes) {
			Map<String, Asset> scopedAssetsMap = new HashMap<String, Asset>();
			AssetScopeStorageUnit assetScope = storage.get(scope.toLowerCase());
			if (assetScope != null) {
				for (Asset asset : assetScope.assets) {
					String key = asset.getAssetKey() + "_" + assetScope.rootParentScope;
					scopedAssetsMap.put(key, Asset.class.cast(asset.clone(false)));
				}

				if (!MASTER_SCOPE.equalsIgnoreCase(assetScope.parentScope)) {
					mergeAssets(scopedAssetsMap, assetsMapFor(assetScope.parentScope));
				}
				mergeAssets(assetsMap, scopedAssetsMap);
			}
		}
		return assetsMap;
	}

	/**
	 * Merge 2 maps of assets
	 * 
	 * @param container
	 *            current container of assets
	 * @param others
	 *            the others assets for add
	 */
	private void mergeAssets(Map<String, Asset> container, Map<String, Asset> others) {
		for (Map.Entry<String, Asset> other : others.entrySet()) {
			if (container.containsKey(other.getKey())) {
				Asset asset = container.get(other.getKey());
				int smallestValue = asset.storagePosition < other.getValue().storagePosition ? asset.storagePosition
						: other.getValue().storagePosition;
				if (asset.getVersion().equalsIgnoreCase(other.getValue().getVersion())) {
					asset.storagePosition = smallestValue;
					if (other.getValue().getLocations() != null) {
						for (Map.Entry<String, String> location : other.getValue().getLocations().entrySet()) {
							if (!asset.getLocations().containsKey(location.getKey())) {
								asset.getLocations().put(location.getKey(), location.getValue());
							}
						}
					}
					if (other.getValue().getAttributesOnlyName() != null) {
						for (String attribute : other.getValue().getAttributesOnlyName()) {
							if (!Arrays.asList(asset.getAttributesOnlyName()).contains(attribute)) {
								asset.addAttribute(attribute);
							}
						}
					}
					if (other.getValue().getAttributes() != null) {
						for (Map.Entry<String, String> attribute : other.getValue().getAttributes().entrySet()) {
							if (!asset.getAttributes().containsKey(attribute.getKey())) {
								asset.addAttribute(attribute.getKey(), attribute.getValue());
							}
						}
					}
				}
				else if (smallestValue == asset.storagePosition) {
					Asset _asset = other.getValue();
					_asset.storagePosition = asset.storagePosition;
					container.put(other.getKey(), _asset);
				}
				else if (smallestValue == other.getValue().storagePosition) {
					asset.storagePosition = smallestValue;
				}
			}
			else {
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

	/**
	 * Check if the storage contains any asset
	 * 
	 * @return <code>true</code> if one (or more) asset is store
	 */
	public boolean containsAnyAsset() {
		for (AssetScopeStorageUnit unit : storage.values()) {
			if (!unit.assets.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
