package com.github.dandelion.core.asset.cache;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class AssetCacheTest {

    @Test
    public void should_load_another_cache_system() {
        assertThat(AssetCacheSystem.getAssetsCacheName()).isEqualTo(new AnotherAssetCache().getAssetsCacheName());
    }
}
