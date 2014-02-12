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

import static com.github.dandelion.core.asset.AssetStorage.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.loader.AssetLoaderSystem;
import com.github.dandelion.core.asset.loader.spi.AssetLoader;
import com.github.dandelion.core.asset.processor.impl.AssetAggregationProcessor;
import com.github.dandelion.core.asset.processor.impl.AssetMinificationProcessor;
import com.github.dandelion.core.asset.wrapper.AssetLocationWrapperSystem;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.config.Configuration;

/**
 * Load Assets configuration
 * <ul>
 * <li>assetsLoader :
 * <ul>
 * <li>the {@link com.github.dandelion.core.asset.loader.spi.AssetLoader} found
 * in 'dandelion/dandelion.properties' for the key 'assetsLoader'</li>
 * <li>or
 * {@link com.github.dandelion.core.asset.loader.impl.AbstractAssetJsonLoader}
 * by default</li>
 * </ul>
 * </li>
 * <li>assets.locations : type of access to assets content(remote [by default],
 * local)</li>
 * </ul>
 * 
 * @author Romain Lespinasse
 */
public class AssetConfigurator {

	private static final Logger LOG = LoggerFactory.getLogger(AssetConfigurator.class);

	AssetStorage assetStorage;
	List<AssetLoader> assetLoaders;
	List<String> assetsLocations;
	List<String> excludedBundles;
	List<String> excludedAssets;
	Map<String, AssetLocationWrapper> assetsLocationWrappers;

	private Map<String, List<Asset>> assetsByBundle = new HashMap<String, List<Asset>>();
	private Map<String, List<String>> bundlesByParentBundle = new HashMap<String, List<String>>();
	private Map<String, String> parentBundlesByBundle = new HashMap<String, String>();
	private Map<String, List<Asset>> overrideAssetsByBundle = new HashMap<String, List<Asset>>();

	AssetConfigurator(AssetStorage assetStorage) {
		this.assetStorage = assetStorage;
	}

	/**
	 * Initialization of Assets Configurator on application load
	 */
	void initialize() {
		Properties configuration = Configuration.getProperties();

		assetsLocations = setPropertyAsList(configuration.getProperty("assets.locations"), ",");
		excludedBundles = setPropertyAsList(configuration.getProperty("assets.excluded.bundles"), ",");
		excludedAssets = setPropertyAsList(configuration.getProperty("assets.excluded.assets"), ",");

		assetLoaders = AssetLoaderSystem.getLoaders();
		assetsLocationWrappers = AssetLocationWrapperSystem.getWrappersWithKey();
		
		if (new AssetMinificationProcessor().isMinificationEnabled()
				|| new AssetAggregationProcessor().isAggregationEnabled()) {
			activateLocationWrapper("cdn");
		}
		processAssetsLoading(true);
	}

	/**
	 * Set the default configuration when it's needed
	 */
	void setDefaultsIfNeeded() {
		if (assetsLocations == null) {
			assetsLocations = setPropertyAsList("cdn,classpath", ",");
		}
		if (excludedBundles == null) {
			excludedBundles = new ArrayList<String>();
		}
		if (excludedAssets == null) {
			excludedAssets = new ArrayList<String>();
		}
	}

	/**
	 * Process to the assets loading from defined asset loader
	 */
	void processAssetsLoading(boolean defaultsNeeded) {
		if (defaultsNeeded)
			setDefaultsIfNeeded();

		for (AssetLoader assetLoader : assetLoaders) {
			prepareAssetsLoading(assetLoader.loadAssets());
		}

		repairOrphanParentBundle();
		overrideAssetsByBundle();

		storeAssetsFromBundle(ROOT_BUNDLE, null);
		storeAssetsFromBundle(DETACHED_PARENT_BUNDLE, null);

		clearAllAssetsProcessElements();
	}

	void activateLocationWrapper(String locationKey){
		if (assetsLocationWrappers.containsKey(locationKey)) {
			assetsLocationWrappers.get(locationKey).setActive(true);
			LOG.debug("Asset location wrapper with the key {} is enabled", locationKey);
		}
    }
	private void repairOrphanParentBundle() {
		Set<String> orphans = new HashSet<String>();
		for (String parentBundle : parentBundlesByBundle.values()) {
			if (!ROOT_BUNDLE.equalsIgnoreCase(parentBundle) && !DETACHED_PARENT_BUNDLE.equalsIgnoreCase(parentBundle)
					&& !parentBundlesByBundle.containsKey(parentBundle)) {
				orphans.add(parentBundle);
			}
		}
		if (!orphans.isEmpty()) {
			if (!bundlesByParentBundle.containsKey(ROOT_BUNDLE)) {
				bundlesByParentBundle.put(ROOT_BUNDLE, new ArrayList<String>());
			}
			for (String orphan : orphans) {
				parentBundlesByBundle.put(orphan, ROOT_BUNDLE);
				bundlesByParentBundle.get(ROOT_BUNDLE).add(orphan);
			}
		}
	}

	/**
	 * Override all assets of a bundle by `override` assets
	 */
	private void overrideAssetsByBundle() {
		for (Map.Entry<String, List<Asset>> entry : assetsByBundle.entrySet()) {
			if (overrideAssetsByBundle.containsKey(entry.getKey())) {
				entry.setValue(overrideAssetsByBundle.get(entry.getKey()));
			}
		}
	}

