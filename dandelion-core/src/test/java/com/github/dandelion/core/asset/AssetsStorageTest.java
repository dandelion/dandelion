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

import com.github.dandelion.core.DandelionExceptionMatcher;
import com.github.dandelion.core.DandelionException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

public class AssetsStorageTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    AssetsStorage assetsStorage;
    
    static Map<String, String> locations; 
    static Asset asset, asset2, asset3, asset4, assetConflict;

    @BeforeClass
    public static void set_up_class() {
        locations = new HashMap<String, String>();
        locations.put("remote", "remoteURL");
        locations.put("local", "localPath");

        asset = new Asset("name", "version", AssetType.js, locations);
        asset2 = new Asset("name2", "version", AssetType.img, locations);
        asset3 = new Asset("name3", "version", AssetType.js, locations);
        asset4 = new Asset("name4", "version", AssetType.css, locations);
        assetConflict = new Asset("name", "versionConflict", AssetType.js, locations);
    }
    
    @Before
    public void set_up() {
        assetsStorage = new AssetsStorage();
    }

    @Test
    public void should_not_store_invalid_asset() {
        assetsStorage.store(new Asset());

        assertThat(assetsStorage.assetsFor()).hasSize(0);
    }

    @Test
    public void should_store_asset_in_default_scope() {
        assetsStorage.store(asset);

        assertThat(assetsStorage.assetsFor("default")).hasSize(1).contains(asset);
    }

    @Test
    public void should_store_assets_in_default_scope() {
        assetsStorage.store(asset);
        assetsStorage.store(asset2);

        assertThat(assetsStorage.assetsFor("default")).hasSize(2).contains(asset, asset2);
    }

    @Test
    public void should_access_to_assets_without_any_scope() {
        assetsStorage.store(asset);
        assetsStorage.store(asset2);

        assertThat(assetsStorage.assetsFor()).hasSize(2);
    }

    @Test
    public void should_store_asset_in_another_scope() {
        assetsStorage.store(asset);
        assetsStorage.store(asset2);
        assetsStorage.store(asset3, "another");

        assertThat(assetsStorage.assetsFor("another")).hasSize(3).contains(asset, asset2, asset3);
    }

    @Test
    public void should_store_assets_in_another_level_scope() {
        assetsStorage.store(asset);
        assetsStorage.store(asset2);
        assetsStorage.store(asset3, "another");
        assetsStorage.store(asset4, "another_level", "another");

        assertThat(assetsStorage.assetsFor("another_level")).hasSize(4).contains(asset, asset2, asset3, asset4);
    }

    @Test
    public void should_not_store_assets_with_same_scope_but_not_parent_scopes() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
            new DandelionExceptionMatcher(AssetsStorageError.PARENT_SCOPE_INCOMPATIBILITY)
                .set("scope", "same_scope")
                .set("parentScope", "parent_scope")
        );

        assetsStorage.store(asset, "parent_scope");
        assetsStorage.store(asset2, "another_parent_scope");
        assetsStorage.store(asset3, "same_scope", "parent_scope");
        assetsStorage.store(asset4, "same_scope", "another_parent_scope");
    }

    @Test
    public void should_not_store_asset_with_unknown_parent_scope() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
                new DandelionExceptionMatcher(AssetsStorageError.UNDEFINED_PARENT_SCOPE)
                        .set("parentScope", "unknown_parent_scope")
        );

        assetsStorage.store(asset, "scope", "unknown_parent_scope");
    }

    @Test
    public void should_manage_assets_with_different_types() {
        Asset assetDifferentType = new Asset("name", "version", AssetType.css, locations);
        assetsStorage.store(asset);
        assetsStorage.store(assetDifferentType, "differentTypes");
        assertThat(assetsStorage.assetsFor("differentTypes")).hasSize(2).contains(assetDifferentType);
    }

    @Test
    public void should_store_empty_scope_by_lazy_workaround() {
        assetsStorage.store(asset);
        assetsStorage.store(null, "empty_scope");
        assetsStorage.store(asset2, "not_empty_scope", "empty_scope");
        assertThat(assetsStorage.assetsFor("not_empty_scope")).hasSize(2).contains(asset, asset2);
    }

    @Test
    public void should_manage_override_assets() {
        Asset assetOverride = new Asset("name", "version2", AssetType.js, locations);
        assetsStorage.store(asset);
        assetsStorage.store(assetOverride, "override");
        assertThat(assetsStorage.assetsFor("override")).hasSize(1).contains(assetOverride);
    }

    @Test
    public void should_detect_conflicts_before_storage() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
                new DandelionExceptionMatcher(AssetsStorageError.ASSET_ALREADY_EXISTS_IN_SCOPE)
                        .set("originalAsset", asset)
        );

        assetsStorage.store(asset);
        assetsStorage.store(assetConflict);
    }

    @Test
    public void should_manage_conflicts_on_demand() {
        assetsStorage.store(asset2);
        assetsStorage.store(asset, "scope");
        assetsStorage.store(assetConflict, "another_scope");

        assertThat(assetsStorage.assetsFor("scope", "another_scope")).hasSize(2).contains(asset, asset2);
    }

    @Test
    public void should_manage_priorities() {
        Asset assetPriority = new Asset("name", "version2", AssetType.js, locations);

        assetsStorage.store(asset);
        assetsStorage.store(asset4);
        assetsStorage.store(asset2);
        assetsStorage.store(asset3);
        assetsStorage.store(assetPriority, "scope");

        assertThat(assetsStorage.assetsFor("scope")).hasSize(4).containsSequence(assetPriority, asset4, asset2, asset3);
    }

    @Test
    public void should_manage_detached_scope() {
        Asset assetWithDetachedScope = new Asset("detached", "version", AssetType.js, locations);

        assetsStorage.store(asset);
        assetsStorage.store(assetWithDetachedScope, "scope", "none");

        assertThat(assetsStorage.assetsFor("scope")).hasSize(1).contains(assetWithDetachedScope);
        assertThat(assetsStorage.assetsFor("default", "scope")).hasSize(2).contains(assetWithDetachedScope, asset);
    }

    @Test
    public void should_detach_scope_not_override_other_assets() {
        Asset assetWithDetachedScope = new Asset("name", "version", AssetType.js, locations);

        assetsStorage.store(asset);
        assetsStorage.store(assetWithDetachedScope, "scope", "none");

        assertThat(assetsStorage.assetsFor("scope")).hasSize(1).contains(assetWithDetachedScope);
        assertThat(assetsStorage.assetsFor("default", "scope")).hasSize(2).contains(assetWithDetachedScope, asset);
    }

    @Test
    public void should_not_allow_the_usage_of_detached_scope_as_a_scope() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
                new DandelionExceptionMatcher(AssetsStorageError.DETACHED_SCOPE_NOT_ALLOWED)
                        .set("detachedScope", "none")
        );

        assetsStorage.store(asset, "none");
    }
}
