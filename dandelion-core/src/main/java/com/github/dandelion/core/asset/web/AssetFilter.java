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
import java.util.Set;

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
import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.html.HtmlTag;
import com.github.dandelion.core.monitoring.GraphViewer;
import com.github.dandelion.core.utils.HtmlUtils;

/**
 * <p>
 * TODO
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class AssetFilter extends AbstractDandelionFilter {

	private static Logger LOG = LoggerFactory.getLogger(AssetFilter.class);

	public static final String DANDELION_CONTEXT_ATTRIBUTE = "dandelion_context";
	private Context context;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("Initializing the Dandelion configuration");
		context = new Context(filterConfig);
		LOG.info("Dandelion configuration initialized");
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

		request.setAttribute(DANDELION_CONTEXT_ATTRIBUTE, context);
		// Override the response with the graph viewer
		if(DevMode.isEnabled() && request.getParameter(DANDELION_SHOW_GRAPH) != null){
			GraphViewer graphViewer = new GraphViewer(context);
			response.getWriter().println(graphViewer.getView(request, response, filterChain));
			return;
		}
		
		// Only filter requests that accept HTML
		if (isFilterApplyable(request)) {
			LOG.trace("The AssetFilter applies to the request {}", request.getRequestURL().toString());

			AssetFilterResponseWrapper wrapper = new AssetFilterResponseWrapper(response);
			filterChain.doFilter(request, wrapper);
			
			String html = wrapper.getWrappedContent();
			AssetRequestContext arc = AssetRequestContext.get(request);

			if (isDandelionApplyable(request, arc, wrapper)) {
				
				Set<Asset> assetsHead = new AssetQuery(request, context)
					.withPosition(AssetDomPosition.head)
					.perform();
				
//				Set<Asset> assetsHead = Assets.assetsFor(request, AssetDomPosition.head);
				if (!assetsHead.isEmpty()) {
					StringBuilder htmlHead = new StringBuilder();
					for (Asset asset : assetsHead) {
						HtmlTag tag = HtmlUtils.transformAsset(asset);
						htmlHead.append(tag.toHtml());
						htmlHead.append("\n");
					}
					
					html = html.replace("</head>", htmlHead + "\n</head>");
				}
				
				Set<Asset> assetsBody = new AssetQuery(request,context)
				.withPosition(AssetDomPosition.body)
				.perform();
				
//				Set<Asset> assetsBody = Assets.assetsFor(request, AssetDomPosition.body);
				if (!assetsBody.isEmpty()) {
					StringBuilder htmlBody = new StringBuilder();
					for (Asset asset : assetsBody) {
						HtmlTag tag = HtmlUtils.transformAsset(asset);
						htmlBody.append(tag.toHtml());
						htmlBody.append("\n");
					}
					html = html.replace("</body>", htmlBody + "</body>");
				}
				
				// The Content-Length header is deliberately not updated here
				// because it causes issues with Thymeleaf
			}

			response.getWriter().println(html);
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
	private boolean isDandelionApplyable(HttpServletRequest request, AssetRequestContext context, AssetFilterResponseWrapper wrapper) {
		if (wrapper.getContentType() == null || !wrapper.getContentType().contains("text/html")) {
			return false;
		}
//		else if (!Assets.existsAssetsFor(request)) {
//			return false;
//		}
		return true;
	}

	@Override
	public void destroy() {
		// Nothing to do here
	}
}