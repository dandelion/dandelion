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
package com.github.dandelion.core.web.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.dandelion.core.Context;

/**
 * <p>
 * Wrapper object holding the context in which a {@link HandlerChain} is
 * applied.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class HandlerContext {

   private final Context context;
   private final HttpServletRequest request;
   private final HttpServletResponse response;
   private byte[] responseAsBytes;

   public HandlerContext(Context context, HttpServletRequest request, HttpServletResponse response,
         byte[] responseAsBytes) {
      super();
      this.context = context;
      this.request = request;
      this.response = response;
      this.responseAsBytes = responseAsBytes;
   }

   public Context getContext() {
      return context;
   }

   public HttpServletRequest getRequest() {
      return request;
   }

   public HttpServletResponse getResponse() {
      return response;
   }

   public byte[] getResponseAsBytes() {
      return responseAsBytes;
   }

   public void setResponseAsBytes(byte[] responseAsBytes) {
      this.responseAsBytes = responseAsBytes;
   }
}
