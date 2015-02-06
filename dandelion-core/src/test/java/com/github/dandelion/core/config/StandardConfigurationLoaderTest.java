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
package com.github.dandelion.core.config;

import java.io.File;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.dandelion.core.DandelionException;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardConfigurationLoaderTest {

   private static StandardConfigurationLoader loader;

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @BeforeClass
   public static void setup() {
      loader = new StandardConfigurationLoader();
   }

   @Before
   public void before() throws Exception {
      System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
      System.clearProperty(Profile.DANDELION_PROFILE_ACTIVE);
   }

   @Test
   public void should_load_properties_from_classpath() throws Exception {
      Properties userProperties = loader.loadUserConfiguration();

      assertThat(userProperties).hasSize(1);
      assertThat(userProperties.getProperty("asset.locations.resolution.strategy")).isEqualTo("webapp,remote");
   }

   @Test
   public void should_load_user_properties_from_system_property_with_the_default_profile() throws Exception {
      String path = new File("src/test/resources/configuration-loader/default-mode".replace("/", File.separator))
            .getAbsolutePath();
      System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);

      Properties userProperties = loader.loadUserConfiguration();

      assertThat(userProperties).hasSize(2);
      assertThat(userProperties.getProperty("defaultKey1")).isEqualTo("defaultVal1");
      assertThat(userProperties.getProperty("defaultKey2")).isEqualTo("defaultVal2");

   }

   @Test
   public void should_throw_an_exception_if_the_configuration_file_does_not_exist_for_the_active_profile()
         throws Exception {
      String path = new File("src/test/resources/configuration-loader/default-mode".replace("/", File.separator))
            .getAbsolutePath();
      System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);
      System.setProperty(Profile.DANDELION_PROFILE_ACTIVE, "missing");

      exception.expect(DandelionException.class);
      exception.expectMessage("No file \"dandelion_missing.properties\" was found in");

      loader.loadUserConfiguration();
   }

   @Test
   public void should_load_user_properties_from_system_property_with_the_dev_profile() throws Exception {
      String path = new File("src/test/resources/configuration-loader/default-mode".replace("/", File.separator))
            .getAbsolutePath();
      System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);
      System.setProperty(Profile.DANDELION_PROFILE_ACTIVE, "dev");

      Properties userProperties = loader.loadUserConfiguration();

      assertThat(userProperties).hasSize(2);
      assertThat(userProperties.getProperty("devKey1")).isEqualTo("devVal1");
      assertThat(userProperties.getProperty("devKey2")).isEqualTo("devVal2");

   }

   @AfterClass
   public static void teardown() {
      System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
      System.clearProperty(Profile.DANDELION_PROFILE_ACTIVE);
   }
}
