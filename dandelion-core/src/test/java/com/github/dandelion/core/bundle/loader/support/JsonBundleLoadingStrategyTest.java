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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.bundle.loader.strategy.JsonBundleLoadingStrategy;
import com.github.dandelion.core.bundle.loader.strategy.LoadingStrategy;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JsonBundleLoadingStrategyTest {

   private LoadingStrategy loadingStrategy;

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Mock(answer = Answers.RETURNS_DEEP_STUBS)
   private Context context;

   @Before
   public void setup() {
      loadingStrategy = new JsonBundleLoadingStrategy(context);
   }

   @Test
   public void should_return_empty_set() {
      Set<String> resourcePaths = loadingStrategy.getResourcePaths("unkown-folder", null);
      assertThat(resourcePaths).isEmpty();
   }

   @Test
   public void should_find_json_resource_paths() {
      Set<String> resourcePaths = loadingStrategy.getResourcePaths("bundle-loader/json-strategy", null);
      assertThat(resourcePaths).hasSize(2);
   }

   @Test
   public void should_map_to_bundles_using_resource_paths() {
      Set<String> resourcePaths = loadingStrategy.getResourcePaths("bundle-loader/json-strategy", null);
      List<BundleStorageUnit> bundles = loadingStrategy.mapToBundles(resourcePaths);

      assertThat(bundles).hasSize(2);
      assertThat(bundles).extracting("dependencies").containsExactly(null, null);
      assertThat(bundles).extracting("name").contains("bundle2");
      assertThat(bundles).extracting("assetStorageUnits").hasSize(2);
   }

   @Test
   public void should_throw_an_exception_when_the_bundle_is_badly_formatted() {

      exception.expect(DandelionException.class);
      exception.expectMessage(CoreMatchers.containsString("line: 5, column: 30"));
      loadingStrategy.mapToBundles(new HashSet<String>(Arrays
            .asList("bundle-loader/wrong-format/dandelion/bundle-wrong-format1.json")));
   }
}
