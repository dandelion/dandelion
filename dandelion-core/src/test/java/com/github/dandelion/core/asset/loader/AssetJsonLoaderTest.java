package com.github.dandelion.core.asset.loader;

import com.github.dandelion.core.asset.AssetStorage;
import com.github.dandelion.core.asset.json.AssetJsonLoader;
import org.fest.assertions.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AssetJsonLoaderTest {
    static AssetJsonLoader loader;

    @BeforeClass
    public static void set_up() {
        AssetStorage.clearAll();
        loader = new AssetJsonLoader();
        loader.load();
    }

    @Test
    public void should_load_default_json() {
        Assertions.assertThat(AssetStorage.assetsFor()).isNotEmpty();
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
