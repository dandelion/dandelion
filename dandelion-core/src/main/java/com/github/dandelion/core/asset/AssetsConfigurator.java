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
import com.github.dandelion.core.asset.loader.AssetsJsonLoader;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.DandelionScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.github.dandelion.core.asset.AssetsStorage.*;

/**
 * Load Assets configuration
 * <ul>
 *     <li>assetsLoader :
 *          <ul>
 *               <li>the {@link AssetsLoader}
 * found in 'dandelion/dandelion.properties' for key 'assetsLoader'</li>
 *               <li>or {@link com.github.dandelion.core.asset.loader.AssetsJsonLoader} by default</li>
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
    Map<String, AssetsLocationWrapper> assetsLocationWrappers;

    private Map<String, List<Asset>> componentsByScope = new HashMap<String, List<Asset>>();
    private Map<String, List<String>> scopesByParentScope = new HashMap<String, List<String>>();
    private Map<String, String> parentScopesByScope = new HashMap<String, String>();

    AssetsConfigurator(AssetsStorage assetsStorage) {
        this.assetsStorage = assetsStorage;
    }

    /**
     * Initialization of Assets Configurator on application load
     */
    void initialize() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Properties configuration = Configuration.getProperties();

        assetsLocations = setPropertyAsList(configuration.getProperty("assets.locations"), ",");
        excludedScopes = setPropertyAsList(configuration.getProperty("assets.excluded.scopes"), ",");
        excludedAssets = setPropertyAsList(configuration.getProperty("assets.excluded.assets"), ",");
        assetsLoaders = extractAssetsLoaders(classLoader, configuration);
        assetsLocationWrappers = extractAssetsLocationWrappers(classLoader, configuration);

        processAssetsLoading(true);
    }

    /**
     * @param classLoader class loader
     * @param properties configuration properties
     * @return instances of assets loader
     */
    private List<AssetsLoader> extractAssetsLoaders(ClassLoader classLoader, Properties properties) {
        List<String> assetsLoaders = setPropertyAsList(properties.getProperty("assets.loaders"), ",");
        if(assetsLoaders == null) return null;
        List<AssetsLoader> loaders = new ArrayList<AssetsLoader>();
        for(String loader:assetsLoaders) {
            AssetsLoader _loader = getAssetsLoader(classLoader, loader);
            if(_loader != null) {
                loaders.add(_loader);
            }
        }
        return loaders;
    }

    /**
     * @param classLoader class loader
     * @param assetsLoader assets loader class name
     * @return instance of assets loader
     */
    private AssetsLoader getAssetsLoader(ClassLoader classLoader, String assetsLoader) {
        if(assetsLoader != null) {
            try {
                Class<AssetsLoader> cal = (Class<AssetsLoader>) classLoader.loadClass(assetsLoader);
                return cal.newInstance();
            } catch (ClassCastException e) {
                LOG.warn("the 'assetsLoader[{}]' must implements '{}'",
                        assetsLoader, AssetsLoader.class.getCanonicalName());
            } catch (InstantiationException e) {
                LOG.warn("the 'assetsLoader[{}]' should authorize instantiation", assetsLoader);
            } catch (IllegalAccessException e) {
                LOG.warn("the 'assetsLoader[{}]' should authorize access from '{}'",
                        assetsLoader, AssetsConfigurator.class.getCanonicalName());
            } catch (ClassNotFoundException e) {
                LOG.warn("the 'assetsLoader[{}]' must exists in the classpath", assetsLoader);
            }
        }
        return null;
    }

    /**
     * Load all wrappers found in configuration properties<br/>
     * a wrapper configuration have a key like assets.location.wrapper.{location}<br/>
     * {location} must match {@link com.github.dandelion.core.asset.AssetsLocationWrapper#locationKey()}
     *
     * @param classLoader class loader
     * @param properties configuration properties
     * @return all wrappers
     */
    private Map<String, AssetsLocationWrapper> extractAssetsLocationWrappers(ClassLoader classLoader, Properties properties) {
        Map<String, AssetsLocationWrapper> wrappers = new HashMap<String, AssetsLocationWrapper>();
        for(String property:properties.stringPropertyNames()) {
            if(property.startsWith("assets.location.wrapper.")) {
                AssetsLocationWrapper alw = getPropertyAsAssetsLocationWrapper(classLoader, properties.getProperty(property));
                if(alw != null) {
                    String location = property.replace("assets.location.wrapper.", "");
                    if(location.equalsIgnoreCase(alw.locationKey())) {
                        wrappers.put(location, alw);
                    }
                }
            }
        }
        return wrappers;
    }

    /**
     * @param classLoader class loader
     * @param wrapper wrapper class name
     * @return an instance of a wrapper
     */
    private AssetsLocationWrapper getPropertyAsAssetsLocationWrapper(ClassLoader classLoader, String wrapper) {
        if(wrapper != null) {
            try {
                Class<AssetsLocationWrapper> cal = (Class<AssetsLocationWrapper>) classLoader.loadClass(wrapper);
                return cal.newInstance();
            } catch (ClassCastException e) {
                LOG.warn("the 'wrapper[{}]' must implements '{}'",
                        wrapper, AssetsLocationWrapper.class.getCanonicalName());
            } catch (InstantiationException e) {
                LOG.warn("the 'wrapper[{}]' should authorize instantiation", wrapper);
            } catch (IllegalAccessException e) {
                LOG.warn("the 'wrapper[{}]' should authorize access from '{}'",
                        wrapper, AssetsConfigurator.class.getCanonicalName());
            } catch (ClassNotFoundException e) {
                LOG.warn("the 'wrapper[{}]' must exists in the classpath", wrapper);
            }
        }
        return null;
    }

    /**
     * Set the default configuration when it's needed
     */
    void setDefaultsIfNeeded() {
        if(assetsLoaders == null) {
            assetsLoaders = new ArrayList<AssetsLoader>();
            assetsLoaders.add(new AssetsJsonLoader());
        }
        if(assetsLocations == null) {
            assetsLocations = setPropertyAsList("cdn,classpath", ",");
        }
        if(excludedScopes == null) {
            excludedScopes = new ArrayList<String>();
        }
        if(excludedAssets == null) {
            excludedAssets = new ArrayList<String>();
        }
        if(assetsLocationWrappers == null) {
            assetsLocationWrappers = new HashMap<String, AssetsLocationWrapper>();
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

        storeAssetsFromScope(ROOT_SCOPE, true);
        storeAssetsFromScope(DETACHED_PARENT_SCOPE, true);

        clearAllAssetsProcessElements();
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

                prepareParentScope(component);
                prepareScope(component);
                prepareAssets(component);
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
        if(componentsByScope.containsKey(scope)) {
            List<Asset> _assets = componentsByScope.get(scope);
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
        LOG.debug("Store '{}' in scope '{}/{}'", asset, scope, parentScope);
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
        LOG.debug("Clear all assets process elements");
        componentsByScope.clear();
        scopesByParentScope.clear();
        parentScopesByScope.clear();
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
            scopesByParentScope.put(component.getParent(), new ArrayList());
        }
        List<String> _scopes = scopesByParentScope.get(component.getParent());

        if(!_scopes.contains(component.getScope())) {
            LOG.debug("Store {} as child of {}", component.getScope(), component.getParent());
            _scopes.add(component.getScope());
        } else {
            LOG.debug("Store {} is already a child of {}", component.getScope(), component.getParent());
        }
    }

    private void prepareParentScope(AssetsComponent component) {
        LOG.debug("Store {} as parent of {}", component.getParent(), component.getScope());
        if(ROOT_SCOPE.equalsIgnoreCase(component.getParent())
                && ROOT_SCOPE.equalsIgnoreCase(component.getScope())) {
            component.setParent(MASTER_SCOPE);
        }
        parentScopesByScope.put(component.getScope(), component.getParent());
    }

    private void prepareAssets(AssetsComponent component) {
        if (!componentsByScope.containsKey(component.getScope())) {
            componentsByScope.put(component.getScope(), new ArrayList<Asset>());
        }
        List<Asset> _assets = componentsByScope.get(component.getScope());

        for(Asset asset:component.getAssets()) {
            if(!excludedAssets.contains(asset.getName())) {
                LOG.debug("Store {} as child of {}", asset.getName(), component.getScope());
                _assets.add(asset);
            } else {
                LOG.debug("{} is exclude", asset.getName());
            }
        }
    }
}
