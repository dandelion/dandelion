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
package com.github.dandelion.core.storage.support;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.BundleStorageLogBuilder;
import com.github.dandelion.core.utils.PathUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * TODO
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public final class BundleUtils {

	private static final Logger LOG = LoggerFactory.getLogger(BundleUtils.class);

	/**
	 * <p>
	 * Performs several initializations on {@link BundleStorageUnit} in order
	 * for them to be consistent before building the {@link BundleDag}.
	 * </p>
	 * 
	 * @param loadedBundles
	 * @param context
	 */
	public static void finalizeBundleConfiguration(List<BundleStorageUnit> loadedBundles, Context context) {

		LOG.debug("Finishing bundles configuration...");

		for (BundleStorageUnit bsu : loadedBundles) {
			finalizeBundleConfiguration(bsu, context);
		}
	}

	/**
	 * <p>
	 * Performs several initializations on {@link BundleStorageUnit} in order
	 * for them to be consistent before building the {@link BundleDag}.
	 * </p>
	 * 
	 * @param loadedBundles
	 * @param context
	 */
	public static void finalizeBundleConfiguration(BundleStorageUnit bsu, Context context) {

		LOG.trace("Finalizing configuration of bundle \"{}\"", bsu);

		// The name of the bundle is extracted from its path if not
		// specified
		if (StringUtils.isBlank(bsu.getName())) {
			String extractedName = PathUtils.extractLowerCasedName(bsu.getRelativePath());
			bsu.setName(extractedName);
			LOG.trace("Name of the bundle extracted from its path: \"{}\"", extractedName);
		}

		if (bsu.getAssetStorageUnits() != null) {

			for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
				String firstFoundLocation = asu.getLocations().values().iterator().next();
				if (StringUtils.isBlank(asu.getName())) {
					String extractedName = PathUtils.extractLowerCasedName(firstFoundLocation);
					asu.setName(extractedName);
					LOG.trace("Name of the asset extracted from its first location: \"{}\"", extractedName);
				}
				if (asu.getType() == null) {
					AssetType extractedType = AssetType.typeOf(firstFoundLocation);
					asu.setType(extractedType);
					LOG.trace("Type of the asset extracted from its first location: \"{}\"", extractedType);
				}
			}

			// Perform variable substitutions only if the user uses a
			// configuration file
			if (context.getConfiguration().getProperties() != null) {
				for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
					Map<String, String> locations = asu.getLocations();
					for (Entry<String, String> locationEntry : asu.getLocations().entrySet()) {
						locations.put(locationEntry.getKey(), StringUtils.substitute(locationEntry.getValue(), context
								.getConfiguration().getProperties()));
					}
				}
			}
		}
	}

	public static void checkRequiredConfiguration(BundleStorageLogBuilder logBuilder, BundleStorageUnit... bundleStorageUnits) {

		// Check that the DAG contains no empty bundles
		for (BundleStorageUnit bsu : bundleStorageUnits) {
			if (bsu.getAssetStorageUnits() == null || bsu.getAssetStorageUnits().isEmpty()) {
				logBuilder.error("- Empty bundle", "   [" + bsu.getName() + "] The bundle \"" + bsu.getName()
						+ "\" is empty. You would better remove it.");
			}
		}

		// Check that every asset of every bundle contains at least one
		// locationKey/location pair because both name and type will be deducted
		// from it

		for (BundleStorageUnit bsu : bundleStorageUnits) {
			for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {

				// Check locations
				if (asu.getLocations().isEmpty()) {
					logBuilder.error("- Missing asset location(s)", "[" + bsu.getName()
							+ "] The bundle contain asset with no location whereas it is required.");
				}
				else {
					for (String locationKey : asu.getLocations().keySet()) {
						if (StringUtils.isBlank(locationKey)) {
							logBuilder.error(
									"- Missing location key",
									"["
											+ bsu.getName()
											+ "] One of the assets contained in this bundle has a location with no location key. Please correct it before continuing.");

						}
					}

					for (String location : asu.getLocations().values()) {
						if (StringUtils.isBlank(location)) {
							logBuilder.error("- Missing asset location", "[" + bsu.getName()
									+ "] One of the assets contained in the bundle \"" + bsu.getName()
									+ "\" has an empty location. Please correct it before continuing.");
						}
						else {
							// The asset type can be specified explicitely
							if(asu.getType() == null){
								boolean extensionNotFound = true;
								for (AssetType assetType : AssetType.values()) {
									if (location.toLowerCase().endsWith("." + assetType.toString())) {
										extensionNotFound = false;
										break;
									}
								}
								if (extensionNotFound) {
									logBuilder.error("- Missing extension", "[" + bsu.getName()
											+ "] The extension is required in all locations.");
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * <p>
	 * Suppress default constructor for noninstantiability.
	 * </p>
	 */
	private BundleUtils() {
		throw new AssertionError();
	}
}
