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
package com.github.dandelion.core.asset.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.github.dandelion.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.loader.spi.AssetLoader;

/**
 * System in charge of discovering all implementations of {@link AssetLoader}
 * available in the classpath.
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public final class AssetLoaderSystem {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(AssetLoaderSystem.class);

	private static ServiceLoader<AssetLoader> serviceLoader = ServiceLoader.load(AssetLoader.class);
	private static List<AssetLoader> loaders;
	private static List<AssetLoader> allLoaders;

	private AssetLoaderSystem() {
	}

	private static void initialize() {
		if (loaders == null) {
			initializeIfNeeded();
		}
	}

	synchronized private static void initializeIfNeeded() {
		if (loaders != null) {
			return;
		}

		List<AssetLoader> als = new ArrayList<AssetLoader>();
		List<AssetLoader> aals = new ArrayList<AssetLoader>();
		for (AssetLoader al : serviceLoader) {
			if (!"false".equalsIgnoreCase(Configuration.getProperty("asset.loader." + al.getName() + ".active"))) {
				als.add(al);
				LOG.info("Active AssetLoader found: {}", al.getClass().getSimpleName());
			}
			else {
				LOG.info("Inactive AssetLoader found: {}", al.getClass().getSimpleName());
			}
			aals.add(al);
		}

		loaders = als;
		allLoaders = aals;
	}

	public static List<AssetLoader> getLoaders() {
		initialize();
		return loaders;
	}

	public static List<AssetLoader> getLoaders(boolean withInactiveLoaders) {
		initialize();
		return withInactiveLoaders ? allLoaders : getLoaders();
	}
}
