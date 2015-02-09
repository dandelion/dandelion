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

package com.github.dandelion.core.util;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.html.AbstractHtmlTag;
import com.github.dandelion.core.html.HtmlLink;
import com.github.dandelion.core.html.HtmlScript;

/**
 * <p>
 * Collection of utilities to ease working with HTML tags.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public final class HtmlUtils {

   public static AbstractHtmlTag transformAsset(Asset asset) {
      AbstractHtmlTag tag;
      switch (asset.getType()) {
      case css:
         tag = new HtmlLink(asset.getFinalLocation(), asset.getCondition());
         break;
      case js:
         tag = new HtmlScript(asset.getFinalLocation(), asset.getCondition());
         break;
      default:
         tag = null;
      }
      if (tag != null) {
         tag.addAttributesOnlyName(asset.getAttributesOnlyName());
         tag.addAttributes(asset.getAttributes());
      }
      return tag;
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private HtmlUtils() {
      throw new AssertionError();
   }
}
