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
package com.github.dandelion.core.bundle.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.bundle.BundleDag;
import com.github.dandelion.core.bundle.loader.impl.DandelionBundleLoader;
import com.github.dandelion.core.bundle.loader.impl.VendorBundleLoader;
import com.github.dandelion.core.bundle.loader.spi.BundleLoader;
import com.github.dandelion.core.config.Configuration;

/**
 * <p>
 * System in charge of discovering all providers of {@link BundleLoader}
 * available in the classpath and intializing the loaders.
 * 
 * <p>
 * The order in which all {@link BundleLoader} are stored is important here: all
 * bundles loaded from the {@link DandelionBundleLoader} must be loaded into the
 * {@link BundleDag} in last.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class BundleLoaderSystem {

	private static final Logger LOG = LoggerFactory.getLogger(BundleLoaderSystem.class);

	private static ServiceLoader<BundleLoader> blServiceLoader = ServiceLoader.load(BundleLoader.class);
	private static List<BundleLoader> loaders;

	private static void initializeIfNeeded() {
		if (loaders == null || DevMode.isEnabled()) {
			initialize();
		}
	}

	private static synchronized void initialize() {

		VendorBundleLoader vendorLoader = new VendorBundleLoader();
		DandelionBundleLoader dandelionLoader = new DandelionBundleLoader();

		List<BundleLoader> bundleLoaders = new ArrayList<BundleLoader>();

		// All vendor bundles must be loaded in first
		bundleLoaders.add(vendorLoader);

		// Then all bundles of the components present in the classpath
		for (BundleLoader bl : blServiceLoader) {
			if (!"false".equalsIgnoreCase(Configuration.getProperty("bundle.loader." + bl.getName() + ".active"))) {
				bundleLoaders.add(bl);
				LOG.info("Active bundle loader found: {}", bl.getClass().getSimpleName());
			}
			else {
				LOG.info("Inactive bundle loader found: {}", bl.getClass().getSimpleName());
			}
		}

		// Finally all bundles created by users
		bundleLoaders.add(dandelionLoader);

		loaders = bundleLoaders;
	}

	public static List<BundleLoader> getLoaders() {
		initializeIfNeeded();
		return loaders;
	}

	public static List<BundleLoader> getLoaders(boolean withInactiveLoaders) {
		initializeIfNeeded();
		return loaders;
	}

	/**
	 * Prevents instantiation.
	 */
	private BundleLoaderSystem() {
	}
}