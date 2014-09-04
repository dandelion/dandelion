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

import com.github.dandelion.core.asset.generator.AssetBuffer;

/**
 * <p>
 * JQuery-flavoured implementation of an {@link AssetBuffer}
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public class JQueryAssetBuffer implements AssetBuffer {

	private StringBuilder beforeAll;
	private StringBuilder beforeStartDocumentReady;
	private StringBuilder afterStartDocumentReady;
	private StringBuilder componentConf;
	private StringBuilder beforeEndDocumentReady;
	private StringBuilder afterEndDocumentReady;
	private StringBuilder afterAll;

	public StringBuilder getBeforeAll() {
		return beforeAll;
	}

	public void appendToBeforeAll(String beforeAll) {
		if (this.beforeAll == null) {
			this.beforeAll = new StringBuilder();
		}
		this.beforeAll.append(beforeAll != null ? beforeAll : "");
	}

	public StringBuilder getBeforeStartDocumentReady() {
		return beforeStartDocumentReady;
	}

	public void appendToBeforeStartDocumentReady(String beforeStartDocumentReady) {
		if (this.beforeStartDocumentReady == null) {
			this.beforeStartDocumentReady = new StringBuilder();
		}
		this.beforeStartDocumentReady.append(beforeStartDocumentReady != null ? beforeStartDocumentReady : "");
	}

	public StringBuilder getAfterStartDocumentReady() {
		return afterStartDocumentReady;
	}

	public void appendToAfterStartDocumentReady(String afterStartDocumentReady) {
		if (this.afterStartDocumentReady == null) {
			this.afterStartDocumentReady = new StringBuilder();
		}
		this.afterStartDocumentReady.append(afterStartDocumentReady != null ? afterStartDocumentReady : "");
	}

	public StringBuilder getComponentConf() {
		return componentConf;
	}

	public void appendToComponentConf(String componentConf) {
		if (this.componentConf == null) {
			this.componentConf = new StringBuilder();
		}
		this.componentConf.append(componentConf != null ? componentConf : "");
	}

	public StringBuilder getBeforeEndDocumentReady() {
		return beforeEndDocumentReady;
	}

	public void appendToBeforeEndDocumentReady(String beforeEndDocumentReady) {
		if (this.beforeEndDocumentReady == null) {
			this.beforeEndDocumentReady = new StringBuilder();
		}
		this.beforeEndDocumentReady.append(beforeEndDocumentReady != null ? beforeEndDocumentReady : "");
	}

	public StringBuilder getAfterAll() {
		return afterAll;
	}

	public void appendToAfterAll(String afterAll) {
		if (this.afterAll == null) {
			this.afterAll = new StringBuilder();
		}
		this.afterAll.append(afterAll);
	}

	public void appendToAfterEndDocumentReady(String afterEndDocumentReady) {
		if (this.afterEndDocumentReady == null) {
			this.afterEndDocumentReady = new StringBuilder();
		}
		this.afterEndDocumentReady.append(afterEndDocumentReady != null ? afterEndDocumentReady : "");
	}

	public StringBuilder getAfterEndDocumentReady() {
		return afterEndDocumentReady;
	}
}
