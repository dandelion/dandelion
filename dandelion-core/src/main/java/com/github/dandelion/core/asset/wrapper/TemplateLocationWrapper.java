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

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.utils.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.github.dandelion.core.DevMode.isDevModeEnabled;

/**
 * Wrapper for "template" location
 */
public class TemplateLocationWrapper extends CacheableLocationWrapper {
    private Map<String, String> cache;

    public TemplateLocationWrapper() {
        cache = new HashMap<String, String>();
    }

    private String getTemplateContent(String tplLocation) {
        if(isDevModeEnabled() || !cache.containsKey(tplLocation))
            cache.put(tplLocation, ResourceUtils.getFileContentFromClasspath(tplLocation, false));
        return cache.get(tplLocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String locationKey() {
        return "template";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request) {
        String content = getTemplateContent(location);
        for(Map.Entry<String, Object> entry:parameters.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue().toString());
        }
        return content;
    }
}
