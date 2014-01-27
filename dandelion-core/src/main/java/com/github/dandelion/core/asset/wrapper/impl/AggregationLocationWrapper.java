package com.github.dandelion.core.asset.wrapper.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.impl.AssetAggregationProcessorEntry;

/**
 * 
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class AggregationLocationWrapper extends CacheableLocationWrapper {
   
	/**
	 * {@inheritDoc}
	 */
	@Override
    public String getLocationKey() {
        return AssetAggregationProcessorEntry.AGGREGATION;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public String getWrappedLocation(Asset asset, HttpServletRequest request) {
        throw new IllegalStateException("the location key " + getLocationKey() + " can't be use to define a location, it's for internal purpose only");
    }

    @Override
    protected String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request) {
        throw new IllegalStateException("the location key " + getLocationKey() + " can't be use to define a location, it's for internal purpose only");
    }
}
