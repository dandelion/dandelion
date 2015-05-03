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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.dandelion.core.storage.BundleStorage;

/**
 * <p>
 * Extension of the {@link LogBuilder} used when building the
 * {@link BundleStorage}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class BundleStorageLogBuilder extends LogBuilder {

   private Map<String, Set<String>> errorMap;

   public BundleStorageLogBuilder() {
      super();
      this.errorMap = new HashMap<String, Set<String>>();
   }

   public void error(String errorType, String errorMessage) {
      if (errorMap.containsKey(errorType)) {
         errorMap.get(errorType).add(errorMessage);
      }
      else {
         Set<String> errorMessages = new HashSet<String>();
         errorMessages.add(errorMessage);
         errorMap.put(errorType, errorMessages);
      }
   }

   public void error(String errorType, String bundleName, String errorMessage) {
      StringBuilder error = new StringBuilder("   [");
      error.append(bundleName);
      error.append("] ");
      error.append(errorMessage);

      if (errorMap.containsKey(errorType)) {
         errorMap.get(errorType).add(error.toString());
      }
      else {
         Set<String> errorMessages = new HashSet<String>();
         errorMessages.add(error.toString());
         errorMap.put(errorType, errorMessages);
      }
   }

   @Override
   public String toString() {

      line("The bundle graph is not consistent for the following reasons:");

      for (String errorType : errorMap.keySet()) {
         line(errorType);
         for (String error : errorMap.get(errorType)) {
            line("   " + error);
         }
      }

      return this.logBuilder.toString();
   }

   public Map<String, Set<String>> getErrorMap() {
      return errorMap;
   }

   public boolean hasError() {
      return !this.errorMap.isEmpty();
   }
}
