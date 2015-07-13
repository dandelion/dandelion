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
import org.thymeleaf.processor.ProcessorResult;

import com.github.dandelion.core.web.AssetRequestContext;
import com.github.dandelion.thymeleaf.dialect.AssetAttributeNames;
import com.github.dandelion.thymeleaf.dialect.DandelionDialect;
import com.github.dandelion.thymeleaf.util.ArgumentsUtil;
import com.github.dandelion.thymeleaf.util.AttributesUtil;

/**
 * <p>
 * Attribute processor for all attributes present in {@link AssetAttributeNames}
 * .
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetAttrProcessor extends DandelionAttrProcessor {

   public AssetAttrProcessor(String attributeName) {
      super(attributeName);
   }

   @Override
   public int getPrecedence() {
      return DandelionDialect.HIGHEST_PRECEDENCE + 1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ProcessorResult doProcessAttribute(Arguments arguments, Element element, String attributeName) {

      String strippedAttributeName = AttributesUtil.stripPrefix(attributeName, DandelionDialect.DIALECT_PREFIX);
      AssetAttributeNames assetsAttributeName = (AssetAttributeNames) AttributesUtil.find(strippedAttributeName,
            AssetAttributeNames.values());

      HttpServletRequest request = ArgumentsUtil.getWebContext(arguments).getHttpServletRequest();
      AssetRequestContext arc = AssetRequestContext.get(request);
      switch (assetsAttributeName) {
      case JS_EXCLUDES:
         arc.excludeJs(element.getAttributeValue(attributeName));
         break;
      case CSS_EXCLUDES:
         arc.excludeCss(element.getAttributeValue(attributeName));
         break;
      default:
         break;
      }

      return ProcessorResult.ok();
   }
}
