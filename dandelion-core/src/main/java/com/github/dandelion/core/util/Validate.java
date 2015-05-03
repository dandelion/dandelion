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

import java.util.Collection;

/**
 * <p>
 * Collection of utilities to ease validating arguments.
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class Validate {

   public static void notNull(Object object, String message) {
      if (object == null) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void notBlank(String object, String message) {
      if (StringUtils.isBlank(object)) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void notBlank(StringBuilder object, String message) {
      Validate.notNull(object, message);
      if (object.length() == 0) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void notEmpty(Collection<?> object, String message) {
      if (object == null || object.size() == 0) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void notEmpty(Object[] object, String message) {
      if (object == null || object.length == 0) {
         throw new IllegalArgumentException(message);
      }
   }

   public static void containsNoNulls(Iterable<?> collection, String message) {
      for (Object object : collection) {
         notNull(object, message);
      }
   }

   public static void containsNoEmpties(Iterable<String> collection, String message) {
      for (String object : collection) {
         notBlank(object, message);
      }
   }

   public static void containsNoNulls(Object[] array, String message) {
      for (Object object : array) {
         notNull(object, message);
      }
   }

   public static void isTrue(boolean condition, String message) {
      if (!condition) {
         throw new IllegalArgumentException(message);
      }
   }

   /**
    * Prevents instantiation.
    */
   private Validate() {
      super();
   }
}
