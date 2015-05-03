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
package com.github.dandelion.core.asset.generator.js.jquery;

import java.util.Map;

import com.github.dandelion.core.asset.generator.AbstractAssetPlaceholderContent;

import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.AFTER_ALL;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.AFTER_END_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.AFTER_START_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.BEFORE_ALL;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.BEFORE_END_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.BEFORE_START_DOCUMENT_READY;
import static com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder.COMPONENT_CONFIGURATION;

/**
 * <p>
 * JQuery-flavoured implementation of an {@link AbstractAssetPlaceholderContent}
 * that uses {@link JQueryContentPlaceholder} as placeholders.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 1.0.0
 * 
 * @see com.github.dandelion.core.asset.generator.js.jquery.JQueryContentPlaceholder
 */

public class JQueryContent extends AbstractAssetPlaceholderContent<JQueryContentPlaceholder> {

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#BEFORE_ALL} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToBeforeAll(String content) {
      appendToPlaceholder(BEFORE_ALL, content);
   }

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#BEFORE_START_DOCUMENT_READY} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToBeforeStartDocumentReady(String content) {
      appendToPlaceholder(BEFORE_START_DOCUMENT_READY, content);
   }

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#AFTER_START_DOCUMENT_READY} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToAfterStartDocumentReady(String content) {
      appendToPlaceholder(AFTER_START_DOCUMENT_READY, content);
   }

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#COMPONENT_CONFIGURATION} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToComponentConfiguration(String content) {
      appendToPlaceholder(COMPONENT_CONFIGURATION, content);
   }

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#BEFORE_END_DOCUMENT_READY} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToBeforeEndDocumentReady(String content) {
      appendToPlaceholder(BEFORE_END_DOCUMENT_READY, content);
   }

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#AFTER_END_DOCUMENT_READY} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToAfterEndDocumentReady(String content) {
      appendToPlaceholder(AFTER_END_DOCUMENT_READY, content);
   }

   /**
    * <p>
    * Appends the provided content to the
    * {@link JQueryContentPlaceholder#AFTER_ALL} placeholder.
    * </p>
    * 
    * @param content
    *           The content to be appended to the placeholder.
    */
   public void appendToAfterAll(String content) {
      appendToPlaceholder(AFTER_ALL, content);
   }

   public Map<JQueryContentPlaceholder, StringBuilder> getPlaceholderContent() {
      return super.getPlaceholderContent();
   }
}
