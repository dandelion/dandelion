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

package com.github.dandelion.core.asset.locator.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.GlobalOptionsRule;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.web.WebConstants;

import static java.util.Collections.singletonMap;

import static org.assertj.core.api.Assertions.assertThat;

public class WebappLocatorTest {

   private WebappLocator locator = new WebappLocator();
   private MockHttpServletRequest request;
   private String CONTEXT_RELATIVE_URL = "/assets/js/my.js";
   private String CONTEXT_ABSOLUTE_URL = "http://my-domain.com/context/assets/js/my.js";

   @Rule
   public GlobalOptionsRule options = new GlobalOptionsRule();
   
   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Before
   public void setup() {
      request = new MockHttpServletRequest();
      request.setContextPath("/context");
      request.setAttribute(WebConstants.DANDELION_CONTEXT_ATTRIBUTE, new Context(new MockFilterConfig()));
   }

   @Test
   public void should_return_the_processed_context_absolute_url() {
      AssetStorageUnit asu = new AssetStorageUnit("my.js", singletonMap("webapp", CONTEXT_RELATIVE_URL));
      String location = locator.getLocation(asu, request);
      assertThat(location).isEqualTo("/context" + CONTEXT_RELATIVE_URL);
   }

   @Test
   public void should_return_the_processed_context_relative_url() {
      AssetStorageUnit asu = new AssetStorageUnit("my.js", singletonMap("webapp", CONTEXT_ABSOLUTE_URL));
      String location = locator.getLocation(asu, request);
      assertThat(location).isEqualTo(CONTEXT_ABSOLUTE_URL);
   }
}
