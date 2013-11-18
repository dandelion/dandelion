/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.asset;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class AssetStackTest {
    private static MockHttpServletRequest request;

    @BeforeClass
    public static void setup(){
        request = new MockHttpServletRequest();
    }

    @Test
    public void should_contains_assets() {
        assertThat(AssetStack.isEmpty()).isFalse();
    }

    @Test
    public void should_load_default_json() {
        assertThat(AssetStack.assetsFor()).hasSize(1);
    }

    @Test
    public void should_load_the_assets_locations() {
        assertThat(AssetStack.getAssetsLocations()).contains("remote");
    }

    @Test
    public void should_be_the_remote_url_for_all_assets() {
        List<Asset> assets = AssetStack.prepareAssetsFor(request, new String[]{"default", "detachedScope", "plugin1", "plugin2"}, new String[0]);
        assertThat(assets).hasSize(6);
        for(Asset asset:assets) {
            assertThat(asset.getLocations().values()).contains("remoteURL");
        }
    }

    @Test
    public void should_contains_assets_for_scope() {
        assertThat(AssetStack.existsAssetsFor(new String[] {"plugin1", "plugin2", "plugin1addon2", "plugin3addon"}, new String[0])).isTrue();
    }

    @Test
    public void should_exclude_assets_by_name() {
        List<Asset> assets = AssetStack.assetsFor("detachedScope");
        assertThat(AssetStack.excludeByName(assets, "asset3addon")).hasSize(1);
        assertThat(AssetStack.excludeByName(assets, "asset1")).hasSize(0);
    }

    @Test
    public void should_filter_assets_by_type() {
        List<Asset> assets = AssetStack.assetsFor("plugin1", "plugin2", "plugin1addon2", "plugin3addon");
        assertThat(assets).hasSize(7);
        assertThat(AssetStack.filterByType(assets, AssetType.css)).hasSize(3);
        assertThat(AssetStack.filterByType(assets, AssetType.js)).hasSize(4);
    }

    @Test
    public void should_filter_assets_by_dom() {
        List<Asset> assets = AssetStack.assetsFor("plugin1", "plugin2", "plugin1addon2", "plugin3addon");
        assertThat(assets).hasSize(7);
        assertThat(AssetStack.filterByDOMPosition(assets, AssetDOMPosition.head)).hasSize(3);
        assertThat(AssetStack.filterByDOMPosition(assets, AssetDOMPosition.body)).hasSize(4);
    }

    @Test
    public void should_manage_unknown_location() {
        List<Asset> assets = AssetStack.prepareAssetsFor(request, new String[]{"unknown_location"}, new String[0]);
        assertThat(assets).hasSize(2);
        for(Asset asset:assets) {
            assertThat(asset.getLocations().values()).hasSize(1).contains("URL");
        }
    }

    @Test
    public void should_respect_locations_order() {
        List<Asset> assets = AssetStack.prepareAssetsFor(request, new String[]{"locations_order"}, new String[0]);
        assertThat(assets).hasSize(3);
        for(Asset asset:assets) {
            assertThat(asset.getLocations().values()).contains("otherURL");
        }
    }
}
