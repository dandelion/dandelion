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

/**
 * <p>
 * Utility used to buffer logs mainly raised by exceptions, before being
 * pretty-printed flushed using the configured logger.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class LogBuilder {

   private static String PLACEHOLDER = "\\{\\}";
   protected StringBuilder logBuilder;

   public LogBuilder() {
      this.logBuilder = new StringBuilder();
   }

   protected void end(String line) {
      this.logBuilder.append(line);
   }

   protected void line(String line) {
      this.logBuilder.append(line).append("\n");
   }

   protected void line(String line, Object p1) {
      this.logBuilder.append(replace(line, p1)).append("\n");
   }

   protected void line(String line, Object p1, Object p2) {
      this.logBuilder.append(replace(replace(line, p1), p2)).append("\n");
   }

   protected void line(String line, Object[] pArr) {
      String newLine = line;
      for (Object aPArr : pArr) {
         newLine = replace(newLine, aPArr);
      }
      this.logBuilder.append(newLine).append("\n");
   }

   @Override
   public String toString() {
      return this.logBuilder.toString();
   }

   private String replace(String str, Object replacement) {
      return str.replaceFirst(PLACEHOLDER, (replacement == null ? "" : param(replacement)));
   }

   private String param(Object p) {
      if (p == null) {
         return null;
      }
      return p.toString().replaceAll("\\$", "\\.");
   }
}
