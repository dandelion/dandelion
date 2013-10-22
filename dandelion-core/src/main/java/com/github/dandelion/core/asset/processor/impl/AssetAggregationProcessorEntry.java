/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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
package com.github.dandelion.core.asset.processor.impl;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;
import com.github.dandelion.core.asset.processor.spi.AssetProcessorEntry;
import com.github.dandelion.core.asset.wrapper.spi.AssetsLocationWrapper;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.github.dandelion.core.asset.web.AssetsServlet.DANDELION_ASSETS_URL;

public class AssetAggregationProcessorEntry extends AssetProcessorEntry {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetAggregationProcessorEntry.class);

    public static final String AGGREGATION = "aggregation";
    public static final String AGGREGATION_ENABLED_KEY = "dandelion.aggregation.enabled";
    private boolean aggregationEnabled = true;

    public AssetAggregationProcessorEntry() {
        this.aggregationEnabled = Boolean.TRUE.toString().equals(
                Configuration.getProperty(AGGREGATION_ENABLED_KEY, Boolean.toString(aggregationEnabled)));

        if(DevMode.isDevModeEnabled()) {
            this.aggregationEnabled = false;
        }

        LOG.info("Dandelion Asset Aggregation is {}", aggregationEnabled?"enabled":"disabled");
    }

    @Override
    public String getTreatmentKey() {
        return AGGREGATION;
    }

    @Override
    public int getRank() {
        return 1000;
    }

    @Override
    public List<Asset> process(List<Asset> assets, HttpServletRequest request) {
        if(!aggregationEnabled) {
            return assets;
        }

        String context = RequestUtils.getCurrentUrl(request, true);
        context = context.replaceAll("\\?", "_").replaceAll("&", "_");

        String baseUrl = RequestUtils.getBaseUrl(request);

        List<Asset> aggregatedAssets = new ArrayList<Asset>();
        for (AssetType type : AssetType.values()) {
            LOG.debug("Aggregation for asset type {}", type.name());
            List<Asset> typedAssets = AssetStack.filterByType(assets, type);

            if(typedAssets.isEmpty()) {
                LOG.debug("No asset for type {}", type.name());
                continue;
            }

            String aggregationKey = generateAggregationKey(typedAssets);
            String cacheKey = AssetsCacheSystem.generateCacheKey(context, AGGREGATION, aggregationKey, type);

            if (!AssetsCacheSystem.checkCacheKey(cacheKey)) {
                LOG.debug("cache assets aggregation for type {}", type.name());
                cacheAggregatedContent(request, context, type, typedAssets, aggregationKey);
            }

            String accessLocation = baseUrl + DANDELION_ASSETS_URL + cacheKey;

            Map<String, String> locations = new HashMap<String, String>();
            locations.put(AGGREGATION, accessLocation);

            aggregatedAssets.add(new Asset(aggregationKey, AGGREGATION, type, locations));
        }
        return aggregatedAssets;
    }

    private void cacheAggregatedContent(HttpServletRequest request, String context, AssetType type, List<Asset> typedAssets, String generatedAssetKey) {
        Map<String, AssetsLocationWrapper> wrappers = AssetStack.getAssetsLocationWrappers();
        StringBuilder aggregatedContent = new StringBuilder();

        for (Asset asset : typedAssets) {
            for (Map.Entry<String, String> location : asset.getLocations().entrySet()) {
                AssetsLocationWrapper wrapper = wrappers.get(location.getKey());
                List<String> contents;
                if (wrapper == null) {
                    contents = Arrays.asList(ResourceUtils.getContentFromUrl(location.getValue(), true));
                } else {
                    contents = wrapper.getContents(asset, request);
                }
                for (String content : contents) {
                    aggregatedContent.append(content).append("\n");
                }
            }
        }

        AssetsCacheSystem.storeCacheContent(context, AGGREGATION, generatedAssetKey, type, aggregatedContent.toString());
    }

    private String generateAggregationKey(List<Asset> assets) {
        StringBuilder key = new StringBuilder();

        for (Asset asset : assets) {
            key.append(asset.getAssetKey()).append("|");
        }
        return key.toString();
    }
}
