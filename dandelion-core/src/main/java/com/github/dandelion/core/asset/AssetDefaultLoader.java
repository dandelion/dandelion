package com.github.dandelion.core.asset;

import com.github.dandelion.core.api.asset.Asset;
import com.github.dandelion.core.api.asset.AssetLoader;
import com.github.dandelion.core.api.asset.AssetsComponent;
import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static com.github.dandelion.core.asset.AssetStorage.DETACH_PARENT_SCOPE;
import static com.github.dandelion.core.asset.AssetStorage.ROOT_SCOPE;

/**
 * Load all assets available from Asset Default Loader<br/>
 *
 * Default Asset Loader is
 * <ul>
 *     <li>the {@link AssetLoader}
 *     found in 'dandelion/dandelion.properties' for key 'assetDefaultLoader'</li>
 *     <li>or {@link AssetJsonLoader} by default</li>
 * </ul>
 */
public class AssetDefaultLoader {
    static AssetDefaultLoader defaultLoader = new AssetDefaultLoader();
    AssetLoader assetLoader = null;
    private Map<String, List<Asset>> componentsByScope = new HashMap<String, List<Asset>>();
    private Map<String, List<String>> scopesByParentScope = new HashMap<String, List<String>>();
    private Map<String, String> parentScopesByScope = new HashMap<String, String>();

    private AssetDefaultLoader() {
    }

    /**
     * Initialization of Asset Default Loader on application load
     *
     * @throws IOException if a I/O error appends when 'dandelion/dandelion.properties' is loaded
     */
    @PostConstruct
    void initialize() throws IOException {
        ClassPathResource[] resources = new ClassPathScanner().scanForResources("dandelion", "dandelion.properties", "dandelion.properties");

        if(resources.length == 0) {
            assetLoader = new AssetJsonLoader();
        } else if(resources.length > 0) {
            throw new IllegalStateException("only one file 'dandelion/dandelion.properties' can exists");
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Properties properties = new Properties();
            properties.load(classLoader.getResourceAsStream(resources[0].getLocation()));

            String adl = properties.getProperty("assetDefaultLoader");
            try {
                Class<AssetLoader> cal = (Class<AssetLoader>) classLoader.loadClass(adl);
                assetLoader = cal.newInstance();
            } catch (ClassCastException e) {
                System.out.println("the 'assetDefaultLoader["+ adl
                        +"]' must implements 'com.github.dandelion.core.api.asset.AssetLoader'");
                return;
            } catch (InstantiationException e) {
                System.out.println("the 'assetDefaultLoader[" + adl
                        + "]' should authorize instantiation");
                return;
            } catch (IllegalAccessException e) {
                System.out.println("the 'assetDefaultLoader[" + adl
                        + "]' should authorize access from com.github.dandelion.core.asset.AssetDefaultLoader");
                return;
            } catch (ClassNotFoundException e) {
                System.out.println("the 'assetDefaultLoader[" + adl
                        + "]' must exists in the classpath");
                return;
            }
        }

        process();
    }

    /**
     * Process to the asset load from defined asset loader
     */
    void process() {
        if(assetLoader == null) {
            throw new IllegalStateException("a asset loader must be define");
        }

        prepareAssetsStorage(assetLoader.loadAssets());

        storeAssetsFromScope(ROOT_SCOPE, true);
        storeAssetsFromScope(DETACH_PARENT_SCOPE, true);

        clearAll();
    }

    /**
     * Prepare Assets storage
     * @param components components to analyze
     */
    private void prepareAssetsStorage(List<AssetsComponent> components) {
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
            AssetStorage.store(asset, scope, parentScope);
        } catch (AssetAlreadyExistsInScopeException e) {
            System.out.println("Asset already exists, original asset -> "
                    + e.getOriginalAsset());
        } catch (ParentScopeIncompatibilityException e) {
            System.out.println("Incompatibility with Scope/Parent Scope -> '"
                    + e.getScope() + "/" + e.getParentScope() + "'");
        } catch (DetachScopeNotAllowedException e) {
            System.out.println("Not allowed usage of the detached scope "
                    + e.getDetachScope() + " as scope of asset");
        } catch (UndefinedParentScopeException e) {
            System.out.println("Use of a undefined scope as a parent -> '"
                    + parentScope + "'");
            System.out.println("To avoid any configuration problem, a scope '"
                    + parentScope + "' with no assets is created");
            AssetStorage.store(null, parentScope);
            storeAsset(asset, scope, parentScope);
        }
    }

    /**
     * Clear all working attributes
     */
    void clearAll() {
        componentsByScope.clear();
        scopesByParentScope.clear();
        parentScopesByScope.clear();
    }
}
