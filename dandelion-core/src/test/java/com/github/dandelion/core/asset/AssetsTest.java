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
 * 3. Neither the name of DataTables4j nor the names of its contributors
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

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class AssetsTest {

    @BeforeClass
    public static void set_up_class() {
        Assets.assetsConfigurator = null;
        Assets.assetsStorage = null;
        Assets.initializeIfNeeded();
    }

    @Test
    public void should_load_default_json() {
        assertThat(Assets.assetsFor()).hasSize(1);
    }

    @Test
    public void should_load_the_assets_locations() {
        assertThat(Assets.getAssetsLocations()).contains("remote");
    }

    @Test
    public void should_be_the_remote_url_for_all_assets() {
        List<Asset> assets = Assets.assetsFor("default","detachedScope","plugin1","plugin2");
        assertThat(assets).hasSize(6);
        for(Asset asset:assets) {
            assertThat(Assets.getAssetLocation(asset)).isEqualTo("remoteURL");
        }
    }

    @Test
    public void should_exclude_assets_by_name() {
        List<Asset> assets = Assets.assetsFor("detachedScope");
        assertThat(Assets.excludeByName(assets, "asset3addon")).hasSize(1);
        assertThat(Assets.excludeByName(assets, "asset1")).hasSize(0);
    }

    @Test
    public void should_filter_assets_by_type() {
        List<Asset> assets = Assets.assetsFor("plugin1", "plugin2", "plugin1addon2", "plugin3addon");
        assertThat(assets).hasSize(6);
        assertThat(Assets.filterByType(assets, AssetType.css)).hasSize(2);
        assertThat(Assets.filterByType(assets, AssetType.js)).hasSize(4);
    }

    @Test
    public void should_manage_unknown_location() {
        List<Asset> assets = Assets.assetsFor("unknown_location");
        assertThat(assets).hasSize(2);
        for(Asset asset:assets) {
            assertThat(Assets.getAssetLocation(asset)).isNotEqualTo("URL").isEmpty();
        }
    }

    @Test
    public void should_respect_locations_order() {
        List<Asset> assets = Assets.assetsFor("locations_order");
        assertThat(assets).hasSize(3);
        for(Asset asset:assets) {
            assertThat(Assets.getAssetLocation(asset)).isEqualTo("otherURL");
        }
    }
}
