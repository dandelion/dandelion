package com.github.dandelion.core.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipException;

import com.github.dandelion.core.api.asset.AssetsLoader;
import com.github.dandelion.core.api.asset.AssetsComponent;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.dandelion.core.utils.ClassPathResource;
import com.github.dandelion.core.utils.scanner.ClassPathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assets Loader for JSON definition
 */
public class AssetsJsonLoader implements AssetsLoader {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetsJsonLoader.class);
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Load assets from 'dandelion/*.json' files by Classpath Scanning.
	 */
	public List<AssetsComponent> loadAssets() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        List<AssetsComponent> assetsComponentList = new ArrayList<AssetsComponent>();
		try {
			ClassPathResource[] resources = new ClassPathScanner().scanForResources("dandelion", "", "json");
			System.out.println("resources = " + resources);
			for(ClassPathResource resource : resources){
				LOG.debug("Location = {}", resource.getLocation());
			}

			for (ClassPathResource resource : resources) {
				InputStream configFileStream = classLoader.getResourceAsStream(resource.getLocation());

				AssetsComponent assetsComponent = mapper.readValue(configFileStream, AssetsComponent.class);
				LOG.debug("found {}", assetsComponent);
                assetsComponentList.add(assetsComponent);
			}
		} catch (IOException e) {
            LOG.error(e.getMessage(), e);
		}
        return assetsComponentList;
	}
}
