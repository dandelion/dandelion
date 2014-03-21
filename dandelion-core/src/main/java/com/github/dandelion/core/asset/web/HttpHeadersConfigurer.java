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
package com.github.dandelion.core.asset.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import com.github.dandelion.core.DevMode;
import com.github.dandelion.core.asset.web.data.AssetContent;

/**
 * 
 * TODO
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class HttpHeadersConfigurer {

	public static final long ONE_YEAR_IN_MILLISECONDS = 365 * 24 * 60 * 60 * 1000L;
	public static final String DEFAULT_CACHE_CONTROL = "public, max-age=315360000";
	private static final SimpleDateFormat DATE_FORMAT;

	static {
		DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public void configureResponseHeaders(HttpServletResponse response, AssetContent assetContent) {
		
		response.setHeader("Content-Length", String.valueOf(assetContent.getContent().getBytes().length));
		response.setContentType(assetContent.getContentType());
		
		if (DevMode.isEnabled()) {
			response.setHeader(HttpHeader.CACHE_CONTROL.getName(), "no-cache");
			response.setHeader(HttpHeader.EXPIRES.getName(), String.valueOf(1));
		}
		else {
			response.setHeader(HttpHeader.CACHE_CONTROL.getName(), DEFAULT_CACHE_CONTROL);
			response.setHeader(HttpHeader.EXPIRES.getName(), String.valueOf(ONE_YEAR_IN_MILLISECONDS));
			
			//trim the milliseconds off the value since the header is only accurate down to the second
			long lastModified = new Date().getTime();
			lastModified = TimeUnit.MILLISECONDS.toSeconds(lastModified);
			lastModified = TimeUnit.SECONDS.toMillis(lastModified);
			response.setHeader(HttpHeader.LAST_MODIFIED.getName(), DATE_FORMAT.format(lastModified));
		}
	}
}
