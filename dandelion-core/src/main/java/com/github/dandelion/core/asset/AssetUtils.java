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
package com.github.dandelion.core.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Some utilities to deal with {@link Asset}s.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class AssetUtils {

	/**
	 * <p>
	 * Filters the given set of {@link Asset}s using the given array of
	 * {@link AssetType}.
	 * 
	 * @param assets
	 *            The set of {@link Asset}s to filter.
	 * @param filters
	 *            Types of asset used to filter.
	 * @return a filtered collection of {@link Asset}s.
	 */
	public static Set<Asset> filtersByType(Set<Asset> assets, AssetType... filters) {
		Set<Asset> retval = new LinkedHashSet<Asset>();
		List<AssetType> types = new ArrayList<AssetType>(Arrays.asList(filters));
		for (Asset asset : assets) {
			if (types.contains(asset.getType())) {
				retval.add(asset);
			}
		}
		return retval;
	}

	/**
	 * <p>
	 * Filters the given set of {@link Asset}s by removing all elements whose
	 * name is present in the given array of excluded asset names.
	 * 
	 * @param assets
	 *            The collection of {@link Asset}s to filter.
	 * @param excludedAssetNames
	 *            The collection of asset names to exclude from the collection.
	 * @return a filtered collection of {@link Asset}s.
	 */
	public static Set<Asset> filtersByName(Set<Asset> assets, String[] excludedAssetNames) {

		List<String> excludedAssetNameList = Arrays.asList(excludedAssetNames);

		Set<Asset> filteredAsus = new LinkedHashSet<Asset>();
		for (Asset asset : assets) {

			if (!excludedAssetNameList.contains(asset.getName().trim().toLowerCase())) {
				filteredAsus.add(asset);
			}
		}

		return filteredAsus;
	}

	/**
	 * <p>
	 * Filters the given set of {@link Asset}s using the given
	 * {@link AssetDomPosition}.
	 * 
	 * @param assets
	 *            The set of {@link Asset}s to filter.
	 * @param desiredPosition
	 *            The DOM position used to filter.
	 * @return a filtered collection of {@link Asset}s.
	 */
	public static Set<Asset> filtersByDomPosition(Set<Asset> assets, AssetDomPosition desiredPosition) {
		Set<Asset> filteredAsus = new LinkedHashSet<Asset>();
		for (Asset asset : assets) {

			AssetDomPosition assetPosition = asset.getDom() == null ? asset.getType().getDefaultDom() : asset.getDom();
			if (assetPosition.equals(desiredPosition)) {
				filteredAsus.add(asset);
			}
		}

		return filteredAsus;
	}

	/**
	 * Prevents instantiation;
	 */
	private AssetUtils() {
	}
}