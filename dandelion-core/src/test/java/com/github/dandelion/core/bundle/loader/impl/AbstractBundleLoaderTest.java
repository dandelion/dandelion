package com.github.dandelion.core.bundle.loader.impl;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.config.DandelionConfig;

public class AbstractBundleLoaderTest {

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Before
   public void setup() {
      System.setProperty(DandelionConfig.BUNDLE_LOCATION.getName(), "bundle-loader/wrong-format");
   }

   @After
   public void teardown() {
      System.clearProperty(DandelionConfig.BUNDLE_LOCATION.getName());
   }

   @Test
   public void should_throw_an_exception_when_trying_to_initialize_context() {

      exception.expect(DandelionException.class);
      exception.expectMessage("The bundle graph is not consistent for the following reasons:");
      exception
            .expectMessage(CoreMatchers
                  .containsString("The file 'bundle-loader/wrong-format/dandelion/bundle-wrong-format1.json' is wrongly formatted"));
      exception
            .expectMessage(CoreMatchers
                  .containsString("The file 'bundle-loader/wrong-format/dandelion/bundle-wrong-format2.json' is wrongly formatted"));

      new Context(new MockFilterConfig());
   }
}
