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
package com.github.dandelion.thymeleaf.processor;

import javax.servlet.http.HttpServletRequest;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.ProcessorResult;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.thymeleaf.dialect.DandelionDialect;
import com.github.dandelion.thymeleaf.dialect.PlaceholderAttributeNames;
import com.github.dandelion.thymeleaf.util.ArgumentsUtil;
import com.github.dandelion.thymeleaf.util.AttributesUtil;

/**
 * <p>
 * Attribute processor for all attributes present in
 * {@link PlaceholderAttributeNames} .
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 */
public class PlaceholderAttrProcessor extends DandelionAttrProcessor {

   private static final String PLACEHOLDER_TYPE_JS = "js";
   private static final String PLACEHOLDER_TYPE_CSS = "css";

   public PlaceholderAttrProcessor(String attributeName) {
      super(attributeName);
   }

   @Override
   public int getPrecedence() {
      return DandelionDialect.HIGHEST_PRECEDENCE + 1;
   }

   @Override
   protected ProcessorResult doProcessAttribute(Arguments arguments, Element element, String attributeName) {

      String strippedAttributeName = AttributesUtil.stripPrefix(attributeName, DandelionDialect.DIALECT_PREFIX);
      PlaceholderAttributeNames placeholderAttributeName = (PlaceholderAttributeNames) AttributesUtil
            .find(strippedAttributeName, PlaceholderAttributeNames.values());

      HttpServletRequest request = ArgumentsUtil.getWebContext(arguments).getHttpServletRequest();
      AssetRequestContext arc = AssetRequestContext.get(request);

      String attributeValue = element.getAttributeValue(attributeName);
      if (StringUtils.isBlank(attributeValue)) {
         throw new DandelionException("The \"type\" attribute cannot be blank. Possible values: \""
               + PLACEHOLDER_TYPE_JS + "\" or \"" + PLACEHOLDER_TYPE_CSS + "\".");
      }

      String placeholder = "dandelionPlaceholder" + attributeValue;
      handlePlaceholder(attributeValue, arc, placeholder);

      switch (placeholderAttributeName) {

      // Include a new Text node containing the placeholder within the existing
      // element
      case PLACEHOLDER_INCLUDE:

         Text text = new Text(placeholder);
         element.insertChild(element.getChildren().size(), text);

         break;

      // Replace the current element by the placeholder
      case PLACEHOLDER_REPLACE:

         text = new Text(placeholder);
         element.clearChildren();
         element.addChild(text);

         break;
      }

      return ProcessorResult.ok();
   }

   private void handlePlaceholder(String attributeValue, AssetRequestContext arc, String placeholder) {
      if (PLACEHOLDER_TYPE_JS.equalsIgnoreCase(attributeValue)) {
         String existingPlaceholder = arc.getJsPlaceholder();
         if (StringUtils.isNotBlank(existingPlaceholder)) {
            throw new DandelionException(
                  "The placeholder type \"" + PLACEHOLDER_TYPE_JS + "\" can be used only once in the same page");
         }
         else {
            arc.setJsPlaceholder(placeholder);
         }
      }
      else if (PLACEHOLDER_TYPE_CSS.equalsIgnoreCase(attributeValue)) {

         String existingPlaceholder = arc.getCssPlaceholder();
         if (StringUtils.isNotBlank(existingPlaceholder)) {
            throw new DandelionException(
                  "The placeholder type \"" + PLACEHOLDER_TYPE_CSS + "\" can be used only once in the same page");
         }
         else {
            arc.setCssPlaceholder(placeholder);
         }
      }
      else {
         throw new DandelionException("Only \"" + PLACEHOLDER_TYPE_JS + "\" and \"" + PLACEHOLDER_TYPE_CSS
               + "\" are allowed in the \"placeholder-*\" attributes");
      }
   }
}