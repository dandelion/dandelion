package com.github.dandelion.core.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipException;

import com.github.dandelion.api.asset.*;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;

import static com.github.dandelion.api.asset.AssetStorage.ROOT_SCOPE;

/**
 * Assets Loader for JSON definition
 */
public class JsonAssetLoader {
    private Map<String, Component> components = new HashMap<String, Component>();
    private Map<String, List<String>> scopes = new HashMap<String, List<String>>();

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Load assets from 'dandelion/*.json' files by Classpath Scanning.
	 */
	public void load() {

		// Get current classloader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

		try {
			System.out.println("=================================");
			ClassPathResource[] resources = new ClassPathScanner().scanForResources("dandelion", "", "json");
			for(ClassPathResource resource : resources){
				System.out.println("Location = " + resource.getLocation());
				System.out.println("Ressource = " + resource.getFilename());
			}

			for (ClassPathResource resource : resources) {

				// Get default file as stream
				InputStream configFileStream = classLoader.getResourceAsStream(resource.getLocation());

				Component component = mapper.readValue(configFileStream, Component.class);
				System.out.println("Component = " + component);

                components.put(component.getScope(), component);
                fillScopes(scopes, component);
			}

            storeAssets(ROOT_SCOPE);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void storeAssets(String scope) {
        Component component = components.get(scope);
        for(Asset asset:component.getAssets()) {
            storeAsset(asset, component);
        }
        components.remove(component);
        if(scopes.containsKey(scope)) {
            for(String _scope:scopes.get(scope)) {
                storeAssets(_scope);
            }
        }
    }

    private void storeAsset(Asset asset, Component component) {
        System.out.println("Store " + asset.toString() + " in scope '"
                + component.getScope() + "'/'"+component.getParent()+"'");
        try {
            AssetStorage.store(asset, component.getScope(), component.getParent());
        } catch (AssetAlreadyExistsInScopeException e) {
            System.out.println("Asset already exists, original asset -> "
                    + e.getOriginalAsset());
        } catch (ParentScopeIncompatibilityException e) {
            System.out.println("Incompatibility with Scope/Parent Scope -> '"
                    + e.getScope() + "/" + e.getParentScope() + "'");
        } catch (UndefinedParentScopeException e) {
            System.out.println("Use of a undefined scope as a parent -> '" + component.getParent() +"'");
            System.out.println("To avoid any configuration problem, a scope '"
                    + component.getParent() + "' with no assets is created");
            AssetStorage.store(null, component.getParent());
            storeAsset(asset, component);
        }
    }

    private void fillScopes(Map<String, List<String>> scopes, Component component) {
        if(!scopes.containsKey(component.getParent())) {
            if(!component.getScope().equalsIgnoreCase(ROOT_SCOPE)) {
                List<String> list = new ArrayList();
                list.add(component.getScope());
                scopes.put(component.getParent(), list);
            }
        } else {
            List<String> _scopes = scopes.get(component.getParent());
            if(!_scopes.contains(component.getScope())) {
                _scopes.add(component.getScope());
            }
        }
    }
}
