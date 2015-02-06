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
package com.github.dandelion.core.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.storage.support.BundleDag;
import com.github.dandelion.core.storage.support.TopologicalSorter;

/**
 * <p>
 * Storage for all bundles, backed by a {@link BundleDag} instance.
 * </p>
 * <p>
 * This facade provides several utilities intented to
 * </p>
 * <ul>
 * <li>check the consistency of {@link BundleStorageUnit}</li>
 * <li>build and fill the {@link BundleDag}</li>
 * <li>query the {@link BundleDag}</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class BundleStorage {

	private static final Logger LOG = LoggerFactory.getLogger(BundleStorage.class);

	private final BundleDag bundleDag;

	public BundleStorage() {
		this.bundleDag = new BundleDag();
	}

	/**
	 * <p>
	 * Load all given {@link BundleStorageUnit}s into the {@link BundleDag}.
	 * </p>
	 * 
	 * @param bundleStorageUnits
	 *            All bundle storage units to load into the dag.
	 * @return the {@link BundleDag} updated with the new
	 *         {@link BundleStorageUnit} and {@link AssetStorageUnit}.
	 * @throws DandelionException
	 *             as soon as a cycle is detected in the bundle DAG.
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

			// Let's see if each asset already exists in any bundle
			for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {

				boolean assetAlreadyExists = false;
				for (BundleStorageUnit existingBundle : bundleDag.getVerticies()) {
					for (AssetStorageUnit existingAsu : existingBundle.getAssetStorageUnits()) {

						// If the asset has both the same name
						// (case-insensitive) and type, the old one is simply
						// overriden
						if (existingAsu.getName().equalsIgnoreCase(asu.getName())
								&& existingAsu.getType().equals(asu.getType())) {

							LOG.trace(
									"Replacing asset '{}' ({}) from the bundle '{}' by the asset {} ({}) from the bundle {}.",
									existingAsu.getName(), existingAsu.getVersion(), existingBundle.getName(),
									asu.getName(), asu.getVersion(), bsuToAdd.getName());

							existingAsu.setVersion(asu.getVersion());
							existingAsu.setLocations(asu.getLocations());
							existingAsu.setDom(asu.getDom());
							existingAsu.setBundle(existingBundle.getName());
							existingAsu.setType(asu.getType());
							existingAsu.setAttributes(asu.getAttributes());
							existingAsu.setAttributesOnlyName(asu.getAttributesOnlyName());
							assetAlreadyExists = true;
							break;
						}
					}

					if (assetAlreadyExists) {
						break;
					}
				}

				// If the asset doesn't already exist, we just add it to the
				// current bundle
				if (!assetAlreadyExists) {

					LOG.trace("Adding {} '{}' ({}) to the bundle '{}'", asu.getType(), asu.getName(), asu.getVersion(),
							bsuToAdd.getName());
					bsuToAdd.getAssetStorageUnits().add(asu);
				}
			}
		}

		return bundleDag;
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

	/**
	 * @return the internal {@link BundleDag} used to store the bundle graph.
	 */
	public BundleDag getBundleDag() {
		return bundleDag;
	}

	public void consolidateBundles(List<BundleStorageUnit> allBundles) {

		for (BundleStorageUnit bsu : bundleDag.getVerticies()) {

			for (BundleStorageUnit rawBsu : allBundles) {
				if (rawBsu.getName().equalsIgnoreCase(bsu.getName())) {

					bsu.setDependencies(rawBsu.getDependencies());
					bsu.setAssetStorageUnits(rawBsu.getAssetStorageUnits());
					bsu.setRelativePath(rawBsu.getRelativePath());
					bsu.setOrigin(rawBsu.getOrigin());
					break;
				}
			}
		}
	}
}
