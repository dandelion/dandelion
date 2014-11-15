package com.github.dandelion.core.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class DandelionFilterTest {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private DandelionFilter dandelionFilter;

	@Before
	public void setup() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		dandelionFilter = new DandelionFilter();
	}

//	@Test
//	public void should_only_be_relevant_with_the_right_contentType() {
//		response.setContentType("text/css");
//		assertThat(dandelionFilter.isAssetInjectionApplicable(request, new ByteArrayResponseWrapper(response))).isFalse();
//
//		response.setContentType("application/pdf");
//		assertThat(dandelionFilter.isAssetInjectionApplicable(request, new ByteArrayResponseWrapper(response))).isFalse();
//
//		response.setContentType("text/html");
//		assertThat(dandelionFilter.isAssetInjectionApplicable(request, new ByteArrayResponseWrapper(response))).isTrue();
//	}
//
//	@Test
//	public void should_be_irrevelant_if_explicitely_disabled_by_request_parameter() {
//		request.setParameter(WebConstants.DANDELION_ASSET_FILTER_STATE, "false");
//		assertThat(dandelionFilter.isAssetInjectionApplicable(request, new ByteArrayResponseWrapper(response))).isFalse();
//	}
//
//	@Test
//	public void should_be_irrevelant_if_explicitely_disabled_by_request_attribute() {
//		request.setAttribute(WebConstants.DANDELION_ASSET_FILTER_STATE, "false");
//		assertThat(dandelionFilter.isAssetInjectionApplicable(request, new ByteArrayResponseWrapper(response))).isFalse();
//	}
}
