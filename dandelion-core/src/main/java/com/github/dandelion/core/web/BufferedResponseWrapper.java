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

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * <p>
 * {@link javax.servlet.http.HttpServletResponse} wrapper that caches all
 * content written to the {@link #getOutputStream() output stream} and
 * {@link #getWriter() writer}, and allows this content to be retrieved via a
 * {@link #getContentAsBytes() byte array}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.2.0
 */
public class BufferedResponseWrapper extends HttpServletResponseWrapper {

   /**
    * Buffer for the original OutputStream.
    */
   private ByteArrayServletOutputStream bufferOutputStream;

   /**
    * Buffer for the original PrintWriter.
    */
   private CharArrayWriter bufferedWriter;

   /**
    * Wrapping OutputStream.
    */
   private ServletOutputStream outStream;

   /**
    * Wrapping PrintWriter.
    */
   private PrintWriter outWriter;

   /**
    * Flag which indicates if the current response is part of a redirect
    * scenario.
    */
   private boolean redirect;

   public BufferedResponseWrapper(HttpServletResponse response) {
      super(response);
   }

   @Override
   public ServletOutputStream getOutputStream() throws IOException {
      if (outStream == null) {
         if (outWriter != null) {
            throw new IllegalStateException("getWriter() has already been called for this response");
         }
         bufferOutputStream = new ByteArrayServletOutputStream();
         outStream = bufferOutputStream;
      }
      return outStream;
   }

   @Override
   public PrintWriter getWriter() throws IOException {
      if (outWriter == null) {
         if (outStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
         }
         bufferedWriter = new CharArrayWriter();
         outWriter = new PrintWriter(bufferedWriter);
      }
      return outWriter;
   }

   @Override
   public void sendRedirect(String location) throws IOException {
      this.redirect = true;
      super.sendRedirect(location);
   }

   /**
    * @return {@code true} if the current response is part of a redirect
    *         scenario, {@code false} otherwise.
    */
   public boolean isRedirect() {
      return redirect;
   }

   /**
    * <p>
    * Returns the buffered servlet output content as a byte array, no matter if
    * a stream or writer is used.
    * </p>
    */
   public byte[] getContentAsBytes() {

      if (outStream != null) {
         return bufferOutputStream.toByteArray();
      }
      else if (outWriter != null) {
         return bufferedWriter.toString().getBytes();
      }
      else {
         return new byte[0];
      }
   }

   /**
    * <p>
    * Extension of {@link ServletOutputStream} that stores the servlet output
    * stream into a byte array.
    * </p>
    * 
    * @author Thibault Duchateau
    * @since 1.2.0
    */
   private class ByteArrayServletOutputStream extends ServletOutputStream {

      /**
       * Buffer that wraps the stream.
       */
      protected final ByteArrayOutputStream buffer;

      public ByteArrayServletOutputStream() {
         buffer = new ByteArrayOutputStream();
      }

      /**
       * @return the content of the stream as a byte array.
       */
      public byte[] toByteArray() {
         return buffer.toByteArray();
      }

      @Override
      public void write(int b) {
         buffer.write(b);
      }
   }
}
