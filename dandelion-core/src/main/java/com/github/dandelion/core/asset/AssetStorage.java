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
 * An asset is stored inside a bundle.<br/>
 * All bundles have a parent except for ROOT parent (aka "root bundle").<br/>
 * An asset can be accessed by bundle.<br/>
 */
public final class AssetStorage {
	static final int ASSET_BUNDLE_STORAGE_POSITION = 1000;

	private enum StorageCommand {
		INSERT, MERGE
	};

	/**
	 * Assets Storage Units
	 */
	private Map<String, AssetBundleStorageUnit> storage;

	/**
	 * Root bundle string representation
	 */
	public static final String MASTER_BUNDLE = "master_bundle_" + System.currentTimeMillis();

	/**
	 * Root bundle string representation
	 */
	public static final String ROOT_BUNDLE = "default";

	/**
	 * Detached bundle string representation
	 */
	public static final String DETACHED_PARENT_BUNDLE = "none";

	/**
	 * Assets Storage Utility
	 */
	AssetStorage() {
		// initialize storage
		storage = new HashMap<String, AssetBundleStorageUnit>();
		// initialize root storage unit
		AssetBundleStorageUnit rootUnit = new AssetBundleStorageUnit(ROOT_BUNDLE, MASTER_BUNDLE);
		rootUnit.rootParentBundle = ROOT_BUNDLE;
		rootUnit.storagePosition = 0;
		storage.put(ROOT_BUNDLE, rootUnit);
		// initialize detached storage unit
		AssetBundleStorageUnit detachedUnit = new AssetBundleStorageUnit(DETACHED_PARENT_BUNDLE, MASTER_BUNDLE);
		detachedUnit.rootParentBundle = DETACHED_PARENT_BUNDLE;
		detachedUnit.storagePosition = 0;
		storage.put(DETACHED_PARENT_BUNDLE, detachedUnit);
	}

	/**
	 * Stores an asset in the root bundle.
	 * 
	 * @param asset
	 *            asset to store
	 */
	public void store(Asset asset) {
		store(asset, ROOT_BUNDLE, MASTER_BUNDLE);
	}

	/**
	 * Stores the given asset in the given bundle (with the root bundle as
	 * parent).
	 * 
	 * @param asset
	 *            The asset to store.
	 * @param bundle
	 *            The bundle in which to store the asset.
	 */
	public void store(Asset asset, String bundle) {
		store(asset, bundle, ROOT_BUNDLE);
	}

	/**
	 * Stores the given asset in the given bundle (with the given parent
	 * bundle).
	 * 
	 * @param asset
	 *            The asset to store.
	 * @param bundle
	 *            The bundle in which to store the asset.
	 * @param parentBundle
	 *            The parent bundle.
	 */
	public void store(Asset asset, String bundle, String parentBundle) {

		// don't store if we found invalid asset
		if (asset == null || !asset.isValid())
			return;

		if (DETACHED_PARENT_BUNDLE.equalsIgnoreCase(bundle)) {
			throw new DandelionException(AssetStorageError.DETACHED_BUNDLE_NOT_ALLOWED).set("detachedBundle",
					DETACHED_PARENT_BUNDLE);
		}
		AssetBundleStorageUnit bundleUnit = getStorageUnit(bundle, parentBundle);
		insertAsset(asset, bundleUnit);
	}

	/**
	 * Retrieves the assets for a groups of bundles.
	 * 
	 * @param bundles
	 *            Bundles in which assets are retrieved.
	 * @return the list of assets stored inside the given bundles.
	 */
	public List<Asset> assetsFor(String... bundles) {
		List<Asset> assets = new ArrayList<Asset>(assetsMapFor(bundles).values());
		Collections.sort(assets, assetStoragePositionComparator);
		return assets;
	}

	/**
	 * Store an asset in the Storage Unit of this bundle.
	 * 
	 * @param asset
	 *            asset to store
	 * @param bundleUnit
	 *            storage unit of the asset's bundle.
	 */
	private void insertAsset(Asset asset, AssetBundleStorageUnit bundleUnit) {
		StorageCommand command = checkAssetStorageIncompatibility(asset, bundleUnit);
		switch (command) {
		case INSERT:
			// set up position in the storage and the storage unit
			asset.storagePosition = bundleUnit.storagePosition * ASSET_BUNDLE_STORAGE_POSITION + bundleUnit.assets.size();
			bundleUnit.assets.add(asset);
			break;
		case MERGE:
			Asset originalAsset = bundleUnit.assets.get(bundleUnit.assets.indexOf(asset));
			combineAsset(originalAsset, asset);
			break;
		default:
			return;
		}
	}

