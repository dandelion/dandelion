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

package com.github.dandelion.extras.webjar.asset.wrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.webjars.WebJarAssetLocator;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.wrapper.AssetLocationWrapperError;
import com.github.dandelion.core.asset.wrapper.impl.CacheableLocationWrapper;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * Location wrapper for assets that use {@code webjars} as a location key.
 * 
 * <p>
 * This {@link AssetLocationWrapper} uses the {@link WebJarAssetLocator} to
 * locate assets in the classpath before getting their content.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class WebjarLocationWrapper extends CacheableLocationWrapper {
	
	private static WebJarAssetLocator locator = new WebJarAssetLocator();
	private static Pattern pattern = Pattern.compile("url\\((.*)\\)", Pattern.CASE_INSENSITIVE);
	private HttpServletRequest request;

	public WebjarLocationWrapper() {
		this.active = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocationKey() {
		return "webjar";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request) {
		this.request = request;

		String webjarsLocation = locator.getFullPath(location);

		String content = ResourceUtils.getFileContentFromClasspath(webjarsLocation, false);

		if (asset.getType().equals(AssetType.css) && content.contains("..")) {
			content = processRelativePaths(content, webjarsLocation.substring(webjarsLocation.indexOf("webjars")));
		}
		return content;
	}

	public String processRelativePaths(String content, String url) {

		StringWriter sw = new StringWriter();
		BufferedReader reader = new BufferedReader(new StringReader(content));
		BufferedWriter writer = new BufferedWriter(sw);

		try {
			String line = null;

			while ((line = reader.readLine()) != null) {

				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {

					int lvl = StringUtils.countMatches(line, "..");

					if (lvl > 0) {
						String parentPath = null;
						int c = lvl + 1;
						for (int i = url.length() - 1; i >= 0; i--) {
							if (c > 0 && url.charAt(i) == '/') {
								parentPath = url.substring(0, i + 1);
								c--;
							}

							if (c == 0) {
								break;
							}
						}
						line = line.replaceAll("(\\.\\./)+", request.getContextPath() + "/" + parentPath);
					}
				}
				writer.write(line + '\n');
			}
		}
		catch (IOException e) {
			throw new DandelionException(AssetLocationWrapperError.ASSET_RELATIVE_PATHS);
		}
		finally {
			try {
				if (reader != null)
					reader.close();
			}
			catch (IOException e) {
				// Should never happen
				throw new DandelionException(AssetLocationWrapperError.ASSET_RELATIVE_PATHS);
			}
			try {
				if (writer != null)
					writer.close();
			}
			catch (IOException e) {
				// Should never happen
				throw new DandelionException(AssetLocationWrapperError.ASSET_RELATIVE_PATHS);
			}
		}

		return sw.getBuffer().toString();
	}
}