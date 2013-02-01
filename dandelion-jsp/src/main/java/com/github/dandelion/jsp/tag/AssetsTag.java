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

package com.github.dandelion.jsp.tag;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.AssetsRequestContext;
import com.github.dandelion.core.html.LinkTag;
import com.github.dandelion.core.html.ScriptTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
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
 * 
 * @author Thibault Duchateau
 */
public class AssetsTag extends TagSupport {
	private static final Logger LOG = LoggerFactory.getLogger(AssetsTag.class);
		
	private String scopes;

	public int doStartTag() throws JspException {

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();

        AssetsRequestContext context = AssetsRequestContext
                .get(pageContext.getRequest())
                .addScopes(scopes);
        if(context.isAlreadyRendered()) {
            LOG.warn("This page have multiples assets tag, only one is needed");
        }
		if (context.hasScopes()) {
			List<Asset> assets = Assets.assetsFor(context.getScopes());
            LOG.debug("scope = " + scopes + ", assets = " + assets);

			try {
				for (Asset asset : assets) {
					switch (asset.getType()) {
					case css:
						out.println(new LinkTag(Assets.getAssetLocation(asset)).toHtml());
						break;
					case img:
						break;
					case js:
						out.println(new ScriptTag(Assets.getAssetLocation(asset)).toHtml());
						break;
					default:
						break;
					}
				}
                context.hasBeenRendered();
			} catch (IOException e) {
				throw new JspException(e);
			}
		}

		return EVAL_PAGE;
	}

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }
}