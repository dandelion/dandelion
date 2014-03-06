/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDOMPosition;
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.processor.AssetProcessorSystem;
import com.github.dandelion.core.bundle.Bundle;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class GraphViewer {

	private static ObjectMapper mapper = new ObjectMapper();

	public void displayCurrentGrapth(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

		AssetFilterResponseWrapper wrapper = new AssetFilterResponseWrapper(response);
		filterChain.doFilter(request, wrapper);

		StringBuilder sbNodes = new StringBuilder();
		StringBuilder sbHead = new StringBuilder();
		StringBuilder sbBody = new StringBuilder();

		String debugPage = ResourceUtils.getContentFromInputStream(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("dandelion/internal/graphViewer/graphViewer.html"));

		Set<Asset> assetsHead = Assets.assetsFor(request, AssetDOMPosition.head);
		Iterator<Asset> iteratorAssetHead = assetsHead.iterator();
		while (iteratorAssetHead.hasNext()) {
			sbHead.append("    &lt;link href=\"" + iteratorAssetHead.next().getLocation() + "\" />");
			if (iteratorAssetHead.hasNext()) {
				sbHead.append('\n');
			}
		}

		Set<Asset> assetsBody = Assets.assetsFor(request, AssetDOMPosition.body);
		Iterator<Asset> iteratorAssetBody = assetsBody.iterator();
		while (iteratorAssetBody.hasNext()) {
			sbBody.append("    &lt;script src=\"" + iteratorAssetBody.next().getLocation() + "\"></script>");
			if (iteratorAssetBody.hasNext()) {
				sbBody.append('\n');
			}
		}

		Set<Bundle> allBundles = Assets.bundlesFor(request);

		for (Bundle b : allBundles) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("label", b.getName());
			Set<Asset> set = new LinkedHashSet<Asset>();
			if(b.getAssets() != null && !b.getAssets().isEmpty()){
				set.addAll(b.getAssets());
				map.put("assets", convertToD3Assets(AssetProcessorSystem.process(set, request)));
				String bundle = mapper.writeValueAsString(map);
				sbNodes.append("g.addNode('" + b.getName() + "'," + bundle + ");").append('\n');
			}
		}

		Set<String> edges = new HashSet<String>();
		for (Bundle b : allBundles) {
			if (b.getChildren() != null && !b.getChildren().isEmpty()) {
				for (Bundle childBundle : b.getChildren()) {
					edges.add("g.addEdge(null, '" + b.getName() + "', '" + childBundle.getName()
							+ "', { label: \"depends on\" });");
				}
			}
		}
		for (String edge : edges) {
			sbNodes.append(edge).append('\n');
		}
		// Apply replacements
		String currentUri = UrlUtils.getCurrentUri(request).toString();

		debugPage = debugPage.replace("[NODES]", sbNodes.toString());
		debugPage = debugPage.replace("[CURRENT_URL]",
				currentUri.substring(0, currentUri.indexOf(AssetFilter.DANDELION_SHOW_GRAPH) - 1));
		debugPage = debugPage.replace("[HEAD]", sbHead.toString());
		debugPage = debugPage.replace("[BODY]", sbBody.toString());

		response.getWriter().println(debugPage);
		response.getWriter().close();
	}

	public List<D3Asset> convertToD3Assets(Set<Asset> assets) {
		List<D3Asset> d3Assets = new ArrayList<GraphViewer.D3Asset>();
		for (Asset a : assets) {
			d3Assets.add(new D3Asset(a));
		}
		return d3Assets;
	}
	
	public class D3Asset {
		private String url;
		private String name;
		private String version;
		private String type;
		private String location;

		public D3Asset(Asset a) {
			this.name = a.getName();
			this.version = a.getVersion();
			this.type = a.getType().toString();
			for (Entry<String, String> locationEntry : a.getLocations().entrySet()) {
				this.location = locationEntry.getKey();
				this.url = locationEntry.getValue();
			}
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}
	}
}