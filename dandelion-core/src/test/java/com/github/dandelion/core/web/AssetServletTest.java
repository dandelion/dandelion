package com.github.dandelion.core.web;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.web.DandelionServlet;
import com.github.dandelion.core.web.WebConstants;

public class AssetServletTest {
	
	private DandelionServlet servlet = new DandelionServlet();

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private Context context;

	@Before
	public void setup() {
		context = new Context(new MockFilterConfig());
		request = new MockHttpServletRequest();
		request.setContextPath("/context");
		request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, context);
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void should_retrieve_content_from_cache() throws ServletException, IOException {

		Asset asset = new Asset();
		asset.setName("my-asset");
		asset.setVersion("1.0.0");
		asset.setType(AssetType.css);
		String content = "CONTENT" + Math.random();
		
		String cacheKey = context.getCacheManager().generateCacheKey("should_retrieve_content_from_cache", asset);
		
		context.getCacheManager().storeContent(cacheKey, content);
		request.setRequestURI(DandelionServlet.DANDELION_ASSETS_URL + context.getCacheManager().generateCacheKey("should_retrieve_content_from_cache", asset));

		servlet.doGet(request, response);

		assertThat(response.getContentType()).isEqualTo(AssetType.css.getContentType());
		assertThat(response.getContentAsString()).isEqualTo(content);

	}
}
