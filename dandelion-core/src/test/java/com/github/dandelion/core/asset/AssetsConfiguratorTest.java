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
package com.github.dandelion.core.asset;

import static com.github.dandelion.core.asset.AssetDOMPosition.body;
import static com.github.dandelion.core.asset.AssetDOMPosition.head;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.util.Collections.list;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dandelion.core.asset.loader.spi.AssetsLoader;
import com.github.dandelion.fakedomain.AssetsFakeLoader;

public class AssetsConfiguratorTest {
    static AssetsConfigurator assetsConfigurator;

    @BeforeClass
    public static void set_up() {
        assetsConfigurator = new AssetsConfigurator(new AssetsStorage());
        assetsConfigurator.initialize();
    }

    @Test
    public void should_load_default_scope() {
        assertThat(assetsConfigurator.assetsStorage.assetsFor()).hasSize(1);
    }

    @Test
    public void should_load_other_scopes() {
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin1")).hasSize(3);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin2")).hasSize(3);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin1addon")).hasSize(4);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin1addon", "plugin2")).hasSize(6);
        assertThat(assetsConfigurator.assetsStorage.assetsFor("plugin4")).hasSize(3)
                .onProperty("dom").containsSequence(head, null, body);
    }

    @Test
    public void should_load_the_assets_locations() {
        assertThat(assetsConfigurator.assetsLocations).containsSequence("other", "remote", "local");
    }

    @Test
    public void should_work_with_another_loader() {
        AssetsConfigurator anotherConfigurator = new AssetsConfigurator(new AssetsStorage());

        // simulate Default configuration
        anotherConfigurator.setDefaultsIfNeeded();

        // clean loaded configuration
        anotherConfigurator.assetsLoaders = new ArrayList<AssetsLoader>();
        anotherConfigurator.assetsLoaders.add(new AssetsFakeLoader());
        anotherConfigurator.assetsLocations = list("local");

        anotherConfigurator.processAssetsLoading(false);

        assertThat(anotherConfigurator.assetsStorage.assetsFor()).hasSize(0);
        assertThat(anotherConfigurator.assetsStorage.assetsFor("fake")).hasSize(2);
        assertThat(anotherConfigurator.assetsLocations).contains("local");
    }
}
