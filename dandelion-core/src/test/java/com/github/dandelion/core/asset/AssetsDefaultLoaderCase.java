package com.github.dandelion.core.asset;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AssetsDefaultLoaderCase {
    @BeforeClass
    public static void set_up() {
        // clean assets storage before any test
        AssetsStorage.clearAll();

        // clean loaded configuration
        AssetsConfigurator.assetsConfigurator.assetsLoader = null;
        AssetsConfigurator.assetsConfigurator.assetsAccess = null;

        // simulate Default configuration
        AssetsConfigurator.assetsConfigurator.setDefaults();
        AssetsConfigurator.assetsConfigurator.processAssetsLoading();
    }

    @Test
    public void should_load_default_json() {
        assertThat(AssetsStorage.assetsFor()).hasSize(1);
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(AssetsStorage.assetsFor("plugin1")).hasSize(3);
        assertThat(AssetsStorage.assetsFor("plugin2")).hasSize(2);
        assertThat(AssetsStorage.assetsFor("plugin1addon")).hasSize(4);
        assertThat(AssetsStorage.assetsFor("plugin1addon", "plugin2")).hasSize(5);
    }

    @Test
    public void should_load_the_default_loading_type() {
        assertThat(AssetsConfigurator.assetsConfigurator.assetsAccess).isEqualTo("remote");
    }
}
