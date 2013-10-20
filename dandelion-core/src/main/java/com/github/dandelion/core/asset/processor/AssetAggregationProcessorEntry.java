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
package com.github.dandelion.core.asset.processor;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;
import com.github.dandelion.core.asset.web.AssetsServlet;
import com.github.dandelion.core.asset.wrapper.AssetsLocationWrapper;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.DandelionUtils;
import com.github.dandelion.core.utils.RequestUtils;
import com.github.dandelion.core.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AssetAggregationProcessorEntry extends AssetProcessorEntry {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetAggregationProcessorEntry.class);

    public static final String AGGREGATION = "aggregation";
    public static final String AGGREGATION_ENABLED_KEY = "dandelion.aggregation.enabled";
    private boolean aggregationEnabled = true;

    public AssetAggregationProcessorEntry() {
        this.aggregationEnabled = Boolean.TRUE.toString().equals(
                Configuration.getProperty(AGGREGATION_ENABLED_KEY, Boolean.toString(aggregationEnabled)));

        if(DandelionUtils.isDevModeEnabled()) {
            this.aggregationEnabled = false;
        }

        LOG.info("Dandelion Asset Aggregation is {}", aggregationEnabled?"enabled":"disabled");
    }

    @Override
    public String getTreatmentKey() {
        return "aggregation";
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

            String generatedAssetKey = generateAssetKey(typedAssets, type);
            String key = AssetsCacheSystem.getCacheKey(context, AGGREGATION, generatedAssetKey);

            if (!AssetsCacheSystem.checkCacheKey(key)) {
                LOG.debug("cache assets aggregation for type {}", type.name());
                cacheAggregatedContent(request, context, typedAssets, generatedAssetKey);
            }

            String accessLocation = baseUrl + AssetsServlet.DANDELION_ASSETS_URL + generatedAssetKey
                    + "?c=" + context + "&id=" + AGGREGATION + "&r=" + generatedAssetKey;

            Map<String, String> locations = new HashMap<String, String>();
            locations.put(AGGREGATION, accessLocation);

            aggregatedAssets.add(new Asset(generatedAssetKey, AGGREGATION, type, locations));
        }
        return aggregatedAssets;
    }

    private void cacheAggregatedContent(HttpServletRequest request, String context, List<Asset> typedAssets, String generatedAssetKey) {
        Map<String, AssetsLocationWrapper> wrappers = AssetStack.getAssetsLocationWrappers();
        StringBuilder aggregatedContent = new StringBuilder();

        for (Asset asset : typedAssets) {
            for (Map.Entry<String, String> location : asset.getLocations().entrySet()) {
                AssetsLocationWrapper wrapper = wrappers.get(location.getKey());
                List<String> contents;
                if (wrapper == null) {
                    contents = Arrays.asList(UrlUtils.getUrlContent(location.getValue()));
                } else {
                    contents = wrapper.getContents(asset, request);
                }
                for (String content : contents) {
                    aggregatedContent.append(content).append("\n");
                }
            }
        }

        AssetsCacheSystem.storeCacheContent(context, AGGREGATION, generatedAssetKey, aggregatedContent.toString());
    }

    private String generateAssetKey(List<Asset> assets, AssetType type) {
        StringBuilder key = new StringBuilder();

        for (Asset asset : assets) {
            key.append(asset.getAssetKey()).append("|");
        }

        try {
            return sha1(key.toString()) + "." + type.name();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }


    private String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aResult : result) {
            sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
