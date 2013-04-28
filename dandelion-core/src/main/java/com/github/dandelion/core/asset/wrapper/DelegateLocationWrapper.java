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

package com.github.dandelion.core.asset.wrapper;

import com.github.dandelion.core.asset.*;
import com.github.dandelion.core.asset.web.AssetParameters;
import com.github.dandelion.core.asset.web.AssetsRequestContext;
import com.github.dandelion.core.asset.web.AssetsServlet;
import com.github.dandelion.core.utils.RequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.dandelion.core.utils.DandelionUtils.isDevModeEnabled;

public class DelegateLocationWrapper implements AssetsLocationWrapper {
    public static final String DELEGATE_CONTENT_PARAM = "DELEGATE_CONTENT";

    @Override
    public String locationKey() {
        return "delegate";
    }

    @Override
    public List<String> wrapLocation(Asset asset, HttpServletRequest request) {
        List<String> locations = new ArrayList<String>();
        AssetParameters params = AssetsRequestContext.get(request).getParameters();
        // Preparation of common variables
        String dcLocation = asset.getLocations().get(locationKey());
        String dcContext = RequestUtils.getCurrentUrl(request, true);
        dcContext = dcContext.replaceAll("\\?", "_").replaceAll("&", "_");

        // delegate are link to multiple groups (due to the possibility to have more than one asset in a page.
        for(String groupId:params.getGroupIds(asset)) {

            // on each group, generate the cache key.
            String cacheKey = AssetsCache.generateCacheKey(dcContext, groupId, dcLocation);

            if(isDevModeEnabled() || !AssetsCache.cache.containsKey(cacheKey)) {
                Map<String, Object> dcParams = params.getParameters(asset, groupId);
                String content = ((DelegateContent) dcParams
                        .get(DELEGATE_CONTENT_PARAM)).getContent(request);

                // and store the specific content into the cache system
                AssetsCache.store(dcContext, groupId, dcLocation, content);
            }

            // Always set the location to retrieve the content from the case
            // See AssetsServlet
            String baseUrl = RequestUtils.getBaseUrl(request);
            String accessLocation = new StringBuilder(baseUrl)
                    .append(AssetsServlet.DANDELION_ASSETS_URL)
                    .append("?c=").append(dcContext)
                    .append("&id=").append(groupId)
                    .append("&r=").append(dcLocation).toString();

            locations.add(accessLocation);
        }
        return locations;
    }
}
