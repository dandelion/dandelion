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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.DandelionException;

/**
 * 
 * TODO
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class ResourceUtils {

   private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

   public static InputStream getFileFromClasspath(String pathToFile) {
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathToFile);
   }

   public static String getFileContentFromClasspath(String pathToFile) {
      return getFileContentFromClasspath(pathToFile, true);
   }

   public static String getFileContentFromClasspath(String pathToFile, boolean neverFail) {
      try {
         InputStream in = getFileFromClasspath(pathToFile);
         return getContentFromInputStream(in);
      }
      catch (IOException e) {
         StringBuilder sb = new StringBuilder("The content pointed by the path ");
         sb.append(pathToFile);
         sb.append(" can't be read from an inputStream.");
         throw new DandelionException(sb.toString(), e);
      }
   }

   public static String getContentFromInputStream(InputStream input) throws IOException {
      StringWriter sw = new StringWriter();
      InputStreamReader in = new InputStreamReader(input);

      char[] buffer = new char[DEFAULT_BUFFER_SIZE];
      int n;
      while (-1 != (n = in.read(buffer))) {
         sw.write(buffer, 0, n);
      }

      return sw.toString();
   }

   public static String getContentFromUrl(HttpServletRequest request, String url, boolean neverFail) {
      try {
         if (UrlUtils.isProtocolRelative(url)) {
            url = request.isSecure() ? "https:" : "http:" + url;
         }
         if (UrlUtils.isContextRelative(url, request)) {
            url = UrlUtils.getBaseUrl(request, false) + url;
         }

         URL urlLocation = new URL(url);
         return ResourceUtils.getContentFromInputStream(urlLocation.openStream());
      }
      catch (IOException e) {
         if (neverFail) {
            return "";
         }

         StringBuilder sb = new StringBuilder("The content pointed by the url ");
         sb.append(url);
         sb.append(" can't be read.");
         throw new DandelionException(sb.toString(), e);
      }
   }

   /**
    * Prevents instantiation.
    */
   private ResourceUtils() {
   }
}