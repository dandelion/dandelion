package com.github.dandelion.core.asset.web;

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.MapAssert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class AssetsRequestContextTest {

    @Test
    public void should_store_scopes() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        AssetsRequestContext context = AssetsRequestContext.get(request);
        context.addScopes("scope1,scope2");
        assertThat(context.getScopes(false)).hasSize(2).contains("scope1", "scope2");

        context.addScopes("scope3", "scope4");
        assertThat(context.getScopes(false)).hasSize(4).contains("scope3", "scope4");
    }

    @Test
    public void should_exclude_scopes() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        AssetsRequestContext context = AssetsRequestContext.get(request);
        context.addScopes("scope1,scope2,scope3,scope4");
        context.excludeScopes("scope2,scope4");

        assertThat(context.getExcludedScopes()).contains("scope2", "scope4");
        assertThat(context.getScopes(false)).hasSize(4).contains("scope2", "scope4");
        assertThat(context.getScopes(true)).hasSize(2).contains("scope1", "scope3");
    }

    @Test
    public void should_exclude_assets() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        AssetsRequestContext context = AssetsRequestContext.get(request);
        context.excludeAssets("asset1,asset2");

        assertThat(context.getExcludedAssets()).hasSize(2).contains("asset1", "asset2");
    }

    @Test
    public void should_keep_a_rendering_state() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        AssetsRequestContext context = AssetsRequestContext.get(request);
        assertThat(context.isAlreadyRendered()).isFalse();

        context.hasBeenRendered();
        assertThat(context.isAlreadyRendered()).isTrue();
        assertThat(AssetsRequestContext.get(request).isAlreadyRendered()).isTrue();
    }

    @Test
    public void should_store_parameters() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        AssetsRequestContext context = AssetsRequestContext.get(request);

        context.addParameter("asset1", "param1", "value1");
        context.addParameter("asset1", "param2", "value2");

        assertThat(context.getParameters("asset1")).hasSize(2).includes(
                MapAssert.entry("param1", "value1"), MapAssert.entry("param2", "value2"));
        assertThat(context.getParameterValue("asset1", "unknown")).isNull();
    }
}
