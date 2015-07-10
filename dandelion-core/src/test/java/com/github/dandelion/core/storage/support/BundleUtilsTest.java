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
package com.github.dandelion.core.storage.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BundleUtilsTest {

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Test
   public void should_finish_initializarion_of_the_bundle() {

      BundleStorageUnit bsu = new BundleStorageUnit();
      bsu.setRelativePath("some-relative-path/until/the/fileName.json");
      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);

      BundleUtils.finalize(bsu, context);

      assertThat(bsu.getName()).isEqualTo("filename");
   }

   @Test
   public void should_finish_initialization_of_assets_using_the_location() {

      Set<AssetStorageUnit> assets = new HashSet<AssetStorageUnit>();
      AssetStorageUnit asu1 = new AssetStorageUnit();
      Map<String, String> locations = new HashMap<String, String>();
      locations.put("locationKey", "/assets/js/some-asset.js");
      asu1.setLocations(locations);
      assets.add(asu1);

      BundleStorageUnit bsu = new BundleStorageUnit("bundle-name");
      bsu.setAssetStorageUnits(assets);

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);

      BundleUtils.finalize(bsu, context);

      assertThat(asu1.getName()).isEqualTo("some-asset");
      assertThat(asu1.getType()).isEqualTo(AssetType.js);
   }

   @Test
   public void should_finish_initialization_of_assets_using_the_first_location() {

      Set<AssetStorageUnit> assets = new HashSet<AssetStorageUnit>();
      AssetStorageUnit asu1 = new AssetStorageUnit();
      Map<String, String> locations = new HashMap<String, String>();
      locations.put("locationKey", "/assets/js/some-asset.css");
      locations.put("anotherLocationKey", "//cdn.com/assets/js/some-asset.css");
      asu1.setLocations(locations);
      assets.add(asu1);

      BundleStorageUnit bsu = new BundleStorageUnit("bundle-name");
      bsu.setAssetStorageUnits(assets);

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);

      BundleUtils.finalize(bsu, context);

      assertThat(asu1.getName()).isEqualTo("some-asset");
      assertThat(asu1.getType()).isEqualTo(AssetType.css);
   }

   @Test
   public void should_perform_variable_substitution_inside_locations() {

      Set<AssetStorageUnit> assets = new HashSet<AssetStorageUnit>();
      AssetStorageUnit asu1 = new AssetStorageUnit();
      Map<String, String> locations = new HashMap<String, String>();
      locations.put("locationKey", "http://%SERVER%/assets/js/some-asset.css");
      asu1.setLocations(locations);
      assets.add(asu1);

      BundleStorageUnit bsu = new BundleStorageUnit("bundle-name");
      bsu.setAssetStorageUnits(assets);

      Properties properties = new Properties();
      properties.put("SERVER", "my-server");

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
      when(context.getConfiguration().getProperties()).thenReturn(properties);

      BundleUtils.finalize(bsu, context);

      assertThat(asu1.getLocations().get("locationKey")).isEqualTo("http://my-server/assets/js/some-asset.css");
   }

   @Test
   public void should_not_perform_variable_substitution_inside_locations_if_no_property_exist() {

      Set<AssetStorageUnit> assets = new HashSet<AssetStorageUnit>();
      AssetStorageUnit asu1 = new AssetStorageUnit();
      Map<String, String> locations = new HashMap<String, String>();
      locations.put("locationKey", "http://%SERVER%/assets/js/some-asset.css");
      asu1.setLocations(locations);
      assets.add(asu1);

      BundleStorageUnit bsu = new BundleStorageUnit("bundle-name");
      bsu.setAssetStorageUnits(assets);

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
      when(context.getConfiguration().getProperties()).thenReturn(null);

      BundleUtils.finalize(bsu, context);

      assertThat(asu1.getLocations().get("locationKey")).isEqualTo("http://%SERVER%/assets/js/some-asset.css");
   }

   @Test
   public void should_throw_an_exception_if_the_variable_does_not_exist() {

      Set<AssetStorageUnit> assets = new HashSet<AssetStorageUnit>();
      AssetStorageUnit asu1 = new AssetStorageUnit();
      Map<String, String> locations = new HashMap<String, String>();
      locations.put("locationKey", "http://%SERVER%/assets/js/some-asset.css");
      asu1.setLocations(locations);
      assets.add(asu1);

      BundleStorageUnit bsu = new BundleStorageUnit("bundle-name");
      bsu.setAssetStorageUnits(assets);

      Context context = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
      when(context.getConfiguration().getProperties()).thenReturn(new Properties());

      exception.expect(IllegalArgumentException.class);
      exception.expectMessage("The supplied set of variables doesn't contain a variable named \"SERVER\"");
      BundleUtils.finalize(bsu, context);
   }
}
