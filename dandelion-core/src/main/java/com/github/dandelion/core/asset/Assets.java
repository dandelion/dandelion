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
 * 3. Neither the name of DataTables4j nor the names of its contributors
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

package com.github.dandelion.core.asset;

import java.util.List;

/**
 * Assets API
 */
public final class Assets {
    static AssetsConfigurator assetsConfigurator;
    static AssetsStorage assetsStorage;

    /**
     * Initialize Assets only if needed
     */
    static void initializeIfNeeded() {
        if(assetsConfigurator == null) {
            if(assetsStorage == null) {
                initializeStorageIfNeeded();
            }
            initializeConfiguratorIfNeeded();
        }
    }

    /**
     * Initialize Assets Configurator only if needed
     */
    synchronized private static void initializeConfiguratorIfNeeded() {
        if(assetsConfigurator == null) {
            assetsConfigurator = new AssetsConfigurator(assetsStorage);
            assetsConfigurator.initialize();
        }
    }

    /**
     * Initialize Assets Storage only if needed
     */
    synchronized private static void initializeStorageIfNeeded() {
        if(assetsStorage == null) {
            assetsStorage = new AssetsStorage();
        }
    }

    /**
     * Get Source of Assets<br/>
     *
     * Configured by assetsSource in 'dandelion/dandelion.properties'
     *
     * @return source of Assets
     */
    public static AssetsSource getAssetsSource() {
        initializeIfNeeded();
        return assetsConfigurator.assetsSource;
    }

    /**
     * Find Assets for Scopes
     * @param scopes scopes of assets
     * @return Assets of scopes
     */
    public static List<Asset> assetsFor(String ... scopes) {
        initializeIfNeeded();
        return assetsStorage.assetsFor(scopes);
    }
}
