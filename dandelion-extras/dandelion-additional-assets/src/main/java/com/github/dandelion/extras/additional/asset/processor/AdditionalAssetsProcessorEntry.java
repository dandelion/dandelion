/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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

package com.github.dandelion.extras.additional.asset.processor;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStack;
import com.github.dandelion.core.asset.processor.spi.AssetProcessorEntry;
import com.github.dandelion.core.asset.web.AssetsRequestContext;
import com.github.dandelion.core.asset.wrapper.spi.AssetLocationWrapper;
import com.github.dandelion.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Asset Processor Entry for "additional_assets" location key
 */
public class AdditionalAssetsProcessorEntry extends AssetProcessorEntry {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AdditionalAssetsProcessorEntry.class);

    public static final String ADDITIONAL_ASSETS_ENABLED_KEY = "dandelion.addition.assets.enabled";
    private boolean additionalAssetsEnabled = true;

    public AdditionalAssetsProcessorEntry() {
        this.additionalAssetsEnabled = Boolean.TRUE.toString().equals(
                Configuration.getProperty(ADDITIONAL_ASSETS_ENABLED_KEY, Boolean.toString(additionalAssetsEnabled)));

        if(DevMode.isDevModeEnabled()) {
            this.additionalAssetsEnabled = false;
        }

        LOG.info("Dandelion Additional Asset is {}", additionalAssetsEnabled?"enabled":"disabled");
    }

    @Override
    public int getRank() {
        return 100;
    }

    @Override
    public List<Asset> process(List<Asset> assets, HttpServletRequest request) {
        if(!additionalAssetsEnabled) {
            return assets;
        }

        List<Asset> processedAssets = new ArrayList<Asset>();
        for(Asset asset:assets) {
            if(asset.getLocations().size() == 1 && asset.getLocations().containsKey(getTreatmentKey())) {
                Map<String, Object> parameters = AssetsRequestContext.get(request).getParameters(asset.getName());
                Map<String, AssetLocationWrapper> wrappers = AssetStack.getAssetsLocationWrappers();
                for(Map.Entry<String, Object> entry:parameters.entrySet()) {
                    Asset additionalAsset = asset.clone(true);
                    additionalAsset.getLocations().put(entry.getKey(), entry.getValue().toString());
                    if(wrappers.containsKey(entry.getKey())) {
                        additionalAsset.getLocations().put(entry.getKey(), wrappers.get(entry.getKey()).wrapLocation(additionalAsset, request));
                    }
                    processedAssets.add(additionalAsset);
                }
            } else {
                processedAssets.add(asset);
            }
        }
        return processedAssets;
    }

    @Override
    public String getTreatmentKey() {
        return "additional_assets";
    }
}
