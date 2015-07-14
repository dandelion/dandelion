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

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.reporting.ReportingType;
import com.github.dandelion.core.util.PropertiesUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Holds the Dandelion raw configuration initialized at server startup and must
 * be accessed through the Dandelion {@link Context}.
 * </p>
 * <p>
 * All configuration present in the {@link DandelionConfig} enum are read using
 * a particular strategy. See {@link #readConfig(DandelionConfig)}.
 * </p>
 * <p>
 * There should be only one instance of this class in the application.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class Configuration {

   private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

   private FilterConfig filterConfig;
   private Properties userProperties;

   // Profile configurations
   private String activeProfile;
   private String activeRawProfile;

   // Component-related configurations
   private List<String> componentsStandalone;

   // Asset-related configurations
   private boolean assetMinificationEnabled;
   private List<String> assetLocationsResolutionStrategy;
   private List<String> assetProcessors;
   private boolean assetJsProcessingEnabled;
   private List<String> assetJsExcludes;
   private List<String> assetCssExcludes;
   private String assetUrlPattern;
   private String assetStorage;

   // Asset versioning configurations
   private String assetVersioningMode;
   private String assetVersioningStrategy;
   private String assetFixedVersionType;
   private String assetFixedVersionValue;
   private String assetFixedVersionDatePattern;

   // Caching-related configurations
   private boolean cachingEnabled;
   private String cacheName;
   private int cacheMaxSize;
   private String cacheConfigurationLocation;

   // Bundle-related configurations
   private String bundleLocation;
   private List<String> bundleIncludes;
   private List<String> bundleExcludes;
   private boolean bundlePreLoaderEnabled;

   // Tooling-related configurations
   private boolean toolDebuggerEnabled;
   private boolean toolAlertReportingEnabled;
   private ReportingType toolAlertReportingMode;
   private boolean toolAssetPrettyPrintingEnabled;
   private boolean toolBundleReloadingEnabled;
   private boolean toolGzipEnabled;
   private Set<String> toolGzipMimeTypes;

   // Monitoring configuration
   private boolean monitoringJmxEnabled;

   // Package manager configurations
   private String bowerComponentsLocation;

   // Misc configurations
   private String encoding;

   public Configuration(FilterConfig filterConfig, Properties userProperties, Context context) {
      this.filterConfig = filterConfig;
      this.userProperties = userProperties;

      // Dandelion profile
      this.activeProfile = Profile.getActiveProfile();
      this.activeRawProfile = Profile.getActiveRawProfile();

      if (Profile.DEFAULT_DEV_PROFILE.equals(this.activeProfile)) {
         LOG.info("===========================================");
         LOG.info("");
         LOG.info("Dandelion \"dev\" profile activated.");
         LOG.info("");
         LOG.info("===========================================");
      }

      // Bundles-related configurations
      this.bundleLocation = readConfig(DandelionConfig.BUNDLE_LOCATION);
      this.bundleIncludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.BUNDLE_INCLUDES));
      this.bundleExcludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.BUNDLE_EXCLUDES));
      this.bundlePreLoaderEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.BUNDLE_PRE_LOADERS));

      // Component-related configurations
      this.componentsStandalone = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.COMPONENTS_STANDALONE));

      // Assets-related configurations
      this.assetMinificationEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.ASSET_MINIFICATION));
      this.assetLocationsResolutionStrategy = PropertiesUtils
            .propertyAsList(readConfig(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY));
      this.assetProcessors = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.ASSET_PROCESSORS));
      this.assetJsProcessingEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.ASSET_JS_PROCESSING));
      this.assetJsExcludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.ASSET_JS_EXCLUDES));
      this.assetCssExcludes = PropertiesUtils.propertyAsList(readConfig(DandelionConfig.ASSET_CSS_EXCLUDES));
      this.assetUrlPattern = getProcessedAssetUrlPattern(readConfig(DandelionConfig.ASSET_URL_PATTERN));
      this.assetStorage = readConfig(DandelionConfig.ASSET_STORAGE);

      // Asset versioning
      this.assetVersioningMode = readConfig(DandelionConfig.ASSET_VERSIONING_MODE);
      this.assetVersioningStrategy = readConfig(DandelionConfig.ASSET_VERSIONING_STRATEGY);
      this.assetFixedVersionType = readConfig(DandelionConfig.ASSET_FIXED_VERSION_TYPE);
      this.assetFixedVersionValue = readConfig(DandelionConfig.ASSET_FIXED_VERSION_VALUE);
      this.assetFixedVersionDatePattern = readConfig(DandelionConfig.ASSET_FIXED_VERSION_DATEPATTERN);

      // Caching-related configurations
      this.cachingEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.CACHE));

      this.cacheName = readConfig(DandelionConfig.CACHE_NAME);
      try {
         this.cacheMaxSize = Integer.parseInt(readConfig(DandelionConfig.CACHE_MAX_SIZE));
      }
      catch (NumberFormatException e) {
         LOG.warn("The '{}' property is incorrectly configured. Falling back to the default value ({})",
               DandelionConfig.CACHE_MAX_SIZE.getName(), DandelionConfig.CACHE_MAX_SIZE.defaultDevValue());
         this.cacheMaxSize = Integer.parseInt(DandelionConfig.CACHE_MAX_SIZE.defaultDevValue());
      }
      this.cacheConfigurationLocation = readConfig(DandelionConfig.CACHE_CONFIGURATION_LOCATION);

      // Tooling-related configurations
      this.toolDebuggerEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.TOOL_DEBUGGER));
      this.toolAlertReportingEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.TOOL_ALERT_REPORTING));
      this.toolAlertReportingMode = ReportingType
            .valueOf(readConfig(DandelionConfig.TOOL_ALERT_REPORTING_MODE).toUpperCase());
      this.toolAssetPrettyPrintingEnabled = Boolean
            .parseBoolean(readConfig(DandelionConfig.TOOL_ASSET_PRETTY_PRINTING));
      this.toolBundleReloadingEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.TOOL_BUNDLE_RELOADING));
      this.toolGzipEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.TOOL_GZIP));
      this.toolGzipMimeTypes = PropertiesUtils.propertyAsSet(readConfig(DandelionConfig.TOOL_GZIP_MIME_TYPES));

      // Package manager configurations
      this.bowerComponentsLocation = readConfig(DandelionConfig.BOWER_COMPONENTS_LOCATION);

      // Monitoring configurations
      this.monitoringJmxEnabled = Boolean.parseBoolean(readConfig(DandelionConfig.MONITORING_JMX));

      // Misc configuration
      this.encoding = readConfig(DandelionConfig.ENCODING);
   }

   /**
    * <p>
    * Reads the given {@link DandelionConfig} in order of priority:
    * </p>
    * <ol>
    * <li>System properties have the highest precedence and thus override
    * everything else</li>
    * <li>Then the filter initialization parameters, coming from the
    * {@code web.xml} file</li>
    * <li>Then the user-defined properties, coming from the
    * {@code dandelion_[activeProfile].properties} file, if it exists</li>
    * <li>Finally the default "dev" value of the given {@link DandelionConfig}
    * if the {@link Profile#DEFAULT_DEV_PROFILE} is enabled, otherwise the
    * default "prod" value.</li>
    * </ol>
    * 
    * @param config
    *           The config to read.
    * @return the value of the given {@link DandelionConfig}.
    */
   public String readConfig(DandelionConfig config) {

      String retval = null;
      if (System.getProperty(config.getName()) != null) {
         retval = System.getProperty(config.getName());
      }

      if (retval == null && this.filterConfig != null) {
         retval = this.filterConfig.getInitParameter(config.getName());
      }

      if (retval == null && this.userProperties != null) {
         retval = this.userProperties.getProperty(config.getName());
      }

      if (retval == null) {
         if (Profile.DEFAULT_DEV_PROFILE.equals(this.activeProfile)) {
            retval = config.defaultDevValue();
         }
         else if (Profile.DEFAULT_PROD_PROFILE.equals(this.activeProfile)) {
            retval = config.defaultProdValue();
         }
         // Always read default dev values if not explicitely declared in the
         // configuration file
         else {
            retval = config.defaultDevValue();
         }
      }

      return StringUtils.isNotBlank(retval) ? retval.trim() : retval;
   }

   public String getActiveProfile() {
      return this.activeProfile;
   }

   public String getActiveRawProfile() {
      return this.activeRawProfile;
   }

   /**
    * @return the list of all components that are used standalone.
    */
   public List<String> getComponentsStandalone() {
      return componentsStandalone;
   }

   /**
    * @return {@code true} is the asset minification is enabled, {@code false}
    *         otherwise.
    */
   public boolean isAssetMinificationEnabled() {
      return this.assetMinificationEnabled;
   }

   /**
    * @return {@code true} if the debugger is accessible, {@code false}
    *         otherwise.
    */
   public boolean isToolDebuggerEnabled() {
      return this.toolDebuggerEnabled;
   }

   /**
    * @return {@code true} if alert reporting is enabled, {@code false}
    *         otherwise.
    */
   public boolean isToolAlertReportingEnabled() {
      return toolAlertReportingEnabled;
   }

   /**
    * @return the reporting type if alert reporting is enabled
    */
   public ReportingType getToolAlertReportingMode() {
      return toolAlertReportingMode;
   }

   /**
    * @return {@code true} if generated asset are automatically pretty printed,
    *         {@code false} otherwise.
    */
   public boolean isToolAssetPrettyPrintingEnabled() {
      return toolAssetPrettyPrintingEnabled;
   }

   /**
    * @return {@code true} if bundle reloading is enabled, {@code false}
    *         otherwise.
    */
   public boolean isToolBundleReloadingEnabled() {
      return this.toolBundleReloadingEnabled;
   }

   /**
    * @return {@code true} if caching is enabled, {@code false} otherwise.
    */
   public boolean isCachingEnabled() {
      return this.cachingEnabled;
   }

   /**
    * @return the asset location resolution strategy, in the form of an ordered
    *         list of location keys.
    */
   public List<String> getAssetLocationsResolutionStrategy() {
      return this.assetLocationsResolutionStrategy;
   }

   /**
    * @return the list of all enabled asset processors.
    */
   public List<String> getAssetProcessors() {
      return this.assetProcessors;
   }

   /**
    * @return {@code true} if the processing of JavaScript assets is enabled,
    *         {@code false} otherwise. (Thymeleaf only).
    */
   public boolean isAssetJsProcessingEnabled() {
      return assetJsProcessingEnabled;
   }

   /**
    * @return the list of all excluded JavaScript assets.
    */
   public List<String> getAssetJsExcludes() {
      return this.assetJsExcludes;
   }

   /**
    * @return the list of all excluded CSS assets.
    */
   public List<String> getAssetCssExcludes() {
      return this.assetCssExcludes;
   }

   public int getCacheMaxSize() {
      return this.cacheMaxSize;
   }

   public String getCacheConfigurationLocation() {
      return this.cacheConfigurationLocation;
   }

   public String getBundleLocation() {
      return this.bundleLocation;
   }

   /**
    * @return the name of all bundles included in the {@link HttpServletRequest}
    *         .
    */
   public List<String> getBundleIncludes() {
      return this.bundleIncludes;
   }

   /**
    * @return the name of all bundles excluded from the
    *         {@link HttpServletRequest}.
    */
   public List<String> getBundleExcludes() {
      return this.bundleExcludes;
   }

   public Properties getProperties() {
      return this.userProperties;
   }

   public void setProperties(Properties properties) {
      this.userProperties = properties;
   }

   public String get(String key) {
      return this.userProperties.getProperty(key);
   }

   public String get(String key, String defaultValue) {
      return this.userProperties.getProperty(key, defaultValue);
   }

   public boolean isMonitoringJmxEnabled() {
      return this.monitoringJmxEnabled;
   }

   public String getAssetVersioningStrategy() {
      return assetVersioningStrategy;
   }

   public String getAssetStorage() {
      return assetStorage;
   }

   public String getCacheName() {
      return this.cacheName;
   }

   public String getAssetFixedVersionType() {
      return assetFixedVersionType;
   }

   public String getAssetFixedVersionValue() {
      return assetFixedVersionValue;
   }

   public String getAssetFixedVersionDatePattern() {
      return assetFixedVersionDatePattern;
   }

   public String getAssetVersioningMode() {
      return assetVersioningMode;
   }

   public boolean isAssetAutoVersioningEnabled() {
      return this.assetVersioningMode.equalsIgnoreCase("auto");
   }

   public String getAssetUrlPattern() {
      return assetUrlPattern;
   }

   public boolean isToolGzipEnabled() {
      return toolGzipEnabled;
   }

   private String getProcessedAssetUrlPattern(String rawAssetUrlPattern) {

      StringBuilder processedAssetUrlPattern = new StringBuilder(rawAssetUrlPattern);
      if (!processedAssetUrlPattern.toString().startsWith("/")) {
         processedAssetUrlPattern.insert(0, '/');
      }
      if (!processedAssetUrlPattern.toString().endsWith("/")) {
         processedAssetUrlPattern.append('/');
      }

      return processedAssetUrlPattern.toString();
   }

   public String getEncoding() {
      return encoding;
   }

   public Set<String> getToolGzipMimeTypes() {
      return toolGzipMimeTypes;
   }

   public String getBowerComponentsLocation() {
      return bowerComponentsLocation;
   }

   public boolean isBundlePreLoaderEnabled() {
      return bundlePreLoaderEnabled;
   }
}