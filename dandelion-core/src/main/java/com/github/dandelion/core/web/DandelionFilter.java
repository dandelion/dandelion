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
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.web.handler.RequestHandler;
import com.github.dandelion.core.web.handler.RequestHandlerContext;

/**
 * <p>
 * Main Dandelion filter that serves several purposes:
 * </p>
 * <ul>
 * <li>It initializes the Dandelion {@link Context}, containing the bundle
 * graph, the active {@link AssetProcessor}s, and all other configurations. The
 * {@link Context} is then injected into all requests.</li>
 * <li>It can intercept some query parameters either to display a debug page or
 * to interact with the bundle graph</li>
 * <li>It reads the {@link AssetRequestContext}, holding all information about
 * requested bundles/assets and then modify the HTML response by injecting all
 * requested assets in their configured location.</li>
 * <li>TODO gzip</li>
 * </ul>
 * 
 * <p>
 * If the URLs pointing to your views always follow the same pattern, you can
 * restrict the scope of application of this filter using a custom mapping,
 * otherwise it is recommended to use <code>/*</code>.
 * </p>
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

	private static final Logger LOG = LoggerFactory.getLogger(DandelionFilter.class);

	/**
	 * The Dandelion context.
	 */
	private Context context;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.info("Initializing the Dandelion context");
		context = new Context(filterConfig);
		LOG.info("Dandelion context initialized");
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		// Only filter HTTP requests
		if (!(servletRequest instanceof HttpServletRequest)) {
			LOG.warn("The DandelionFilter only applies to HTTP requests");
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		// Make the Dandelion context available through a request attribute for
		// potential use by end-users or components
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);

		// Pre-filtering handlers processing
		RequestHandlerContext preHandlerContext = new RequestHandlerContext(context, request, response);
		for (RequestHandler preHandler : context.getPreHandlers()) {
			LOG.debug("Rank {}, {}", preHandler.getRank(), preHandler.getClass().getSimpleName());
			if (preHandler.isApplicable(preHandlerContext)) {
				preHandler.handle(preHandlerContext, null);
			}
		}

		// Wraps the response before applying the filter chain
		ByteArrayResponseWrapper wrappedResponse = new ByteArrayResponseWrapper(response);
		filterChain.doFilter(request, wrappedResponse);

		// Extracts the response as a byte array
		byte[] finalResponse = wrappedResponse.toByteArray();

		// Post-filtering handlers processing
		RequestHandlerContext postHandlerContext = new RequestHandlerContext(context, request, wrappedResponse);
		for (RequestHandler postHandler : context.getPostHandlers()) {
			boolean isHandlerApplicable = postHandler.isApplicable(postHandlerContext);
			LOG.debug("({}/{}) {} (applicable: {})", postHandler.getRank(), context.getPostHandlers().size(),
					postHandler.getClass().getSimpleName(), isHandlerApplicable);
			
			if (postHandler.isApplicable(postHandlerContext) && finalResponse != null) {
				finalResponse = postHandler.handle(postHandlerContext, finalResponse);
			}
		}

		// The response may have been set to null by one of the handlers
		if (finalResponse == null) {
			return;
		}

		response.getOutputStream().write(finalResponse);
	}

	@Override
	public void destroy() {
		context.destroy();
	}

}