	private StorageCommand checkAssetStorageIncompatibility(Asset asset, AssetBundleStorageUnit bundleUnit) {
		// the asset can be store if he isn't store already
		if (!bundleUnit.assets.contains(asset)) {
			return StorageCommand.INSERT;
		}
		// if not, we check if he can be merge
		Asset originalAsset = bundleUnit.assets.get(bundleUnit.assets.indexOf(asset));

		// he can be merge if the versions are equals
		if (!originalAsset.getVersion().equals(asset.getVersion())) {
			throw new DandelionException(AssetStorageError.ASSET_ALREADY_EXISTS_IN_BUNDLE).set("originalAsset", asset);
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
			throw new DandelionException(AssetStorageError.ASSET_LOCATION_ALREADY_EXISTS_IN_BUNDLE).set("locations",
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
			throw new DandelionException(AssetStorageError.ASSET_ATTRIBUTE_ALREADY_EXISTS_IN_BUNDLE).set("attributes",
					attributesInError).set("asset", asset);
		}

		// he can be merge if the DOM position can be merge
		if (originalAsset.getDom() != null && asset.getDom() != null && originalAsset.getDom() != asset.getDom()) {
			throw new DandelionException(AssetStorageError.ASSET_DOM_POSITION_ALREADY_EXISTS_IN_BUNDLE).set(
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

	void setupEmptyParentBundle(String bundle) {
		getStorageUnit(bundle, ROOT_BUNDLE);
	}

	void setupEmptyBundle(String bundle, String parentBundle) {
		getStorageUnit(bundle, parentBundle);
	}

	private AssetBundleStorageUnit getStorageUnit(String bundle, String parentBundle) {
		bundle = bundle.toLowerCase();
		parentBundle = parentBundle.toLowerCase();
		AssetBundleStorageUnit bundleUnit;
		if (storage.containsKey(bundle)) {
			AssetBundleStorageUnit storedBundleUnit = storage.get(bundle);
			checkParentBundleIncompatibility(parentBundle, storedBundleUnit);
			bundleUnit = storedBundleUnit;
		}
		else {
			// create a new empty bundle
			checkUnknownParentBundle(parentBundle);
			bundleUnit = new AssetBundleStorageUnit(bundle, parentBundle);
			AssetBundleStorageUnit parentBundleUnit = storage.get(parentBundle);
			bundleUnit.rootParentBundle = parentBundleUnit.rootParentBundle;
			bundleUnit.storagePosition = parentBundleUnit.storagePosition + 1;
			storage.put(bundle, bundleUnit);
		}
		return bundleUnit;
	}

	/**
	 * Checks if a bundle is known among all existing asset storage units.
	 * 
	 * @param parentBundle
	 *            The bundle to check.
	 */
	private void checkUnknownParentBundle(String parentBundle) {
		if (!storage.containsKey(parentBundle) && !DETACHED_PARENT_BUNDLE.equalsIgnoreCase(parentBundle)) {
			throw new DandelionException(AssetStorageError.UNDEFINED_PARENT_BUNDLE).set("parentBundle", parentBundle);
		}
	}

	/**
	 * Check if an asset don't have a couple of bundle/Parent bundle identical to
	 * the couple bundle/Another parent bundle
	 * 
	 * @param parentBundle
	 *            parent bundle to check
	 * @param storedAssetBundleStorageUnit
	 *            stored storage unit
	 */
	private void checkParentBundleIncompatibility(String parentBundle,
			AssetBundleStorageUnit storedAssetBundleStorageUnit) {
		if (!storedAssetBundleStorageUnit.parentBundle.equalsIgnoreCase(parentBundle)) {
			throw new DandelionException(AssetStorageError.PARENT_BUNDLE_INCOMPATIBILITY).set("bundle",
					storedAssetBundleStorageUnit.bundle).set("parentBundle", storedAssetBundleStorageUnit.parentBundle);
		}
	}

	/**
	 * Retrieve the assets (as map) for a groups of bundles.
	 * 
	 * @param bundles
	 *            bundles of needed assets
	 * @return the map of assets for bundles (key is the Asset#getAssetKey)
	 */
	private Map<String, Asset> assetsMapFor(String... bundles) {
		if (bundles.length == 0) {
			bundles = new String[] { ROOT_BUNDLE };
		}
		Map<String, Asset> assetsMap = new HashMap<String, Asset>();
		for (String bundle : bundles) {
			Map<String, Asset> bundledAssetsMap = new HashMap<String, Asset>();
			AssetBundleStorageUnit assetBundle = storage.get(bundle.toLowerCase());
			if (assetBundle != null) {
				for (Asset asset : assetBundle.assets) {
					String key = asset.getAssetKey() + "_" + assetBundle.rootParentBundle;
					bundledAssetsMap.put(key, Asset.class.cast(asset.clone(false)));
				}

				if (!MASTER_BUNDLE.equalsIgnoreCase(assetBundle.parentBundle)) {
					mergeAssets(bundledAssetsMap, assetsMapFor(assetBundle.parentBundle));
				}
				mergeAssets(assetsMap, bundledAssetsMap);
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
		for (AssetBundleStorageUnit unit : storage.values()) {
			if (!unit.assets.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
