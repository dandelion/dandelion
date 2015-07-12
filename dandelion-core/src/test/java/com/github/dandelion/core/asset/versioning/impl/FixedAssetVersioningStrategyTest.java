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
package com.github.dandelion.core.asset.versioning.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.asset.versioning.AssetVersioningStrategy;
import com.github.dandelion.core.config.DandelionConfig;
import com.github.dandelion.core.util.EnumUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedAssetVersioningStrategyTest {

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Rule
   public ExpectedException expectedEx = ExpectedException.none();

   @Test
   public void should_throw_an_exception_if_a_wrong_versioning_strategy_is_requested() {

      String desiredVersioningStrategy = "unknown-strategy";

      expectedEx.expect(DandelionException.class);

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_STRATEGY.getName(), desiredVersioningStrategy);
      Context dandelionContext = new Context(filterConfig);

      AssetVersioningStrategy strategy = new FixedAssetVersioningStrategy();
      strategy.init(dandelionContext);
   }

   @Test
   public void should_throw_an_exception_if_a_wrong_fixedVersionType_is_requested() {

      String desiredFixedVersionType = "unknown-type";

      expectedEx.expect(DandelionException.class);
      expectedEx.expectMessage("'" + desiredFixedVersionType
            + "' is not a valid versioning type. Possible values are: "
            + EnumUtils.printPossibleValuesOf(FixedVersionType.class));

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_STRATEGY.getName(), "fixed");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_TYPE.getName(), desiredFixedVersionType);
      Context dandelionContext = new Context(filterConfig);

      AssetVersioningStrategy strategy = new FixedAssetVersioningStrategy();
      strategy.init(dandelionContext);
   }

   @Test
   public void should_return_a_fixed_version_based_on_a_string() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_STRATEGY.getName(), "fixed");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_TYPE.getName(), "string");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_VALUE.getName(), "42");
      Context dandelionContext = new Context(filterConfig);

      AssetVersioningStrategy strategy = new FixedAssetVersioningStrategy();
      strategy.init(dandelionContext);

      assertThat(strategy.getAssetVersion(null)).isEqualTo("42");
   }

   @Test
   public void should_return_a_fixed_version_based_on_a_date_and_default_format() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_STRATEGY.getName(), "fixed");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_TYPE.getName(), "date");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_VALUE.getName(), "20141124");
      Context dandelionContext = new Context(filterConfig);

      AssetVersioningStrategy strategy = new FixedAssetVersioningStrategy();
      strategy.init(dandelionContext);

      assertThat(strategy.getAssetVersion(null)).isEqualTo("20141124");
   }

   @Test
   public void should_return_a_fixed_version_based_on_a_date_and_custom_format() {

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_STRATEGY.getName(), "fixed");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_TYPE.getName(), "date");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_DATEPATTERN.getName(), "yyyy");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_VALUE.getName(), "2015");
      Context dandelionContext = new Context(filterConfig);

      AssetVersioningStrategy strategy = new FixedAssetVersioningStrategy();
      strategy.init(dandelionContext);

      assertThat(strategy.getAssetVersion(null)).isEqualTo("2015");
   }

   @Test
   public void should_throw_an_exception_if_a_wrong_date_format_is_used() {

      String desiredDate = "2015";
      String desiredDateFormat = "wrong-format";

      expectedEx.expect(DandelionException.class);
      expectedEx.expectMessage("Wrong date pattern configured : " + desiredDateFormat);

      MockFilterConfig filterConfig = new MockFilterConfig();
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_MODE.getName(), "auto");
      filterConfig.addInitParameter(DandelionConfig.ASSET_VERSIONING_STRATEGY.getName(), "fixed");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_TYPE.getName(), "date");
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_DATEPATTERN.getName(), desiredDateFormat);
      filterConfig.addInitParameter(DandelionConfig.ASSET_FIXED_VERSION_VALUE.getName(), desiredDate);
      new Context(filterConfig);
   }
}
