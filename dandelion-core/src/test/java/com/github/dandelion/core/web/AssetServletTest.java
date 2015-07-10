package com.github.dandelion.core.web;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.StorageEntry;
import com.github.dandelion.core.util.AssetUtils;
import com.github.dandelion.core.util.DigestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetServletTest {

   private DandelionServlet servlet = new DandelionServlet();

   private MockHttpServletRequest request;
   private MockHttpServletResponse response;
   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() {
      context = new Context(new MockFilterConfig());
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
      response = new MockHttpServletResponse();
   }

   @Test
   public void should_retrieve_the_asset_content_from_storage() throws ServletException, IOException {

      Asset asset = new Asset();
      asset.setName("my-asset");
      asset.setBundle("a-bundle");
      asset.setVersion("1.0.0");
      asset.setType(AssetType.css);
      asset.setConfigLocationKey("any-location-key");
      asset.setStorageKey(AssetUtils.generateStorageKey(asset, request));
      String contents = "CONTENT" + Math.random();

      request.setAttribute(WebConstants.DANDELION_REQUEST_KEY, DigestUtils.md5Digest("anystring"));

      context.getAssetStorage().put(asset.getStorageKey(), new StorageEntry(asset, contents));

      String finalLocation = AssetUtils.getAssetFinalLocation(request, asset, "");
      request.setRequestURI(finalLocation);

      servlet.doGet(request, response);

      assertThat(response.getContentType()).isEqualTo(AssetType.css.getContentType());
      assertThat(response.getContentAsString()).isEqualTo(contents);
   }
}
