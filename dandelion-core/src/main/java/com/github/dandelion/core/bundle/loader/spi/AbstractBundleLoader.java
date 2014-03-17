/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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
package com.github.dandelion.core.bundle.loader.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.ResourceScanner;

/**
 * <p>
 * Abstract bundle loader in charge of loading JSON definitions of bundle.
 * 
 * <p>
 * The JSON definitions are scanned in the folder specified by the
 * {@link #getPath()} method.
 * 
 * <p>
 * The lookup is recursive depending on the {@link #isRecursive()} return value.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractBundleLoader implements BundleLoader {

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@inheritDoc}
	 */
	public List<BundleStorageUnit> loadBundles() {

		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

		List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();

		try {
			Set<String> resourcePaths = ResourceScanner.findResourcePaths(getPath(), getExcludedPaths(), null, ".json",
					isRecursive());
			getLogger().debug("{} resources scanned inside the folder '{}'. Parsing to bundle...",
					resourcePaths.size(), getPath());

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			for (String resourcePath : resourcePaths) {
				InputStream configFileStream = classLoader.getResourceAsStream(resourcePath);
				BundleStorageUnit bsu = mapper.readValue(configFileStream, BundleStorageUnit.class);
				getLogger().debug("Parsed {}", bsu);
				bundles.add(bsu);
			}
		}
		catch (IOException e) {
			getLogger().error(e.getMessage(), e);
		}

		return bundles;
	}

	protected abstract Logger getLogger();

	/**
	 * @return the path in which the loader will scan for JSON files.
	 */
	public abstract String getPath();

	/**
	 * @return a set of paths to exclude during the resource scanning.
	 */
	public Set<String> getExcludedPaths() {
		return Collections.emptySet();
	}
}
