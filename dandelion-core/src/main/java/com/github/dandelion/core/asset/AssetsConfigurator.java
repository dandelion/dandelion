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
import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.github.dandelion.core.asset.AssetsStorage.DETACHED_PARENT_SCOPE;
import static com.github.dandelion.core.asset.AssetsStorage.ROOT_SCOPE;

/**
 * Load Assets configuration
 * <ul>
 *     <li>assetsLoader :
 *          <ul>
 *               <li>the {@link AssetsLoader}
 * found in 'dandelion/dandelion.properties' for key 'assetsLoader'</li>
 *               <li>or {@link AssetsJsonLoader} by default</li>
 *          </ul>
 *     </li>
 *     <li>assetsLocations : type of access to assets content(remote [by default], local)</li>
 * </ul>
 * Default Asset Loader is
 *
 */
public class AssetsConfigurator {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetsConfigurator.class);
    AssetsStorage assetsStorage;
    AssetsLoader assetsLoader;
    String assetsLocations;

    private Map<String, List<Asset>> componentsByScope = new HashMap<String, List<Asset>>();
    private Map<String, List<String>> scopesByParentScope = new HashMap<String, List<String>>();
    private Map<String, String> parentScopesByScope = new HashMap<String, String>();

    AssetsConfigurator(AssetsStorage assetsStorage) {
        this.assetsStorage = assetsStorage;
    }

    /**
     * Initialization of Assets Configurator on application load
     *
     * @throws IOException if a I/O error appends when 'dandelion/dandelion.properties' is loaded
     */
    void initialize() {
        try {
            ClassPathResource[] resources = new ClassPathScanner().scanForResources("dandelion", "dandelion", "properties");

            if(resources.length > 1) {
                throw new IllegalStateException("only one file 'dandelion/dandelion.properties' can exists");
            } else if(resources.length == 1) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

                Properties properties = new Properties();
                properties.load(classLoader.getResourceAsStream(resources[0].getLocation()));

                assetsLocations = properties.getProperty("assetsLocations");

                String assetsLoaderClassname = properties.getProperty("assetsLoader");
                if(assetsLoaderClassname != null) {
                    try {
                        Class<AssetsLoader> cal = (Class<AssetsLoader>) classLoader.loadClass(assetsLoaderClassname);
                        assetsLoader = cal.newInstance();
                    } catch (ClassCastException e) {
                        LOG.warn("the 'assetsLoader[{}]' must implements '{}'",
                                assetsLoaderClassname, AssetsLoader.class.getCanonicalName());
                        return;
                    } catch (InstantiationException e) {
                        LOG.warn("the 'assetsLoader[{}]' should authorize instantiation", assetsLoaderClassname);
                        return;
                    } catch (IllegalAccessException e) {
                        LOG.warn("the 'assetsLoader[{}]' should authorize access from '{}'",
                                assetsLoaderClassname, AssetsConfigurator.class.getCanonicalName());
                        return;
                    } catch (ClassNotFoundException e) {
                        LOG.warn("the 'assetsLoader[{}]' must exists in the classpath", assetsLoaderClassname);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Assets configurator can't access/read to the file 'dandelion/dandelion.properties'");
        }

        setDefaults();
        processAssetsLoading();
    }

    /**
     * Set the default configuration when it's needed
     */
    void setDefaults() {
        if(assetsLoader == null) {
            assetsLoader = new AssetsJsonLoader();
        }
        if(assetsLocations == null) {
            assetsLocations = "remote";
        }
    }

    /**
     * Process to the assets loading from defined asset loader
     */
    void processAssetsLoading() {
        if(assetsLoader == null) {
            throw new IllegalStateException("a asset loader must be define");
        }

        prepareAssetsLoading(assetsLoader.loadAssets());

        storeAssetsFromScope(ROOT_SCOPE, true);
        storeAssetsFromScope(DETACHED_PARENT_SCOPE, true);

        clearAssetsProcess();
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
        for(AssetsComponent component:components) {
            parentScopesByScope.put(component.getScope(), component.getParent());
            if(scopesByParentScope.containsKey(component.getParent())) {
                List<String> _scopes = scopesByParentScope.get(component.getParent());
                if(!_scopes.contains(component.getScope())) {
                    _scopes.add(component.getScope());
                }
            } else {
                if(!component.getScope().equalsIgnoreCase(ROOT_SCOPE)) {
                    List<String> list = new ArrayList();
                    list.add(component.getScope());
                    scopesByParentScope.put(component.getParent(), list);
                }
            }

            List<Asset> _assets;
            if(componentsByScope.containsKey(component.getScope())) {
                _assets = componentsByScope.get(component.getScope());
                for(Asset asset:component.getAssets()) {
                    _assets.add(asset);
                }
            } else {
                _assets = new ArrayList<Asset>();
                for(Asset asset:component.getAssets()) {
                    _assets.add(asset);
                }
                componentsByScope.put(component.getScope(), _assets);
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
    void clearAssetsProcess() {
        componentsByScope.clear();
        scopesByParentScope.clear();
        parentScopesByScope.clear();
    }
}
