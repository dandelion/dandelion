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
package com.github.dandelion.core.reporting;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Basic alert used to wrap all necessary information that can be reported.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class Alert {

   public enum AlertType {
      MISSING_BUNDLE;
   }

   /**
    * The missing bundle, if applicable.
    */
   private String requestedBundle;

   /**
    * The alert type.
    */
   private AlertType alertType;

   /**
    * The suggestions, if applicable.
    */
   private Set<Suggestion> suggestions;

   public Alert(String requestedBundle) {
      this.requestedBundle = requestedBundle;
   }

   public AlertType getAlertType() {
      return alertType;
   }

   public void setAlertType(AlertType alertType) {
      this.alertType = alertType;
   }

   public String getRequestedBundle() {
      return requestedBundle;
   }

   public void setRequestedBundle(String requestedBundle) {
      this.requestedBundle = requestedBundle;
   }

   public void addSuggestion(Suggestion suggestion) {
      if (this.suggestions == null) {
         this.suggestions = new HashSet<Suggestion>();
      }
      this.suggestions.add(suggestion);
   }

   public Set<Suggestion> getSuggestions() {
      return suggestions;
   }

   public void setSuggestions(Set<Suggestion> suggestions) {
      this.suggestions = suggestions;
   }

   @Override
   public String toString() {
      return "Alert [requestedBundle=" + requestedBundle + ", alertType=" + alertType + ", suggestions=" + suggestions
            + "]";
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((requestedBundle == null) ? 0 : requestedBundle.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Alert))
         return false;
      Alert other = (Alert) obj;
      if (requestedBundle == null) {
         if (other.requestedBundle != null)
            return false;
      }
      else if (!requestedBundle.equals(other.requestedBundle))
         return false;
      return true;
   }
}
