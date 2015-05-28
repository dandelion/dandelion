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
package com.github.dandelion.core.web;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Wrapper intended to store request attributes for limited time (60s by
 * default).
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class RequestFlashData implements Serializable {

   private static final long serialVersionUID = -1022931524402467694L;
   private final Map<String, Object> attributes;
   private final long expirationStartTime;
   private final int timeToLive;

   public RequestFlashData(HttpServletRequest request) {

      this.expirationStartTime = System.currentTimeMillis();
      this.timeToLive = 60;

      Map<String, Object> currentAttributes = new HashMap<String, Object>();
      Enumeration<String> attrs = request.getAttributeNames();
      while (attrs.hasMoreElements()) {
         String attributeName = (String) attrs.nextElement();
         currentAttributes.put(attributeName, request.getAttribute(attributeName));
      }

      this.attributes = currentAttributes;
   }

   public Map<String, Object> getAttributes() {
      return attributes;
   }

   /**
    * Return whether this instance has expired depending on the amount of
    * elapsed time since the instanciation.
    */
   public boolean isExpired() {
      return (this.expirationStartTime != 0 && (System.currentTimeMillis() - this.expirationStartTime > this.timeToLive * 1000));
   }

   @Override
   public String toString() {
      return "RequestData [attributes=" + attributes + ", expirationStartTime=" + expirationStartTime + ", timeToLive="
            + timeToLive + "]";
   }
}
