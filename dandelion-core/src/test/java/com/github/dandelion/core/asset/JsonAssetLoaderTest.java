package com.github.dandelion.core.asset;

import com.github.dandelion.api.asset.AssetStorage;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class JsonAssetLoaderTest {
    static JsonAssetLoader loader;

    @BeforeClass
    public static void set_up() {
        loader = new JsonAssetLoader();
        loader.load();
    }

    @Test
    public void should_load_default_json() {
        assertThat(AssetStorage.assetsFor()).isNotEmpty();
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(AssetStorage.assetsFor()).hasSize(1);
        assertThat(AssetStorage.assetsFor("plugin1")).hasSize(2);
        assertThat(AssetStorage.assetsFor("plugin2")).hasSize(2);
        assertThat(AssetStorage.assetsFor("plugin1addon")).hasSize(3);
        assertThat(AssetStorage.assetsFor("plugin1addon", "plugin2")).hasSize(4);
    }
}
