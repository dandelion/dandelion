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
package com.github.dandelion.core.asset.locator;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.AssetProcessor;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.web.DandelionFilter;
import com.github.dandelion.core.web.DandelionServlet;

/**
 * <p>
 * SPI for all asset locators.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public interface AssetLocator {

   /**
    * <p>
    * Initializes the configured service provider of the {@link AssetLocator}
    * SPI by using the {@link Context}.
    * 
    * @param context
    *           The {@link Context} initialized in the {@link DandelionFilter}.
    */
   void initLocator(Context context);

   /**
    * @return the location key associated to the locator.
    */
   String getLocationKey();

   /**
    * <p>
    * Computes and returns the location of the asset.
    * 
    * @param AssetStorageUnit
    *           The asset storage unit from which the location should be
    *           extracted.
    * @param request
    *           The current HTTP request.
    * @return the customized location
    */
   String getLocation(AssetStorageUnit asu, HttpServletRequest request);

   /**
    * <p>
    * Returns the content of the given {@link Asset}.
    * <p>
    * Note that this method can be used to access the asset's content after the
    * {@link AssetProcessor}'s execution.
    * 
    * @param asset
    *           The asset from which the content should be extracted.
    * @param request
    *           The current HTTP request.
    * @return a String holding the content of the {@link Asset}.
    */
   String getContent(Asset asset, HttpServletRequest request);

   /**
    * @return {@code true} if the asset has to be cached in order to be accessed
    *         by the {@link DandelionServlet}, otherwise {@code false} if it can
    *         be directly accessed.
    */
   boolean isCachingForced();
}
