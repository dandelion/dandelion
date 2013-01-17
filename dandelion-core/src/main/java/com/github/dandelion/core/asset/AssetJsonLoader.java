package com.github.dandelion.core.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipException;

import com.github.dandelion.core.api.asset.AssetLoader;
import com.github.dandelion.core.api.asset.AssetsComponent;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;

/**
 * Assets Loader for JSON definition
 */
public class AssetJsonLoader implements AssetLoader {
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Load assets from 'dandelion/*.json' files by Classpath Scanning.
	 */
	public List<AssetsComponent> loadAssets() {

		// Get current classloader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        List<AssetsComponent> assetsComponentList = new ArrayList<AssetsComponent>();
		try {
			System.out.println("=================================");
			ClassPathResource[] resources = new ClassPathScanner().scanForResources("dandelion", "", "json");
			for(ClassPathResource resource : resources){
				System.out.println("Location = " + resource.getLocation());
			}

			for (ClassPathResource resource : resources) {
				// Get default file as stream
				InputStream configFileStream = classLoader.getResourceAsStream(resource.getLocation());

				AssetsComponent assetsComponent = mapper.readValue(configFileStream, AssetsComponent.class);
				System.out.println("found " + assetsComponent);
                assetsComponentList.add(assetsComponent);
			}
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return assetsComponentList;
	}
}
