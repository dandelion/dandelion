package com.github.dandelion.core.asset;

import com.github.dandelion.core.api.DandelionException;
import com.github.dandelion.core.api.asset.Asset;
import com.github.dandelion.core.api.asset.AssetsStorageError;
import com.github.dandelion.core.api.asset.AssetsLoader;
import com.github.dandelion.core.api.asset.AssetsComponent;
import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;

import java.io.IOException;
import java.util.*;

import static com.github.dandelion.core.asset.AssetsStorage.DETACH_PARENT_SCOPE;
import static com.github.dandelion.core.asset.AssetsStorage.ROOT_SCOPE;

/**
 * Load Assets configuration
 * <ul>
 *     <li>assetsLoader :
 *          <ul>
 *               <li>the {@link com.github.dandelion.core.api.asset.AssetsLoader}
 * found in 'dandelion/dandelion.properties' for key 'assetsLoader'</li>
 *               <li>or {@link AssetsJsonLoader} by default</li>
 *          </ul>
 *     </li>
 *     <li>assetsAccess : type of access to assets content(remote [by default], local)</li>
 * </ul>
 * Default Asset Loader is
 *
 */
public class AssetsConfigurator {
    static final AssetsConfigurator assetsConfigurator = new AssetsConfigurator();

    static {
        assetsConfigurator.initialize();
    }

    AssetsLoader assetsLoader;
    String assetsAccess;

    private Map<String, List<Asset>> componentsByScope = new HashMap<String, List<Asset>>();
    private Map<String, List<String>> scopesByParentScope = new HashMap<String, List<String>>();
    private Map<String, String> parentScopesByScope = new HashMap<String, String>();

    private AssetsConfigurator() {
    }

    /**
     * @return the Assets configurator instance
     */
    public static AssetsConfigurator getInstance() {
        return assetsConfigurator;
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

                assetsAccess = properties.getProperty("assetsAccess");

                String assetsLoaderClassname = properties.getProperty("assetsLoader");
                if(assetsLoaderClassname != null) {
                    try {
                        Class<AssetsLoader> cal = (Class<AssetsLoader>) classLoader.loadClass(assetsLoaderClassname);
                        assetsLoader = cal.newInstance();
                    } catch (ClassCastException e) {
                        System.out.println("the 'assetsLoader["+ assetsLoaderClassname
                                +"]' must implements 'com.github.dandelion.core.api.asset.AssetsLoader'");
                        return;
                    } catch (InstantiationException e) {
                        System.out.println("the 'assetsLoader[" + assetsLoaderClassname
                                + "]' should authorize instantiation");
                        return;
                    } catch (IllegalAccessException e) {
                        System.out.println("the 'assetsLoader[" + assetsLoaderClassname
                                + "]' should authorize access from com.github.dandelion.core.asset.AssetsConfigurator");
                        return;
                    } catch (ClassNotFoundException e) {
                        System.out.println("the 'assetsLoader[" + assetsLoaderClassname
                                + "]' must exists in the classpath");
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Assets configurator can't access/read to the file 'dandelion/dandelion.properties'");
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
        if(assetsAccess == null || assetsAccess.isEmpty()) {
            assetsAccess = "remote";
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
        storeAssetsFromScope(DETACH_PARENT_SCOPE, true);

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
        System.out.println("Store " + asset.toString()
                + " in scope '" + scope + "/"+ parentScope+"'");
        try {
            AssetsStorage.store(asset, scope, parentScope);
        } catch (DandelionException e) {
            if(e.getErrorCode() == AssetsStorageError.ASSET_ALREADY_EXISTS_IN_SCOPE) {
                System.out.println("Asset already exists, original asset -> "
                            + e.get("originalAsset"));
            } else if(e.getErrorCode() == AssetsStorageError.PARENT_SCOPE_INCOMPATIBILITY) {
                System.out.println("Incompatibility with Scope/Parent Scope -> '"
                        + e.get("scope") + "/" + e.get("parentScope") + "'");
            } else if(e.getErrorCode() == AssetsStorageError.DETACH_SCOPE_NOT_ALLOWED) {
                System.out.println("Not allowed usage of the detached scope "
                        + e.get("detachScope") + " as scope of asset");
            } else if(e.getErrorCode() == AssetsStorageError.UNDEFINED_PARENT_SCOPE) {
                System.out.println("Use of a undefined scope as a parent -> '"
                        + parentScope + "'");
                System.out.println("To avoid any configuration problem, a scope '"
                        + parentScope + "' with no assets is created");
                AssetsStorage.store(null, parentScope);
                storeAsset(asset, scope, parentScope);
            } else {
                throw e;
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
