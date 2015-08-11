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

import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Plain old HTML <code>script</code> tag.
 * </p>
 * 
 * @author Thibault Duchateau
 */
public class HtmlScript extends AbstractHtmlTag {

   private static final String TAG = "script";

   /**
    * Plain old HTML <code>src</code> attribute.
    */
   private String src;

   /**
    * Condition to use in a conditionnal comment (IE 5 to 9).
    */
   private String condition;

   public HtmlScript() {
      this(null, null);
   }

   public HtmlScript(String src) {
      this(src, null);
   }

   public HtmlScript(String src, String condition) {
      this.tag = TAG;
      this.src = src;
      this.condition = condition;
   }

   @Override
   protected StringBuilder getHtmlAttributes() {
      StringBuilder html = super.getHtmlAttributes();
      html.append(writeAttribute(ATTR_SRC, this.src));
      return html;
   }

   public String getSrc() {
      return src;
   }

   public void setSrc(String src) {
      this.src = src;
   }

   public String getCondition() {
      return this.condition;
   }

   public void setCondition(String condition) {
      this.condition = condition;
   }

   @Override
   public StringBuilder toHtml() {
      StringBuilder html = new StringBuilder();
      if (StringUtils.isNotBlank(this.condition)) {
         html.append(IF_OPENING_CONDITION);
         html.append(this.condition);
         html.append(IF_CLOSING_CONDITION);
      }
      html.append(super.toHtml());
      if (StringUtils.isNotBlank(this.condition)) {
         html.append(ENDIF_CONDITION);
      }
      return html;
   }
}
