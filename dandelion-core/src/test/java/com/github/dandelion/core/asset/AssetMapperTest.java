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
package com.github.dandelion.core.asset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.util.DigestUtils;
import com.github.dandelion.core.web.WebConstants;

import static java.util.Collections.singletonMap;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetMapperTest {

   private AssetMapper assetMapper;
   private MockHttpServletRequest request;
   private Context context;
   
   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Test
   public void should_map_an_asu_to_a_vendor_asset() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      assetMapper = new AssetMapper(context, request);

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setType(AssetType.js);
      asu.setVersion("any-version");
      asu.setLocations(singletonMap("classpath", "locator/asset-name.js"));
      asu.setVendor(true);

      Asset asset = assetMapper.mapToAsset(asu);

      assertThat(asset.isVendor()).isTrue();
      assertThat(asset.getName()).isEqualTo("asset-name");
      assertThat(asset.getType()).isEqualTo(AssetType.js);
      assertThat(asset.getVersion()).isEqualTo("any-version");
      assertThat(asset.getConfigLocationKey()).isEqualTo("classpath");
      assertThat(asset.getConfigLocation()).isEqualTo("locator/asset-name.js");
      assertThat(asset.getProcessedConfigLocation()).isEqualTo("locator/asset-name.js");
      assertThat(asset.getFinalLocation()).isEqualTo(asset.getProcessedConfigLocation());
   }

   @Test
   public void should_map_an_asu_to_an_asset_when_minification_is_enabled() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_MINIFICATION.getName(), "true");
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      request.setAttribute(WebConstants.DANDELION_REQUEST_KEY, DigestUtils.md5Digest("anystring"));
      assetMapper = new AssetMapper(context, request);

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setType(AssetType.js);
      asu.setVersion("any-version");
      asu.setLocations(singletonMap("classpath", "locator/asset.js"));

      Asset asset = assetMapper.mapToAsset(asu);

      assertThat(asset.isVendor()).isFalse();
      assertThat(asset.getName()).isEqualTo("asset-name");
      assertThat(asset.getType()).isEqualTo(AssetType.js);
      assertThat(asset.getVersion()).isEqualTo("any-version");
      assertThat(asset.getConfigLocationKey()).isEqualTo("classpath");
      assertThat(asset.getConfigLocation()).isEqualTo("locator/asset.js");
      assertThat(asset.getProcessedConfigLocation()).isEqualTo("locator/asset.js");
      assertThat(asset.getFinalLocation()).matches(
            "/context/dandelion-assets/[a-f0-9]{32}/[a-f0-9]{32}/js/asset-name-any-version.js");
   }

   @Test
   public void should_map_an_asu_to_an_asset_when_caching_is_forced() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      request.setAttribute(WebConstants.DANDELION_REQUEST_KEY, DigestUtils.md5Digest("anystring"));
      assetMapper = new AssetMapper(context, request);

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setType(AssetType.js);
      asu.setVersion("any-version");
      asu.setLocations(singletonMap("classpath", "locator/asset.js"));

      Asset asset = assetMapper.mapToAsset(asu);

      assertThat(asset.isVendor()).isFalse();
      assertThat(asset.getName()).isEqualTo("asset-name");
      assertThat(asset.getType()).isEqualTo(AssetType.js);
      assertThat(asset.getVersion()).isEqualTo("any-version");
      assertThat(asset.getConfigLocationKey()).isEqualTo("classpath");
      assertThat(asset.getConfigLocation()).isEqualTo("locator/asset.js");
      assertThat(asset.getProcessedConfigLocation()).isEqualTo("locator/asset.js");
      assertThat(asset.getFinalLocation()).matches(
            "/context/dandelion-assets/[a-f0-9]{32}/[a-f0-9]{32}/js/asset-name-any-version.js");
   }

   @Test
   public void should_throw_an_exception_when_the_asset_has_no_location() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      assetMapper = new AssetMapper(context, request);

      exception.expect(DandelionException.class);
      exception
            .expectMessage("No location is configured for the asset 'asset-name' (js, v1.0.0). Please add at least one location in the corresponding JSON file.");

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setType(AssetType.js);
      asu.setVersion("1.0.0");
      assetMapper.mapToAsset(asu);
   }

   @Test
   public void should_throw_an_exception_when_the_asset_has_empty_locations() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      assetMapper = new AssetMapper(context, request);

      exception.expect(DandelionException.class);
      exception
            .expectMessage("No location is configured for the asset 'asset-name' (js, v1.0.0). Please add at least one location in the corresponding JSON file.");

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setType(AssetType.js);
      asu.setVersion("1.0.0");
      asu.setLocations(Collections.<String, String> emptyMap());
      assetMapper.mapToAsset(asu);
   }

   @Test
   public void should_throw_an_exception_when_the_asset_has_an_unknown_location() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      assetMapper = new AssetMapper(context, request);

      exception.expect(DandelionException.class);
      exception.expectMessage("The location key 'foo' is not valid. Please choose a valid one among "
            + context.getAssetLocatorsMap().keySet() + ".");

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setType(AssetType.js);
      asu.setVersion("1.0.0");
      asu.setLocations(singletonMap("foo", "/assets/js/asset-name.js"));
      assetMapper.mapToAsset(asu);
   }

   @Test
   public void should_select_classpath_as_a_first_authorized_location() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.getName(),
            DandelionConfig.ASSET_LOCATIONS_RESOLUTION_STRATEGY.defaultDevValue());
      context = new Context(filterConfig);
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      request.setAttribute(WebConstants.DANDELION_REQUEST_KEY, DigestUtils.md5Digest("anystring"));
      assetMapper = new AssetMapper(context, request);

      AssetStorageUnit asu = new AssetStorageUnit();
      asu.setName("asset-name");
      asu.setBundle("any-bundle");
      asu.setType(AssetType.js);

      Map<String, String> locations = new HashMap<String, String>();
      locations.put("remote", "//asset-name.js");
      locations.put("classpath", "locator/asset.js");
      asu.setLocations(locations);

      Asset asset = assetMapper.mapToAsset(asu);

      assertThat(asset.getConfigLocation()).isEqualTo("locator/asset.js");
      assertThat(asset.getConfigLocationKey()).isEqualTo("classpath");
      assertThat(asset.getProcessedConfigLocation()).isEqualTo("locator/asset.js");
      assertThat(asset.getFinalLocation()).matches(
            "/context/dandelion-assets/[a-f0-9]{32}/[a-f0-9]{32}/js/asset-name-[a-f0-9]{32}.js");
   }
}
