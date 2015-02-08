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
package com.github.dandelion.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Main exception of the Dandelion framework.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public class DandelionException extends RuntimeException {

   private static final long serialVersionUID = -1854486123698434827L;

   /**
    * Parameters of this Exception
    */
   private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

   public DandelionException(String message, Throwable cause) {
      super("Dandelion: " + message, cause);
   }

   public DandelionException(String message) {
      super("Dandelion: " + message);
   }

   public DandelionException(Throwable cause) {
      super(cause);
   }

   /**
    * Add a relevant data (field/value) for this exception
    * 
    * @param field
    *           field of this relevant data
    * @param value
    *           value of this relevant data
    * @return this exception (for 'fluent interface' purpose)
    */
   public DandelionException set(String field, Object value) {
      parameters.put(field, value);
      return this;
   }

   /**
    * Get the value of stored data by his field
    * 
    * @param field
    *           field of stored data
    * @param <T>
    *           type of this value
    * @return the casted value
    */
   @SuppressWarnings("unchecked")
   public <T> T get(String field) {
      return (T) parameters.get(field);
   }

   public static DandelionException wrap(Throwable exception) {
      if (exception instanceof DandelionException) {
         DandelionException se = (DandelionException) exception;
         return se;
      }
      else {
         return new DandelionException(exception.getMessage(), exception);
      }
   }
}
