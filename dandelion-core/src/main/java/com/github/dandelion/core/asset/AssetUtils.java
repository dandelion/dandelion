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

import com.github.dandelion.core.storage.AssetStorageUnit;

/**
 * <p>
 * Some utilities to deal with {@link Asset}s and {@link AssetStorageUnit}s.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class AssetUtils {

	/**
	 * <p>
	 * Filters the given set of {@link AssetStorageUnit} using the given array
	 * of {@link AssetType}.
	 * 
	 * @param asus
	 *            The set of {@link AssetStorageUnit} to filter.
	 * @param filters
	 *            Types of asset used to filter.
	 * @return a filtered collection of {@link AssetStorageUnit}.
	 */
	public static Set<AssetStorageUnit> filtersByType(Set<AssetStorageUnit> asus, AssetType... filters) {
		Set<AssetStorageUnit> retval = new LinkedHashSet<AssetStorageUnit>();
		List<AssetType> types = new ArrayList<AssetType>(Arrays.asList(filters));
		for (AssetStorageUnit asu : asus) {
			if (types.contains(asu.getType())) {
				retval.add(asu);
			}
		}
		return retval;
	}

	/**
	 * <p>
	 * Filters the given set of {@link AssetStorageUnit} by removing all
	 * elements whose name is present in the given array of excluded asset
	 * names.
	 * 
	 * @param asus
	 *            The collection of {@link AssetStorageUnit} to filter.
	 * @param excludedAssetNames
	 *            The collection of asset names to exclude from the collection.
	 * @return a filtered collection of {@link AssetStorageUnit}.
	 */
	public static Set<AssetStorageUnit> filtersByName(Set<AssetStorageUnit> asus, String[] excludedAssetNames) {

		List<String> excludedAssetNameList = Arrays.asList(excludedAssetNames);

		Set<AssetStorageUnit> filteredAsus = new LinkedHashSet<AssetStorageUnit>();
		for (AssetStorageUnit asu : asus) {

			if (!excludedAssetNameList.contains(asu.getName().trim().toLowerCase())) {
				filteredAsus.add(asu);
			}
		}

		return filteredAsus;
	}

	/**
	 * <p>
	 * Filters the given set of {@link AssetStorageUnit} using the given
	 * {@link Temp}.
	 * 
	 * @param asus
	 *            The set of {@link AssetStorageUnit} to filter.
	 * @param desiredPosition
	 *            The DOM position used to filter.
	 * @return a filtered collection of {@link AssetStorageUnit}.
	 */
	public static Set<AssetStorageUnit> filtersByDomPosition(Set<AssetStorageUnit> asus,
			Temp desiredPosition) {
		Set<AssetStorageUnit> filteredAsus = new LinkedHashSet<AssetStorageUnit>();
		for (AssetStorageUnit asu : asus) {

			Temp assetPosition = asu.getDom() == null ? asu.getType().getDefaultDom() : asu.getDom();
			if (assetPosition.equals(desiredPosition)) {
				filteredAsus.add(asu);
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
