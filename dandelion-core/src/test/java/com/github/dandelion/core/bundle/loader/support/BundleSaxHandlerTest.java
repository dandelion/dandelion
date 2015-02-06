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

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BundleSaxHandlerTest {

   private BundleSaxHandler bsp;

   @Test
   public void should_parse_all_possible_elements_and_attributes() throws Exception {

      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      SAXParser saxParser = saxParserFactory.newSAXParser();
      bsp = new BundleSaxHandler();
      InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("bundle-loading/xml/xml-strategy/bundle-full.xml");
      saxParser.parse(is, bsp);

      BundleStorageUnit bsu = bsp.getBsu();

      assertThat(bsu.getName()).isEqualTo("bundle-full");
      assertThat(bsu.getDependencies()).hasSize(1);
      assertThat(bsu.getDependencies()).contains("other-bundle");
      assertThat(bsu.getAssetStorageUnits()).hasSize(2);

      assertThat(bsu.getAssetStorageUnits()).extracting("name").contains("asset1", "asset2");
      assertThat(bsu.getAssetStorageUnits()).extracting("version").contains("1.0.0", "2.0.0");
      assertThat(bsu.getAssetStorageUnits()).extracting("type").contains(AssetType.js);
      assertThat(bsu.getAssetStorageUnits()).extracting("locations").hasSize(2);
   }
}
