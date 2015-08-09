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
package com.github.dandelion.core.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.AssetStorage;
import com.github.dandelion.core.storage.StorageEntry;
import com.github.dandelion.core.util.AssetUtils;

/**
 * <p>
 * Dandelion servlet in charge of serving the assets stored in the configured
 * {@link AssetStorage}.
 * </p>
 * <p>
 * Depending on the configuration, processing of the assets is performed here
 * before serving them.
 * </p>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class DandelionServlet extends HttpServlet {

   private static final long serialVersionUID = -6874842638265359418L;

   private static final Logger LOG = LoggerFactory.getLogger(DandelionServlet.class);

   public static final String DANDELION_ASSETS_URL = "/dandelion-assets/";

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      LOG.debug("Dandelion Asset servlet captured GET request {}", request.getRequestURI());

      Context context = (Context) request.getAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE);

      // Get the asset content thanks to the cache key
      String masterStorageKey = AssetUtils.extractCacheKeyFromRequest(request);

      AssetType assetType = AssetUtils.extractAssetTypeFromRequest(request);
      LOG.debug("Retrieved asset type: {}, cache key: {}", assetType, masterStorageKey);

      response.setContentType(assetType.getContentType() == null ? "text/plain" : assetType.getContentType());

      StorageEntry storageEntry = context.getAssetStorage().get(masterStorageKey);

      // Processing
      if (context.getConfiguration().isAssetProcessingEnabled()) {
         storageEntry.processContents(request);
      }

      // Fetching
      String contents = storageEntry.resolveContents(request);

      PrintWriter writer = response.getWriter();
      writer.write(contents);

      // The response is explicitely closed here instead of setting a
      // Content-Length header
      writer.close();
   }
}