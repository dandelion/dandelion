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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.locator.AssetLocatorSystem;
import com.github.dandelion.core.asset.locator.spi.AssetLocator;
import com.github.dandelion.core.bundle.loader.BundleLoaderSystem;
import com.github.dandelion.core.bundle.loader.spi.BundleLoader;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.PropertiesUtils;

/**
 * Load Assets configuration
 * <ul>
 * <li>assetsLoader :
 * <ul>
 * <li>the {@link com.github.dandelion.core.bundle.loader.spi.BundleLoader}
 * found in 'dandelion/dandelion.properties' for the key 'assetsLoader'</li>
 * <li>or
 * {@link com.github.dandelion.core.bundle.loader.spi.AbstractBundleLoader} by
 * default</li>
 * </ul>
 * </li>
 * <li>assets.locations : type of access to assets content(remote [by default],
 * local)</li>
 * </ul>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetConfigurator {

	private static final Logger LOG = LoggerFactory.getLogger(AssetConfigurator.class);

	private List<String> assetsLocations;
	private List<String> excludedBundles;
	private List<String> excludedAssets;
	private List<BundleLoader> bundleLoaders;
	private Map<String, AssetLocator> assetLocatorsMap;
	private BundleStorage bundleStorage;
	
	public AssetConfigurator() {
	}

	/**
	 * Initialization of Assets Configurator on application load
	 */
	public void initialize() {
		assetsLocations = PropertiesUtils.propertyAsList(Configuration.getAssetLocationStrategy(), ",");
		excludedBundles = PropertiesUtils.propertyAsList(Configuration.getBundleExcludes(), ",");
		excludedAssets = PropertiesUtils.propertyAsList(Configuration.getAssetExcludes(), ",");

		bundleLoaders = BundleLoaderSystem.getLoaders();

		bundleStorage = new BundleStorage();
		for (BundleLoader assetLoader : bundleLoaders) {
			prepareAssetsLoading(assetLoader.loadBundles());
		}

		getStorage().checkBundleDag();
		
		assetLocatorsMap = AssetLocatorSystem.getAssetLocatorsMap();
		
		processAssetsLoading();
	}


	/**
	 * Process to the assets loading from defined asset loader
	 */
	public void processAssetsLoading() {

		for (BundleLoader assetLoader : bundleLoaders) {
			prepareAssetsLoading(assetLoader.loadBundles());
		}

		getStorage().checkBundleDag();
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
	private void prepareAssetsLoading(List<BundleStorageUnit> bundles) {
		LOG.debug("Excluded bundles are {}", excludedBundles);
		LOG.debug("Excluded assets are {}", excludedAssets);

		getStorage().loadBundles(bundles);
	}

	public List<String> getAssetLocations() {
		return this.assetsLocations;
	}

	public Map<String, AssetLocator> getAssetLocatorsMap() {
		return this.assetLocatorsMap;
	}

	public List<BundleLoader> getBundleLoaders() {
		return this.bundleLoaders;
	}
	
	public BundleStorage getStorage(){
		return bundleStorage;
	}
}