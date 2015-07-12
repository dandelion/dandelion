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
package com.github.dandelion.core.bundle.loader.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.storage.support.BundleUtils;
import com.github.dandelion.core.util.BundleStorageLogBuilder;
import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.util.scanner.ClasspathResourceScanner;

/**
 * <p>
 * TODO
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class JsonBundleLoadingStrategy implements LoadingStrategy {

   private static final Logger LOG = LoggerFactory.getLogger(JsonBundleLoadingStrategy.class);
   private final Context context;
   private static ObjectMapper mapper;

   static {
      mapper = new ObjectMapper();
      mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
      mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
   }

   public JsonBundleLoadingStrategy(Context context) {
      this.context = context;
   }

   @Override
   public Set<String> getResourcePaths(String bundleLocation, Set<String> excludedPaths) {
      return ClasspathResourceScanner.findResourcePaths(bundleLocation, excludedPaths, null, ".json");
   }

   @Override
   public List<BundleStorageUnit> mapToBundles(Set<String> resourcePaths) {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      BundleStorageLogBuilder bslb = new BundleStorageLogBuilder();

      for (String resourcePath : resourcePaths) {

         try {
            InputStream configFileStream = classLoader.getResourceAsStream(resourcePath);
            BundleStorageUnit bsu = mapper.readValue(configFileStream, BundleStorageUnit.class);
            bsu.setRelativePath(resourcePath);

            // The name of the bundle is extracted from its path if not
            // specified
            if (StringUtils.isBlank(bsu.getName())) {
               String extractedName = PathUtils.extractLowerCasedName(bsu.getRelativePath());
               bsu.setName(extractedName);
               LOG.trace("Name of the bundle extracted from its path: \"{}\"", extractedName);
            }

            if (BundleUtils.isValid(bsu, bslb)) {
               BundleUtils.finalize(bsu, context);
               LOG.trace("Parsed bundle \"{}\" ({})", bsu.getName(), bsu);
               bundles.add(bsu);
            }
         }
         catch (IOException e) {
            StringBuilder error = new StringBuilder("- The file '");
            error.append(resourcePath);
            error.append("' is wrongly formatted for the following reason: " + e.getMessage());
            bslb.error("Wrong bundle format:", error.toString());
         }
      }

      if (bslb.hasError()) {
         throw new DandelionException(bslb.toString());
      }

      return bundles;
   }
}