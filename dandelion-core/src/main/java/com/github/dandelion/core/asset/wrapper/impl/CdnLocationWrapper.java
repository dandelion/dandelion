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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * Location wrapper for assets that use {@code cdn} as a location key.
 * 
 * <p>
 * Note that by default, this wrapper is disabled. It is automatically enabled
 * when minification or aggregation is enabled.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class CdnLocationWrapper extends CacheableLocationWrapper {

	private Pattern pattern = Pattern.compile("url\\((.*)\\)", Pattern.CASE_INSENSITIVE);

	public CdnLocationWrapper() {
		this.active = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLocationKey() {
		return "cdn";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContent(Asset asset, String location, Map<String, Object> parameters, HttpServletRequest request) {

		String content = ResourceUtils.getContentFromUrl(request, location, true);

		// If the content of the asset contains at least one relative path, the
		// content must be processed
		if (asset.getType().equals(AssetType.css) && content.contains("..")) {
			content = processRelativePaths(content, location);
		}

		return content;
	}

	/**
	 * <p>
	 * Processes all relative paths in the given content, line by line and
	 * replace it with an absolute path.
	 * <p>
	 * The given url modified according to the number of occurrences of ".."
	 * counted in the line.
	 * 
	 * <p>
	 * For example, if the CSS file is loaded from:
	 * 
	 * <pre>
	 * http://cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/css/jquery.dataTables.css
	 * </pre>
	 * 
	 * and if it contains:
	 * 
	 * <pre>
	 * .paginate_enabled_previous { background: url('../images/back_enabled.png') no-repeat top left; }
	 * </pre>
	 * 
	 * The line will be replaced by:
	 * 
	 * <pre>
	 * .paginate_enabled_previous { background: url('http://cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/images/back_enabled.png') no-repeat top left; }
	 * </pre>
	 * 
	 * @param content
	 *            The content of the asset to process.
	 * @param url
	 *            The original URL that will be used to build the absolute path.
	 * @return the processed content that should contain only absolute paths.
	 */
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
						String tmp2 = null;
						int c = lvl + 1;
						for (int i = url.length() - 1; i >= 0; i--) {
							if (c > 0 && url.charAt(i) == '/') {
								tmp2 = url.substring(0, i + 1);
								c--;
							}

							if (c == 0) {
								break;
							}
						}
						line = line.replaceAll("(\\.\\./)+", tmp2);
					}
				}
				writer.write(line + '\n');
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				if (reader != null)
					reader.close();
			}
			catch (IOException e) {
				//
			}
			try {
				if (writer != null)
					writer.close();
			}
			catch (IOException e) {
				//
			}
		}

		return sw.getBuffer().toString();
	}
}
