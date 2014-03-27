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
package com.github.dandelion.core.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

	public BundleDag getBundleDag() {
		return bundleDag;
	}

	/**
	 * <p>
	 * Load all given bundle storage units into the {@link BundleDag}.
	 * 
	 * @param bundleStorageUnits
	 *            All bundle storage units to load into the dag.
	 * @return the {@link BundleDag} updated with the new
	 *         {@link BundleStorageUnit} and {@link AssetStorageUnit}.
	 * @throws DandelionException
	 *             as soon as a cycle is introduced in the bundle DAG.
	 */
	public BundleDag storeBundles(List<BundleStorageUnit> bundleStorageUnits) {

		for (BundleStorageUnit bsu : bundleStorageUnits) {

			BundleStorageUnit bsuToAdd = bundleDag.addVertexIfNeeded(bsu);

			// DAG updating and dependencies handling
			if (bsu.getDependencies() != null && !bsu.getDependencies().isEmpty()) {
				for (String dependency : bsu.getDependencies()) {

					BundleStorageUnit to = bundleDag.addVertexIfNeeded(dependency);
					bundleDag.addEdge(bsuToAdd, to);
				}
			}
			else {
				bsuToAdd = bundleDag.addVertexIfNeeded(bsu);
			}

			// Asset updating

			// The bundle to add contains assets
			if (bsu.getAssetStorageUnits() != null) {

				// Let's see if each asset already exists in any bundle
				for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {

					boolean exists = false;
					for (BundleStorageUnit existingBundle : bundleDag.getVerticies()) {
						for (AssetStorageUnit existingAsu : existingBundle.getAssetStorageUnits()) {

							// Si un asset de meme nom existe deja, on l'ecrase
							if (existingAsu.getName().equalsIgnoreCase(asu.getName())
									&& existingAsu.getType().equals(asu.getType())) {

								LOG.debug(
										"Replacing asset '{}' ({}) from the bundle '{}' by the asset {} ({}) from the bundle {}.",
										existingAsu.getName(), existingAsu.getVersion(), existingBundle.getName(),
										asu.getName(), asu.getVersion(), bsuToAdd.getName());

								existingAsu.setVersion(asu.getVersion());
								existingAsu.setLocations(asu.getLocations());
								existingAsu.setDom(asu.getDom());
								existingAsu.setType(asu.getType());
								existingAsu.setAttributes(asu.getAttributes());
								existingAsu.setAttributesOnlyName(asu.getAttributesOnlyName());
								exists = true;
								break;
							}
						}

						if (exists) {
							break;
						}
					}

					// If the asset doesn't already exist, we just add it to the
					// current bundle
					if (!exists) {

						LOG.debug("Adding {} '{}' ({}) to the bundle '{}'", asu.getType(), asu.getName(),
								asu.getVersion(), bsuToAdd.getName());
						bsuToAdd.getAssetStorageUnits().add(asu);
					}
				}
			}
		}

		return bundleDag;
	}

	public void checkBundleDag() {

		for (BundleStorageUnit bsu : bundleDag.getVerticies()) {
			if (bsu.getAssetStorageUnits() == null || bsu.getAssetStorageUnits().isEmpty()) {
				LOG.warn("Empty bundle: {}", bsu.getName());
			}
		}
	}

	/**
	 * Return the list of labels of bundles according to the topological sort.
	 * 
	 * @param bundleName
	 *            The name of the bundle.
	 * 
	 * @return The list of bundle names sorted by a topological order. The list
	 *         also contains the given bundle name, always in last.
	 */
	public Set<BundleStorageUnit> bundlesFor(String bundleName) {
		BundleStorageUnit bsu = bundleDag.getVertex(bundleName);

		if (bsu != null) {
			Set<BundleStorageUnit> retval = null;

			if (bsu.isLeaf()) {
				retval = new HashSet<BundleStorageUnit>(1);
				retval.add(bsu);
			}
			else {
				retval = new LinkedHashSet<BundleStorageUnit>(TopologicalSorter.sort(bsu));
			}

			return retval;
		}

		return Collections.emptySet();
	}

	public Set<BundleStorageUnit> bundlesFor(String... bundleNames) {

		Set<BundleStorageUnit> retval = new LinkedHashSet<BundleStorageUnit>();
		for (String bundleName : bundleNames) {
			retval.addAll(bundlesFor(bundleName.trim()));
		}

		return retval;
	}
}
