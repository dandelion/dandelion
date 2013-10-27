package com.github.dandelion.core.asset.web;

import org.fest.assertions.MapAssert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.github.dandelion.core.asset.web.AssetParameters.GLOBAL_GROUP;
import static org.fest.assertions.Assertions.assertThat;

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
        context.addParameter("asset2", "param1", "value1", "groupId1");
        context.addParameter("asset2", "param1", "value2", "groupId2");

        AssetParameters params = context.getParameters();
        assertThat(params.getGroupIds("asset1")).hasSize(1).contains(GLOBAL_GROUP);
        assertThat(params.getGroupIds("asset2")).hasSize(2).contains("groupId1", "groupId2");
        assertThat(params.getParameters("asset1", GLOBAL_GROUP)).hasSize(2).includes(
                MapAssert.entry("param1", "value1"), MapAssert.entry("param2", "value2"));
        assertThat(params.getParameters("asset2", "groupId1"))
                .hasSize(1).includes(MapAssert.entry("param1", "value1"));
        assertThat(params.getParameters("asset2", "groupId2"))
                .hasSize(1).includes(MapAssert.entry("param1", "value2"));
        assertThat(params.getParameters("asset1", "unknown")).isNull();
    }
}
