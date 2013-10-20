package com.github.dandelion.core.asset.loader.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DandelionAssetsJsonLoader extends AbstractAssetsJsonLoader {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(DandelionAssetsJsonLoader.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public String getFolder() {
        return "dandelion";
    }
}