package com.github.dandelion.core.asset.processor.impl;

import org.junit.Before;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.ProcessingContext;

public abstract class AbstractProcessorTest {

   protected ProcessingContext processingContext;
   protected Context context;

   @Before
   public void setup() {
      context = new Context(new MockFilterConfig());

      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setContextPath("/context-path");

      Asset asset = new Asset();
      asset.setConfigLocation("//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap.css");
      asset.setFinalLocation("/context-path/dandelion-assets/sha1/bootstrap2-2.3.2.css");
      processingContext = new ProcessingContext(context, asset, request);
   }
}
