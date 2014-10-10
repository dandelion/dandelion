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

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.generator.js.AbstractJavascriptPlaceholderContentGenerator;

/**
 * <p>
 * JQuery-flavoured implementation of
 * {@link com.github.dandelion.core.asset.generator.AbstractAssetPlaceholderContentGenerator}
 * , meant to generate Javascript code using the jQuery's {@code ready()}
 * method.
 *
 * <p>
 * All JQuery placeholders can be visualized as follows: <blockquote>
 *
 * <pre>
 * // Beginning of the generated Javascript code
 * <b>[BEFORE_ALL]</b>
 * 
 * <b>[BEFORE_START_DOCUMENT_READY]</b>
 * $(document).ready(function() {
 *    <b>[AFTER_START_DOCUMENT_READY]</b>
 * 
 *    <b>[COMPONENT_CONFIGURATION]</b>
 * 
 *    <b>[AFTER_END_DOCUMENT_READY]</b>
 * });
 * <b>[AFTER_END_DOCUMENT_READY]</b>
 * 
 * <b>[AFTER_ALL]</b>
 * // End of the generated Javascript code
 * </pre>
 *
 * </blockquote>
 *
 * <p>
 * Note that the generated code is not formatted here but by a dedicated
 * {@link com.github.dandelion.core.asset.processor.spi.AssetProcessor}.
 *
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public class JQueryContentGenerator extends
		AbstractJavascriptPlaceholderContentGenerator<JQueryPlaceholderContent, JQueryContent> {

	public JQueryContentGenerator(JQueryContent content) {
		super(content);
	}

	/**
	 * {@inheritDoc}
	 */
	// TODO http://api.jquery.com/jquery.noconflict/
	@Override
	protected String getPlaceholderJavascriptContent(HttpServletRequest request,
			Map<JQueryPlaceholderContent, StringBuilder> contents) {
		StringBuilder javascriptContent = new StringBuilder();

		if (contents.containsKey(BEFORE_ALL)) {
			javascriptContent.append(contents.get(BEFORE_ALL));
		}

		if (contents.containsKey(BEFORE_START_DOCUMENT_READY)) {
			javascriptContent.append(contents.get(BEFORE_START_DOCUMENT_READY));
		}

		javascriptContent.append("$(document).ready(function(){");
		if (contents.containsKey(AFTER_START_DOCUMENT_READY)) {
			javascriptContent.append(contents.get(AFTER_START_DOCUMENT_READY));
		}
		if (contents.containsKey(COMPONENT_CONFIGURATION)) {
			javascriptContent.append(contents.get(COMPONENT_CONFIGURATION));
		}
		if (contents.containsKey(BEFORE_END_DOCUMENT_READY)) {
			javascriptContent.append(contents.get(BEFORE_END_DOCUMENT_READY));
		}
		javascriptContent.append("});");

		if (contents.containsKey(AFTER_END_DOCUMENT_READY)) {
			javascriptContent.append(contents.get(AFTER_END_DOCUMENT_READY));
		}
		if (contents.containsKey(AFTER_ALL)) {
			javascriptContent.append(contents.get(AFTER_ALL));
		}

		return javascriptContent.toString();
	}
}
