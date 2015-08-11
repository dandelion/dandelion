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
package com.github.dandelion.core.bundle.loader.support;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BowerPreLoaderTest {

   private Context context;

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();

   @Before
   public void before() {
      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.BOWER_COMPONENTS_LOCATION.getName(), "classpath:pre-loader");
      context = new Context(filterConfig);
   }

   @Test
   public void should_map_the_bower_component_to_a_bundleStorageUnit() {

      BowerPreLoader bower = new BowerPreLoader();
      bower.init(context);
      List<BundleStorageUnit> bsus = bower.getBundlesFromClasspath("pre-loader");
      assertThat(bsus).hasSize(1);

      BundleStorageUnit bsu = bsus.iterator().next();
      assertThat(bsu.getName()).isEqualTo("jquery");
      assertThat(bsu.getAssetStorageUnits()).hasSize(1);
      AssetStorageUnit asu = bsu.getAssetStorageUnits().iterator().next();
      assertThat(asu.getName()).isEqualTo("jquery");
      assertThat(asu.getVersion()).isEqualTo("2.1.4");
      assertThat(asu.getType()).isEqualTo(AssetType.js);
      assertThat(asu.getLocations()).hasSize(1);
      assertThat(asu.getLocations()).containsKey("webapp");
      assertThat(asu.getLocations()).containsValue("/pre-loaderjquery/dist/jquery.js");
   }
}
