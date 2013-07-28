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
package com.github.dandelion.thymeleaf.util;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDOMPosition;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.Assets;
import org.thymeleaf.dom.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Render of Assets for Thymeleaf
 */
public class AssetsRender {
    /**
     * Render all <code>&lt;link/&gt;</code> of CSS assets by adding them in thymeleaf model
     * @param assets asset to treat
     * @param root thymeleaf element root
     * @param request http request
     * @param positions filter positions
     */
    public static void renderLink(List<Asset> assets, Element root, HttpServletRequest request, AssetDOMPosition... positions) {
        List<AssetDOMPosition> _positions = Arrays.asList(positions);
        for(Asset asset: Assets.filterByType(assets, AssetType.css)) {
            if(_positions.contains(asset.getDom())) {
                for(String location:renderLocations(asset, request)) {
                    Element link = new Element("link");
                    link.setAttribute("rel", "stylesheet");
                    link.setAttribute("href", location);
                    root.insertChild(root.numChildren(), link);
                }
            }
        }
    }

    /**
     * Render all <code>&lt;script/&gt;</code> of JS assets by adding them in thymeleaf model
     * @param assets asset to treat
     * @param root thymeleaf element root
     * @param request http request
     * @param positions filter positions
     */
    public static void renderScript(List<Asset> assets, Element root, HttpServletRequest request, AssetDOMPosition... positions) {
        List<AssetDOMPosition> _positions = Arrays.asList(positions);
        for(Asset asset:Assets.filterByType(assets, AssetType.js)) {
            if(_positions.contains(asset.getDom())) {
                for(String location:renderLocations(asset, request)) {
                    Element script = new Element("script");
                    script.setAttribute("src", location);
                    if(asset.isAsync()) script.setAttribute("async", true, "async");
                    if(asset.isDefer()) script.setAttribute("defer", true, "defer");
                    root.insertChild(root.numChildren(), script);
                }
            }
        }
    }

    private static List<String> renderLocations(Asset asset, HttpServletRequest request) {
        return Assets.getAssetLocations(asset, request);
    }
}
