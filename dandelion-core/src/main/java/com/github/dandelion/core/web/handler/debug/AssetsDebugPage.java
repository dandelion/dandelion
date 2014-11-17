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
package com.github.dandelion.core.web.handler.debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetMapper;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.utils.ResourceUtils;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.handler.RequestHandlerContext;

/**
 * <p>
 * Debug page focused on assets.
 * </p>
 * <p>
 * This page displays all assets injected in the page corresponding to a
 * request. Assets are displayed in different ways: as a graph, as a table and
 * also how they are injected into the HTML page.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public class AssetsDebugPage extends AbstractDebugPage {

	private static ObjectMapper mapper;
	private AssetMapper assetMapper;

	static {
		mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
	}

	public AssetsDebugPage(RequestHandlerContext context) {
		super(context);
		this.assetMapper = new AssetMapper(context.getRequest(), context.getContext());
	}

	@Override
	public String getTemplate(RequestHandlerContext context) throws IOException {
		return ResourceUtils.getContentFromInputStream(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("META-INF/resources/ddl-debugger/html/assets.html"));
	}

	@Override
	protected Map<String, String> getCustomParameters(RequestHandlerContext context) {
		StringBuilder sbNodesRequest = new StringBuilder();

		HttpServletRequest request = context.getRequest();

		Map<String, String> params = new HashMap<String, String>();

		try {
			Set<BundleStorageUnit> bsuRequest = context.getContext().getBundleStorage()
					.bundlesFor(AssetRequestContext.get(request).getBundles(true));

			for (BundleStorageUnit bsu : bsuRequest) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("label", bsu.getName());
				map.put("assets", convertToD3Assets(bsu.getAssetStorageUnits()));
				map.put("shape", "ellipse");
				String bundle;
				bundle = mapper.writeValueAsString(map);

				sbNodesRequest.append("requestGraph.setNode('" + bsu.getName() + "'," + bundle + ");").append('\n');
			}

			Set<String> edgesRequest = new HashSet<String>();
			for (BundleStorageUnit bsu : bsuRequest) {
				if (bsu.getChildren() != null && !bsu.getChildren().isEmpty()) {
					for (BundleStorageUnit childBsu : bsu.getChildren()) {
						edgesRequest.add("requestGraph.setEdge('" + bsu.getName() + "', '" + childBsu.getName()
								+ "', { label: \"depends on\" });");
					}
				}
			}
			for (String edge : edgesRequest) {
				sbNodesRequest.append(edge).append('\n');
			}

			params.put("%NODES_REQUEST%", sbNodesRequest.toString());
		}
		catch (JsonProcessingException e) {
			throw new DandelionException("An error occurred when converting bundles to JSON", e);
		}

		Set<Asset> assets = new AssetQuery(context.getRequest(), context.getContext()).perform();

		StringBuilder table = new StringBuilder("<table class='table table-striped table-hover'><thead>");
		table.append("<tr><th>Bundle</th><th>Asset</th><th>Version</th><th>Location</th></tr></thead><tbody>");
		for (Asset asset : assets) {
			table.append(tr(asset.getBundle(), asset.getName(), asset.getVersion(), asset.getFinalLocation()));
		}
		table.append("</tbody></table>");

		params.put("%ASSETS%", table.toString());

		StringBuilder sbHead = new StringBuilder();
		StringBuilder sbBody = new StringBuilder();
		Set<Asset> assetsHead = new AssetQuery(context.getRequest(), context.getContext()).atPosition(
				AssetDomPosition.head).perform();
		Iterator<Asset> iteratorAssetHead = assetsHead.iterator();
		while (iteratorAssetHead.hasNext()) {
			sbHead.append("    &lt;link href=\"" + iteratorAssetHead.next().getFinalLocation() + "\" />");
			if (iteratorAssetHead.hasNext()) {
				sbHead.append('\n');
			}
		}

		Set<Asset> assetsBody = new AssetQuery(context.getRequest(), context.getContext()).atPosition(
				AssetDomPosition.body).perform();
		Iterator<Asset> iteratorAssetBody = assetsBody.iterator();
		while (iteratorAssetBody.hasNext()) {
			sbBody.append("    &lt;script src=\"" + iteratorAssetBody.next().getFinalLocation() + "\"></script>");
			if (iteratorAssetBody.hasNext()) {
				sbBody.append('\n');
			}
		}

		params.put("%ASSETS_HEAD%", sbHead.toString());
		params.put("%ASSETS_BODY%", sbBody.toString());
		return params;
	}

	public List<D3Asset> convertToD3Assets(Set<AssetStorageUnit> asus) {
		List<D3Asset> d3Assets = new ArrayList<AssetsDebugPage.D3Asset>();
		Set<Asset> assets = assetMapper.mapToAssets(asus);
		for (Asset a : assets) {
			d3Assets.add(new D3Asset(a));
		}
		return d3Assets;
	}

	private class D3Asset {
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
