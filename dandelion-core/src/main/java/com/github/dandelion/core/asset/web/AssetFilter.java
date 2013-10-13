package com.github.dandelion.core.asset.web;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDOMPosition;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.html.LinkTag;
import com.github.dandelion.core.html.ScriptTag;

/**
 * Filter used to inject web resources at the right positions, depending on what
 * the asset stack contains.
 * 
 * @since 0.3.0
 */
@WebFilter(filterName = "AssetFilter", value = { "/*" })
public class AssetFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public void doFilter(ServletRequest servletRequest, ServletResponse serlvetResponse, FilterChain filterChain) throws IOException,
			ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) serlvetResponse;

		PrintWriter out = response.getWriter();

		// Wrap the response to modify it later
		CharResponseWrapper wrapper = new CharResponseWrapper(response);
		filterChain.doFilter(request, wrapper);

        if(!generateHtmlAssets(request, response, out, wrapper)) {
            out.write(wrapper.toString());
        }

		out.close();
	}

    private boolean generateHtmlAssets(HttpServletRequest request, HttpServletResponse response, PrintWriter out, CharResponseWrapper wrapper) throws IOException {
        // not compatible with Assets generation
        if (wrapper.getContentType() == null || !wrapper.getContentType().contains("text/html")) {
            return false;
        }

        // get assets for generation
        AssetsRequestContext context = AssetsRequestContext.get(request);
        List<Asset> assets = AssetStack.prepareAssetsFor(request, context.getScopes(true), context.getExcludedAssets());
        if(assets.isEmpty()) {
            return false;
        }

        // generation
        String html = wrapper.toString();
        html = generateHeadAssets(assets, html);
        html = generateBodyAssets(assets, html);
        printHtml(response, out, html);
        return true;
    }

    private String generateHeadAssets(List<Asset> assets, String html) {
        List<Asset> assetsHead = AssetStack.filterByDOMPosition(assets, AssetDOMPosition.head);
        if(!assetsHead.isEmpty()) {
            StringBuilder htmlHead = new StringBuilder();
            for(AssetType type:AssetType.values()) {
                for(Asset assetHead : AssetStack.filterByType(assetsHead, type)){
                    for(String location: assetHead.getLocations().values()) {
                        htmlHead.append(new LinkTag(location).toHtml());
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
        if(!assetsBody.isEmpty()) {
            StringBuilder htmlBody = new StringBuilder();
            for(AssetType type:AssetType.values()) {
                for(Asset assetBody : AssetStack.filterByType(assetsBody, type)) {
                    for(String location: assetBody.getLocations().values()) {
                        htmlBody.append(new ScriptTag(location, assetBody.isAsync(), assetBody.isDeferred()).toHtml());
                        htmlBody.append("\n");
                    }
                }
            }
            html = html.replace("</body>", htmlBody + "</body>");
        }
        return html;
    }

    private void printHtml(HttpServletResponse response, PrintWriter out, String html) throws IOException {
        CharArrayWriter caw = new CharArrayWriter();
        caw.write(html);
        response.setContentLength(caw.toString().length());
        out.write(caw.toString());
    }

    @Override
	public void destroy() {
	}
}
