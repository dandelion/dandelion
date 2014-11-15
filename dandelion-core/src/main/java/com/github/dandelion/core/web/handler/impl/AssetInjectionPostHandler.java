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
package com.github.dandelion.core.web.handler.impl;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.html.AbstractHtmlTag;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.utils.HtmlUtils;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.AbstractRequestHandler;
import com.github.dandelion.core.web.handler.RequestHandlerContext;

/**
 * <p>
 * Post-filtering request handler in charge of:
 * </p>
 * <ul>
 * <li>querying the {@link BundleStorage} on the lookout for included bundles or
 * assets</li>
 * <li>injecting the assets found in the {@link HttpServletResponse}</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public class AssetInjectionPostHandler extends AbstractRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DebuggerPostHandler.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	public boolean isAfterChaining() {
		return true;
	}

	@Override
	public boolean isApplicable(RequestHandlerContext context) {

		HttpServletRequest request = context.getRequest();
		HttpServletResponse wrappedResponse = context.getResponse();
		boolean retval = true;

		// Not applicable in non-HTML resources
		if (wrappedResponse.getContentType() == null || !wrappedResponse.getContentType().contains("text/html")) {
			retval = false;
		}

		// Also check whether the asset injection has been explicitely disabled
		// (possibly by other components) either from a request attribute...
		if (request.getAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE) != null) {
			retval = retval
					&& Boolean.parseBoolean(String.valueOf(request
							.getAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE)));

			if (!retval) {
				LOG.debug("Asset injection explicitely disabled by the {} attribute for the request '{}'",
						WebConstants.DANDELION_ASSET_FILTER_STATE, request.getRequestURI());
			}
		}
		// ... or from a request parameter
		else if (request.getParameter(WebConstants.DANDELION_ASSET_FILTER_STATE) != null) {

			retval = retval
					&& Boolean.parseBoolean(String.valueOf(request
							.getParameter(WebConstants.DANDELION_ASSET_FILTER_STATE)));

			if (!retval) {
				LOG.debug("Asset injection explicitely disabled by the {} parameter for the request '{}'",
						WebConstants.DANDELION_ASSET_FILTER_STATE, request.getRequestURI());
			}
		}

		return retval;
	}

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	public byte[] handle(RequestHandlerContext context, byte[] response) {

		// Convert the response to a String in order to perform easier
		// replacements
		String html = new String(response);

		// Get all assets to be injected in the <head> section
		Set<Asset> assetsHead = new AssetQuery(context.getRequest(), context.getContext()).atPosition(
				AssetDomPosition.head).perform();

		if (!assetsHead.isEmpty()) {
			StringBuilder htmlHead = new StringBuilder();
			for (Asset asset : assetsHead) {
				AbstractHtmlTag tag = HtmlUtils.transformAsset(asset);
				htmlHead.append(tag.toHtml());
				htmlHead.append('\n');
			}

			html = html.replace("</head>", htmlHead + "\n</head>");
		}

		// Get all assets to be injected in the <body> section
		Set<Asset> assetsBody = new AssetQuery(context.getRequest(), context.getContext()).atPosition(
				AssetDomPosition.body).perform();

		if (!assetsBody.isEmpty()) {
			StringBuilder htmlBody = new StringBuilder();
			for (Asset asset : assetsBody) {
				AbstractHtmlTag tag = HtmlUtils.transformAsset(asset);
				htmlBody.append(tag.toHtml());
				htmlBody.append('\n');
			}
			html = html.replace("</body>", htmlBody + "</body>");
		}

		// Once all requested assets injected, convert back to a byte array to
		// let other handler do their work
		String configuredEncoding = context.getContext().getConfiguration().getEncoding();
		byte[] updatedResponse;
		try {
			context.getResponse().setContentLength(html.getBytes(configuredEncoding).length);
			updatedResponse = html.getBytes(configuredEncoding);
		}
		catch (UnsupportedEncodingException e) {
			throw new DandelionException("Unable to encode the HTML page using the '" + configuredEncoding
					+ "', which doesn't seem to be supported", e);
		}

		return updatedResponse;
	}
}