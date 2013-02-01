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

public class AssetsConfiguratorWithDefaultsTest {
    static AssetsConfigurator assetsConfigurator;

    @BeforeClass
    public static void set_up() {
        assetsConfigurator = new AssetsConfigurator(new AssetsStorage());

        // clean loaded configuration
        assetsConfigurator.assetsLoader = null;
        assetsConfigurator.assetsLocations = null;

        // simulate Default configuration
        assetsConfigurator.setDefaults();
        assetsConfigurator.processAssetsLoading();
    }

    @Test
    public void should_load_default_json() {
        assertThat(assetsConfigurator.assetsStorage.assetsFor()).hasSize(1);
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin1")).hasSize(3);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin2")).hasSize(2);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin1addon")).hasSize(4);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin1addon", "plugin2")).hasSize(5);
    }

    @Test
    public void should_load_the_default_loading_type() {
        assertThat(assetsConfigurator.assetsLocations).isEqualTo("remote");
    }
}
