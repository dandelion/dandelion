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
import com.github.dandelion.core.utils.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.dandelion.core.utils.DandelionUtils.isDevModeEnabled;

/**
 * Wrapper for "template" location
 */
public class TemplateLocationWrapper implements AssetsLocationWrapper {
    private Map<String, String> cache;

    public TemplateLocationWrapper() {
        cache = new HashMap<String, String>();
    }

    private String getTemplateContent(String tplLocation) {
        if(isDevModeEnabled() || !cache.containsKey(tplLocation))
            cache.put(tplLocation, ResourceUtils.getFileContentFromClasspath(tplLocation));
        return cache.get(tplLocation);
    }

    @Override
    public String locationKey() {
        return "template";
    }

    @Override
    public List<String> wrapLocation(Asset asset, HttpServletRequest request) {
        List<String> locations = new ArrayList<String>();
        AssetParameters templateParameters = AssetsRequestContext.get(request).getParameters();
        // Preparation of common variables
        String tplLocation = asset.getLocations().get(locationKey());
        String tplContext = RequestUtils.getCurrentUrl(request, true);
        tplContext = tplContext.replaceAll("\\?", "_").replaceAll("&", "_");
        // extract the template content from classpath
        String tplContent = getTemplateContent(tplLocation);

        // template are link to multiple groups (due to the possibility to have more than one asset in a page.
        for(String groupId:templateParameters.getGroupIds(asset)) {

            // on each group, generate the cache key.
            String cacheKey = AssetsCache.generateCacheKey(tplContext, groupId, tplLocation);

            if(isDevModeEnabled() || !AssetsCache.cache.containsKey(cacheKey)) {
                // extraction of parameters/values
                Map<String, Object> tplParameters
                        = templateParameters.getParameters(asset, groupId);


                // transform the template content into specific content
                String content = tplContent;
                for(Map.Entry<String, Object> entry:tplParameters.entrySet()) {
                    content = content.replace(entry.getKey(), entry.getValue().toString());
                }

                // and store the specific content into the cache system
                AssetsCache.store(tplContext, groupId, tplLocation, content);
            }

            // Always set the location to retrieve the content from the case
            // See AssetsServlet
            String baseUrl = RequestUtils.getBaseUrl(request);
            String accessLocation = new StringBuilder(baseUrl)
                    .append(AssetsServlet.DANDELION_ASSETS_URL)
                    .append("?c=").append(tplContext)
                    .append("&id=").append(groupId)
                    .append("&r=").append(tplLocation).toString();

            locations.add(accessLocation);
        }
        return locations;
    }
}
