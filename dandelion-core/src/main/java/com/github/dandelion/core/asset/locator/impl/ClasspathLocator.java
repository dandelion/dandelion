/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.dandelion.core.asset.locator.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.locator.AbstractAssetLocator;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.util.ResourceUtils;

/**
 * <p>
 * Locator for assets that use {@code classpath} as a location key.
 * </p>
 * <p>
 * Basically, this locator locates assets available in the classpath.
 * </p>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.2.0
 */
public class ClasspathLocator extends AbstractAssetLocator {

   public static final String LOCATION_KEY = "classpath";
   
   public ClasspathLocator() {
      active = true;
   }

   @Override
   public String getLocationKey() {
      return LOCATION_KEY;
   }

   @Override
   public boolean isCachingForced() {
      return true;
   }

   @Override
   public String doGetLocation(AssetStorageUnit asu, HttpServletRequest request) {
      return asu.getLocations().get(getLocationKey());
   }

   @Override
   protected String doGetContent(Asset asset, Map<String, Object> parameters, HttpServletRequest request) {
      return ResourceUtils.getFileContentFromClasspath(asset.getProcessedConfigLocation(), false);
   }
}
