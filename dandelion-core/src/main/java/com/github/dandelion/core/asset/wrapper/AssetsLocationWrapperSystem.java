package com.github.dandelion.core.asset.wrapper;

import com.github.dandelion.core.asset.wrapper.spi.AssetsLocationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

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

}
