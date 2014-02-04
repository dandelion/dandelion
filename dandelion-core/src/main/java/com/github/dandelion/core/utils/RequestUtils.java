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

package com.github.dandelion.core.utils;

import javax.servlet.http.HttpServletRequest;

public final class RequestUtils {

	/**
	 * Return the current URL, without query parameters.
	 * 
	 * @param request
	 *            The current request.
	 * @param preserveParameters
	 *            preserve the url parameters.
	 * @return a String containing the current URL.
	 */
	public static String getCurrentUrl(HttpServletRequest request, boolean preserveParameters) {
		String currentUrl;
		if (request.getAttribute("javax.servlet.forward.request_uri") != null) {
			currentUrl = (String) request.getAttribute("javax.servlet.forward.request_uri");
		}
		else {
			currentUrl = request.getRequestURL().toString();
		}
		if ((!preserveParameters && request.getAttribute("javax.servlet.include.query_string") != null)
				|| (preserveParameters && request.getQueryString() != null)) {
			currentUrl += "?" + request.getQueryString();
		}
		return currentUrl;
	}

	/**
	 * Return the base URL (context path included).<br/>
	 * 
	 * Example : <br/>
	 * With an URL like http://domain.com:port/context/anything, <br/>
	 * this function returns http://domain.com:port/context.
	 * 
	 * @param request
	 *            Current request.
	 * @return the base URL of the current request.
	 */
	public static String getBaseUrl(HttpServletRequest request) {
		if (request.getRequestURI().equals("/") || request.getRequestURI().equals("")) {
			return request.getRequestURL().toString();
		}
		return request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
	}
}
