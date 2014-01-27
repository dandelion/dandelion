package com.github.dandelion.core.asset.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;

/**
 * System in charge of discovering all implementations of
 * {@link AssetLocationWrapper} available in the classpath.
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public final class AssetLocationWrapperSystem {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(AssetLocationWrapperSystem.class);

	private static ServiceLoader<AssetLocationWrapper> locationWrapperServiceLoader = ServiceLoader
			.load(AssetLocationWrapper.class);
	private static List<AssetLocationWrapper> wrappers;

	private static void initialize() {
		if (wrappers == null) {
			initializeIfNeeded();
		}
	}

	synchronized private static void initializeIfNeeded() {
		if (wrappers != null) {
			return;
		}

		List<AssetLocationWrapper> alws = new ArrayList<AssetLocationWrapper>();
		for (AssetLocationWrapper alw : locationWrapperServiceLoader) {
			alws.add(alw);
			LOG.info("Asset location wrapper found: {}", alw.getLocationKey());
		}

		wrappers = alws;
	}

	public static List<AssetLocationWrapper> getWrappers() {
		initialize();
		return wrappers;
	}

	public static Map<String, AssetLocationWrapper> getWrappersWithKey() {
		Map<String, AssetLocationWrapper> wrappers = new HashMap<String, AssetLocationWrapper>();
		for (AssetLocationWrapper wrapper : getWrappers()) {
			wrappers.put(wrapper.getLocationKey(), wrapper);
		}
		return wrappers;
	}
}
