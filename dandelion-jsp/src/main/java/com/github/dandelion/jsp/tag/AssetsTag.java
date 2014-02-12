/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.github.dandelion.core.asset.web.AssetRequestContext;

/**
 * <p>
 * JSP tag in charge of generating necessary HTML <code>script</code> and
 * <code>link</code> tags.
 * 
 * <p>
 * Usage :
 * 
 * <pre>
 * &lt;dandelion:assets bundles="..." /&gt;
 * </pre>
 */
public class AssetsTag extends TagSupport {

	private static final long serialVersionUID = -417156851675582892L;
	private String bundles;
	private String excludedBundles;
	private String excludedAssets;

	public int doEndTag() throws JspException {
		AssetRequestContext.get(pageContext.getRequest()).addBundles(getBundles()).excludeBundles(getExcludedBundles())
				.excludeAssets(getExcludedAssets());
		return EVAL_PAGE;
	}

	public String getBundles() {
		return bundles;
	}

	public void setBundles(String bundles) {
		this.bundles = bundles;
	}

	public String getExcludedBundles() {
		return excludedBundles;
	}

	public void setExcludedBundles(String excludedBundles) {
		this.excludedBundles = excludedBundles;
	}

	public String getExcludedAssets() {
		return excludedAssets;
	}

	public void setExcludedAssets(String excludedAssets) {
		this.excludedAssets = excludedAssets;
	}
}
