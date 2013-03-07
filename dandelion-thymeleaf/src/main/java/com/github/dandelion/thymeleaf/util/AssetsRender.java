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
 * 3. Neither the name of DataTables4j nor the names of its contributors
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

import com.github.dandelion.core.asset.*;
import com.github.dandelion.core.utils.RequestUtils;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.thymeleaf.dialect.AssetsAttributeName;
import com.github.dandelion.thymeleaf.dialect.DandelionDialect;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.dandelion.core.utils.DandelionUtils.isDevModeEnabled;

/**
 * Render of Assets for Thymeleaf
 */
public class AssetsRender {
    /**
     * Render all <code>&lt;link/&gt;</code> of CSS assets by adding them in thymeleaf model
     * @param assets asset to treat
     * @param templateParameters parameters for template asset
     * @param root thymeleaf element root
     * @param request http request
     */
    public static void renderLink(List<Asset> assets, AssetsTemplateParameters templateParameters, Element root, HttpServletRequest request) {
        for(Asset asset: Assets.filterByType(assets, AssetType.css)) {
            for(String location:renderLocations(asset, templateParameters, request)) {
                Element link = new Element("link");
                link.setAttribute("rel", "stylesheet");
                link.setAttribute("href", location);
                root.insertChild(root.numChildren(), link);
            }
        }
    }

    /**
     * Render all <code>&lt;script/&gt;</code> of JS assets by adding them in thymeleaf model
     * @param assets asset to treat
     * @param templateParameters parameters for template asset
     * @param root thymeleaf element root
     * @param request http request
     */
    public static void renderScript(List<Asset> assets, AssetsTemplateParameters templateParameters, Element root, HttpServletRequest request) {
        for(Asset asset:Assets.filterByType(assets, AssetType.js)) {
            for(String location:renderLocations(asset, templateParameters, request)) {
                Element script = new Element("script");
                script.setAttribute("src", location);
                root.insertChild(root.numChildren(), script);
            }
        }
    }

    /**
     * Render the Finalizer for assets management.
     * @param arguments Thymeleaf arguments
     */
    public static void renderFinalizer(Arguments arguments) {
        Element finalizer = new Element("div");
        finalizer.setAttribute("id", AssetsAttributeName.FINALIZER.getAttribute());
        finalizer.setAttribute(DandelionDialect.DIALECT_PREFIX + ":" + AssetsAttributeName.FINALIZER.getAttribute(), "internalUse");
        finalizer.setRecomputeProcessorsImmediately(true);
        arguments.getDocument().insertChild(arguments.getDocument().numChildren(), finalizer);
    }

    private static List<String> renderLocations(Asset asset, AssetsTemplateParameters templateParameters, HttpServletRequest request) {
        List<String> locations = new ArrayList<String>();
        if(!templateParameters.isTemplate(asset)) {
            // In case of simple access, it's just a location
            String location = Assets.getAssetLocation(asset);
            locations.add(location);
        } else {
            // In case of template, it's more complicated
            // Preparation of common variables
            String tplContext = RequestUtils.getCurrentUrl(request, false);
            String tplLocation = Assets.getAssetTemplateLocation(asset);

            // template are link to multiple groups (due to the possibility to have more than one asset in a page.
            for(String groupId:templateParameters.getGroupIds(asset)) {

                // on each group, generate the cache key.
                String cacheKey = AssetsCache.generateCacheKey(tplContext, groupId, tplLocation);

                /*
                    a template is only convert in specific asset when
                     - the dev mode is active
                     - or the cache don't contain the cache key
                */
                if(isDevModeEnabled() || !AssetsCache.cache.containsKey(cacheKey)) {
                    // extraction of parameters/values
                    Map<String, String> tplParameters
                            = templateParameters.getParameters(asset, groupId);

                    // extract the template content from classpath
                    String tplContent = ResourceUtils.getFileContentFromClasspath(tplLocation);

                    // transform the template content into specific content
                    String content = tplContent;
                    for(Map.Entry<String, String> entry:tplParameters.entrySet()) {
                        content = content.replace(entry.getKey(), entry.getValue());
                    }

                    // and store the specific content into the cache system
                    AssetsCache.store(tplContext, groupId, tplLocation, content);
                }

                // Always set the location to retrieve the content from the case
                // See AssetsServlet
                String baseUrl = RequestUtils.getBaseUrl(request);
                String location = new StringBuilder(baseUrl)
                        .append("/dandelion-assets/")
                        .append("?c=").append(tplContext)
                        .append("&id=").append(groupId)
                        .append("&r=").append(tplLocation).toString();

                locations.add(location);
            }
        }
        return locations;
    }
}
