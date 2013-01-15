package com.github.dandelion.api.asset;

import org.junit.Before;
import org.junit.Test;

import static com.github.dandelion.api.asset.AssetStorage.assetsFor;
import static com.github.dandelion.api.asset.AssetStorage.store;
import static org.fest.assertions.Assertions.assertThat;

public class AssetStorageTest {
    Asset asset = new Asset("name", "version", AssetType.js, "remote", "local");
    Asset assetConflict = new Asset("name", "versionConflict", AssetType.js, "remote", "local");
    Asset assetOverride = new Asset("name", "version2", AssetType.css, "remote", "local");
    Asset asset2 = new Asset("name2", "version", AssetType.img, "remote", "local");
    Asset asset3 = new Asset("name3", "version", AssetType.js, "remote", "local");
    Asset asset4 = new Asset("name4", "version", AssetType.css, "remote", "local");
    Asset invalidAsset = new Asset();

    @Before
    public void set_up() {
        AssetStorage.clearAll();
    }

    @Test
    public void should_not_store_invalid_asset() {
        store(invalidAsset);

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

    @Test(expected = ParentScopeIncompatibilityException.class)
    public void should_store_assets_with_same_scope_but_not_parent_scopes() {
        store(asset, "parent_scope");
        store(asset2, "another_parent_scope");
        store(asset3, "same_scope", "parent_scope");
        store(asset4, "same_scope", "another_parent_scope");
    }

    @Test(expected = UndefinedParentScopeException.class)
    public void should_store_asset_with_unknown_parent_scope() {
        store(asset, "scope", "unknown_parent_scope");
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
        store(asset);
        store(assetOverride, "override");
        assertThat(assetsFor("override")).hasSize(1).contains(assetOverride);
    }

    @Test(expected = AssetAlreadyExistsInScopeException.class)
    public void should_manage_conflicts_before_storage() {
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
}
