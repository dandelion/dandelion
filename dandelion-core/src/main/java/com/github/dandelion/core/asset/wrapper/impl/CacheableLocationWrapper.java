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

package com.github.dandelion.core.asset.wrapper.impl;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;
import com.github.dandelion.core.asset.web.AssetParameters;
import com.github.dandelion.core.asset.web.AssetsRequestContext;
import com.github.dandelion.core.asset.wrapper.spi.AssetsLocationWrapper;
import com.github.dandelion.core.utils.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.dandelion.core.asset.web.AssetsServlet.DANDELION_ASSETS_URL;
import static com.github.dandelion.core.DevMode.isDevModeEnabled;

/**
 * Base for Wrapper with caching faculty
 */
public abstract class CacheableLocationWrapper implements AssetsLocationWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> wrapLocations(Asset asset, HttpServletRequest request) {

        String location = asset.getLocations().get(locationKey());
        String context = RequestUtils.getCurrentUrl(request, true);
        context = context.replaceAll("\\?", "_").replaceAll("&", "_");

        List<String> locations = new ArrayList<String>();
        AssetParameters params = AssetsRequestContext.get(request).getParameters();

        List<String> groupIds = params.getGroupIds(asset);
        if (groupIds == null || groupIds.isEmpty()) {
            groupIds = Arrays.asList(AssetParameters.GLOBAL_GROUP);
        }

        for (String groupId : groupIds) {
            String cacheKey = AssetsCacheSystem.generateCacheKey(context, groupId, location, asset.getType());

            Map<String, Object> parameters = params.getParameters(asset, groupId);
            if (isDevModeEnabled() || !AssetsCacheSystem.checkCacheKey(cacheKey)) {
                String content = getContent(asset, location, parameters, request);
                AssetsCacheSystem.storeCacheContent(context, groupId, location, asset.getType(), content);
            }

            String baseUrl = RequestUtils.getBaseUrl(request);
            String accessLocation = baseUrl + DANDELION_ASSETS_URL + cacheKey;

            locations.add(accessLocation);
        }

        return locations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getContents(Asset asset, HttpServletRequest request) {
        String location = asset.getLocations().get(locationKey());
        String context = RequestUtils.getCurrentUrl(request, true);
        context = context.replaceAll("\\?", "_").replaceAll("&", "_");

        List<String> contents = new ArrayList<String>();
        AssetParameters params = AssetsRequestContext.get(request).getParameters();

        List<String> groupIds = params.getGroupIds(asset);
        if (groupIds == null || groupIds.isEmpty()) {
            groupIds = Arrays.asList(AssetParameters.GLOBAL_GROUP);
        }

        for (String groupId : groupIds) {
            String cacheKey = AssetsCacheSystem.generateCacheKey(context, groupId, location, asset.getType());
            String fileContent = AssetsCacheSystem.getCacheContent(cacheKey);
            contents.add(fileContent);
        }
        return contents;
    }

    protected abstract String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request);
}
