/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.bundle.loader.strategy.JsonBundleLoadingStrategy;
import com.github.dandelion.core.bundle.loader.strategy.LoadingStrategy;
import com.github.dandelion.core.bundle.loader.strategy.XmlBundleLoadingStrategy;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.StringBuilderUtils;

/**
 * <p>
 * Abstract bundle loader in charge of loading JSON definitions of bundle.
 * </p>
 * <p>
 * The JSON definitions are scanned in the folder specified by the
 * {@link #getPath()} method.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractBundleLoader implements BundleLoader {

	protected Context context;

	@Override
	public void initLoader(Context context) {
		this.context = context;
	}

	@Override
	public List<BundleStorageUnit> loadBundles() {

		List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();
		LoadingStrategy jsonLoadingStrategy = new JsonBundleLoadingStrategy(context);

		getLogger().debug("Scanning \"{}\" for JSON-formatted bundles...", getBundleLocation());
		Set<String> resourcePaths = jsonLoadingStrategy.getResourcePaths(getBundleLocation(), getExcludedPaths());
		getLogger().debug("{} bundles selected", resourcePaths.size());
		
		if (!resourcePaths.isEmpty()) {
			List<BundleStorageUnit> bsus = jsonLoadingStrategy.mapToBundles(resourcePaths);
			bundles.addAll(bsus);
		}
		else {
			getLogger().debug("No JSON-formatted bundle found in \"{}\". Trying with XML-formatted ones...",
					getBundleLocation());
			LoadingStrategy xmlLoadingStrategy = new XmlBundleLoadingStrategy(context);
			resourcePaths = xmlLoadingStrategy.getResourcePaths(getBundleLocation(), getExcludedPaths());
			if (!resourcePaths.isEmpty()) {
				List<BundleStorageUnit> bsus = xmlLoadingStrategy.mapToBundles(resourcePaths);
				bundles.addAll(bsus);
			}
			else {
				getLogger().debug("No XML-formatted bundle found in \"{}\"");
			}
		}

		if (resourcePaths.isEmpty()) {
			getLogger().debug("No bundle found in {}", getBundleLocation());
		}

		getLogger().debug("Post processing bundles...");
		postProcessBundles(bundles);

		return bundles;
	}

	/**
	 * @return the {@link Logger} bound to the actual implementation.
	 */
	protected abstract Logger getLogger();

	/**
	 * @return the path in which the loader will scan for JSON files.
	 */
	public abstract String getPath();

	/**
	 * @return a set of paths to exclude during the resource scanning. Empty by
	 *         default.
	 */
	public Set<String> getExcludedPaths() {
		return Collections.emptySet();
	}

	private String getBundleLocation() {

		StringBuilder bundleBaseLocation = new StringBuilder(context.getConfiguration().getBundleLocation());
		if (StringBuilderUtils.isNotBlank(bundleBaseLocation)) {
			bundleBaseLocation.append("/");
		}

		bundleBaseLocation.append(getPath());

		return bundleBaseLocation.toString();
	}

	@Override
	public void postProcessBundles(List<BundleStorageUnit> bundles) {
		for (BundleStorageUnit bsu : bundles) {
			for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
				asu.setBundle(bsu.getName());
			}
		}
		doCustomBundlePostProcessing(bundles);
	}
	
	protected abstract void doCustomBundlePostProcessing(List<BundleStorageUnit> bundles);
}
