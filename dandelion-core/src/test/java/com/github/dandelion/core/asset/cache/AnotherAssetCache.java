package com.github.dandelion.core.asset.cache;

import java.util.Set;

import org.slf4j.Logger;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.cache.AbstractCache;

public class AnotherAssetCache extends AbstractCache {

   @Override
   public String getCacheName() {
      return "another";
   }

   @Override
   protected Logger getLogger() {
      return null;
   }

   @Override
   public Set<Asset> doGet(String cacheKey) {
      return null;
   }

   @Override
   public int doPut(String cacheKey, Set<Asset> a) {
      return 0;
   }

   @Override
   public void doClear() {
   }
}
