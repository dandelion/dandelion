/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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
package com.github.dandelion.core.asset.wrapper.impl;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Scanner;

import org.junit.Test;

import com.github.dandelion.core.utils.ResourceUtils;

public class CdnLocationWrapperTest {

	private CdnLocationWrapper wrapper = new CdnLocationWrapper();
	private String url = "http://domain/folder/sub/assets/css/my.css";
	@Test
	public void should_not_process_anything() {
		String content = ResourceUtils.getFileContentFromClasspath("wrapper/test1.css", true);
		content = wrapper.processRelativePaths(content, url);
		assertThat(content).contains("images/my-image.png");
	}
	
	@Test
	public void should_process_one_relative_path_1_lvl() {
		String content = ResourceUtils.getFileContentFromClasspath("wrapper/test2.css", true);
		content = wrapper.processRelativePaths(content, url);
		assertThat(content).contains("http://domain/folder/sub/assets/images/my-image.png");
	}
	
	@Test
	public void should_process_one_relative_path_2_lvl() {
		String content = ResourceUtils.getFileContentFromClasspath("wrapper/test3.css", true);
		content = wrapper.processRelativePaths(content, url);
		assertThat(content).contains("http://domain/folder/sub/images/my-image.png");
	}
	
	@Test
	public void should_process_multiple_relative_paths_2_lvl() {
		String content = ResourceUtils.getFileContentFromClasspath("wrapper/test4.css", true);
		content = wrapper.processRelativePaths(content, url);
		Scanner scanner = new Scanner(content);
		while(scanner.hasNextLine()){
			assertThat(scanner.nextLine()).contains("http://domain/folder/sub/assets/images/my-image.png");
		}
	}
}
