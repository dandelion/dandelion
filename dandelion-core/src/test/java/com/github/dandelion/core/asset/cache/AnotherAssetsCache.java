package com.github.dandelion.core.asset.cache;

import com.github.dandelion.core.asset.cache.impl.DefaultAssetsCache;
import com.github.dandelion.core.asset.cache.spi.AssetsCache;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class AnotherAssetsCache extends DefaultAssetsCache {
    @Override
    public String getAssetsCacheName() {
        return "another";
    }
}
