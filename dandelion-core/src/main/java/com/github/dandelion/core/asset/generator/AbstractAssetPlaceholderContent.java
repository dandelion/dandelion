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
package com.github.dandelion.core.asset.generator;

import java.util.HashMap;
import java.util.Map;

import com.github.dandelion.core.util.StringBuilderUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Abstract class that can associate any asset content with a parameterizable
 * placeholder.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 1.0.0
 * @param <P>
 *           the type of {@link AssetPlaceholder} that the content is associated
 *           with.
 */
public abstract class AbstractAssetPlaceholderContent<P extends AssetPlaceholder> {

   /**
    * The map in which different contents can be associated with different
    * placeholders.
    */
   private final Map<P, StringBuilder> content = new HashMap<P, StringBuilder>();

   /**
    * <p>
    * Appends some String content to the corresponding placeholder, only if it's
    * not null nor empty.
    * </p>
    * 
    * @param placeholder
    *           The placeholder in which the content will be appened.
    * @param content
    *           The content to append.
    */
   public void appendToPlaceholder(P placeholder, String content) {
      if (StringUtils.isNotBlank(content)) {
         if (!this.content.containsKey(placeholder)) {
            this.content.put(placeholder, new StringBuilder());
         }
         this.content.get(placeholder).append(content);
      }
   }

   /**
    * <p>
    * Appends some content to the corresponding placeholder, only if it's not
    * null nor empty.
    * </p>
    * 
    * @param placeholder
    *           The placeholder in which the content will be appened.
    * @param content
    *           The content to append.
    */
   public void appendToPlaceholder(P placeholder, StringBuilder content) {
      if (StringBuilderUtils.isNotBlank(content)) {
         if (!this.content.containsKey(placeholder)) {
            this.content.put(placeholder, new StringBuilder());
         }
         this.content.get(placeholder).append(content);
      }
   }

   /**
    * @return the map that associates content to its placeholder.
    */
   protected Map<P, StringBuilder> getPlaceholderContent() {
      return content;
   }
}
