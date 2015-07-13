package com.github.dandelion.core.asset.locator.impl;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.web.WebConstants;

import static java.util.Collections.singletonMap;

import static org.assertj.core.api.Assertions.assertThat;

public class FileLocatorTest {

   private FileLocator locator = new FileLocator();
   private MockHttpServletRequest request;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Before
   public void setup() {
      request = new MockHttpServletRequest();
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, new Context(new MockFilterConfig()));
   }

   @Test
   public void should_return_the_same_internal_url() {

      File asset = new File("src/test/resources/locator/asset.js");
      String filePath = asset.getAbsolutePath();
      AssetStorageUnit asu = new AssetStorageUnit("my.js", singletonMap("file", filePath));
      String location = locator.getLocation(asu, null);
      assertThat(location).isEqualTo(asset.toURI().toString());
   }
   
   @Test
   public void should_return_the_asset_contents() {
      String filePath = new File("src/test/resources/locator/asset.js").toURI().toString();

      Asset asset = new Asset();
      asset.setProcessedConfigLocation(filePath);
      String content = locator.getContent(asset, request);
      assertThat(content).isEqualTo("/* content */");
   }
}
