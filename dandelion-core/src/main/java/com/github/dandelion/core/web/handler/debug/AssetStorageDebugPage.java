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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.storage.AssetStorage;
import com.github.dandelion.core.storage.StorageEntry;
import com.github.dandelion.core.util.ResourceUtils;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Debug page inteded to browse the {@link AssetStorage}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class AssetStorageDebugPage extends AbstractDebugPage {

   public static final String PAGE_ID = "asset-storage";
   public static final String PAGE_NAME = "Asset storage";
   private static final String PAGE_LOCATION = "META-INF/resources/ddl-debugger/html/core-asset-storage.html";

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

      AssetStorage storage = context.getContext().getAssetStorage();

      Collection<StorageEntry> storageElements = storage.getAll();

      List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();

      for (StorageEntry storageElement : storageElements) {
         Asset asset = storageElement.getAsset();
         String storageKey = asset.getStorageKey();
         options.add(new MapBuilder<String, Object>().entry("name", asset.getName()).entry("type", asset.getType())
               .entry("version", asset.getVersion()).entry("bundle", asset.getBundle())
               .entry("locationKey", asset.getConfigLocationKey()).entry("rawLocation", asset.getConfigLocation())
               .entry("finalLocation", asset.getFinalLocation()).entry("storageKey", asset.getStorageKey())
               .entry("contents", storage.contains(storageKey) ? storage.get(storageKey).getContents() : "Not fetched")
               .entry("presentInStorage", storage.contains(storageKey)).entry("version", asset.getVersion()).create());
      }

      pageContext.put("impl", storage.getClass().getName());
      pageContext.put("number", storage.size());
      pageContext.put("assets", options);

      return pageContext;
   }
}
