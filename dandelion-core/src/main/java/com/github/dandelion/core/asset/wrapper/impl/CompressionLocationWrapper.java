package com.github.dandelion.core.asset.wrapper.impl;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.impl.AssetAggregationProcessorEntry;
import com.github.dandelion.core.asset.processor.impl.AssetCompressionProcessorEntry;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class CompressionLocationWrapper extends CacheableLocationWrapper {
    @Override
    public String locationKey() {
        return AssetCompressionProcessorEntry.COMPRESSION;
    }

    @Override
    public String wrapLocation(Asset asset, HttpServletRequest request) {
        throw new IllegalStateException("the location key " + locationKey() + " can't be use to define a location, it's for internal purpose only");
    }

    @Override
    protected String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request) {
        throw new IllegalStateException("the location key " + locationKey() + " can't be use to define a location, it's for internal purpose only");
    }
}
