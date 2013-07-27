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
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.web.AssetsRequestContext;
import com.github.dandelion.thymeleaf.dialect.AssetsAttributeName;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Utility for Assets Finalizer Processor
 */
public class AssetsFinalizerProcessorUtil {

    public static void initialize(Arguments arguments, String dialectPrefix) {
        for(Element _element: arguments.getDocument().getElementChildren()) {
            if(AssetsAttributeName.FINALIZER.getAttribute().equals(_element.getAttributeValue("id"))) {
                return;
            }
        }
        create(arguments, dialectPrefix);
    }

    private static void create(Arguments arguments, String dialectPrefix) {
        Element finalizer = new Element("div");
        finalizer.setAttribute("id", AssetsAttributeName.FINALIZER.getAttribute());
        finalizer.setAttribute(dialectPrefix + ":" + AssetsAttributeName.FINALIZER.getAttribute(), "internalUse");
        finalizer.setRecomputeProcessorsImmediately(true);
        arguments.getDocument().insertChild(arguments.getDocument().numChildren(), finalizer);
    }

    public static void treat(AssetsRequestContext context, Arguments arguments, HttpServletRequest request, Element element) {
        arguments.getDocument().removeChild(element);

        List<Asset> assets = Assets.assetsFor(context.getScopes(true));
        assets = Assets.excludeByName(assets, context.getExcludedAssets());

        for(Element __element: arguments.getDocument().getFirstElementChild().getElementChildren()) {
            if(__element.getNormalizedName().equals("head")) {
                AssetsRender.renderLink(assets, __element, request, AssetDOMPosition.head, null);
                AssetsRender.renderScript(assets, __element, request, AssetDOMPosition.head);
            } else if(__element.getNormalizedName().equals("body")) {
                AssetsRender.renderLink(assets, __element, request, AssetDOMPosition.body);
                AssetsRender.renderScript(assets, __element, request, AssetDOMPosition.body, null);
            }
        }
    }
}
