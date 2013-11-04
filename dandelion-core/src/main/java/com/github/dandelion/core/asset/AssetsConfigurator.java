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

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.loader.AssetsLoaderSystem;
import com.github.dandelion.core.asset.loader.spi.AssetsLoader;
import com.github.dandelion.core.asset.wrapper.AssetLocationWrapperSystem;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.github.dandelion.core.asset.AssetsStorage.*;

/**
 * Load Assets configuration
 * <ul>
 *     <li>assetsLoader :
 *          <ul>
 *               <li>the {@link AssetsLoader}
 * found in 'dandelion/dandelion.properties' for the key 'assetsLoader'</li>
 *               <li>or {@link com.github.dandelion.core.asset.loader.impl.AbstractAssetsJsonLoader} by default</li>
 *          </ul>
 *     </li>
 *     <li>assets.locations : type of access to assets content(remote [by default], local)</li>
 * </ul>
 * Default Asset Loader is
 *
 */
public class AssetsConfigurator {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetsConfigurator.class);
    AssetsStorage assetsStorage;
    List<AssetsLoader> assetsLoaders;
    List<String> assetsLocations;
    List<String> excludedScopes;
    List<String> excludedAssets;
    Map<String, AssetLocationWrapper> assetsLocationWrappers;

    private Map<String, List<Asset>> assetsByScope = new HashMap<String, List<Asset>>();
    private Map<String, List<String>> scopesByParentScope = new HashMap<String, List<String>>();
    private Map<String, String> parentScopesByScope = new HashMap<String, String>();
    private Map<String, List<Asset>> overrideAssetsByScope = new HashMap<String, List<Asset>>();

    AssetsConfigurator(AssetsStorage assetsStorage) {
        this.assetsStorage = assetsStorage;
    }

    /**
     * Initialization of Assets Configurator on application load
     */
    void initialize() {
        Properties configuration = Configuration.getProperties();

        assetsLocations = setPropertyAsList(configuration.getProperty("assets.locations"), ",");
        excludedScopes = setPropertyAsList(configuration.getProperty("assets.excluded.scopes"), ",");
        excludedAssets = setPropertyAsList(configuration.getProperty("assets.excluded.assets"), ",");

        assetsLoaders = AssetsLoaderSystem.getLoaders();
        assetsLocationWrappers = AssetLocationWrapperSystem.getWrappersWithKey();

        processAssetsLoading(true);
    }

    /**
     * Set the default configuration when it's needed
     */
    void setDefaultsIfNeeded() {
        if(assetsLocations == null) {
            assetsLocations = setPropertyAsList("cdn,classpath", ",");
        }
        if(excludedScopes == null) {
            excludedScopes = new ArrayList<String>();
        }
        if(excludedAssets == null) {
            excludedAssets = new ArrayList<String>();
        }
    }

    /**
     * Process to the assets loading from defined asset loader
     */
    void processAssetsLoading(boolean defaultsNeeded) {
        if(defaultsNeeded) setDefaultsIfNeeded();

        for(AssetsLoader assetsLoader:assetsLoaders) {
            prepareAssetsLoading(assetsLoader.loadAssets());
        }

        //repairOrphanParentScope();
        overrideAssetsByScope();

        storeAssetsFromScope(ROOT_SCOPE, true);
        storeAssetsFromScope(DETACHED_PARENT_SCOPE, true);

        clearAllAssetsProcessElements();
    }

    private void repairOrphanParentScope() {
        List<String> orphans = new ArrayList<String>();
        for(String parentScope:parentScopesByScope.values()) {
            if (!parentScopesByScope.containsKey(parentScope)) {
                orphans.add(parentScope);
            }
        }
        for(String orphan:orphans) {
            parentScopesByScope.put(orphan, ROOT_SCOPE);
            scopesByParentScope.get(ROOT_SCOPE).add(orphan);
        }
    }

    /**
     * Override all assets of a scope by `override` assets
     */
    private void overrideAssetsByScope() {
        for(Map.Entry<String, List<Asset>> entry:assetsByScope.entrySet()) {
            if(overrideAssetsByScope.containsKey(entry.getKey())) {
                entry.setValue(overrideAssetsByScope.get(entry.getKey()));
            }
        }
    }

    /**
     * Prepare Assets Loading by
     *
     * <ul>
     *     <li>link a scope to all his assets</li>
     *     <li>link a scope to his parent scope</li>
     *     <li>link a parent scope to all his scopes</li>
     * </ul>
     *
     * @param components components to analyze
     */
    private void prepareAssetsLoading(List<AssetsComponent> components) {
        LOG.debug("Excludes scopes are {}", excludedScopes);
        LOG.debug("Excludes assets are {}", excludedAssets);

        for(AssetsComponent component:components) {
            LOG.debug("Prepare {}", component);

            if(!excludedScopes.contains(component.getScope())
                    && !excludedScopes.contains(component.getParent())) {
                LOG.debug("Scope {} and his parent {} are not in excludes scopes",
                        component.getScope(), component.getParent());

                if(component.isOverride()) {
                    prepareOverrideAssets(component);
                } else {
                    prepareParentScope(component);
                    prepareScope(component);
                    prepareAssets(component);
                }
            }
        }
    }

    /**
     * Store assets from scope
     *
     * @param scope scope to store
     * @param recursiveMode <code>true</code> to activate recursive mode for scope/parent scope
     */
    private void storeAssetsFromScope(String scope, boolean recursiveMode) {
        if(assetsByScope.containsKey(scope)) {
            List<Asset> _assets = assetsByScope.get(scope);
            for(Asset _asset:_assets) {
                storeAsset(_asset, scope, parentScopesByScope.get(scope));
            }
        }
        if(recursiveMode) {
            if(scopesByParentScope.containsKey(scope)) {
                List<String> _scopes = scopesByParentScope.get(scope);
                for(String _scope:_scopes) {
                    storeAssetsFromScope(_scope, true);
                }
            }
        }
    }

    /**
     * Workflow to store an asset
     *
     * @param asset asset to store
     * @param scope scope of this asset
     * @param parentScope parent of this scope
     */
    private void storeAsset(Asset asset, String scope, String parentScope) {
        LOG.debug("Stored '{}' in scope '{}/{}'", asset, scope, parentScope);
        try {
            assetsStorage.store(asset, scope, parentScope);
        } catch (DandelionException e) {
            LOG.debug(e.getLocalizedMessage());
            if(e.getErrorCode() == AssetsStorageError.UNDEFINED_PARENT_SCOPE) {
                LOG.debug("To avoid any configuration problem, a scope '{}' with no assets is created", parentScope);
                assetsStorage.store(null, parentScope);
                storeAsset(asset, scope, parentScope);
            }
        }
    }

    /**
     * Clear all working attributes
     */
    void clearAllAssetsProcessElements() {
        LOG.debug("Clearing all assets process elements");
        assetsByScope.clear();
        scopesByParentScope.clear();
        parentScopesByScope.clear();
        overrideAssetsByScope.clear();
    }

    private List<String> setPropertyAsList(String values, String delimiter) {
        if(values == null || values.isEmpty()) return null;
        return Arrays.asList(values.split(delimiter));
    }

    private void prepareScope(AssetsComponent component) {
        if (ROOT_SCOPE.equalsIgnoreCase(component.getScope())) {
            LOG.debug("{} is the root scope", component.getScope());
            return;
        }
        if (!scopesByParentScope.containsKey(component.getParent())) {
            scopesByParentScope.put(component.getParent(), new ArrayList<String>());
        }
        List<String> _scopes = scopesByParentScope.get(component.getParent());

        if(!_scopes.contains(component.getScope())) {
            LOG.debug("Stored {} as child of {}", component.getScope(), component.getParent());
            _scopes.add(component.getScope());
        } else {
            LOG.debug("{} is already a child of {}", component.getScope(), component.getParent());
        }
    }

    private void prepareParentScope(AssetsComponent component) {
        LOG.debug("Stored {} as parent of {}", component.getParent(), component.getScope());
        if(ROOT_SCOPE.equalsIgnoreCase(component.getParent())
                && ROOT_SCOPE.equalsIgnoreCase(component.getScope())) {
            component.setParent(MASTER_SCOPE);
        }
        parentScopesByScope.put(component.getScope(), component.getParent());
    }

    private void prepareAssets(AssetsComponent component) {
        if (!assetsByScope.containsKey(component.getScope())) {
            assetsByScope.put(component.getScope(), new ArrayList<Asset>());
        }
        List<Asset> _assets = assetsByScope.get(component.getScope());

        for(Asset asset:component.getAssets()) {
            if(!excludedAssets.contains(asset.getName())) {
                LOG.debug("Stored {} as child of {}", asset.getName(), component.getScope());
                _assets.add(asset);
            } else {
                LOG.debug("{} is excluded", asset.getName());
            }
        }
    }

    private void prepareOverrideAssets(AssetsComponent component) {
        List<Asset> _assets = new ArrayList<Asset>();

        for(Asset asset:component.getAssets()) {
            if(!excludedAssets.contains(asset.getName())) {
                _assets.add(asset);
            }
        }
        overrideAssetsByScope.put(component.getScope(), _assets);
    }
}
