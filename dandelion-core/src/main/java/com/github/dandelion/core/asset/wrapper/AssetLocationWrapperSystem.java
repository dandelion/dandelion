package com.github.dandelion.core.asset.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;

public final class AssetLocationWrapperSystem {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetLocationWrapperSystem.class);

    private static ServiceLoader<AssetLocationWrapper> loader = ServiceLoader.load(AssetLocationWrapper.class);
    private static List<AssetLocationWrapper> wrappers;

    private static void initialize() {
        if(wrappers == null) {
            initializeIfNeeded();
        }
    }

    synchronized private static void initializeIfNeeded() {
        if(wrappers != null) return;

        List<AssetLocationWrapper> alws = new ArrayList<AssetLocationWrapper>();
        for (AssetLocationWrapper alw : loader) {
            alws.add(alw);
            LOG.info("found Dandelion Assets Location Wrapper for {}", alw.locationKey());
        }

        wrappers = alws;
    }

    public static List<AssetLocationWrapper> getWrappers() {
        initialize();
        return wrappers;
    }

    public static Map<String, AssetLocationWrapper> getWrappersWithKey() {
        Map<String, AssetLocationWrapper> wrappers = new HashMap<String, AssetLocationWrapper>();
        for(AssetLocationWrapper wrapper: getWrappers()) {
            wrappers.put(wrapper.locationKey(), wrapper);
        }
        return wrappers;
    }
}