	/**
	 * Prepare Assets Loading by
	 * 
	 * <ul>
	 * <li>link a bundle to all his assets</li>
	 * <li>link a bundle to his parent bundle</li>
	 * <li>link a parent bundle to all his bundles</li>
	 * </ul>
	 * 
	 * @param components
	 *            components to analyze
	 */
	private void prepareAssetsLoading(List<AssetComponent> components) {
		LOG.debug("Excluded bundles are {}", excludedBundles);
		LOG.debug("Excluded assets are {}", excludedAssets);

		for (AssetComponent component : components) {
			LOG.debug("Prepare {}", component);

			if (!excludedBundles.contains(component.getBundle()) && !excludedBundles.contains(component.getParent())) {
				LOG.debug("Bundle {} and his parent {} are not excluded", component.getBundle(),
						component.getParent());

				if (component.isOverride()) {
					prepareOverrideAssets(component);
				}
				else {
					prepareParentBundle(component);
					prepareBundle(component);
					prepareAssets(component);
				}
			}
		}
	}

	/**
	 * Store assets from bundle.
	 * 
	 * @param bundle
	 *            bundles to store
	 */
	private void storeAssetsFromBundle(String bundle, String parentBundle) {
		if (assetsByBundle.containsKey(bundle)) {
			List<Asset> _assets = assetsByBundle.get(bundle);
			if (_assets.isEmpty() && parentBundle != null) {
				assetStorage.setupEmptyBundle(bundle, parentBundle);
			}
			else {
				for (Asset _asset : _assets) {
					storeAsset(_asset, bundle, parentBundlesByBundle.get(bundle));
				}
			}
		}

		if (bundlesByParentBundle.containsKey(bundle)) {
			List<String> _bundles = bundlesByParentBundle.get(bundle);
			for (String _bundle : _bundles) {
				storeAssetsFromBundle(_bundle, bundle);
			}
		}
	}

	/**
	 * Workflow to store an asset
	 * 
	 * @param asset
	 *            asset to store
	 * @param bundle
	 *            bundle of this asset
	 * @param parentBundle
	 *            parent of this bundle
	 */
	private void storeAsset(Asset asset, String bundle, String parentBundle) {
		LOG.debug("Stored '{}' in bundle '{}/{}'", asset, bundle, parentBundle);
		try {
			assetStorage.store(asset, bundle, parentBundle);
		}
		catch (DandelionException e) {
			LOG.debug(e.getLocalizedMessage());
			if (e.getErrorCode() == AssetStorageError.UNDEFINED_PARENT_BUNDLE) {
				LOG.debug("To avoid any configuration problem, a bundle '{}' with no assets is created", parentBundle);
				assetStorage.setupEmptyParentBundle(parentBundle);
				storeAsset(asset, bundle, parentBundle);
			}
		}
	}

	/**
	 * Clear all working attributes
	 */
	void clearAllAssetsProcessElements() {
		LOG.debug("Clearing all assets process elements");
		assetsByBundle.clear();
		bundlesByParentBundle.clear();
		parentBundlesByBundle.clear();
		overrideAssetsByBundle.clear();
	}

	private List<String> setPropertyAsList(String values, String delimiter) {
		if (values == null || values.isEmpty())
			return null;
		return Arrays.asList(values.split(delimiter));
	}

	private void prepareBundle(AssetComponent component) {
		if (ROOT_BUNDLE.equalsIgnoreCase(component.getBundle())) {
			LOG.debug("{} is the root bundle", component.getBundle());
			return;
		}
		if (!bundlesByParentBundle.containsKey(component.getParent())) {
			bundlesByParentBundle.put(component.getParent(), new ArrayList<String>());
		}
		List<String> _bundles = bundlesByParentBundle.get(component.getParent());

		if (!_bundles.contains(component.getBundle())) {
			LOG.debug("Stored {} as child of {}", component.getBundle(), component.getParent());
			_bundles.add(component.getBundle());
		}
		else {
			LOG.debug("{} is already a child of {}", component.getBundle(), component.getParent());
		}
	}

	private void prepareParentBundle(AssetComponent component) {
		LOG.debug("Stored {} as parent of {}", component.getParent(), component.getBundle());
		if (ROOT_BUNDLE.equalsIgnoreCase(component.getParent()) && ROOT_BUNDLE.equalsIgnoreCase(component.getBundle())) {
			component.setParent(MASTER_BUNDLE);
		}
		parentBundlesByBundle.put(component.getBundle(), component.getParent());
	}

	private void prepareAssets(AssetComponent component) {
		if (!assetsByBundle.containsKey(component.getBundle())) {
			assetsByBundle.put(component.getBundle(), new ArrayList<Asset>());
		}
		List<Asset> _assets = assetsByBundle.get(component.getBundle());

		for (Asset asset : component.getAssets()) {
			if (!excludedAssets.contains(asset.getName())) {
				LOG.debug("Stored {} as child of {}", asset.getName(), component.getBundle());
				_assets.add(asset);
                for(Map.Entry<String, String> entry:asset.getLocations().entrySet()) {
                    if(assetsLocationWrappers == null || !assetsLocationWrappers.containsKey(entry.getKey())) {
                        LOG.warn("Asset {} have a location {} without {} wrapper", asset.getName(), entry.getValue(), entry.getKey());
                    }
                }
			}
			else {
				LOG.debug("{} is excluded", asset.getName());
			}
		}
	}

	private void prepareOverrideAssets(AssetComponent component) {
		List<Asset> _assets = new ArrayList<Asset>();

		for (Asset asset : component.getAssets()) {
			if (!excludedAssets.contains(asset.getName())) {
				_assets.add(asset);
			}
		}
		overrideAssetsByBundle.put(component.getBundle(), _assets);
	}
}
