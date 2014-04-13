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
package com.github.dandelion.core.web;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.html.HtmlTag;
import com.github.dandelion.core.monitoring.GraphViewer;
import com.github.dandelion.core.utils.HtmlUtils;

/**
 * <p>
 * Main Dandelion filter in charge of injecting {@link Asset}s into pages.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class DandelionFilter implements Filter {

	private static Logger LOG = LoggerFactory.getLogger(DandelionFilter.class);

	private Context context;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("Initializing the Dandelion context");
		context = new Context(filterConfig);
		LOG.info("Dandelion context initialized");
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
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);

		// Override the response with the graph viewer
		if (context.isDevModeEnabled() && request.getParameter(WebConstants.DANDELION_SHOW_GRAPH) != null) {
			GraphViewer graphViewer = new GraphViewer(context);
			response.getWriter().println(graphViewer.getView(request, response, filterChain));
			return;
		}

//		if(context.isDevModeEnabled()) {
//			init(filterConfig);
//		}
		
		// Only filter requests that accept HTML
		if (isFilterApplyable(request)) {
			LOG.trace("The AssetFilter applies to the request {}", request.getRequestURL().toString());

			DandelionFilterResponseWrapper wrapper = new DandelionFilterResponseWrapper(response);
			filterChain.doFilter(request, wrapper);

			String html = wrapper.getWrappedContent();
			AssetRequestContext arc = AssetRequestContext.get(request);

			if (isDandelionApplyable(request, arc, wrapper)) {

				Set<Asset> assetsHead = new AssetQuery(request, context).withPosition(AssetDomPosition.head).perform();

				if (!assetsHead.isEmpty()) {
					StringBuilder htmlHead = new StringBuilder();
					for (Asset asset : assetsHead) {
						HtmlTag tag = HtmlUtils.transformAsset(asset);
						htmlHead.append(tag.toHtml());
						htmlHead.append("\n");
					}

					html = html.replace("</head>", htmlHead + "\n</head>");
				}

				Set<Asset> assetsBody = new AssetQuery(request, context).withPosition(AssetDomPosition.body).perform();

				if (!assetsBody.isEmpty()) {
					StringBuilder htmlBody = new StringBuilder();
					for (Asset asset : assetsBody) {
						HtmlTag tag = HtmlUtils.transformAsset(asset);
						htmlBody.append(tag.toHtml());
						htmlBody.append("\n");
					}
					html = html.replace("</body>", htmlBody + "</body>");
				}
			}

			response.getWriter().println(html);

			// The response is explicitely closed here instead of setting a
			// Content-Length header
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
		if (request.getHeader(HttpHeader.CONTENT_TYPE.getName()) != null
				&& request.getHeader(HttpHeader.CONTENT_TYPE.getName()).contains("text/html")) {
			applyFilter = true;
		}
		else if (request.getHeader(HttpHeader.ACCEPT.getName()) != null
				&& request.getHeader(HttpHeader.ACCEPT.getName()).contains("text/html")) {
			applyFilter = true;
		}

		// Then, check whether the filter has been explicitely disabled
		// (possibly by other components) either from a request attribute...
		if (request.getAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE) != null) {
			applyFilter = applyFilter
					&& Boolean.parseBoolean(String.valueOf(request
							.getAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE)));
			return applyFilter;
		}
		// ... or from a request parameter
		else if (request.getParameter(WebConstants.DANDELION_ASSET_FILTER_STATE) != null) {
			applyFilter = applyFilter
					&& Boolean.parseBoolean(request.getParameter(WebConstants.DANDELION_ASSET_FILTER_STATE));
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
	private boolean isDandelionApplyable(HttpServletRequest request, AssetRequestContext context,
			DandelionFilterResponseWrapper wrapper) {
		if (wrapper.getContentType() == null || !wrapper.getContentType().contains("text/html")) {
			return false;
		}
		// else if (!Assets.existsAssetsFor(request)) {
		// return false;
		// }
		return true;
	}

	@Override
	public void destroy() {
		// Nothing to do here
	}
}