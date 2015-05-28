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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.bundle.loader.support.BundleSaxHandler;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.storage.support.BundleUtils;
import com.github.dandelion.core.util.BundleStorageLogBuilder;
import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.util.scanner.ResourceScanner;

/**
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class XmlBundleLoadingStrategy implements LoadingStrategy {

   private static final Logger LOG = LoggerFactory.getLogger(XmlBundleLoadingStrategy.class);

   private static final String XSD_LOCATION = "dandelion/internal/xsd/dandelion-bundle.xsd";
   private static SAXParserFactory saxParserFactory;
   private static SAXParser saxParser;
   private final Context context;

   /**
    * @return the uniq static instance of {@link XmlBundleLoadingStrategy}.
    */
   public XmlBundleLoadingStrategy(Context context) {
      this.context = context;
   }

   static {
      try {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

         saxParserFactory = SAXParserFactory.newInstance();
         saxParserFactory.setValidating(true);
         saxParserFactory.setNamespaceAware(true);

         saxParser = saxParserFactory.newSAXParser();
         saxParser.setProperty(BundleSaxHandler.JAXP_SCHEMA_LANGUAGE, BundleSaxHandler.W3C_XML_SCHEMA);
         saxParser.setProperty(BundleSaxHandler.JAXP_SCHEMA_SOURCE, classLoader.getResourceAsStream(XSD_LOCATION));
      }
      catch (ParserConfigurationException e) {
         throw new DandelionException("Unable to configure the SAX parser", e);
      }
      catch (SAXException e) {
         throw new DandelionException("Unable to configure the SAX parser", e);
      }
   }

   @Override
   public Set<String> getResourcePaths(String bundleLocation, Set<String> excludedPaths) {

      Set<String> resourcePaths = null;

      try {
         resourcePaths = ResourceScanner.findResourcePaths(bundleLocation, excludedPaths, null, ".xml");
      }
      catch (IOException e) {
         throw new DandelionException("Something went wrong when scanning files in " + bundleLocation, e);
      }

      return resourcePaths;
   }

   @Override
   public List<BundleStorageUnit> mapToBundles(Set<String> resourcePaths) {

      List<BundleStorageUnit> bundles = new ArrayList<BundleStorageUnit>();

      BundleStorageLogBuilder bslb = new BundleStorageLogBuilder();
      BundleSaxHandler bundleSaxHandler = new BundleSaxHandler();

      for (String resourcePath : resourcePaths) {

         try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream configFileStream = classLoader.getResourceAsStream(resourcePath);
            saxParser.parse(configFileStream, bundleSaxHandler);
            BundleStorageUnit bsu = bundleSaxHandler.getBsu();
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
         catch (SAXException e) {
            e.printStackTrace();
            StringBuilder error = new StringBuilder("- The file '");
            error.append(resourcePath);
            error.append("' is wrongly formatted for the following reason: " + e.getMessage());
            bslb.error("Wrong bundle format:", error.toString());
         }
         catch (IOException e) {
            e.printStackTrace();
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
