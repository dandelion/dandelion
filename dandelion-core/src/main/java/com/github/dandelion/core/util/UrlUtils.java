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
package com.github.dandelion.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Collection of utilities to ease working with URLs.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class UrlUtils {

   public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";
   public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";
   public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";

   /**
    * <p>
    * Get the current URI with potentiel request parameters.
    * 
    * @param request
    *           the request to extract the URI and parameters from
    * @return the current URI.
    */
   public static StringBuilder getCurrentUri(HttpServletRequest request) {

      StringBuilder currentUri = new StringBuilder();

      // Get request URI
      if (request.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE) != null) {
         currentUri.append(request.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE));
      }
      else if (request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null) {
         currentUri.append(request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE));
      }
      else {
         currentUri.append(request.getRequestURI());
      }

      // Get request parameters
      if (request.getAttribute(FORWARD_QUERY_STRING_ATTRIBUTE) != null) {
         currentUri.append("?").append(request.getAttribute(FORWARD_QUERY_STRING_ATTRIBUTE));
      }
      else if (request.getQueryString() != null) {
         currentUri.append("?").append(request.getQueryString());
      }

      return currentUri;
   }

   /**
    * <P>
    * Return the current URL, with or without query parameters, depending on the
    * {@code perserveParameters} parameter.
    * 
    * @param request
    *           The current {@link HttpServletRequest}.
    * @param preserveParameters
    *           preserve the url parameters.
    * @return a String containing the current URL.
    */
   public static StringBuilder getCurrentUrl(HttpServletRequest request, boolean preserveParameters) {

      StringBuilder currentUrl = new StringBuilder(request.getRequestURL());

      // Get request parameters
      if (preserveParameters) {
         if (request.getAttribute(FORWARD_QUERY_STRING_ATTRIBUTE) != null) {
            currentUrl.append("?").append(request.getAttribute(FORWARD_QUERY_STRING_ATTRIBUTE));
         }
         else if (request.getQueryString() != null) {
            currentUrl.append("?").append(request.getQueryString());
         }
      }

      return currentUrl;
   }

   public static String getProcessedUrl(String url, HttpServletRequest request, HttpServletResponse response) {
      String processedUrl = null;

      if (isContextRelative(url, request) && !url.startsWith(request.getContextPath())) {
         processedUrl = request.getContextPath() + url;
      }
      else if (isServerRelative(url)) {
         // remove the "~" from the link base
         processedUrl = url.substring(1);
      }
      else if (isAbsolute(url)) {
         processedUrl = url;
      }
      else {
         // Link base is current-URL-relative
         processedUrl = url;
      }

      return response != null ? response.encodeURL(processedUrl) : processedUrl;
   }

   public static String getProcessedUrl(StringBuilder url, HttpServletRequest request, HttpServletResponse response) {
      return getProcessedUrl(url.toString(), request, response);
   }

   /**
    * <p>
    * Check whether the passed URL is absolute.
    */
   public static boolean isAbsolute(String url) {
      return (url.contains("://") || url.toLowerCase().startsWith("mailto:") || url.startsWith("//"));
   }

   public static boolean isContextRelative(String url, HttpServletRequest request) {
      return url.startsWith("/") && !url.startsWith("//");
   }

   public static boolean isServerRelative(String url) {
      return url.startsWith("~/");
   }

   public static boolean isProtocolRelative(String url) {
      return url.startsWith("//");
   }

   public static StringBuilder getContext(HttpServletRequest request) {
      return new StringBuilder(request.getContextPath());
   }

   public static void addParameter(StringBuilder url, String name) {
      if (url.toString().contains(name)) {
         return;
      }

      if (url.indexOf("?") == -1) {
         url.append("?");
      }
      else {
         url.append("&");
      }
      url.append(name);
   }

   public static void addParameter(StringBuilder url, String name, Object value) {
      if (url.toString().contains(name)) {
         return;
      }

      if (url.indexOf("?") == -1) {
         url.append("?");
      }
      else {
         url.append("&");
      }
      try {
         url.append(name).append("=").append(URLEncoder.encode(String.valueOf(value), "UTF-8"));
      }
      catch (UnsupportedEncodingException e) {
         // throw new
         // ConfigurationProcessingException("Exception while adding the parameter "
         // + name + "=" + value
         // + "to the URL " + url);
         e.printStackTrace();
         // TODO
      }
   }

   public static boolean containsParameter(HttpServletRequest request, String parameterName) {
      return getCurrentUrl(request, true).toString().contains(parameterName);
   }

   public static boolean doesNotContainParamater(HttpServletRequest request, String parameterName) {
      return !containsParameter(request, parameterName);
   }

   /**
    * <p>
    * Returns the base URL, with the context path included by default.
    * 
    * <p>
    * For Example, for an URL like
    * {@code http://domain.com:port/context/foo/bar}, this function returns
    * {@code http://domain.com:port/context}.
    * 
    * @param request
    *           The {@link HttpServletRequest} from which to extract the base
    *           URL.
    * @return the base URL of the current request.
    */
   public static String getBaseUrl(HttpServletRequest request) {
      return getBaseUrl(request, true);
   }

   /**
    * <p>
    * Returns the base URL, containing the context path or not depending on the
    * {@code withContextPath} param.
    * 
    * <p>
    * For Example, for an URL like
    * {@code http://domain.com:port/context/foo/bar}, this function returns
    * {@code http://domain.com:port/context}.
    * 
    * @param request
    *           The {@link HttpServletRequest} from which to extract the base
    *           URL.
    * @param withContextPath
    *           {@code true} to include the context path, {@code false}
    *           otherwise.
    * @return the base URL of the current request.
    */
   public static String getBaseUrl(HttpServletRequest request, boolean withContextPath) {
      if (request.getRequestURI().equals("/") || request.getRequestURI().equals("")) {
         return request.getRequestURL().toString();
      }
      if (withContextPath) {
         return request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
      }
      else {
         return request.getRequestURL().toString().replace(request.getRequestURI(), "");
      }
   }
}