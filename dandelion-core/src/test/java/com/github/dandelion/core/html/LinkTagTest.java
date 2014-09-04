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

package com.github.dandelion.core.html;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LinkTagTest {

	@Test
	public void should_have_correct_html_rendering() {
		assertThat(new LinkTag().toHtml().toString()).isEqualTo("<link rel=\"stylesheet\"/>");
		assertThat(new LinkTag("url").toHtml().toString()).isEqualTo("<link rel=\"stylesheet\" href=\"url\"/>");
		assertThat(new LinkTag("url", "rel").toHtml().toString()).isEqualTo("<link rel=\"rel\" href=\"url\"/>");
	}

	@Test
	public void should_have_full_access_to_the_tag() {
		LinkTag tag = new LinkTag("url", "rel");
		assertThat(tag.getHref()).isEqualTo("url");
		assertThat(tag.getRel()).isEqualTo("rel");

		tag.setHref("anotherHref");
		tag.setRel("anotherRel");
		assertThat(tag.getHref()).isEqualTo("anotherHref");
		assertThat(tag.getRel()).isEqualTo("anotherRel");
	}
}
