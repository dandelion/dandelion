package com.github.dandelion.core.asset.cache;

import com.github.dandelion.core.asset.cache.impl.DefaultAssetsCache;

public class AnotherAssetsCache extends DefaultAssetsCache {
    @Override
    public String getAssetsCacheName() {
        return "another";
    }
}
