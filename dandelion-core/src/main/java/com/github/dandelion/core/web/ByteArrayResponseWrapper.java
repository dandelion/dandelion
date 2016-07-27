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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
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
   private ByteArrayOutputStream baos;

   /**
    * PrintWriter that sits on top of the byte-output stream.
    */
   private PrintWriter pw;
   
   private ServletOutputStream so;

   /**
    * Flag which indicates if the current response is part of a redirect
    * scenario.
    */
   private boolean isRedirect;

   public ByteArrayResponseWrapper(HttpServletResponse response) {
      super(response);
   }

   @Override
   public PrintWriter getWriter() throws IOException {
      if (so!=null)
         throw new IllegalStateException("getOutputStream called");
      if (pw==null) {
         baos = new ByteArrayOutputStream();
         pw = new PrintWriter(new OutputStreamWriter(baos,getCharacterEncoding()));
      }
      return pw;
   }

   @Override
   public ServletOutputStream getOutputStream() throws IOException
   {
      if (pw!=null)
         throw new IllegalStateException("getWriter called");
      if (so==null) {
    	 baos = new ByteArrayOutputStream();
    	 so = new ServletOutputStream() {
    	   @Override
    	   public void write(byte[] b) throws IOException
    	   {
    		  baos.write(b);
    	   }
    	   @Override
    	   public void write(int b)
    	   {
    		  baos.write(b);
    	   }
    	   @Override
    	   public void write(byte[] b, int off, int len)
    	   {
    		  baos.write(b,off,len);
    	   }
    	   @Override
    	   public void close() throws IOException
    	   {
    		  baos.close();
    	   }
    	 };
      }
      return so;
   }
      
   @Override
   public void flushBuffer() throws IOException {
      // suppress the flushBuffer
   }

   @Override
   public void resetBuffer() {
      pw=null;
      baos=null;
   }

   @Override
   public void setContentLength(int len) {
      // suppress the content-length, so content may be altered
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
      if (pw!=null)
          pw.flush();
      if (baos!=null)
          return baos.toByteArray();
      return new byte[0];
   }

   public boolean isRedirect() {
      return isRedirect;
   }
}