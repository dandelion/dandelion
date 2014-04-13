package com.github.dandelion.core.web;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;

import org.fest.assertions.MapAssert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.core.web.WebConstants;

public class AssetRequestContextTest {

	private MockHttpServletRequest request;

	@Before
	public void setup() {
		request = new MockHttpServletRequest();
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, new Context(new MockFilterConfig()));
	}

	@Test
	public void should_store_bundles() {

		AssetRequestContext context = AssetRequestContext.get(request);
		context.addBundles("bundle1,bundle2");
		assertThat(context.getBundles(false)).hasSize(2).contains("bundle1", "bundle2");

		context.addBundles("bundle3", "bundle4");
		assertThat(context.getBundles(false)).hasSize(4).contains("bundle3", "bundle4");
	}

	@Test
	public void should_exclude_bundles() {

		AssetRequestContext context = AssetRequestContext.get(request);
		context.addBundles("bundle1,bundle2,bundle3,bundle4");
		context.excludeBundles("bundle2,bundle4");

		assertThat(context.getExcludedBundles()).contains("bundle2", "bundle4");
		assertThat(context.getBundles(false)).hasSize(4).contains("bundle2", "bundle4");
		assertThat(context.getBundles(true)).hasSize(2).contains("bundle1", "bundle3");
	}

	@Test
	public void should_add_excluded_js() {

		AssetRequestContext context = AssetRequestContext.get(request);
		context.excludeJs("asset1,asset2");

		assertThat(context.getExcludedJs()).hasSize(2).contains("asset1", "asset2");
	}

	@Test
	public void should_add_excluded_css() {

		AssetRequestContext context = AssetRequestContext.get(request);
		context.excludeCss("asset1,asset2");

		assertThat(context.getExcludedCss()).hasSize(2).contains("asset1", "asset2");
	}
	
	@Test
	public void should_store_parameters() {
		AssetRequestContext context = AssetRequestContext.get(request);

		context.addParameter("asset1", "param1", "value1");
		context.addParameter("asset1", "param2", "value2");

		assertThat(context.getParameters("asset1")).hasSize(2).includes(MapAssert.entry("param1", "value1"),
				MapAssert.entry("param2", "value2"));
		assertThat(context.getParameterValue("asset1", "unknown")).isNull();
	}

	@Test
	public void should_dont_blink_a_eye() {

		AssetRequestContext context = AssetRequestContext.get(request);
		context.addBundles("");
		assertThat(context.getBundles(false)).isEmpty();

		String nullValue = null;
		context.addBundles(nullValue);
		assertThat(context.getBundles(false)).isEmpty();
	}
	
	@Test
	public void should_initialize_excluded_bundles_from_context(){
		Context context = new Context(new MockFilterConfig());
		context.getConfiguration().setBundleExcludes(Arrays.asList("bundle1"));
		
		request = new MockHttpServletRequest();
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
		
		AssetRequestContext arc = AssetRequestContext.get(request);
		assertThat(arc.getExcludedBundles()).contains("bundle1");
		assertThat(arc.getExcludedJs()).isEmpty();
		assertThat(arc.getExcludedCss()).isEmpty();
	}
	
	@Test
	public void should_initialize_excluded_js_from_context(){
		Context context = new Context(new MockFilterConfig());
		context.getConfiguration().setAssetJsExcludes(Arrays.asList("js1, js2"));
		
		request = new MockHttpServletRequest();
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
		
		AssetRequestContext arc = AssetRequestContext.get(request);
		assertThat(arc.getExcludedBundles()).isEmpty();
		assertThat(arc.getExcludedJs()).contains("js1", "js2");
		assertThat(arc.getExcludedCss()).isEmpty();
	}
}
