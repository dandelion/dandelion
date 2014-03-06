package com.github.dandelion.core.asset.cache;

import com.github.dandelion.core.asset.cache.impl.HashMapAssetCache;

public class AnotherAssetCache extends HashMapAssetCache {

	@Override
	public String getCacheName() {
		return "another";
	}
}
