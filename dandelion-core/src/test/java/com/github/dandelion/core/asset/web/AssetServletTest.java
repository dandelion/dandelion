package com.github.dandelion.core.asset.web;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.cache.AssetsCacheSystem;

public class AssetServletTest {
    private AssetServlet servlet = new AssetServlet();

    @Test
    public void should_retrieve_content_from_cache() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String content = "CONTENT" + Math.random();
        AssetsCacheSystem.storeCacheContent("should_retrieve_content_from_cache", "location", "resourceName", AssetType.css, content);
        request.setRequestURI("/test/" + AssetsCacheSystem.generateCacheKey("should_retrieve_content_from_cache", "location", "resourceName", AssetType.css));

        servlet.doGet(request, response);

        assertThat(response.getContentType()).isEqualTo(AssetType.css.getContentType());
        assertThat(response.getContentAsString()).isEqualTo(content);

    }


    @Test
    public void should_never_fail_to_retrieve_missing_content_from_cache() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/test/" + AssetsCacheSystem.generateCacheKey("should_fail_to_retrieve_missing_content_from_cache", "location", "resourceName", AssetType.css));

        servlet.doGet(request, response);

        assertThat(response.getContentAsString()).isEqualTo("");
    }


    @Test
    public void should_manage_unknown_type() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI("/test/" + AssetsCacheSystem.generateCacheKey("should_manage_unknown_type", "location", "resourceName", AssetType.css) + System.currentTimeMillis());

        servlet.doGet(request, response);

        assertThat(response.getContentAsString()).isEmpty();



    }
}
