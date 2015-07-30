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
package com.github.dandelion.core.asset;

import static com.github.dandelion.core.asset.AssetDomPosition.body;
import static com.github.dandelion.core.asset.AssetDomPosition.head;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Types of an asset. Currently, only stylesheets and scripts are supported.
 * </p>
 * 
 * @author Romain Lespinasse
 * @since 0.2.0
 */
public enum AssetType {

   css("text/css", head, "css"), js("application/javascript", body, "js");

   private String contentType;
   private AssetDomPosition defaultDom;
   private String[] extensions;

   private AssetType(String contentType, AssetDomPosition defaultDom, String... extensions) {
      this.contentType = contentType;
      this.defaultDom = defaultDom;
      this.extensions = extensions;
   }

   public String getContentType() {
      return contentType;
   }

   public AssetDomPosition getDefaultDom() {
      return defaultDom;
   }

   public String[] getExtensions() {
      return extensions;
   }

   /**
    * @return all asset extensions supported by Dandelion.
    */
   public static List<String> getCompatibleExtensions() {
      List<String> retval = new ArrayList<String>();
      for (AssetType assetType : values()) {
         Collections.addAll(retval, assetType.getExtensions());
      }
      return retval;
   }

   public static AssetType extractFromRequest(HttpServletRequest request) {

      Pattern p = Pattern.compile("/[a-f0-9]{32}/(.*)/");
      Matcher m = p.matcher(request.getRequestURL());

      String assetType = null;
      if (m.find()) {
         assetType = m.group(1);
      }

      for (AssetType type : values()) {
         if (assetType.toLowerCase().endsWith(type.name())) {
            return type;
         }
      }
      return null;
   }

   public static AssetType extractFromAssetLocation(String assetLocation) {
      for (AssetType type : values()) {
         if (assetLocation.toLowerCase().endsWith(type.name())) {
            return type;
         }
      }
      return null;
   }
}
