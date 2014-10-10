/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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

import static com.github.dandelion.core.asset.generator.js.jquery.JQueryPlaceholderContent.*;

import java.util.Map;

import com.github.dandelion.core.asset.generator.AbstractAssetPlaceholderContent;

/**
 * <p>
 * JQuery-flavoured implementation of an Asset Content with with placeholder
 *
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.11.0
 *
 * @see com.github.dandelion.core.asset.generator.js.jquery.JQueryPlaceholderContent
 */
public class JQueryContent extends AbstractAssetPlaceholderContent<JQueryPlaceholderContent> {

	/**
	 * Append content on [@link JQueryPlaceholderContent#BEFORE_ALL] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToBeforeAll(String content) {
		appendToPlaceholder(BEFORE_ALL, content);
	}

	/**
	 * Append content on [@link
	 * JQueryPlaceholderContent#BEFORE_START_DOCUMENT_READY] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToBeforeStartDocumentReady(String content) {
		appendToPlaceholder(BEFORE_START_DOCUMENT_READY, content);
	}

	/**
	 * Append content on [@link
	 * JQueryPlaceholderContent#AFTER_START_DOCUMENT_READY] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToAfterStartDocumentReady(String content) {
		appendToPlaceholder(AFTER_START_DOCUMENT_READY, content);
	}

	/**
	 * Append content on [@link
	 * JQueryPlaceholderContent#COMPONENT_CONFIGURATION] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToComponentConfiguration(String content) {
		appendToPlaceholder(COMPONENT_CONFIGURATION, content);
	}

	/**
	 * Append content on [@link
	 * JQueryPlaceholderContent#BEFORE_END_DOCUMENT_READY] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToBeforeEndDocumentReady(String content) {
		appendToPlaceholder(BEFORE_END_DOCUMENT_READY, content);
	}

	/**
	 * Append content on [@link
	 * JQueryPlaceholderContent#AFTER_END_DOCUMENT_READY] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToAfterEndDocumentReady(String content) {
		appendToPlaceholder(AFTER_END_DOCUMENT_READY, content);
	}

	/**
	 * Append content on [@link JQueryPlaceholderContent#AFTER_ALL] placeholder
	 * 
	 * @param content
	 *            content to append
	 */
	public void appendToAfterAll(String content) {
		appendToPlaceholder(AFTER_ALL, content);
	}

	protected Map<JQueryPlaceholderContent, StringBuilder> getPlaceholderContent() {
		return super.getPlaceholderContent();
	}
}
