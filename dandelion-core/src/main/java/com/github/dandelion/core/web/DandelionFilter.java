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
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.html.HtmlTag;
import com.github.dandelion.core.monitoring.GraphViewer;
import com.github.dandelion.core.utils.HtmlUtils;

/**
 * <p>
 * Main Dandelion filter that serves several purposes:
 * <ul>
 * <li>It initializes the Dandelion {@link Context}, containing the bundle
 * graph, the active {@link AssetProcessor}s, and all other configurations. The
 * {@link Context} is then injected into all requests.</li>
 * <li>It can intercept some query parameters either to display a debug page or
 * to interact with the bundle graph</li>
 * <li>It reads the {@link AssetRequestContext}, holding all information about
 * requested bundles/assets and then modify the HTML response by injecting all
 * requested assets in their configured location.</li>
 * </ul>
 * 
 * <p>
 * If the URLs pointing to your views always follow the same pattern, you can
 * restrict the scope of application of this filter using a custom mapping,
 * otherwise it is recommended to use <code>/*</code>.
 * 
 * <pre>
 * &lt;!-- Dandelion filter definition and mapping --&gt;
 * &lt;filter&gt;
 *    &lt;filter-name&gt;dandelionFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;com.github.dandelion.core.web.DandelionFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;dandelionFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </pre>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class DandelionFilter implements Filter {

	private static Logger LOG = LoggerFactory.getLogger(DandelionFilter.class);

	/**
	 * The Dandelion context.
	 */
	private Context context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("Initializing the Dandelion context");
		context = new Context(filterConfig);
		LOG.info("Dandelion context initialized");
	}

	/**
	 * {@inheritDoc}
	 */
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

		// Bundle reloading (development mode only)
		if (context.isDevModeEnabled() && request.getParameter(WebConstants.DANDELION_RELOAD_BUNDLES) != null) {
			LOG.info("Bundle reloading requested via request parameter");
			context.initBundleStorage();
			LOG.info("Bundle reloaded");
		}

		// Wraps the response before applying the filter chain
		ByteArrayResponseWrapper wrappedResponse = new ByteArrayResponseWrapper(response);
		filterChain.doFilter(request, wrappedResponse);

		// Bundle graph viewer display (development mode only)
		if (context.isDevModeEnabled() && request.getParameter(WebConstants.DANDELION_SHOW_GRAPH) != null) {
			GraphViewer graphViewer = new GraphViewer(context);
			response.getWriter().print(graphViewer.getView(request, response, filterChain));
			return;
		}

		byte[] bytes = wrappedResponse.toByteArray();

		if (isRelevant(request, wrappedResponse)) {

			String html = new String(bytes);

			Set<Asset> assetsHead = new AssetQuery(request, context).withPosition(AssetDomPosition.head).perform();

			if (!assetsHead.isEmpty()) {
				StringBuilder htmlHead = new StringBuilder();
				for (Asset asset : assetsHead) {
					HtmlTag tag = HtmlUtils.transformAsset(asset);
					htmlHead.append(tag.toHtml());
					htmlHead.append('\n');
				}

				html = html.replace("</head>", htmlHead + "\n</head>");
			}

			Set<Asset> assetsBody = new AssetQuery(request, context).withPosition(AssetDomPosition.body).perform();

			if (!assetsBody.isEmpty()) {
				StringBuilder htmlBody = new StringBuilder();
				for (Asset asset : assetsBody) {
					HtmlTag tag = HtmlUtils.transformAsset(asset);
					htmlBody.append(tag.toHtml());
					htmlBody.append('\n');
				}
				html = html.replace("</body>", htmlBody + "</body>");
			}

			// Modified HTML written to the response
			response.getWriter().print(html);
		}
		else {
			// The response is left untouched
			response.getOutputStream().write(bytes);
		}
	}

	/**
	 * <p>
	 * Checks whether the asset injection should occur or not.
	 * <p>
	 * For now, this filter is supposed to process only response with a
	 * <code>text/html</code> content type (in upcoming versions, this filter
	 * will be used for more than that).
	 * 
	 * @param request
	 *            The current {@link HttpServletRequest}.
	 * @param wrappedResponse
	 *            The response wrapper.
	 * @return {@code true} if asset injection can be performed, {@code false}
	 *         otherwise.
	 */
	public boolean isRelevant(HttpServletRequest request, ByteArrayResponseWrapper wrappedResponse) {
		boolean dandelionFilterApplyable = true;

		if (wrappedResponse.getContentType() == null || !wrappedResponse.getContentType().contains("text/html")) {
			dandelionFilterApplyable = false;
		}

		// Then, check whether the filter has been explicitely disabled
		// (possibly by other components) either from a request attribute...
		if (request.getAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE) != null) {
			dandelionFilterApplyable = dandelionFilterApplyable
					&& Boolean.parseBoolean(String.valueOf(request
							.getAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE)));

			if (!dandelionFilterApplyable) {
				LOG.debug("DandelionFilter explicitely disabled by the {} attribute for the request '{}'",
						WebConstants.DANDELION_ASSET_FILTER_STATE, request.getRequestURI());
			}
		}
		// ... or from a request parameter
		else if (request.getParameter(WebConstants.DANDELION_ASSET_FILTER_STATE) != null) {

			dandelionFilterApplyable = dandelionFilterApplyable
					&& Boolean.parseBoolean(String.valueOf(request
							.getParameter(WebConstants.DANDELION_ASSET_FILTER_STATE)));

			if (!dandelionFilterApplyable) {
				LOG.debug("DandelionFilter explicitely disabled by the {} parameter for the request '{}'",
						WebConstants.DANDELION_ASSET_FILTER_STATE, request.getRequestURI());
			}
		}

		return dandelionFilterApplyable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		context.destroy();
	}
}