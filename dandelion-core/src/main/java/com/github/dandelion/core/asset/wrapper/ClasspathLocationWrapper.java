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
import com.github.dandelion.core.asset.web.AssetsServlet;
import com.github.dandelion.core.utils.RequestUtils;
import com.github.dandelion.core.utils.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.github.dandelion.core.utils.DandelionUtils.isDevModeEnabled;

/**
 * Wrapper for "classpath" location
 */
public class ClasspathLocationWrapper implements AssetsLocationWrapper {
    @Override
    public String locationKey() {
        return "classpath";
    }

    @Override
    public List<String> wrapLocation(Asset asset, HttpServletRequest request) {
        String tplLocation = asset.getLocations().get(locationKey());
        String tplContext = RequestUtils.getCurrentUrl(request, true);
        tplContext = tplContext.replaceAll("\\?", "_").replaceAll("&", "_");

        String cacheKey = AssetsCache.generateCacheKey(tplContext, AssetsCache.GLOBAL_GROUP, tplLocation);
        if(isDevModeEnabled() || !AssetsCache.cache.containsKey(cacheKey)) {
            String content = ResourceUtils.getFileContentFromClasspath(tplLocation);
            AssetsCache.store(tplContext, AssetsCache.GLOBAL_GROUP, tplLocation, content);
        }

        String baseUrl = RequestUtils.getBaseUrl(request);
        String accessLocation = new StringBuilder(baseUrl)
                .append(AssetsServlet.DANDELION_ASSETS_URL)
                .append("?c=").append(tplContext)
                .append("&id=").append(AssetsCache.GLOBAL_GROUP)
                .append("&r=").append(tplLocation).toString();

        return Arrays.asList(accessLocation);
    }
}