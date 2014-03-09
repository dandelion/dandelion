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

package com.github.dandelion.core.asset.wrapper.impl;

import static com.github.dandelion.core.asset.cache.AssetCacheSystem.generateCacheKey;
import static com.github.dandelion.core.asset.cache.AssetCacheSystem.storeContent;
import static com.github.dandelion.core.asset.web.AssetRequestContext.get;
import static com.github.dandelion.core.asset.web.AssetServlet.DANDELION_ASSETS_URL;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.AssetCacheSystem;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * <p>
 * Abstract base class for all {@link AssetLocationWrapper} that need to cache
 * the asset.
 * 
 * <p>
 * Once the asset retrieved, Dandelion will use the {@link AssetCacheSystem} to
 * store the asset in the configured cache.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public abstract class CacheableLocationWrapper extends BaseLocationWrapper {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWrappedLocation(Asset asset, HttpServletRequest request) {

		String location = asset.getLocations().get(getLocationKey());
		
		String context = UrlUtils.getBaseUrl(request).toString();
		context = context.replaceAll("\\?", "_").replaceAll("&", "_");

		String cacheKey = generateCacheKey(context, location, asset.getName(), asset.getType());
		String content = AssetCacheSystem.getContent(cacheKey);

		if (content == null || DevMode.enabled()) {
			Map<String, Object> parameters = get(request).getParameters(asset.getName());
			content = getContent(asset, location, parameters, request);
			
			// Apply replacement if parameters are provided
			if(!parameters.isEmpty()){
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {
					content = content.replace(entry.getKey(), entry.getValue().toString());
				}
			}
			
			// Finally store the final content in cache
			storeContent(context, location, asset.getName(), asset.getType(), content);
		}

		return UrlUtils.getProcessedUrl(DANDELION_ASSETS_URL + cacheKey, request, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWrappedContent(Asset asset, HttpServletRequest request) {
		String location = asset.getLocations().get(getLocationKey());
		String prefixCacheKey = UrlUtils.getProcessedUrl(DANDELION_ASSETS_URL, request, null);
		String cacheKey = location.replaceAll(prefixCacheKey, "");
		return AssetCacheSystem.getContent(cacheKey);
	}

	protected abstract String getContent(Asset asset, String location, Map<String, Object> parameters,
			HttpServletRequest request);
}
