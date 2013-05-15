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
package com.github.dandelion.thymeleaf.processor;

import com.github.dandelion.core.asset.web.AssetsRequestContext;
import com.github.dandelion.thymeleaf.dialect.AssetsAttributeName;
import com.github.dandelion.thymeleaf.dialect.DandelionDialect;
import com.github.dandelion.thymeleaf.util.ArgumentsUtil;
import com.github.dandelion.thymeleaf.util.AssetsFinalizerProcessorUtil;
import com.github.dandelion.thymeleaf.util.AttributesUtil;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.ProcessorResult;

import javax.servlet.http.HttpServletRequest;

/**
 * Processor for all attribute names for 'Assets' feature
 */
public class AssetsAttrProcessor extends DandelionAttrProcessor {

    public AssetsAttrProcessor(String attributeName) {
        super(attributeName);
    }

    @Override
    public int getPrecedence() {
        return DandelionDialect.HIGHEST_PRECEDENCE;
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
        HttpServletRequest request = ArgumentsUtil.getWebContext(arguments).getHttpServletRequest();
        AssetsRequestContext context
                = AssetsRequestContext.get(request);
        switch (assetsAttributeName) {
            case STACK:
                AssetsFinalizerProcessorUtil.initialize(arguments, DandelionDialect.DIALECT_PREFIX);
            case SCOPES:
                context.addScopes(element.getAttributeValue(attributeName));
                AssetsFinalizerProcessorUtil.initialize(arguments, DandelionDialect.DIALECT_PREFIX);
                break;
            case EXCLUDED_SCOPES:
                context.excludeScopes(element.getAttributeValue(attributeName));
                AssetsFinalizerProcessorUtil.initialize(arguments, DandelionDialect.DIALECT_PREFIX);
                break;
            case EXCLUDED_ASSETS:
                context.excludeAssets(element.getAttributeValue(attributeName));
                AssetsFinalizerProcessorUtil.initialize(arguments, DandelionDialect.DIALECT_PREFIX);
                break;
            case FINALIZER:
                AssetsFinalizerProcessorUtil.treat(context, arguments, request, element);
                break;
        }

        return ProcessorResult.ok();
    }
}
