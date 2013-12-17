package com.github.dandelion.core.asset.loader.impl;

import com.github.dandelion.core.asset.AssetsComponent;
import com.github.dandelion.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

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

    @Override
    public List<AssetsComponent> loadAssets() {
        if("false".equalsIgnoreCase(Configuration.getProperty("dandelion.asset.json.loader.active"))) {
            return Collections.emptyList();
        }
        return super.loadAssets();
    }
}