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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.AssetRequestContext;

/**
 * <p>
 * JSP tag used for configuring placeholder where Dandelion will inject assets.
 * </p>
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * &lt;dandelion:placeholder type="js" />
 * </pre>
 * <p>
 * will tell Dandelion to inject all JavaScript assets at the specified position
 * of the tag instead of injecting them at the end of the body section, which is
 * the default behaviour.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 */
public class PlaceholderTag extends TagSupport {

   private static final long serialVersionUID = -8657669703834740046L;
   private static final String PLACEHOLDER_TYPE_JS = "js";
   private static final String PLACEHOLDER_TYPE_CSS = "css";

   /**
    * (Required) Type of placeholder to configure (either "js" or "css").
    */
   private String placeholderType;

   @Override
   public int doEndTag() throws JspException {

      String placeholder = "dandelionPlaceholder" + placeholderType;

      if (PLACEHOLDER_TYPE_JS.equalsIgnoreCase(this.placeholderType)) {

         String existingPlaceholder = AssetRequestContext.get(pageContext.getRequest()).getJsPlaceholder();
         if (StringUtils.isNotBlank(existingPlaceholder)) {
            throw new JspException(
                  "The placeholder type \"" + PLACEHOLDER_TYPE_JS + "\" can be used only once in the same page");
         }
         else {
            AssetRequestContext.get(pageContext.getRequest()).setJsPlaceholder(placeholder);
         }
      }
      else if (PLACEHOLDER_TYPE_CSS.equalsIgnoreCase(this.placeholderType)) {
         String existingPlaceholder = AssetRequestContext.get(pageContext.getRequest()).getCssPlaceholder();
         if (StringUtils.isNotBlank(existingPlaceholder)) {
            throw new JspException(
                  "The placeholder type \"" + PLACEHOLDER_TYPE_CSS + "\" can be used only once in the same page");
         }
         else {
            AssetRequestContext.get(pageContext.getRequest()).setCssPlaceholder(placeholder);
         }
      }
      else {
         throw new JspException("Only \"" + PLACEHOLDER_TYPE_JS + "\" and \"" + PLACEHOLDER_TYPE_CSS
               + "\" are allowed in the \"type\" attribute");
      }

      try {
         this.pageContext.getOut().println(placeholder);
      }
      catch (IOException e) {
         throw new JspException("Unable to update the page with a HTML placeholder", e);
      }

      return EVAL_PAGE;
   }

   public void setType(String placeholderType) throws JspException {
      if (StringUtils.isBlank(placeholderType)) {
         throw new JspException("The \"type\" attribute cannot be blank. Possible values: \"" + PLACEHOLDER_TYPE_JS
               + "\" or \"" + PLACEHOLDER_TYPE_CSS + "\"");
      }
      this.placeholderType = placeholderType;
   }
}