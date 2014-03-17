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
package com.github.dandelion.core.asset.locator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.locator.spi.AssetLocator;

/**
 * <p>
 * System in charge of discovering and manipulating all providers of
 * {@link AssetLocator} available in the classpath.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class AssetLocatorSystem {

	private static final Logger LOG = LoggerFactory.getLogger(AssetLocatorSystem.class);

	private static ServiceLoader<AssetLocator> alServiceLoader = ServiceLoader.load(AssetLocator.class);
	private static List<AssetLocator> locators;

	private static void initializeIfNeeded() {
		if (locators == null) {
			initializeLocators();
		}
	}

	private synchronized static void initializeLocators() {

		List<AssetLocator> alws = new ArrayList<AssetLocator>();
		for (AssetLocator al : alServiceLoader) {
			alws.add(al);
			LOG.info("Asset locator found: {} ({})", al.getLocationKey(), al.isActive() ? "active" : "inactive");
		}

		locators = alws;
	}

	public static List<AssetLocator> getLocators() {
		initializeIfNeeded();
		return locators;
	}

	public static Map<String, AssetLocator> getAssetLocatorsMap() {
		Map<String, AssetLocator> assetLocatorsMap = new HashMap<String, AssetLocator>();
		for (AssetLocator al : getLocators()) {
			assetLocatorsMap.put(al.getLocationKey(), al);
		}
		return assetLocatorsMap;
	}

	/**
	 * Prevents instantiation.
	 */
	private AssetLocatorSystem() {
	}
}