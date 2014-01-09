package com.github.dandelion.core.asset.web;

import java.io.IOException;
import java.util.List;

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

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDOMPosition;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.html.HtmlTag;

/**
 * <p>
 * Dandelion filter used to inject web resources at the right positions in the
 * HTML, depending on the content of the asset stack.
 * 
 * @since 0.3.0
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
            LOG.debug("AssetFilter apply only on HTTP request");
			filterChain.doFilter(servletRequest, serlvetResponse);
			return;
		}

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) serlvetResponse;

		// Only filter requests that accept HTML
        if (isFilterApplyable(request)) {
            LOG.debug("AssetFilter apply on this request {}", request.getRequestURL().toString());

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

				// FIXME Break the page loading when using Thymeleaf
                // Update the content length to new value
//                response.setIntHeader("Content-Length", html.getBytes().length);
			}

            response.getWriter().println(html);
            response.getWriter().close();
		}
		// All other requests are not filtered
		else {
            LOG.debug("AssetFilter apply only on content type 'text/html' on this request {}", request.getRequestURL().toString());
			filterChain.doFilter(request, response);
		}
	}

    private boolean isFilterApplyable(HttpServletRequest request) {
    	
    	boolean applyFilter = false;
    	
    	// First check the request headers to see if the content is of type HTML
    	if(request.getHeader("Content-Type") != null && request.getHeader("Content-Type").contains("text/html")) {
    		applyFilter = true;
    	}
    	else if(request.getHeader("Accept") != null && request.getHeader("Accept").contains("text/html")) {
    		applyFilter = true;
    	}
    	
		// Then, check whether the filter has been explicitely disabled
		// (possibly by other components)
    	if(request.getAttribute(DANDELION_ASSET_FILTER_STATE) != null){
			applyFilter = applyFilter && Boolean.parseBoolean(String.valueOf(request.getAttribute(DANDELION_ASSET_FILTER_STATE)));
			return applyFilter;
		}
    	else if(request.getParameter(DANDELION_ASSET_FILTER_STATE) != null) {
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
        } else if (!AssetStack.existsAssetsFor(context.getScopes(false), context.getExcludedAssets())) {
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