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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.dandelion.core.asset.AssetQuery;
import com.github.dandelion.core.reporting.Alert;
import com.github.dandelion.core.util.UrlUtils;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Abstract base class for all debug pages.
 * </p>
 * <p>
 * Also provides some utilities to help building template parameters.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public abstract class AbstractDebugPage implements DebugPage {

   protected HandlerContext context;
   protected static ObjectMapper mapper;

   static {
      mapper = new ObjectMapper();
      mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
      mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
      mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
      mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
      mapper.configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, true);
   }

   public void initWith(HandlerContext context) {
      this.context = context;
   }

   @Override
   public String getContext() {
      String mustacheContext = null;
      Map<String, Object> ctx = new HashMap<String, Object>();
      ctx.putAll(getCommonPageContext());
      ctx.putAll(getPageContext());
      try {
         mustacheContext = mapper.writeValueAsString(ctx);
      }
      catch (JsonProcessingException e) {
         e.printStackTrace();
      }

      return mustacheContext;
   }

   private Map<String, Object> getCommonPageContext() {
      Map<String, Object> commonCtx = new HashMap<String, Object>();

      StringBuilder currentUrlWithParams = UrlUtils.getCurrentUrl(context.getRequest(), true);

      commonCtx.put("currentContextPath", UrlUtils.getContext(context.getRequest()).toString());

      commonCtx.put("pageHeader", getName());

      String currentUri = UrlUtils.getCurrentUri(context.getRequest()).toString();
      String appUri = currentUri.substring(0, currentUri.indexOf(WebConstants.DANDELION_DEBUGGER) - 1);
      commonCtx.put("currentUri", appUri);

      StringBuilder debuggerUrl = new StringBuilder(UrlUtils.getContext(context.getRequest()));
      UrlUtils.addParameter(debuggerUrl, WebConstants.DANDELION_DEBUGGER);
      commonCtx.put("debuggerUrl", debuggerUrl);

      StringBuilder debuggerUrlWithParam = new StringBuilder(currentUrlWithParams);
      UrlUtils.addParameter(debuggerUrlWithParam, WebConstants.DANDELION_DEBUGGER);
      commonCtx.put("debuggerUrlWithParam", debuggerUrlWithParam);

      StringBuilder clearStorageUrl = new StringBuilder(currentUrlWithParams);
      UrlUtils.addParameter(clearStorageUrl, WebConstants.DANDELION_CLEAR_STORAGE);
      commonCtx.put("clearStorageUrl", clearStorageUrl);

      StringBuilder clearCacheUrl = new StringBuilder(currentUrlWithParams);
      UrlUtils.addParameter(clearCacheUrl, WebConstants.DANDELION_CLEAR_CACHE);
      commonCtx.put("clearCacheUrl", clearCacheUrl);

      StringBuilder reloadBundleUrl = new StringBuilder(currentUrlWithParams);
      UrlUtils.addParameter(reloadBundleUrl, WebConstants.DANDELION_RELOAD_BUNDLES);
      commonCtx.put("reloadBundleUrl", reloadBundleUrl);

      StringBuilder alertReportingUrl = new StringBuilder(debuggerUrl);
      UrlUtils.addParameter(alertReportingUrl, "ddl-debug-page", "alerts");
      commonCtx.put("alertReportingUrl", alertReportingUrl);

      Set<Alert> alerts = new AssetQuery(context.getRequest(), context.getContext()).alerts();
      commonCtx.put("alertCount", alerts.size());

      List<Map<String, Object>> menusMap = new ArrayList<Map<String, Object>>();
      for (DebugMenu debugMenu : context.getContext().getDebugMenuMap().values()) {

         List<Map<String, String>> pagesMap = new ArrayList<Map<String, String>>();
         for (DebugPage page : debugMenu.getPages()) {
            pagesMap.add(new MapBuilder<String, String>()
                  .entry("pageName", page.getName())
                  .entry("pageUri", getComponentDebugPageUrl(appUri, page))
                  .entry("pageActive",
                        this.getClass().getSimpleName().equals(page.getClass().getSimpleName()) ? "active" : "")
                  .create());
         }

         menusMap.add(new MapBuilder<String, Object>().entry("menuPages", pagesMap)
               .entry("menuName", debugMenu.getDisplayName()).create());
      }
      commonCtx.put("menus", menusMap);

      return commonCtx;
   }

   @Override
   public Map<String, String> getExtraParams() {
      return Collections.emptyMap();
   }

   protected abstract Map<String, Object> getPageContext();

   private String getComponentDebugPageUrl(String appUri, DebugPage debugPage) {
      StringBuilder componentPage = new StringBuilder(appUri);
      if (componentPage.indexOf("?") == -1) {
         componentPage.append("?");
      }
      else {
         componentPage.append("&");
      }
      componentPage.append(WebConstants.DANDELION_DEBUGGER);
      componentPage.append("&ddl-debug-page=");
      componentPage.append(debugPage.getId());
      return componentPage.toString();
   }

   public class MapBuilder<K, V> {

      private Map<K, V> map = null;

      public MapBuilder() {
         map = new HashMap<K, V>();
      }

      public MapBuilder<K, V> entry(K key, V value) {
         map.put(key, value);
         return this;
      }

      public Map<K, V> create() {
         return map;
      }

      public MapBuilder<K, V> addTo(List<Map<K, V>> dest) {
         dest.add(map);
         return this;
      }
   }
}
