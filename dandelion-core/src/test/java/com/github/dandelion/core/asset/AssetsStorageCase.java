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

import com.github.dandelion.api.DandelionExceptionMatcher;
import com.github.dandelion.core.DandelionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.dandelion.core.asset.AssetsStorage.assetsFor;
import static com.github.dandelion.core.asset.AssetsStorage.store;
import static org.fest.assertions.Assertions.assertThat;

public class AssetsStorageCase {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    Asset asset = new Asset("name", "version", AssetType.js, "remote", "local");
    Asset asset2 = new Asset("name2", "version", AssetType.img, "remote", "local");
    Asset asset3 = new Asset("name3", "version", AssetType.js, "remote", "local");
    Asset asset4 = new Asset("name4", "version", AssetType.css, "remote", "local");
    Asset assetConflict = new Asset("name", "versionConflict", AssetType.js, "remote", "local");

    @Before
    public void set_up() {
        AssetsStorage.clearAll();
    }

    @Test
    public void should_not_store_invalid_asset() {
        store(new Asset());

        assertThat(assetsFor()).hasSize(0);
    }

    @Test
    public void should_store_asset_in_default_scope() {
        store(asset);

        assertThat(assetsFor("default")).hasSize(1).contains(asset);
    }

    @Test
    public void should_store_assets_in_default_scope() {
        store(asset);
        store(asset2);

        assertThat(assetsFor("default")).hasSize(2).contains(asset, asset2);
    }

    @Test
    public void should_access_to_assets_without_any_scope() {
        store(asset);
        store(asset2);

        assertThat(assetsFor()).hasSize(2);
    }

    @Test
    public void should_store_asset_in_another_scope() {
        store(asset);
        store(asset2);
        store(asset3, "another");

        assertThat(assetsFor("another")).hasSize(3).contains(asset, asset2, asset3);
    }

    @Test
    public void should_store_assets_in_another_level_scope() {
        store(asset);
        store(asset2);
        store(asset3, "another");
        store(asset4, "another_level", "another");

        assertThat(assetsFor("another_level")).hasSize(4).contains(asset, asset2, asset3, asset4);
    }

    @Test
    public void should_store_assets_with_same_scope_but_not_parent_scopes() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
            new DandelionExceptionMatcher(AssetsStorageError.PARENT_SCOPE_INCOMPATIBILITY)
                .set("scope", "same_scope")
                .set("parentScope", "parent_scope")
        );

        store(asset, "parent_scope");
        store(asset2, "another_parent_scope");
        store(asset3, "same_scope", "parent_scope");
        store(asset4, "same_scope", "another_parent_scope");
    }

    @Test
    public void should_store_asset_with_unknown_parent_scope() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
                new DandelionExceptionMatcher(AssetsStorageError.UNDEFINED_PARENT_SCOPE)
                        .set("parentScope", "unknown_parent_scope")
        );

        store(asset, "scope", "unknown_parent_scope");
    }

    @Test
    public void should_manage_assets_with_different_types() {
        Asset assetDifferentType = new Asset("name", "version", AssetType.css, "remote", "local");
        store(asset);
        store(assetDifferentType, "differentTypes");
        assertThat(assetsFor("differentTypes")).hasSize(2).contains(assetDifferentType);
    }

    @Test
    public void should_store_empty_scope_by_lazy_workaround() {
        store(asset);
        store(null, "empty_scope");
        store(asset2, "not_empty_scope", "empty_scope");
        assertThat(assetsFor("not_empty_scope")).hasSize(2).contains(asset, asset2);
    }

    @Test
    public void should_manage_override_assets() {
        Asset assetOverride = new Asset("name", "version2", AssetType.js, "remote", "local");
        store(asset);
        store(assetOverride, "override");
        assertThat(assetsFor("override")).hasSize(1).contains(assetOverride);
    }

    @Test
    public void should_manage_conflicts_before_storage() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
                new DandelionExceptionMatcher(AssetsStorageError.ASSET_ALREADY_EXISTS_IN_SCOPE)
                        .set("originalAsset", asset)
        );

        store(asset);
        store(assetConflict);
    }

    @Test
    public void should_manage_conflicts_on_demand() {
        store(asset2);
        store(asset, "scope");
        store(assetConflict, "another_scope");

        assertThat(assetsFor("scope", "another_scope")).hasSize(2).contains(asset, asset2);
    }

    @Test
    public void should_manage_priorities() {
        Asset assetPriority = new Asset("name", "version2", AssetType.js, "remote", "local");

        store(asset);
        store(asset4);
        store(asset2);
        store(asset3);
        store(assetPriority, "scope");

        assertThat(assetsFor("scope")).hasSize(4).containsSequence(assetPriority, asset4, asset2, asset3);
    }

    @Test
    public void should_manage_detached_scope() {
        Asset assetWithDetachedScope = new Asset("detached", "version", AssetType.js, "remote", "local");

        store(asset);
        store(assetWithDetachedScope, "scope", "none");

        assertThat(assetsFor("scope")).hasSize(1).contains(assetWithDetachedScope);
        assertThat(assetsFor("default", "scope")).hasSize(2).contains(assetWithDetachedScope, asset);
    }

    @Test
    public void should_detach_scope_not_override_other_assets() {
        Asset assetWithDetachedScope = new Asset("name", "version", AssetType.js, "remote", "local");

        store(asset);
        store(assetWithDetachedScope, "scope", "none");

        assertThat(assetsFor("scope")).hasSize(1).contains(assetWithDetachedScope);
        assertThat(assetsFor("default", "scope")).hasSize(2).contains(assetWithDetachedScope, asset);
    }

    @Test
    public void should_not_allow_the_usage_of_detached_scope_as_a_scope() {
        expectedEx.expect(DandelionException.class);
        expectedEx.expect(
                new DandelionExceptionMatcher(AssetsStorageError.DETACHED_SCOPE_NOT_ALLOWED)
                        .set("detachedScope", "none")
        );

        store(asset, "none");
    }
}
