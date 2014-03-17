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
package com.github.dandelion.core.monitoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDOMPosition;
import com.github.dandelion.core.asset.Assets;
import com.github.dandelion.core.asset.web.AssetFilter;
import com.github.dandelion.core.asset.web.AssetFilterResponseWrapper;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.utils.UrlUtils;

/**
 * <p>
 * Build the page allowing to visualize the asset graphs, both for the current
 * request and the whole application.
 * 
 * <p>
 * Once built, the page is directly written in the {@link HttpServletResponse}.
 * 
 * <p>
 * This development tool is only accessible when the {@link DevMode} is enabled.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class GraphViewer {

	private static ObjectMapper mapper;

	public GraphViewer() {
		mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
	}

	public String getView(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		AssetFilterResponseWrapper wrapper = new AssetFilterResponseWrapper(response);
		filterChain.doFilter(request, wrapper);

		StringBuilder sbNodesRequest = new StringBuilder();
		StringBuilder sbNodesApplication = new StringBuilder();
		StringBuilder sbHead = new StringBuilder();
		StringBuilder sbBody = new StringBuilder();

		String graphView = ResourceUtils.getContentFromInputStream(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("dandelion/internal/graphViewer/graphViewer.html"));

		Set<Asset> assetsHead = Assets.assetsFor(request, AssetDOMPosition.head, true, false);
		Iterator<Asset> iteratorAssetHead = assetsHead.iterator();
		while (iteratorAssetHead.hasNext()) {
			sbHead.append("    &lt;link href=\"" + iteratorAssetHead.next().getFinalLocation() + "\" />");
			if (iteratorAssetHead.hasNext()) {
				sbHead.append('\n');
			}
		}

		Set<Asset> assetsBody = Assets.assetsFor(request, AssetDOMPosition.body, true, false);
		Iterator<Asset> iteratorAssetBody = assetsBody.iterator();
		while (iteratorAssetBody.hasNext()) {
			sbBody.append("    &lt;script src=\"" + iteratorAssetBody.next().getFinalLocation() + "\"></script>");
			if (iteratorAssetBody.hasNext()) {
				sbBody.append('\n');
			}
		}

		// Request nodes
		Set<BundleStorageUnit> bsuRequest = Assets.bundlesFor(request);

		for (BundleStorageUnit bsu : bsuRequest) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("label", bsu.getName());
			map.put("assets", convertToD3Assets(Assets.assetsFor(request, bsu.getName(), true, false)));
			String bundle = mapper.writeValueAsString(map);
			sbNodesRequest.append("requestGraph.addNode('" + bsu.getName() + "'," + bundle + ");").append('\n');
		}

		Set<String> edgesRequest = new HashSet<String>();
		for (BundleStorageUnit bsu : bsuRequest) {
			if (bsu.getChildren() != null && !bsu.getChildren().isEmpty()) {
				for (BundleStorageUnit childBsu : bsu.getChildren()) {
					edgesRequest.add("requestGraph.addEdge(null, '" + bsu.getName() + "', '" + childBsu.getName()
							+ "', { label: \"depends on\" });");
				}
			}
		}
		for (String edge : edgesRequest) {
			sbNodesRequest.append(edge).append('\n');
		}

		// Application nodes
		List<BundleStorageUnit> allBundles = Assets.configurator().getStorage().getBundleDag().getVerticies();
		for (BundleStorageUnit bsu : allBundles) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("label", bsu.getName());
			map.put("assets", convertToD3Assets(Assets.assetsFor(request, bsu.getName(), true, false)));
			String bundle = mapper.writeValueAsString(map);
			sbNodesApplication.append("applicationGraph.addNode('" + bsu.getName() + "'," + bundle + ");").append('\n');
		}

		Set<String> edgesApplication = new HashSet<String>();
		for (BundleStorageUnit bsu : allBundles) {
			if (bsu.getChildren() != null && !bsu.getChildren().isEmpty()) {
				for (BundleStorageUnit childBsu : bsu.getChildren()) {
					edgesApplication.add("applicationGraph.addEdge(null, '" + bsu.getName() + "', '"
							+ childBsu.getName() + "', { label: \"depends on\" });");
				}
			}
		}
		for (String edge : edgesApplication) {
			sbNodesApplication.append(edge).append('\n');
		}

		// Apply replacements
		String currentUri = UrlUtils.getCurrentUri(request).toString();

		graphView = graphView.replace("[NODES_REQUEST]", sbNodesRequest.toString());
		graphView = graphView.replace("[NODES_APPLICATION]", sbNodesApplication.toString());
		graphView = graphView.replace("[CURRENT_URL]",
				currentUri.substring(0, currentUri.indexOf(AssetFilter.DANDELION_SHOW_GRAPH) - 1));
		graphView = graphView.replace("[HEAD]", sbHead.toString());
		graphView = graphView.replace("[BODY]", sbBody.toString());

		return graphView;
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
			this.location = a.getConfigLocationKey();
			this.url = a.getFinalLocation();
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