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
package com.github.dandelion.core.asset.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDOMPosition;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.html.HtmlTag;

/**
 * <p>
 * Dandelion filter used to inject web resources at the right positions in the
 * HTML, depending on the content of the {@link AssetStack}.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class AssetFilter implements Filter {

	// Logger
	private static Logger LOG = LoggerFactory.getLogger(AssetFilter.class);

	public static final String DANDELION_ASSET_FILTER_STATE = "dandelionAssetFilterState";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("initialize the Dandelion AssetFilter");
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse serlvetResponse, FilterChain filterChain)
			throws IOException, ServletException {

		// Only filter HTTP requests
		if (!(servletRequest instanceof HttpServletRequest)) {
			LOG.warn("The AssetFilter only applies to HTTP requests");
			filterChain.doFilter(servletRequest, serlvetResponse);
			return;
		}

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) serlvetResponse;

		// Only filter requests that accept HTML
		if (isFilterApplyable(request)) {
			LOG.trace("The AssetFilter applies to the request {}", request.getRequestURL().toString());

			AssetFilterResponseWrapper wrapper = new AssetFilterResponseWrapper(response);
			filterChain.doFilter(request, wrapper);

			String html = wrapper.getWrappedContent();
			AssetRequestContext context = AssetRequestContext.get(request);

			if (isDandelionApplyable(context, wrapper)) {
				LOG.debug("Dandelion Assets Generation apply on this request {}", request.getRequestURL().toString());

				List<Asset> assets = AssetStack.prepareAssetsFor(request, context.getScopes(true),
						context.getExcludedAssets());

				html = generateHeadAssets(assets, html);
				html = generateBodyAssets(assets, html);

				// The Content-Length header is deliberately not updated here
				// because it causes issues with Thymeleaf
			}

			response.getWriter().println(html);
			response.getWriter().close();
		}
		// All other requests are not filtered
		else {
			LOG.trace(
					"The AssetFilter only applies to the content type 'text/html', whereas the request {} has a content type '{}'",
					request.getRequestURL().toString(), request.getContentType());
			filterChain.doFilter(request, response);
		}
	}

	private boolean isFilterApplyable(HttpServletRequest request) {

		boolean applyFilter = false;

		// First check the request headers to see if the content is of type HTML
		if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").contains("text/html")) {
			applyFilter = true;
		}
		else if (request.getHeader("Accept") != null && request.getHeader("Accept").contains("text/html")) {
			applyFilter = true;
		}

		// Then, check whether the filter has been explicitely disabled
		// (possibly by other components)
		if (request.getAttribute(DANDELION_ASSET_FILTER_STATE) != null) {
			applyFilter = applyFilter
					&& Boolean.parseBoolean(String.valueOf(request.getAttribute(DANDELION_ASSET_FILTER_STATE)));
			return applyFilter;
		}
		else if (request.getParameter(DANDELION_ASSET_FILTER_STATE) != null) {
			applyFilter = applyFilter && Boolean.parseBoolean(request.getParameter(DANDELION_ASSET_FILTER_STATE));
			return applyFilter;
		}

		return applyFilter;
	}

	/**
	 * Only update the response if:
	 * <ul>
	 * <li>the response to process is of type HTML (based on the content type)</li>
	 * <li>the asset stack contains at least one asset</li>
	 * </ul>
	 * 
	 * @param context
	 *            The asset request context used for the current HTTP request.
	 * @param wrapper
	 *            The wrapper around the response to generate.
	 * @return true if the response can be updated.
	 */
	private boolean isDandelionApplyable(AssetRequestContext context, AssetFilterResponseWrapper wrapper) {
		if (wrapper.getContentType() == null || !wrapper.getContentType().contains("text/html")) {
			return false;
		}
		else if (!AssetStack.existsAssetsFor(context.getScopes(false), context.getExcludedAssets())) {
			return false;
		}
		return true;
	}

	private String generateHeadAssets(List<Asset> assets, String html) {
		List<Asset> assetsHead = AssetStack.filterByDOMPosition(assets, AssetDOMPosition.head);
		if (!assetsHead.isEmpty()) {
			StringBuilder htmlHead = new StringBuilder();
			for (AssetType type : AssetType.values()) {
				for (Asset assetHead : AssetStack.filterByType(assetsHead, type)) {
					for (String location : assetHead.getLocations().values()) {
						HtmlTag tag = HtmlUtil.transformAsset(assetHead, location);
						htmlHead.append(tag.toHtml());
						htmlHead.append("\n");
					}
				}
			}
			html = html.replace("</head>", htmlHead + "</head>");
		}
		return html;
	}

	private String generateBodyAssets(List<Asset> assets, String html) {
		List<Asset> assetsBody = AssetStack.filterByDOMPosition(assets, AssetDOMPosition.body);
		if (!assetsBody.isEmpty()) {
			StringBuilder htmlBody = new StringBuilder();
			for (AssetType type : AssetType.values()) {
				for (Asset assetBody : AssetStack.filterByType(assetsBody, type)) {
					for (String location : assetBody.getLocations().values()) {
						HtmlTag tag = HtmlUtil.transformAsset(assetBody, location);
						htmlBody.append(tag.toHtml());
						htmlBody.append("\n");
					}
				}
			}
			html = html.replace("</body>", htmlBody + "</body>");
		}
		return html;
	}

	@Override
	public void destroy() {
		// Nothing to do here
	}
}
