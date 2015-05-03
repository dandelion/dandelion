/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetDomPosition;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.ResourceUtils;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.handler.HandlerContext;

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
 * @since 1.0.0
 */
public class AssetsDebugPage extends AbstractDebugPage {

   public static final String PAGE_ID = "assets";
   public static final String PAGE_NAME = "Current assets";
   private static final String PAGE_LOCATION = "META-INF/resources/ddl-debugger/html/core-assets.html";

   @Override
   public String getId() {
      return PAGE_ID;
   }

   @Override
   public String getName() {
      return PAGE_NAME;
   }

   @Override
   public String getTemplate(HandlerContext context) throws IOException {
      return ResourceUtils.getContentFromInputStream(Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(PAGE_LOCATION));
   }

   @Override
   protected Map<String, Object> getPageContext() {
      Map<String, Object> pageContext = new HashMap<String, Object>();

      Set<Asset> assetsHead = new AssetQuery(context.getRequest(), context.getContext()).atPosition(
            AssetDomPosition.head).perform();
      List<String> assetsInHead = new ArrayList<String>();
      for (Asset asset : assetsHead) {
         assetsInHead.add(asset.getFinalLocation());
      }
      pageContext.put("assetsInHead", assetsInHead);

      Set<Asset> assetsBody = new AssetQuery(context.getRequest(), context.getContext()).atPosition(
            AssetDomPosition.body).perform();
      List<String> assetsInBody = new ArrayList<String>();
      for (Asset asset : assetsBody) {
         assetsInBody.add(asset.getFinalLocation());
      }
      pageContext.put("assetsInBody", assetsInBody);

      Set<Asset> allAssets = new HashSet<Asset>(assetsHead);
      allAssets.addAll(assetsBody);
      pageContext.put("assets", allAssets);
      return pageContext;
   }

   @Override
   public Map<String, String> getExtraParams() {
      StringBuilder sbNodesRequest = new StringBuilder();
      Set<BundleStorageUnit> bsuRequest = context.getContext().getBundleStorage()
            .bundlesFor(AssetRequestContext.get(context.getRequest()).getBundles(true));

      for (BundleStorageUnit bsu : bsuRequest) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("label", bsu.getName());

         List<Map<String, Object>> assets = new ArrayList<Map<String, Object>>();

         for (AssetStorageUnit asu : bsu.getAssetStorageUnits()) {
            assets.add(new MapBuilder<String, Object>().entry("name", asu.getName()).entry("type", asu.getType())
                  .entry("version", asu.getVersion()).entry("bundle", asu.getBundle())
                  .entry("locations", asu.getLocations()).create());
         }
         map.put("assets", assets);
         map.put("shape", "ellipse");
         String bundle = null;
         try {
            bundle = mapper.writeValueAsString(map);
         }
         catch (JsonProcessingException e) {
            throw new DandelionException("An error occured when generating the debug page of current assets", e);
         }

         sbNodesRequest.append("requestGraph.setNode(\"" + bsu.getName() + "\"," + bundle + ");").append('\n');
      }

      Set<String> edgesRequest = new HashSet<String>();
      for (BundleStorageUnit bsu : bsuRequest) {
         if (bsu.getChildren() != null && !bsu.getChildren().isEmpty()) {
            for (BundleStorageUnit childBsu : bsu.getChildren()) {
               edgesRequest.add("requestGraph.setEdge(\"" + bsu.getName() + "\", \"" + childBsu.getName()
                     + "\", { label: \"depends on\", lineInterpolate: \"basis\" });");
            }
         }
      }
      for (String edge : edgesRequest) {
         sbNodesRequest.append(edge).append('\n');
      }

      return new MapBuilder<String, String>().entry("%EXTRA%", sbNodesRequest.toString()).create();
   }
}
