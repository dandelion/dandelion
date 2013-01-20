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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AssetsDefaultLoaderCase {
    @BeforeClass
    public static void set_up() {
        // clean assets storage before any test
        AssetsStorage.clearAll();

        // clean loaded configuration
        AssetsConfigurator.assetsConfigurator.assetsLoader = null;
        AssetsConfigurator.assetsConfigurator.assetsAccess = null;

        // simulate Default configuration
        AssetsConfigurator.assetsConfigurator.setDefaults();
        AssetsConfigurator.assetsConfigurator.processAssetsLoading();
    }

    @Test
    public void should_load_default_json() {
        assertThat(AssetsStorage.assetsFor()).hasSize(1);
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(AssetsStorage.assetsFor("plugin1")).hasSize(3);
        assertThat(AssetsStorage.assetsFor("plugin2")).hasSize(2);
        assertThat(AssetsStorage.assetsFor("plugin1addon")).hasSize(4);
        assertThat(AssetsStorage.assetsFor("plugin1addon", "plugin2")).hasSize(5);
    }

    @Test
    public void should_load_the_default_loading_type() {
        assertThat(AssetsConfigurator.assetsConfigurator.assetsAccess).isEqualTo("remote");
    }
}
