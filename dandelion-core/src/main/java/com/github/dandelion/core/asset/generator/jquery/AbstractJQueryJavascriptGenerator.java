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
package com.github.dandelion.core.asset.generator.jquery;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.generator.AbstractJavascriptGenerator;
import com.github.dandelion.core.asset.generator.AssetBuffer;
import com.github.dandelion.core.asset.generator.JavascriptGenerator;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;
import com.github.dandelion.core.utils.StringBuilderUtils;
import com.github.dandelion.core.utils.Validate;

/**
 * <p>
 * JQuery-flavoured abstract implementation of {@link JavascriptGenerator},
 * meant to generate Javascript code using the jQuery's {@code ready()} method.
 * 
 * <p>
 * Some placeholders exist and are intended to be filled by subclasses, thanks
 * to {@link #appendToPlaceholder(Placeholder, StringBuilder)} method.
 * <p>
 * All placeholders can be visualized as follows: <blockquote>
 * 
 * <pre>
 * // Beginning of the generated Javascript code 
 * <b>[BEFOREALL]</b>
 * <b>[BEFORESTARTDOCUMENTREADY]</b>
 * $(document).ready(function(){
 *    <b>[AFTERSTARTDOCUMENTREADY]</b>
 *    <b>[COMPONENTCONFIGURATION]</b>
 *    <b>[BEFOREENDDOCUMENTREADY]</b>
 * });
 * <b>[AFTERENDDOCUMENTREADY]</b>
 * <b>[AFTERALL]</b>
 * // End of the generated Javascript code
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * Note that the generated code is not formatted here but by a dedicated
 * {@link AssetProcessor}.
 * 
 * See {@link StandardJQueryJavascriptGenerator} which the standard
 * implementation.
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 * @see StandardJQueryJavascriptGenerator
 */
public abstract class AbstractJQueryJavascriptGenerator extends AbstractJavascriptGenerator {

	public static final String INDENTATION = "   ";
	public static final String NEWLINE = "\n";

	private StringBuilder beforeAll;
	private StringBuilder beforeStartDocumentReady;
	private StringBuilder afterStartDocumentReady;
	private StringBuilder componentConfiguration;
	private StringBuilder beforeEndDocumentReady;
	private StringBuilder afterEndDocumentReady;
	private StringBuilder afterAll;

	/**
	 * Available placeholders when generating jQuery-based Javascript code.
	 * 
	 * @author Thibault Duchateau
	 * @since 0.11.0
	 */
	public enum Placeholder {
		BEFORE_ALL, BEFORE_START_DOCUMENT_READY, AFTER_START_DOCUMENT_READY, COMPONENT_CONFIGURATION, BEFORE_END_DOCUMENT_READY, AFTER_END_DOCUMENT_READY, AFTER_ALL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fillBuffer(AssetBuffer assetBuffer) {
		fillBuffer((JQueryAssetBuffer) assetBuffer);
	}

	public abstract void fillBuffer(JQueryAssetBuffer jsResource);

	/**
	 * <p>
	 * Appends the Javascript code in the corresponding placeholder.
	 * 
	 * @param placeholder
	 *            The {@link Placeholder} in which the Javascript code will be
	 *            appended.
	 * @param javascriptCode
	 *            The Javascript code to append.
	 */
	protected void appendToPlaceholder(Placeholder placeholder, StringBuilder javascriptCode) {

		Validate.notNull(placeholder, "The placeholder cannot be null");

		if (StringBuilderUtils.isBlank(javascriptCode)) {
			return;
		}

		switch (placeholder) {
		case BEFORE_ALL:
			if (this.beforeAll == null) {
				this.beforeAll = new StringBuilder();
			}
			this.beforeAll.append(javascriptCode);
			break;
		case BEFORE_START_DOCUMENT_READY:
			if (this.beforeStartDocumentReady == null) {
				this.beforeStartDocumentReady = new StringBuilder();
			}
			this.beforeStartDocumentReady.append(javascriptCode);
			break;
		case AFTER_START_DOCUMENT_READY:
			if (this.afterStartDocumentReady == null) {
				this.afterStartDocumentReady = new StringBuilder();
			}
			this.afterStartDocumentReady.append(javascriptCode);
			break;
		case COMPONENT_CONFIGURATION:
			if (this.componentConfiguration == null) {
				this.componentConfiguration = new StringBuilder();
			}
			this.componentConfiguration.append(javascriptCode);
			break;
		case BEFORE_END_DOCUMENT_READY:
			if (this.beforeEndDocumentReady == null) {
				this.beforeEndDocumentReady = new StringBuilder();
			}
			this.beforeEndDocumentReady.append(javascriptCode);
			break;
		case AFTER_END_DOCUMENT_READY:
			if (this.afterEndDocumentReady == null) {
				this.afterEndDocumentReady = new StringBuilder();
			}
			this.afterEndDocumentReady.append(javascriptCode);
			break;
		case AFTER_ALL:
			if (this.afterAll == null) {
				this.afterAll = new StringBuilder();
			}
			this.afterAll.append(javascriptCode);
			break;
		default:
			break;
		}
	}

	/**
	 * TODO gérer jQuery.noConflict 
	 */
	@Override
	public String getGeneratedAsset(HttpServletRequest request) {
		StringBuilder retval = new StringBuilder();

		if (this.beforeAll != null) {
			retval.append(this.beforeAll);
		}

		if (this.beforeStartDocumentReady != null) {
			retval.append(this.beforeStartDocumentReady);
		}

		retval.append("$(document).ready(function(){");
		if (this.afterStartDocumentReady != null) {
			retval.append(this.afterStartDocumentReady);
		}
		if (this.componentConfiguration != null) {
			retval.append(this.componentConfiguration);
		}

		if (this.beforeEndDocumentReady != null) {
			retval.append(this.beforeEndDocumentReady);
		}
		retval.append("});");

		if (this.afterEndDocumentReady != null) {
			retval.append(this.afterEndDocumentReady);
		}

		if (this.afterAll != null) {
			retval.append(this.afterAll);
		}

		return retval.toString();
	}
}
