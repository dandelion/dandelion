package com.github.dandelion.core.asset.cache;

import java.util.Collection;

import org.slf4j.Logger;

import com.github.dandelion.core.cache.AbstractRequestCache;
import com.github.dandelion.core.cache.CacheEntry;

public class AnotherAssetCache extends AbstractRequestCache {

   @Override
   public String getCacheName() {
      return "another";
   }

   @Override
   protected Logger getLogger() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected CacheEntry doGet(String cacheKey) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected Collection<CacheEntry> doGetAll() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected int doPut(String cacheKey, CacheEntry cacheElement) {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   protected void doClear() {
      // TODO Auto-generated method stub

   }

}
