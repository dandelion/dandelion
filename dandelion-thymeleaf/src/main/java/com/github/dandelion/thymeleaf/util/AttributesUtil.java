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
package com.github.dandelion.thymeleaf.util;

/**
 * Util for Thymeleaf Attribute
 */
public class AttributesUtil {
   /**
    * strip the Dialect Prefix from the attribute name
    * 
    * @param attributeName
    *           attribute name
    * @param dialectPrefix
    *           dialect prefix
    * @return the clean attribute name
    */
   public static String stripPrefix(String attributeName, String dialectPrefix) {
      if (!attributeName.startsWith(dialectPrefix))
         return attributeName;
      return attributeName.substring(dialectPrefix.length() + 1, attributeName.length());
   }

   /**
    * Find the Attribute Name object who matches the attribute name String
    * 
    * @param attributeName
    *           attribute name
    * @param names
    *           attribute name objects
    * @return the matched Attribute Name
    */
   public static AttributeName find(String attributeName, AttributeName[] names) {
      for (AttributeName name : names) {
         String processedName = attributeName.contains("data-") ? attributeName.substring(9) : attributeName;
         if (name.getAttribute().equalsIgnoreCase(processedName)) {
            return name;
         }
      }
      return null;
   }
}
