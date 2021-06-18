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
package com.github.dandelion.core.web;

import java.io.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * <p>
 * Used to wrap the real {@link HttpServletResponse} so that it can be modified
 * after the target of the request has delivered its response.
 * </p>
 * <p>
 * All streams sit on top of an underlying byte-output stream that will be used
 * in the {@link DandelionFilter} to adapt the response with the requested
 * assets.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.1
 */
public class ByteArrayResponseWrapper extends HttpServletResponseWrapper {

   /**
    * The underlying byte-output stream.
    */
   private ByteArrayOutputStream baos = new ByteArrayOutputStream();

   /**
    * PrintWriter that sits on top of the byte-output stream.
    */
   private PrintWriter pw;

   /**
    * Flag which indicates if the current response is part of a redirect
    * scenario.
    */
   private boolean isRedirect;

   public ByteArrayResponseWrapper(HttpServletResponse response, String encoding) throws UnsupportedEncodingException {
      super(response);
      this.pw = new PrintWriter(new OutputStreamWriter(baos, encoding));
   }

   @Override
   public PrintWriter getWriter() throws IOException {
      return pw;
   }

   @Override
   public void sendRedirect(String location) throws IOException {
      this.isRedirect = true;
      super.sendRedirect(location);
   }

   /**
    * <p>
    * Gets the content of the underlying byte-output stream.
    * </p>
    * 
    * @return the byte array containing the response.
    */
   public byte[] toByteArray() {

      pw.flush();
      return baos.toByteArray();
   }

   public boolean isRedirect() {
      return isRedirect;
   }
}
