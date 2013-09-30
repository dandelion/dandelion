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
import com.github.dandelion.core.html.LinkTag;
import com.github.dandelion.core.html.ScriptTag;

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

		CharArrayWriter caw = new CharArrayWriter();
		String html = wrapper.toString();
		
		AssetsRequestContext context = AssetsRequestContext.get(request);
		List<Asset> assets = AssetStack.prepareAssetsFor(request, context.getScopes(true), new String[]{});
		List<Asset> assetsHead = AssetStack.filterByDOMPosition(assets, AssetDOMPosition.head);
		List<Asset> assetsBody = AssetStack.filterByDOMPosition(assets, AssetDOMPosition.body);
		
		StringBuffer htmlHead = new StringBuffer();
		StringBuffer htmlBody = new StringBuffer();
		
		for(Asset assetHead : assetsHead){
			for(String location: assetHead.getLocations().values()) {
				htmlHead.append(new LinkTag(location).toHtml());
			}
		}
		
		for(Asset assetBody : assetsBody){
			for(String location: assetBody.getLocations().values()) {
				htmlBody.append(new ScriptTag(location, assetBody.isAsync(), assetBody.isDeferred()).toHtml());
			}
		}
	
		if (wrapper.getContentType() != null && wrapper.getContentType().indexOf("text/html") != -1) {
			
			// Process CSS assets
			html = html.replace("</head>", htmlHead + "</head>");
			
			// Process CSS assets
			html = html.replace("</body>", htmlBody + "</body>");
			
			caw.write(html);
			response.setContentLength(caw.toString().length());
			out.write(caw.toString());
		} else {
			out.write(wrapper.toString());
		}
		out.close();
	}

	@Override
	public void destroy() {
	}
}
