package com.github.dandelion.core.asset.cache;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AssetsCacheTest {

    @Test
    public void should_load_another_cache_system() {
        assertThat(AssetsCacheSystem.getAssetsCacheName()).isEqualTo(new AnotherAssetsCache().getAssetsCacheName());
    }
}
