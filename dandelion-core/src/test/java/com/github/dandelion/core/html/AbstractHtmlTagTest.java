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

package com.github.dandelion.core.html;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractHtmlTagTest {

   static AbstractHtmlTag tag = new AbstractHtmlTag() {
      @Override
      public StringBuilder toHtml() {
         return new StringBuilder("test");
      }
   };

   @Test
   public void should_have_correct_html_rendering() {
      assertThat(tag.toHtml().toString()).isEqualTo("test");
   }

   @Test
   public void should_have_full_access_to_the_tag() {
      tag.setId("id");
      assertThat(tag.getId()).isEqualTo("id");

      tag.addCssClass("class1");
      assertThat(tag.getCssClass().toString()).isEqualTo("class1");

      tag.addCssStyle("style1");
      assertThat(tag.getCssStyle().toString()).isEqualTo("style1");

      tag.addCssClass("class2");
      assertThat(tag.getCssClass().toString()).isEqualTo("class1 class2");

      tag.addCssStyle("style2");
      assertThat(tag.getCssStyle().toString()).isEqualTo("style1;style2");

      tag.setCssClass(new StringBuilder("class1"));
      assertThat(tag.getCssClass().toString()).isEqualTo("class1");

      tag.setCssStyle(new StringBuilder("style1"));
      assertThat(tag.getCssStyle().toString()).isEqualTo("style1");
   }
}
