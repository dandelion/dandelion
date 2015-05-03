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
package com.github.dandelion.core.web.handler.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.UrlUtils;
import com.github.dandelion.core.web.handler.AbstractHandlerChain;
import com.github.dandelion.core.web.handler.HandlerContext;
import com.github.dandelion.core.web.handler.cache.HttpHeader;

/**
 * <p>
 * Post-filtering request handler in charge of compressing text-based resources
 * {@link HttpServletResponse} using GZIP.
 * </p>
 * <p>
 * Part of this code has been kindly borrowed and adapted from the <a
 * href="https://github.com/jhipster/generator-jhipster">JHipster project</a>.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class GzipCompressionPostHandler extends AbstractHandlerChain {

   private static final Logger LOG = LoggerFactory.getLogger(GzipCompressionPostHandler.class);

   /**
    * <p>
    * Gzipping an empty file or stream always results in a 20 byte output. This
    * is in java or elsewhere.
    * </p>
    * <p>
    * Therefore 20 bytes can be used to indicate that the gzip byte[] will be
    * empty when ungzipped.
    * </p>
    */
   private static final int EMPTY_GZIPPED_CONTENT_SIZE = 20;

   @Override
   protected Logger getLogger() {
      return LOG;
   }

   @Override
   public boolean isAfterChaining() {
      return true;
   }

   @Override
   public int getRank() {
      return 30;
   }

   @Override
   public boolean isApplicable(HandlerContext handlerContext) {

      // Retrieves the content type from the filtered response
      String mimeType = handlerContext.getResponse().getContentType();

      // Required conditions
      boolean gzipEnabled = handlerContext.getContext().getConfiguration().isToolGzipEnabled();
      boolean requestNotIncluded = !isIncluded(handlerContext.getRequest());
      boolean browserAcceptsGzip = acceptsGzip(handlerContext.getRequest());
      boolean responseNotCommited = !handlerContext.getResponse().isCommitted();
      boolean compatibleMimeType = getSupportedMimeTypes(handlerContext.getContext()).contains(mimeType);

      LOG.trace(
            "gzipEnabled: {}, requestNotInclude: {}, browserAcceptsGzip: {}, responseNotCommited: {}, compatibleMimeType: {}",
            gzipEnabled, requestNotIncluded, browserAcceptsGzip, responseNotCommited, compatibleMimeType);

      return gzipEnabled && requestNotIncluded && browserAcceptsGzip && responseNotCommited && compatibleMimeType;
   }

   @Override
   public boolean handle(HandlerContext handlerContext) {

      byte[] compressedContent = getGzippedContent(handlerContext.getResponseAsBytes());

      // Double check one more time before writing out
      // response might have been committed due to error
      if (handlerContext.getResponse().isCommitted()) {
         return false;
      }

      // Special cases where the response does not need to be compressed
      switch (handlerContext.getResponse().getStatus()) {
      case HttpServletResponse.SC_NO_CONTENT:
      case HttpServletResponse.SC_RESET_CONTENT:
      case HttpServletResponse.SC_NOT_MODIFIED:
         return false;
      default:
      }

      // No reason to add GZIP headers or write body if no content was written
      // or status code specifies no content
      boolean shouldGzippedBodyBeZero = shouldGzippedBodyBeZero(compressedContent, handlerContext.getRequest());
      boolean shouldBodyBeZero = shouldBodyBeZero(handlerContext.getRequest(), handlerContext.getResponse().getStatus());
      if (shouldGzippedBodyBeZero || shouldBodyBeZero) {
         handlerContext.getResponse().setContentLength(0);
         return false;
      }

      handlerContext.getResponse().setContentLength(compressedContent.length);
      addGzipHeader(handlerContext.getResponse());

      handlerContext.setResponseAsBytes(compressedContent);

      return true;
   }

   /**
    * <p>
    * Checks if the request uri is an "include". These ones cannot be gzipped.
    * </p>
    * 
    * @param request
    *           The client HTTP request.
    * @return {@code true} if the request is an "include", otherwise
    *         {@code false}.
    */
   private boolean isIncluded(HttpServletRequest request) {

      String uri = (String) request.getAttribute(UrlUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
      boolean includeRequest = !(uri == null);

      if (includeRequest && LOG.isTraceEnabled()) {
         LOG.trace("{} resulted in an include request. This is unusable, because"
               + "the response will be assembled into the overrall response. Not gzipping.", request.getRequestURL());
      }
      return includeRequest;
   }

   /**
    * <p>
    * Checks whether the "gzip" encoding is supported or not.
    * </p>
    * 
    * @param request
    *           The client HTTP request.
    * @return {@code true} if gzip is supported, otherwise {@code false}.
    */
   private boolean acceptsGzip(HttpServletRequest request) {
      String acceptEncoding = request.getHeader(HttpHeader.ACCEPT_ENCODING.getName());
      return acceptEncoding != null && (acceptEncoding.contains("gzip") || acceptEncoding.contains("*"));
   }

   /**
    * <p>
    * Compresses the provided byte array in the GZIP file format.
    * </p>
    * 
    * @param response
    *           A byte array holding the data to compress.
    * @return the data compressed in the GZIP file format.
    */
   private byte[] getGzippedContent(byte[] response) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         OutputStream os = new GZIPOutputStream(new BufferedOutputStream(baos));
         IOUtils.copy(new ByteArrayInputStream(response), os);
         os.close();
         return baos.toByteArray();
      }
      catch (IOException e) {
         throw new DandelionException("Problem while computing gzipped content", e);
      }
   }

   /**
    * <p>
    * Updates the response headers to indicate the the response has been
    * gzipped.
    * </p>
    * <p>
    * This is need when a gzipped body is returned so that browsers can properly
    * decompress it.
    * </p>
    * 
    * @param response
    *           The response which will have a header added to it.
    * @throws DandelionException
    *            Either if the response is already committed or if the request
    *            has been called using the include method from a
    *            {@link javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
    *            method and the set header is ignored.
    */
   private static void addGzipHeader(HttpServletResponse response) {
      response.setHeader(HttpHeader.CONTENT_ENCODING.getName(), "gzip");
      boolean containsEncoding = response.containsHeader(HttpHeader.CONTENT_ENCODING.getName());
      if (!containsEncoding) {
         throw new DandelionException("Failure when attempting to set " + "Content-Encoding: gzip");
      }
   }

   /**
    * <p>
    * Checks whether a gzipped body is actually empty and should just be zero.
    * When the compressedBytes is {@link #EMPTY_GZIPPED_CONTENT_SIZE} it should
    * be zero.
    * </p>
    * 
    * @param compressedBytes
    *           The gzipped response body.
    * @param request
    *           The client HTTP request.
    * @return {@code true} if the response should be 0.
    */
   private static boolean shouldGzippedBodyBeZero(byte[] compressedBytes, HttpServletRequest request) {

      // Check for 0 length body
      if (compressedBytes.length == EMPTY_GZIPPED_CONTENT_SIZE) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("{} resulted in an empty response.", request.getRequestURL());
         }
         return true;
      }
      else {
         return false;
      }
   }

   /**
    * <p>
    * Performs a number of checks to ensure response saneness according to the
    * rules of RFC2616:
    * </p>
    * <ol>
    * <li>If the response code is
    * {@link javax.servlet.http.HttpServletResponse#SC_NO_CONTENT} then it is
    * illegal for the body to contain anything. See <a
    * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5"
    * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5</a>
    * <li>If the response code is
    * {@link javax.servlet.http.HttpServletResponse#SC_NOT_MODIFIED} then it is
    * illegal for the body to contain anything. See <a
    * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5"
    * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5</a>
    * </ol>
    * 
    * @param request
    *           the client HTTP request
    * @param responseStatus
    *           the responseStatus
    * @return {@code true} if the response should be 0.
    */
   private static boolean shouldBodyBeZero(HttpServletRequest request, int responseStatus) {

      // Check for NO_CONTENT
      if (responseStatus == HttpServletResponse.SC_NO_CONTENT) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("{} resulted in a {} response. Removing message body in accordance with RFC2616.",
                  request.getRequestURL(), HttpServletResponse.SC_NO_CONTENT);
         }
         return true;
      }

      // Check for NOT_MODIFIED
      if (responseStatus == HttpServletResponse.SC_NOT_MODIFIED) {
         if (LOG.isDebugEnabled()) {
            LOG.debug("{} resulted in a {} response. Removing message body in accordance with RFC2616.",
                  request.getRequestURL(), HttpServletResponse.SC_NOT_MODIFIED);
         }
         return true;
      }
      return false;
   }

   /**
    * @return a set of mime types that can be compressed with gzip.
    */
   public Set<String> getSupportedMimeTypes(Context context) {
      return context.getConfiguration().getToolGzipMimeTypes();
   }
}
