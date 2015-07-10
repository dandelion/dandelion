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
package com.github.dandelion.core.config;

import com.github.dandelion.core.Beta;
import com.github.dandelion.core.cache.impl.MemoryRequestCache;
import com.github.dandelion.core.web.DandelionServlet;

/**
 * <p>
 * Enum containing all configuration properties and their associated value both
 * with {@link Profile#DEFAULT_DEV_PROFILE} and
 * {@link Profile#DEFAULT_PROD_PROFILE}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public enum DandelionConfig {

   // Component-related configurations
   COMPONENTS_STANDALONE("components.standalone", "", ""),
   
   // Asset-related configurations
   ASSET_MINIFICATION("asset.minification", "false", "true"), 
   ASSET_LOCATIONS_RESOLUTION_STRATEGY("asset.locations.resolution.strategy", "webapp,webjar,classpath,jar,file,remote", "webapp,webjar,classpath,jar,file,remote"), 
   ASSET_PROCESSORS("asset.processors", "cssurlrewriting,jsmin,cssmin", "cssurlrewriting,jsmin,cssmin"),
   ASSET_JS_PROCESSING("asset.js.processing", "true", "true"),
   ASSET_JS_EXCLUDES("asset.js.excludes", "", ""), 
   ASSET_CSS_EXCLUDES("asset.css.excludes", "", ""), 
   ASSET_URL_PATTERN("asset.url.pattern", DandelionServlet.DANDELION_ASSETS_URL, DandelionServlet.DANDELION_ASSETS_URL), 
   ASSET_STORAGE("asset.storage", "memory", "memory"),

   // Asset versioning
   ASSET_VERSIONING_MODE("asset.versioning.mode", "auto", "auto"), 
   ASSET_VERSIONING_STRATEGY("asset.versioning.strategy", "content", "content"), 
   ASSET_FIXED_VERSION_TYPE("asset.fixed.version.type", "string", "string"), 
   ASSET_FIXED_VERSION_VALUE("asset.fixed.version.value", "UNDEFINED_VERSION", "UNDEFINED_VERSION"), 
   ASSET_FIXED_VERSION_DATEPATTERN("asset.fixed.version.datepattern", "yyyyMMdd", "yyyyMMdd"),

   // Caching-related configurations
   CACHE("cache", "false", "true"), 
   CACHE_NAME("cache.name", MemoryRequestCache.CACHE_NAME, MemoryRequestCache.CACHE_NAME), 
   CACHE_MAX_SIZE("cache.max.size", "500", "500"), 
   CACHE_CONFIGURATION_LOCATION("cache.configuration.location", "", ""),

   // Bundle-related configurations
   BUNDLE_LOCATION("bundle.location", "", ""), 
   BUNDLE_INCLUDES("bundle.includes", "", ""), 
   BUNDLE_EXCLUDES("bundle.excludes", "", ""),
   BUNDLE_PRE_LOADERS("bundle.pre.loaders", "true", "true"),
   
   // Tooling-related configurations
   TOOL_GZIP("tool.gzip", "false", "true"), 
   TOOL_GZIP_MIME_TYPES("tool.gzip.mime.types", "text/html,text/css,application/x-javascript,application/javascript,text/javascript,text/plain,text/xml,application/xhtml+xml,image/svg+xml", "text/html,text/css,application/x-javascript,application/javascript,text/javascript,text/plain,text/xml,application/xhtml+xml,image/svg+xml"), 
   TOOL_DEBUGGER("tool.debugger", "true", "false"),
   TOOL_ALERT_REPORTING("tool.alert.reporting", "true", "false"),
   TOOL_ALERT_REPORTING_MODE("tool.alert.reporting.mode", "all", "all"),
   TOOL_ASSET_PRETTY_PRINTING("tool.asset.pretty.printing", "true", "false"), 
   TOOL_BUNDLE_RELOADING("tool.bundle.reloading", "true", "false"),

   // Moniroting configurations
   @Beta
   MONITORING_JMX("monitoring.jmx", "false", "false"),

   // Package manager configurations
   BOWER_COMPONENTS_LOCATION("bower.components.location", "", ""),
   
   // Misc configurations
   ENCODING("encoding", "UTF-8", "UTF-8");

   /**
    * The configuration name.
    */
   private String propertyName;

   /**
    * The default value to be used if the {@link Profile#DEFAULT_DEV_PROFILE} is
    * activated or if a custom profile is activated but the corresponding
    * configuration is not specified.
    */
   private String defaultDevValue;

   /**
    * The default value to be used if the {@link Profile#DEFAULT_PROD_PROFILE}
    * is activated.
    */
   private String defaultProdValue;

   private DandelionConfig(String propertyName, String defaultDevValue, String defaultProdValue) {
      this.propertyName = propertyName;
      this.defaultDevValue = defaultDevValue;
      this.defaultProdValue = defaultProdValue;
   }

   public String getName() {
      return propertyName;
   }

   public String defaultDevValue() {
      return defaultDevValue;
   }

   public String defaultProdValue() {
      return defaultProdValue;
   }
}
