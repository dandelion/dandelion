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
package com.github.dandelion.core.asset.cache;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.spi.AssetCache;
import com.github.dandelion.core.utils.Sha1Utils;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * <p>
 * System in charge of manipulating the selected implementation of
 * {@link AssetCache}.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetCacheManager {

	private static final Logger LOG = LoggerFactory.getLogger(AssetCacheManager.class);
	
	private final Context context;

	public AssetCacheManager(Context context) {
		this.context = context;
	}

	public String generateCacheKey(HttpServletRequest request, Asset asset) {
		String context = UrlUtils.getCurrentUrl(request, true).toString();
		context = context.replaceAll("\\?", "_").replaceAll("&", "_");
		
		StringBuilder key = new StringBuilder(Sha1Utils.generateSha1(context, true));
		key.append("/");
		key.append(asset.getName());
		key.append('/');
		key.append(asset.getType().name());
//		key.append("-");
//		key.append(asset.getVersion());
//		key.append(".");
//		key.append(asset.getType().name());
		return key.toString();
	}

	public String generateMinCacheKey(HttpServletRequest request, Asset asset) {
		String context = UrlUtils.getCurrentUrl(request, true).toString();
		context = context.replaceAll("\\?", "_").replaceAll("&", "_");
		
		StringBuilder key = new StringBuilder(Sha1Utils.generateSha1(context, true));
		key.append("/");
		key.append(asset.getName());
		key.append("/min/");
		key.append(asset.getType().name());
		return key.toString();
	}
	
	public String generateCacheKeyMin(String context, Asset asset) {
		StringBuilder key = new StringBuilder(Sha1Utils.generateSha1(context, true));
		key.append("/");
		key.append(asset.getName());
		key.append("/");
		key.append(asset.getType().name());
		return key.toString();
	}

	public String getCacheKeyFromRequest(HttpServletRequest request) {
		Pattern p = Pattern.compile("dandelion-assets/(.*)/");
		Matcher m = p.matcher(request.getRequestURL());

		String cacheKey = null;
		if (m.find()) {
			cacheKey = m.group(1);
		}
		 
		return cacheKey;
	}

	public String getContent(String cacheKey) {
		LOG.debug("Retrieving asset with the key {}", cacheKey);
		return context.getAssetCache().getAssetContent(cacheKey);
	}

	public Set<Asset> getAssets(String cacheKey) {
		return context.getAssetCache().getRequestAssets(cacheKey);
	}

	public String storeContent(String key, String content) {
		LOG.debug("Storing asset under the key {}", key);
		context.getAssetCache().storeAssetContent(key, content);
		return content;
	}

	public Set<Asset> storeAssets(String key, Set<Asset> a) {
		context.getAssetCache().storeRequestAssets(key, a);
		return a;
	}

	public void remove(String key) {
		LOG.debug("Removing asset under the key {}", key);
		context.getAssetCache().remove(key);
	}

	public String getCacheName() {
		return context.getAssetCache().getCacheName();
	}
	
	public void clearCache(){
		context.getAssetCache();
	}
}