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
package com.github.dandelion.core.asset.loader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.dandelion.core.asset.AssetComponent;
import com.github.dandelion.core.asset.loader.AssetLoaderSystem;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import com.github.dandelion.core.asset.loader.spi.AssetLoader;
import com.github.dandelion.core.utils.ResourceScanner;

/**
 * <p>
 * Abstract asset loader in charge of loading JSON definitions. The JSON
 * definitions are scanned in the folder specified by the {@link #getPath()}
 * method. The lookup is recursive depending on the {@link #isRecursive()}
 * return value.
 */
public abstract class AbstractAssetJsonLoader implements AssetLoader {

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@inheritDoc}
	 */
	public List<AssetComponent> loadAssets() {

		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
		
		List<AssetComponent> assetComponentList = new ArrayList<AssetComponent>();

		// Init the excluded folder list
		List<String> excludedFolders = getExcludedPaths();

		try {
			Set<String> resources = ResourceScanner.getResources(getPath(), excludedFolders, null, ".json", isRecursive());
			getLogger().debug("{} resources scanned inside the folder '{}'. Parsing to AssetComponent...",
					resources.size(), getPath());
			
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			for (String resource : resources) {
				getLogger().debug("resources {}", resource);
				InputStream configFileStream = classLoader.getResourceAsStream(resource);

				AssetComponent assetComponent = mapper.readValue(configFileStream, AssetComponent.class);

				getLogger().debug("found {}", assetComponent);
				assetComponentList.add(assetComponent);
			}
		} catch (IOException e) {
			getLogger().error(e.getMessage(), e);
		}
		
		return assetComponentList;
	}

	protected abstract Logger getLogger();

	public abstract String getPath();

	@Override
	public String getType() {
		return "json";
	}

	/**
	 * <p>
	 * Since each implementation of {@link AbstractAssetJsonLoader} is in
	 * charge of loading their own definitions, each one of them must be aware
	 * of the existence of the others.
	 * 
	 * @return a list of paths to exclude during the classpath scanning.
	 */
	private List<String> getExcludedPaths() {

		List<String> excludedPaths = new ArrayList<String>();

		for (AssetLoader loader : AssetLoaderSystem.getLoaders()) {
			if (loader instanceof AbstractAssetJsonLoader) {
				String path = ((AbstractAssetJsonLoader) loader).getPath();
				if (path.startsWith(getPath()) && !path.equalsIgnoreCase(getPath())) {
					excludedPaths.add(path);
				}
			}
		}

		return excludedPaths;
	}
}