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
package com.github.dandelion.core.asset.loader;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.loader.spi.AssetsLoader;
import com.github.dandelion.core.asset.processor.impl.AssetLocationProcessorEntry;
import com.github.dandelion.core.asset.processor.spi.AssetProcessorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

public final class AssetsLoaderSystem {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetsLoaderSystem.class);

    private static ServiceLoader<AssetsLoader> serviceLoader = ServiceLoader.load(AssetsLoader.class);
    private static List<AssetsLoader> loaders;

    private AssetsLoaderSystem() {
    }

    private static void initialize() {
        if(loaders == null) {
            initializeIfNeeded();
        }
    }

    synchronized private static void initializeIfNeeded() {
        if(loaders != null) return;

        List<AssetsLoader> als = new ArrayList<AssetsLoader>();
        for (AssetsLoader al : serviceLoader) {
            als.add(al);
            LOG.info("found AssetLoader for {} named {}", al.getType(), al.getClass().getSimpleName());
        }

        loaders = als;
    }

    public static List<AssetsLoader> getLoaders() {
        initialize();
        return loaders;
    }
}
