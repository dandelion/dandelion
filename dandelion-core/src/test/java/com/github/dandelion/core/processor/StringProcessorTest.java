package com.github.dandelion.core.processor;

import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.option.DefaultOptionProcessingContext;
import com.github.dandelion.core.option.Option;
import com.github.dandelion.core.option.OptionProcessingContext;
import com.github.dandelion.core.option.OptionProcessor;
import com.github.dandelion.core.option.StringProcessor;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.WebConstants;

import static org.assertj.core.api.Assertions.assertThat;

public class StringProcessorTest {

   private HttpServletRequest request;
   private Option<String> option = new Option<String>("optionName", new StringProcessor(), 0);

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() {
      request = new MockHttpServletRequest();
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, new Context(new MockFilterConfig()));
   }

   @Test
   public void should_update_the_entry_with_null_when_using_an_empty_string() throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, "");
      OptionProcessor processor = new StringProcessor();
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
      assertThat(entry.getValue()).isNull();
   }

   @Test
   public void should_update_the_entry_with_the_same_string() throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, "someString");
      OptionProcessor processor = new StringProcessor();
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
      assertThat(entry.getValue()).isEqualTo("someString");
   }

   @Test
   public void should_update_the_table_entry_with_the_same_string_and_update_active_bundles() throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, "bundleToAdd#someString");
      OptionProcessor processor = new StringProcessor(true);
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
      assertThat(entry.getValue()).isEqualTo("someString");
      assertThat(AssetRequestContext.get(request).getBundles(true)).contains("bundleToAdd");
   }

   @Test
   public void should_update_the_column_entry_with_the_same_string_and_update_active_bundles_with_one_bundle()
         throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, "bundleToAdd#someString");
      OptionProcessor processor = new StringProcessor(true);
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
      assertThat(entry.getValue()).isEqualTo("someString");
      assertThat(AssetRequestContext.get(request).getBundles(true)).contains("bundleToAdd");
   }

   @Test
   public void should_update_the_column_entry_with_the_same_string_and_update_active_bundles_with_multiple_bundles()
         throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, " bundle1,bundle2#someString");
      OptionProcessor processor = new StringProcessor(true);
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
      assertThat(entry.getValue()).isEqualTo("someString");
      assertThat(AssetRequestContext.get(request).getBundles(true)).contains("bundle1", "bundle2");
   }

   @Test(expected = DandelionException.class)
   public void should_throw_an_exception_when_using_a_wrong_format() throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, "bundleToAdd#");
      OptionProcessor processor = new StringProcessor(true);
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
   }

   @Test(expected = DandelionException.class)
   public void should_throw_an_exception_when_using_a_wrong_format2() throws Exception {
      Entry<Option<?>, Object> entry = new MapEntry<Option<?>, Object>(option, "#someString");
      OptionProcessor processor = new StringProcessor(true);
      OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
            processor.isBundleGraphUpdatable());
      processor.process(pc);
   }
}