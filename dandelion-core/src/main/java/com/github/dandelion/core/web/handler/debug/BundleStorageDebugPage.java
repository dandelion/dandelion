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
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.ResourceUtils;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Debug page inteded to browse the {@link BundleStorage}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class BundleStorageDebugPage extends AbstractDebugPage {

   public static final String PAGE_ID = "bundle-storage";
   public static final String PAGE_NAME = "Bundle storage";
   private static final String PAGE_LOCATION = "META-INF/resources/ddl-debugger/html/core-bundle-storage.html";

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

      BundleStorage bundleStorage = context.getContext().getBundleStorage();

      List<Map<String, Object>> bundles = new ArrayList<Map<String, Object>>();

      for (BundleStorageUnit bsu : bundleStorage.getBundleDag().getVerticies()) {
         bundles.add(new MapBuilder<String, Object>()
               .entry("name", bsu.getName())
               .entry("dependencies", bsu.getDependencies())
               .entry("relativePath", bsu.getRelativePath())
               .entry("loader", bsu.getBundleLoaderOrigin())
               .entry("vendor", bsu.isVendor() ? "vendor" : "")
               .entry("labelType", "success")
               .create());
      }

      pageContext.put("number", context.getContext().getBundleStorage().getBundleDag().getVerticies().size());
      pageContext.put("bundles", bundles);

      return pageContext;
   }

   @Override
   public Map<String, String> getExtraParams() {
      StringBuilder sbNodesRequest = new StringBuilder();
      List<BundleStorageUnit> bsuRequest = context.getContext().getBundleStorage().getBundleDag().getVerticies();

      for (BundleStorageUnit bsu : bsuRequest) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("label", bsu.getName());
         map.put("shape", "ellipse");
         String bundle = null;
         try {
            bundle = mapper.writeValueAsString(map);
         }
         catch (JsonProcessingException e) {
            e.printStackTrace();
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
