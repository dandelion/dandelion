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

import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.util.PropertiesUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class DevProfileTest {

   @After
   public void after() {
      System.clearProperty(Profile.DANDELION_PROFILE_ACTIVE);
      System.clearProperty(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName());
      System.clearProperty(DandelionConfig.CACHE_MAX_SIZE.getName());
   }

   @Test
   public void should_set_dev_profile_by_default() {
      Configuration config = new Configuration(new MockFilterConfig(), new Properties(), null);
      assertThat(Profile.getActiveProfile()).isEqualTo(Profile.DEFAULT_DEV_PROFILE);
      assertThat(config.getActiveRawProfile()).isEqualTo(Profile.getActiveRawProfile());
   }

   @Test
   public void should_load_configuration_from_default_dev_profile() {
      Configuration config = new Configuration(new MockFilterConfig(), new Properties(), null);

      // Bundle-related configurations
      assertThat(config.getBundleLocation()).isEmpty();
      assertThat(config.getBundleIncludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_INCLUDES.defaultDevValue()));
      assertThat(config.getBundleExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_EXCLUDES.defaultDevValue()));

      // Asset-related configurations
      assertThat(config.isAssetMinificationEnabled()).isFalse();
      assertThat(config.getAssetLocationsResolutionStrategy()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.defaultDevValue()));
      assertThat(config.getAssetProcessors()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_PROCESSORS.defaultDevValue()));
      assertThat(config.getAssetCssExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_CSS_EXCLUDES.defaultDevValue()));
      assertThat(config.getAssetJsExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_JS_EXCLUDES.defaultDevValue()));

      // Caching-related configurations
      assertThat(config.isCachingEnabled()).isEqualTo(Boolean.parseBoolean(DandelionConfig.CACHE.defaultDevValue()));
      assertThat(config.getCacheName()).isEqualTo(DandelionConfig.CACHE_NAME.defaultDevValue());
      assertThat(config.getCacheConfigurationLocation()).isEqualTo(
            DandelionConfig.CACHE_CONFIGURATION_LOCATION.defaultDevValue());
      assertThat(config.getCacheMaxSize())
            .isEqualTo(Integer.parseInt(DandelionConfig.CACHE_MAX_SIZE.defaultDevValue()));

      // Tooling-related configurations
      assertThat(config.isToolDebuggerEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_DEBUGGER.defaultDevValue()));
      assertThat(config.isToolBundleReloadingEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_BUNDLE_RELOADING.defaultDevValue()));

      // Misc configurations
      assertThat(config.isMonitoringJmxEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.MONITORING_JMX.defaultDevValue()));
   }

   @Test
   public void should_load_configuration_from_properties_and_complete_with_default_dev_values() {
      Properties userProperties = new Properties();
      userProperties.put(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "foo,bar");
      userProperties.put(DandelionConfig.CACHE_MAX_SIZE.getName(), "40");

      Configuration config = new Configuration(new MockFilterConfig(), userProperties, null);

      // Bundle-related configurations
      assertThat(config.getBundleLocation()).isEmpty();
      assertThat(config.getBundleIncludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_INCLUDES.defaultDevValue()));
      assertThat(config.getBundleExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_EXCLUDES.defaultDevValue()));

      // Asset-related configurations
      assertThat(config.isAssetMinificationEnabled()).isFalse();
      assertThat(config.getAssetLocationsResolutionStrategy()).isEqualTo(PropertiesUtils.propertyAsList("foo,bar")); // OVERRIDEN
      assertThat(config.getAssetProcessors()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_PROCESSORS.defaultDevValue()));
      assertThat(config.getAssetCssExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_CSS_EXCLUDES.defaultDevValue()));
      assertThat(config.getAssetJsExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_JS_EXCLUDES.defaultDevValue()));

      // Caching-related configurations
      assertThat(config.isCachingEnabled()).isEqualTo(Boolean.parseBoolean(DandelionConfig.CACHE.defaultDevValue()));
      assertThat(config.getCacheName()).isEqualTo(DandelionConfig.CACHE_NAME.defaultDevValue());
      assertThat(config.getCacheConfigurationLocation()).isEqualTo(
            DandelionConfig.CACHE_CONFIGURATION_LOCATION.defaultDevValue());
      assertThat(config.getCacheMaxSize()).isEqualTo(40); // OVERRIDEN

      // Tooling-related configurations
      assertThat(config.isToolDebuggerEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_DEBUGGER.defaultDevValue()));
      assertThat(config.isToolBundleReloadingEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_BUNDLE_RELOADING.defaultDevValue()));

      // Misc configurations
      assertThat(config.isMonitoringJmxEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.MONITORING_JMX.defaultDevValue()));
   }

   @Test
   public void should_load_configuration_from_initparams_and_complete_with_default_dev_values() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "  foo,bar , baz");
      filterConfig.addInitParameter(DandelionConfig.CACHE_MAX_SIZE.getName(), "30");

      Configuration config = new Configuration(filterConfig, null, null);

      // Bundle-related configurations
      assertThat(config.getBundleLocation()).isEmpty();
      assertThat(config.getBundleIncludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_INCLUDES.defaultDevValue()));
      assertThat(config.getBundleExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_EXCLUDES.defaultDevValue()));

      // Asset-related configurations
      assertThat(config.isAssetMinificationEnabled()).isFalse();
      assertThat(config.getAssetLocationsResolutionStrategy()).isEqualTo(PropertiesUtils.propertyAsList("foo,bar,baz")); // OVERRIDEN
      assertThat(config.getAssetProcessors()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_PROCESSORS.defaultDevValue()));
      assertThat(config.getAssetCssExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_CSS_EXCLUDES.defaultDevValue()));
      assertThat(config.getAssetJsExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_JS_EXCLUDES.defaultDevValue()));

      // Caching-related configurations
      assertThat(config.isCachingEnabled()).isEqualTo(Boolean.parseBoolean(DandelionConfig.CACHE.defaultDevValue()));
      assertThat(config.getCacheName()).isEqualTo(DandelionConfig.CACHE_NAME.defaultDevValue());
      assertThat(config.getCacheConfigurationLocation()).isEqualTo(
            DandelionConfig.CACHE_CONFIGURATION_LOCATION.defaultDevValue());
      assertThat(config.getCacheMaxSize()).isEqualTo(30); // OVERRIDEN

      // Tooling-related configurations
      assertThat(config.isToolDebuggerEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_DEBUGGER.defaultDevValue()));
      assertThat(config.isToolBundleReloadingEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_BUNDLE_RELOADING.defaultDevValue()));

      // Misc configurations
      assertThat(config.isMonitoringJmxEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.MONITORING_JMX.defaultDevValue()));
   }

   @Test
   public void should_load_configuration_from_system_properties_and_complete_with_default_dev_values() {
      System.setProperty(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(), "bar ,foo  ,baz,qux  ");
      System.setProperty(DandelionConfig.CACHE_MAX_SIZE.getName(), "20");

      Configuration config = new Configuration(new MockFilterConfig(), null, null);

      // Bundle-related configurations
      assertThat(config.getBundleLocation()).isEmpty();
      assertThat(config.getBundleIncludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_INCLUDES.defaultDevValue()));
      assertThat(config.getBundleExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.BUNDLE_EXCLUDES.defaultDevValue()));

      // Asset-related configurations
      assertThat(config.isAssetMinificationEnabled()).isFalse();
      assertThat(config.getAssetLocationsResolutionStrategy()).isEqualTo(
            PropertiesUtils.propertyAsList("bar,foo,baz,qux")); // OVERRIDEN
      assertThat(config.getAssetProcessors()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_PROCESSORS.defaultDevValue()));
      assertThat(config.getAssetCssExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_CSS_EXCLUDES.defaultDevValue()));
      assertThat(config.getAssetJsExcludes()).isEqualTo(
            PropertiesUtils.propertyAsList(DandelionConfig.ASSET_JS_EXCLUDES.defaultDevValue()));

      // Caching-related configurations
      assertThat(config.isCachingEnabled()).isEqualTo(Boolean.parseBoolean(DandelionConfig.CACHE.defaultDevValue()));
      assertThat(config.getCacheName()).isEqualTo(DandelionConfig.CACHE_NAME.defaultDevValue());
      assertThat(config.getCacheConfigurationLocation()).isEqualTo(
            DandelionConfig.CACHE_CONFIGURATION_LOCATION.defaultDevValue());
      assertThat(config.getCacheMaxSize()).isEqualTo(20); // OVERRIDEN

      // Tooling-related configurations
      assertThat(config.isToolDebuggerEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_DEBUGGER.defaultDevValue()));
      assertThat(config.isToolBundleReloadingEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.TOOL_BUNDLE_RELOADING.defaultDevValue()));

      // Misc configurations
      assertThat(config.isMonitoringJmxEnabled()).isEqualTo(
            Boolean.parseBoolean(DandelionConfig.MONITORING_JMX.defaultDevValue()));

      System.clearProperty(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName());
      System.clearProperty(DandelionConfig.CACHE_MAX_SIZE.getName());
   }

   @Test
   public void should_set_default_cacheMaxSize_if_wrong_value() {
      Properties userProperties = new Properties();
      userProperties.put(DandelionConfig.CACHE_MAX_SIZE.getName(), "text");
      Configuration config = new Configuration(new MockFilterConfig(), userProperties, null);

      assertThat(config.getCacheMaxSize())
            .isEqualTo(Integer.parseInt(DandelionConfig.CACHE_MAX_SIZE.defaultDevValue()));
   }
}
