/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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
package com.github.dandelion.core.bundle.loader.impl;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.bundle.loader.BundleLoader;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DandelionBundleLoaderTest {

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Before
   public void setup() {
      System.setProperty(DandelionConfig.BUNDLE_LOCATION.getName(), "bundle-loader/loading-vendor-user");
   }

   @After
   public void teardown() {
      System.clearProperty(DandelionConfig.BUNDLE_LOCATION.getName());
   }

   @Test
   public void shouldLoadUserBundles() {

      BundleLoader dandelionBundleLoader = new DandelionBundleLoader(new Context(new MockFilterConfig()), false);
      List<BundleStorageUnit> bsus = dandelionBundleLoader.getRegularBundles();
      assertThat(bsus).hasSize(2);
      assertThat(bsus).extracting("name").contains("regular-bundle1", "regular-bundle2");
      assertThat(bsus).extracting("name").doesNotContain("vendor-bundle1");
   }

   @Test
   public void shouldLoadVendorBundlesInNonStandaloneMode() {

      BundleLoader dandelionBundleLoader = new DandelionBundleLoader(new Context(new MockFilterConfig()), false);
      List<BundleStorageUnit> bsus = dandelionBundleLoader.getVendorBundles();
      assertThat(bsus).hasSize(1);
      assertThat(bsus).extracting("name").contains("vendor-bundle1");
      assertThat(bsus).extracting("name").doesNotContain("regular-bundle1", "regular-bundle2");
   }

   @Test
   public void shouldLoadVendorBundlesInStandaloneMode() {

      BundleLoader dandelionBundleLoader = new DandelionBundleLoader(new Context(new MockFilterConfig()), true);
      List<BundleStorageUnit> bsus = dandelionBundleLoader.getVendorBundles();
      assertThat(bsus).hasSize(0);
   }
}
