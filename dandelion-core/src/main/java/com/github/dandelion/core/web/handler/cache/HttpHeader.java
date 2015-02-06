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

/**
 * <p>
 * All HTTP headers manipulated (R and/or W) by Dandelion.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public enum HttpHeader {

   /**
    * RFC 2616, section 14.9
    */
   CACHE_CONTROL("Cache-Control"),

   /**
    * RFC 2616, section 14.21
    */
   EXPIRES("Expires"),

   /**
    * RFC 2616, section 14.29
    */
   LAST_MODIFIED("Last-Modified"),

   /**
    * RFC 2616, section 14.17
    */
   CONTENT_TYPE("Content-Type"),

   /**
    * RFC 2616, section 14.1
    */
   ACCEPT("Accept"),

   /**
    * RFC 2616, section 14.3
    */
   ACCEPT_ENCODING("Accept-Encoding"),

   /**
    * RFC 2616, section 14.11
    */
   CONTENT_ENCODING("Content-Encoding"),

   /**
    * RFC 2616, section 14.44
    */
   VARY("Vary"),

   /**
    * RFC 2616, section 14.19
    */
   ETAG("ETag"),

   /**
    * RFC 2616, section 14.26
    */
   IFNONEMATCH("If-None-Match");

   private String name;

   private HttpHeader(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }
}
