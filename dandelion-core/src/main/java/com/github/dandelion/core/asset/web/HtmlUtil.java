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

package com.github.dandelion.core.asset.web;



import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.cache.AssetCacheSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.web.data.AssetContent;
import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.html.HtmlTag;
import com.github.dandelion.core.html.LinkTag;
import com.github.dandelion.core.html.ScriptTag;

/**
 * Utilities on Html tags creation
 */
public class HtmlUtil {
    private static Logger LOG = LoggerFactory.getLogger(HtmlUtil.class);

    public static final String DEFAULT_CACHE_CONTROL = "no-cache";
    private static final String CACHE_CONTROL = "assets.servlet.cache.control";
    private static String cacheControl;

    synchronized private static void initializeCacheControl() {
        if (cacheControl != null) {
            return;
        }

        String _cacheControl = Configuration.getProperty(CACHE_CONTROL);
        if (DevMode.enabled() || _cacheControl == null || _cacheControl.isEmpty()) {
            _cacheControl = DEFAULT_CACHE_CONTROL;
        }
        cacheControl = _cacheControl;
    }

    public static String getCacheControl() {
        if (cacheControl == null) {
            initializeCacheControl();
        }
        return cacheControl;
    }

    public static HtmlTag transformAsset(Asset asset, String location) {
        HtmlTag tag;
        switch (asset.getType()) {
            case css:
                tag = new LinkTag(location);
                break;
            case js:
                tag = new ScriptTag(location);
                break;
            default:
                tag = null;
        }
        if(tag != null) {
            tag.addAttributesOnlyName(asset.getAttributesOnlyName());
            tag.addAttributes(asset.getAttributes());
        }
        return tag;
    }

    public static AssetContent getAssetContent(String assetKey) {
        String content = "";
        String contentType = null;
        AssetType resourceType = AssetType.typeOfAsset(assetKey);
        if (resourceType != null) {
            content = AssetCacheSystem.getCacheContent(assetKey);
            if (content == null) {
                LOG.debug("missing content from key {}", assetKey);
                content = "";
            }
            contentType = resourceType.getContentType();
        } else {
            content = "";
            contentType = "text/plain";
            LOG.debug("unknown asset type from key {}", assetKey);
        }
        return new AssetContent(content, contentType);
    }
}
