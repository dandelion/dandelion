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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.processor.impl.AssetAggregationProcessor;
import com.github.dandelion.core.asset.processor.impl.AssetMinificationProcessor;
import com.github.dandelion.core.asset.wrapper.AssetLocationWrapperSystem;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.bundle.Bundle;
import com.github.dandelion.core.bundle.loader.BundleLoaderSystem;
import com.github.dandelion.core.bundle.loader.spi.BundleLoader;
import com.github.dandelion.core.config.Configuration;

/**
 * Load Assets configuration
 * <ul>
 * <li>assetsLoader :
 * <ul>
 * <li>the {@link com.github.dandelion.core.bundle.loader.spi.BundleLoader} found
 * in 'dandelion/dandelion.properties' for the key 'assetsLoader'</li>
 * <li>or
 * {@link com.github.dandelion.core.bundle.loader.impl.AbstractBundleLoader}
 * by default</li>
 * </ul>
 * </li>
 * <li>assets.locations : type of access to assets content(remote [by default],
 * local)</li>
 * </ul>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 */
public class AssetConfigurator {

	private static final Logger LOG = LoggerFactory.getLogger(AssetConfigurator.class);

	private List<BundleLoader> bundleLoaders;
	private List<String> assetsLocations;
	private List<String> excludedBundles;
	private List<String> excludedAssets;
	private Map<String, AssetLocationWrapper> assetsLocationWrappers;

	public AssetConfigurator() {
	}

	/**
	 * Initialization of Assets Configurator on application load
	 */
	public void initialize() {
		Properties configuration = Configuration.getProperties();

		assetsLocations = setPropertyAsList(configuration.getProperty("assets.locations"), ",");
		excludedBundles = setPropertyAsList(configuration.getProperty("assets.excluded.bundles"), ",");
		excludedAssets = setPropertyAsList(configuration.getProperty("assets.excluded.assets"), ",");

		bundleLoaders = BundleLoaderSystem.getLoaders();
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
	public void setDefaultsIfNeeded() {
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
	public void processAssetsLoading(boolean defaultsNeeded) {
		if (defaultsNeeded){
			setDefaultsIfNeeded();
		}

		for (BundleLoader assetLoader : bundleLoaders) {
			prepareAssetsLoading(assetLoader.loadBundles());
		}

		Assets.getStorage().checkBundleDag();
//		repairOrphanParentBundle();
//		overrideAssetsByBundle();
//
//		storeAssetsFromBundle(ROOT_BUNDLE, null);
//		storeAssetsFromBundle(DETACHED_PARENT_BUNDLE, null);
//
//		clearAllAssetsProcessElements();
	}

	public void activateLocationWrapper(String locationKey){
		if (assetsLocationWrappers.containsKey(locationKey)) {
			assetsLocationWrappers.get(locationKey).setActive(true);
			LOG.debug("Asset location wrapper with the key {} is enabled", locationKey);
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
	private void prepareAssetsLoading(List<Bundle> bundles) {
		LOG.debug("Excluded bundles are {}", excludedBundles);
		LOG.debug("Excluded assets are {}", excludedAssets);

		Assets.getStorage().loadBundles(bundles);

//		for (AssetComponent component : components) {
//			LOG.debug("Prepare {}", component);
//
//			if (!excludedBundles.contains(component.getBundle()) && !excludedBundles.contains(component.getParent())) {
//				LOG.debug("Bundle {} and his parent {} are not excluded", component.getBundle(),
//						component.getParent());
//
//				if (component.isOverride()) {
//					prepareOverrideAssets(component);
//				}
//				else {
//					prepareParentBundle(component);
//					prepareBundle(component);
//					prepareAssets(component);
//				}
//			}
//		}
	}

	private List<String> setPropertyAsList(String values, String delimiter) {
		if (values == null || values.isEmpty())
			return null;
		return Arrays.asList(values.split(delimiter));
	}

	public List<String> getAssetLocations(){
		return this.assetsLocations;
	}
	
	public void setBundleLoaders(List<BundleLoader> bundleLoaders) {
		this.bundleLoaders = bundleLoaders;
	}

	public void setAssetsLocationWrappers(Map<String, AssetLocationWrapper> assetsLocationWrappers) {
		this.assetsLocationWrappers = assetsLocationWrappers;
	}

	public Map<String, AssetLocationWrapper> getAssetsLocationWrappers(){
		return this.assetsLocationWrappers;
	}
	
	public List<BundleLoader> getBundleLoaders(){
		return this.bundleLoaders;
	}
}