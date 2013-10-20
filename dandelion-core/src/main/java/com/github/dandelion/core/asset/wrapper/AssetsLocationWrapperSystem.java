package com.github.dandelion.core.asset.wrapper;

import com.github.dandelion.core.asset.wrapper.spi.AssetsLocationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AssetsLocationWrapperSystem {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetsLocationWrapperSystem.class);

    private static ServiceLoader<AssetsLocationWrapper> loader = ServiceLoader.load(AssetsLocationWrapper.class);
    private static List<AssetsLocationWrapper> wrappers;

    private AssetsLocationWrapperSystem() {
    }

    private static void initialize() {
        if(wrappers == null) {
            initializeIfNeeded();
        }
    }

    synchronized private static void initializeIfNeeded() {
        if(wrappers != null) return;

        List<AssetsLocationWrapper> alws = new ArrayList<AssetsLocationWrapper>();
        for (AssetsLocationWrapper alw : loader) {
            alws.add(alw);
            LOG.info("found Dandelion Assets Location Wrapper for {}", alw.locationKey());
        }

        wrappers = alws;
    }

    public static List<AssetsLocationWrapper> getWrappers() {
        initialize();
        return wrappers;
    }

    public static Map<String, AssetsLocationWrapper> getWrappersWithKey() {
        Map<String, AssetsLocationWrapper> wrappers = new HashMap<String, AssetsLocationWrapper>();
        for(AssetsLocationWrapper wrapper: getWrappers()) {
            wrappers.put(wrapper.locationKey(), wrapper);
        }
        return wrappers;
    }
}
