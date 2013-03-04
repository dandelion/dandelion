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
package com.github.dandelion.thymeleaf.processor;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.AssetsRequestContext;
import com.github.dandelion.thymeleaf.dialect.AssetsAttributeName;
import com.github.dandelion.thymeleaf.dialect.DandelionDialect;
import com.github.dandelion.thymeleaf.util.ArgumentsUtil;
import com.github.dandelion.thymeleaf.util.AssetsRender;
import com.github.dandelion.thymeleaf.util.AttributesUtil;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.*;

import java.util.List;

/**
 * Processor for all attribute names for 'Assets' feature
 */
public class AssetsAttrProcessor extends DandelionAttrProcessor {

    public AssetsAttrProcessor(String attributeName) {
        super(attributeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProcessorResult doProcessAttribute(Arguments arguments, Element element, String attributeName) {
        String strippedAttributeName
                = AttributesUtil.stripPrefix(attributeName, DandelionDialect.DIALECT_PREFIX);
        AssetsAttributeName assetsAttributeName
                = (AssetsAttributeName) AttributesUtil.find(strippedAttributeName, AssetsAttributeName.values());
        AssetsRequestContext context
                = AssetsRequestContext.get(ArgumentsUtil.getWebContext(arguments).getHttpServletRequest());
        switch (assetsAttributeName) {
            case SCOPES:
                context.addScopes(element.getAttributeValue(attributeName));
                initializeIfNeeded(arguments);
                break;
            case EXCLUDED_SCOPES:
                context.excludeScopes(element.getAttributeValue(attributeName));
                initializeIfNeeded(arguments);
                break;
            case EXCLUDED_ASSETS:
                context.excludeAssets(element.getAttributeValue(attributeName));
                initializeIfNeeded(arguments);
                break;
            case FINALIZER:
                finalize(context, arguments, element);
                break;
        }

        return ProcessorResult.ok();
    }

    private void initializeIfNeeded(Arguments arguments) {
        for(Element _element: arguments.getDocument().getElementChildren()) {
            if(AssetsAttributeName.FINALIZER.getAttribute().equals(_element.getAttributeValue("id"))) {
                return;
            }
        }
        AssetsRender.renderFinalizer(arguments);
    }

    private void finalize(AssetsRequestContext context, Arguments arguments, Element element) {
        arguments.getDocument().removeChild(element);

        List<Asset> assets = Assets.assetsFor(context.getScopes(true));
        assets = Assets.excludeByName(assets, context.getExcludedAssets());

        for(Element __element: arguments.getDocument().getFirstElementChild().getElementChildren()) {
            if(__element.getNormalizedName().equals("head")) {
                AssetsRender.renderLink(assets, __element);
            } else if(__element.getNormalizedName().equals("body")) {
                AssetsRender.renderScript(assets, __element);
            }
        }
    }
}
