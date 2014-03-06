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
package com.github.dandelion.core.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;

/**
 * <p>
 * Storage for all bundles, based on a directed acyclic graph (dag).
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class BundleStorage {

	private static final Logger LOG = LoggerFactory.getLogger(BundleStorage.class);
	private BundleDag bundleDag;

	public BundleStorage() {
		this.bundleDag = new BundleDag();
	}

	/**
	 * <p>
	 * Load all given bundles into the dag.
	 * 
	 * @throws DandelionException
	 *             as soon as a cycle is introduced in the bundle DAG.
	 */
	public BundleDag loadBundles(List<Bundle> bundles) {

		for (Bundle bundle : bundles) {
			
			// DAG updating and dependencies handling
			if (bundle.getDependencies() != null && !bundle.getDependencies().isEmpty()) {
				for (String dependency : bundle.getDependencies()) {

					Bundle from = null;
					if (bundleDag.getBundleNames().contains(bundle.getName())) {
						LOG.debug("Bundle {} has been overriden and now contains {}", bundle.getName(), bundle.getAssets());
						from = bundleDag.getVertex(bundle.getName());
					}
					else {
						from = bundleDag.addVertex(bundle);
					}

					Bundle to = null;
					if (bundleDag.getBundleNames().contains(dependency)) {
						to = bundleDag.getVertex(dependency);
					}
					else {
						to = bundleDag.addVertex(dependency);
					}

					bundleDag.addEdge(from, to);
				}
			}
			else {
				bundleDag.addVertex(bundle);
			}
			
			// Assets handling
			if (bundleDag.getBundleNames().contains(bundle.getName())) {
				LOG.debug("The bundle {} already exists. Overriding assets with the new ones.", bundle.getName());
				bundleDag.getVertex(bundle.getName()).getAssets().addAll(bundle.getAssets());
			}
			else {
			}
			bundleDag.getVertex(bundle.getName()).setAssets(bundle.getAssets());
		}

		return bundleDag;
	}


	public void checkBundleDag() {

		List<Bundle> emptyBundles = new ArrayList<Bundle>();
		for (Entry<String, Bundle> entries : bundleDag.getVertexMap().entrySet()) {
			if (entries.getValue().getAssets() == null || entries.getValue().getAssets().isEmpty()) {
				emptyBundles.add(entries.getValue());
			}
		}
		
		if (!emptyBundles.isEmpty()) {
//			throw new DandelionException(BundleStorageError.UNDEFINED_BUNDLE).set("emptyBundles", emptyBundles);
			LOG.warn("Some bundles are empty: {}.", emptyBundles.toString());
		}
	}

	public BundleDag getBundleDag() {
		return bundleDag;
	}
}
