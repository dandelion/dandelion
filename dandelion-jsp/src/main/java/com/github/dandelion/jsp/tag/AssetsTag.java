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

package com.github.dandelion.jsp.tag;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.web.AssetsRequestContext;
import com.github.dandelion.jsp.util.AssetsRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/**
 * <p>
 * JSP tag in charge of generating necessary HTML <code>script</code> and
 * <code>link</code> tags.
 *
 * <p>
 * Usage :
 *
 * <pre>
 * &lt;dandelion:assets scopes="..." /&gt;
 * </pre>
 */
public class AssetsTag extends TagSupport {
	private static final Logger LOG = LoggerFactory.getLogger(AssetsTag.class);

	private String scopes;
    private String excludedScopes;
    private String excludedAssets;
    private boolean renderer = true;

	public int doEndTag() throws JspException {
        AssetsRequestContext context = AssetsRequestContext
                .get(pageContext.getRequest())
                .addScopes(getScopes())
                .excludeScopes(getExcludedScopes())
                .excludeAssets(getExcludedAssets());
        if(isRenderer()) {
            if(context.isAlreadyRendered()) {
                LOG.warn("This page has multiple 'assets' tags, only one is needed for the asset rendering");
                LOG.warn("Please consider to set the 'renderer' attribute to 'false' on all previous 'assets' tags");
            }

            List<Asset> assets = AssetStack.prepareAssetsFor((HttpServletRequest) pageContext.getRequest(), context.getScopes(true), context.getExcludedAssets());

            AssetsRender.render(assets, pageContext);
            context.hasBeenRendered();
        } else {
            LOG.debug("The renderer of assets is inactive for this time");
        }

		return EVAL_PAGE;
	}

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getExcludedScopes() {
        return excludedScopes;
    }

    public void setExcludedScopes(String excludedScopes) {
        this.excludedScopes = excludedScopes;
    }

    public String getExcludedAssets() {
        return excludedAssets;
    }

    public void setExcludedAssets(String excludedAssets) {
        this.excludedAssets = excludedAssets;
    }

    public boolean isRenderer() {
        return renderer;
    }

    public void setRenderer(boolean renderer) {
        this.renderer = renderer;
    }
}