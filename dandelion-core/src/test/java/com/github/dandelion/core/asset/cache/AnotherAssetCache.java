package com.github.dandelion.core.asset.cache;

import java.util.Set;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.cache.spi.AbstractAssetCache;

public class AnotherAssetCache extends AbstractAssetCache {

	@Override
	public String getCacheName() {
		return "another";
	}

	@Override
	public String getAssetContent(String cacheKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Asset> getRequestAssets(String cacheKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeAssetContent(String cacheKey, String cacheContent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeRequestAssets(String cacheKey, Set<Asset> a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(String cacheKey) {
		// TODO Auto-generated method stub
		
	}
}
