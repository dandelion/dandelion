package com.github.dandelion.core.asset;

import org.fest.assertions.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AssetsConfiguratorCase {
    @BeforeClass
    public static void set_up() {
        new AssetsConfiguratorBean();
    }

    @Test
    public void should_load_default_json() {
        Assertions.assertThat(AssetsStorage.assetsFor()).hasSize(0);
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(AssetsStorage.assetsFor("fake")).hasSize(2);
    }

    @Test
    public void should_load_the_loading_type() {
        assertThat(AssetsConfigurator.assetsConfigurator.assetsAccess).isEqualTo("local");
    }
}
