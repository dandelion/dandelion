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
package com.github.dandelion.core.web.handler.cache;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.util.DigestUtils;
import com.github.dandelion.core.web.handler.HandlerContext;

/**
 * <p>
 * Collection of utilities to ease working with HTTP headers.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class HttpHeaderUtils {

   private static final Logger LOG = LoggerFactory.getLogger(HttpHeaderUtils.class);

   /**
    * <p>
    * Computes a MD5 hash of the provided response. This hash is used as an ETag
    * value.
    * </p>
    * <p>
    * The ETag is wrapped with {@code "} as suggested in the RFC 2616, section
    * 14.19.
    * </p>
    * 
    * @param response
    *           The response from which the ETag is computed.
    * @param context
    *           The context in which the ETag must be computed.
    * @return a MD5 hash of the provided response, used as an ETag.
    */
   public static String computeETag(byte[] response, HandlerContext context) {

      Configuration configuration = context.getContext().getConfiguration();

      StringBuilder etagValue = new StringBuilder();

      if (response == null || response.length == 0) {
         return etagValue.toString();
      }

      try {
         etagValue.append("\"");
         etagValue.append(DigestUtils.md5Digest(new String(response, configuration.getEncoding())));
         etagValue.append("\"");
      }
      catch (UnsupportedEncodingException e) {
         LOG.warn("Unable to calculte the ETag of the resource corresponding to the request URL: {}", context
               .getRequest().getRequestURL());
      }

      return etagValue.toString();
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private HttpHeaderUtils() {
      throw new AssertionError();
   }
}
