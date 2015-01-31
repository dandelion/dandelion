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

package com.github.dandelion.core.asset.locator.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.locator.AbstractAssetLocator;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * <p>
 * Locator for assets that use {@code webapp} as a location key.
 * 
 * <p>
 * Basically, a "webapp asset" is an asset stored inside the web application
 * folder.
 * 
 * @author Romain Lespinasse
 * @since 0.2.0
 */
public class WebappLocator extends AbstractAssetLocator {

	public WebappLocator() {
		active = true;
	}

	@Override
	public String getLocationKey() {
		return "webapp";
	}

	@Override
	public String doGetLocation(AssetStorageUnit asu, HttpServletRequest request) {
		String location = asu.getLocations().get(getLocationKey());
		return UrlUtils.getProcessedUrl(location, request, null);
	}

	@Override
	protected String doGetContent(String finalLocation, Map<String, Object> parameters, HttpServletRequest request) {
		return ResourceUtils.getContentFromUrl(request, finalLocation, true);
	}
}
