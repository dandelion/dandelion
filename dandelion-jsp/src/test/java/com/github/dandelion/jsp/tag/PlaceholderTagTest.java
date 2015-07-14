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
package com.github.dandelion.jsp.tag;

import java.io.UnsupportedEncodingException;

import javax.servlet.jsp.JspException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;

import com.github.dandelion.core.web.AssetRequestContext;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

public class PlaceholderTagTest {

   private PlaceholderTag tag;
   private MockServletContext mockServletContext;
   private MockPageContext mockPageContext;
   private MockHttpServletRequest mockServletRequest;

   @Mock
   private AssetRequestContext arc;

   @Before
   public void setup() {
      mockServletContext = new MockServletContext();
      mockServletRequest = new MockHttpServletRequest();
      mockPageContext = new MockPageContext(mockServletContext, mockServletRequest);

      tag = new PlaceholderTag();
      tag.setPageContext(mockPageContext);
      MockitoAnnotations.initMocks(this);
   }

   @Rule
   public ExpectedException expectedEx = ExpectedException.none();

   @Test
   public void shouldThrowAnExceptionUsingAWrongPlaceholderType() throws JspException {

      expectedEx.expect(JspException.class);
      expectedEx.expectMessage("Only \"js\" and \"css\" are allowed in the \"type\" attribute");

      tag.setType("wrong-placeholder-type");
      tag.doEndTag();
   }

   @Test
   public void shouldUpdateTheResponseWithAPlaceholderForJsInjection()
         throws JspException, UnsupportedEncodingException {

      mockServletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      tag.setType("js");
      tag.doEndTag();
      assertThat(mockPageContext.getContentAsString()).contains("dandelionPlaceholderjs");
   }

   @Test
   public void shouldUpdateTheResponseWithAPlaceholderForCssInjection()
         throws JspException, UnsupportedEncodingException {

      mockServletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      tag.setType("css");
      tag.doEndTag();
      assertThat(mockPageContext.getContentAsString()).contains("dandelionPlaceholdercss");
   }

   @Test
   public void shouldThrowAnExceptionWhenUsingTheSameJsPlaceholderTwice() throws JspException {

      when(arc.getJsPlaceholder()).thenReturn("dandelionPlaceholderjs");
      mockServletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      expectedEx.expect(JspException.class);
      expectedEx.expectMessage("The placeholder type \"js\" can be used only once in the same page");

      tag.setType("js");
      tag.doEndTag();
   }

   @Test
   public void shouldThrowAnExceptionWhenUsingTheSameCssPlaceholderTwice() throws JspException {

      when(arc.getCssPlaceholder()).thenReturn("dandelionPlaceholdercss");
      mockServletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      expectedEx.expect(JspException.class);
      expectedEx.expectMessage("The placeholder type \"css\" can be used only once in the same page");

      tag.setType("css");
      tag.doEndTag();
   }

   @Test
   public void shouldThrowAnExceptionWhenTheRequiredAttributeIsEmpty() throws JspException {

      mockServletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), arc);

      expectedEx.expect(JspException.class);
      expectedEx.expectMessage("The \"type\" attribute cannot be blank. Possible values: \"js\" or \"css\"");

      tag.setType("");
      tag.doEndTag();
   }
}
