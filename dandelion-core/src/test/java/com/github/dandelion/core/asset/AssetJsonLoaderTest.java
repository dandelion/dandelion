package com.github.dandelion.core.asset;

import org.fest.assertions.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class AssetJsonLoaderTest {
    @BeforeClass
    public static void set_up() throws IOException {
        AssetStorage.clearAll();
        AssetDefaultLoader.defaultLoader.initialize();
    }

    @Test
    public void should_load_default_json() {
        Assertions.assertThat(AssetStorage.assetsFor()).isNotEmpty();
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(AssetStorage.assetsFor()).hasSize(1);
        assertThat(AssetStorage.assetsFor("plugin1")).hasSize(3);
        assertThat(AssetStorage.assetsFor("plugin2")).hasSize(2);
        assertThat(AssetStorage.assetsFor("plugin1addon")).hasSize(4);
        assertThat(AssetStorage.assetsFor("plugin1addon", "plugin2")).hasSize(5);
    }
}
