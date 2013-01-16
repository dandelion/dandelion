package com.github.dandelion.core.asset.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipException;

import com.github.dandelion.core.asset.*;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;

import static com.github.dandelion.core.asset.AssetStorage.ROOT_SCOPE;

/**
 * Assets Loader for JSON definition
 */
public class AssetJsonLoader {
    private Map<String, JsonComponent> components = new HashMap<String, JsonComponent>();
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

				JsonComponent jsonComponent = mapper.readValue(configFileStream, JsonComponent.class);
				System.out.println("JsonComponent = " + jsonComponent);

                components.put(jsonComponent.getScope(), jsonComponent);
                fillScopes(jsonComponent);
			}

            storeAssets(ROOT_SCOPE);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void storeAssets(String scope) {
        if(components.containsKey(scope)) {
            JsonComponent jsonComponent = components.get(scope);
            for(Asset asset: jsonComponent.getAssets()) {
                storeAsset(asset, jsonComponent);
            }
            components.remove(jsonComponent);
        }
        if(scopes.containsKey(scope)) {
            for(String _scope:scopes.get(scope)) {
                storeAssets(_scope);
            }
        }
    }

    private void storeAsset(Asset asset, JsonComponent jsonComponent) {
        System.out.println("Store " + asset.toString() + " in scope '"
                + jsonComponent.getScope() + "'/'"+ jsonComponent.getParent()+"'");
        try {
            AssetStorage.store(asset, jsonComponent.getScope(), jsonComponent.getParent());
        } catch (AssetAlreadyExistsInScopeException e) {
            System.out.println("Asset already exists, original asset -> "
                    + e.getOriginalAsset());
        } catch (ParentScopeIncompatibilityException e) {
            System.out.println("Incompatibility with Scope/Parent Scope -> '"
                    + e.getScope() + "/" + e.getParentScope() + "'");
        } catch (DetachScopeNotAllowedException e) {
            System.out.println("Not allowed usage of the detached scope " + e.getDetachScope() + " as scope of asset");
        } catch (UndefinedParentScopeException e) {
            System.out.println("Use of a undefined scope as a parent -> '" + jsonComponent.getParent() +"'");
            System.out.println("To avoid any configuration problem, a scope '"
                    + jsonComponent.getParent() + "' with no assets is created");
            AssetStorage.store(null, jsonComponent.getParent());
            storeAsset(asset, jsonComponent);
        }
    }

    private void fillScopes(JsonComponent jsonComponent) {
        if(!scopes.containsKey(jsonComponent.getParent())) {
            if(!jsonComponent.getScope().equalsIgnoreCase(ROOT_SCOPE)) {
                List<String> list = new ArrayList();
                list.add(jsonComponent.getScope());
                scopes.put(jsonComponent.getParent(), list);
            }
        } else {
            List<String> _scopes = scopes.get(jsonComponent.getParent());
            if(!_scopes.contains(jsonComponent.getScope())) {
                _scopes.add(jsonComponent.getScope());
            }
        }
    }
}
