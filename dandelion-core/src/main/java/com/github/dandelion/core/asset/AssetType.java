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

/**
 * <p>
 * All asset types supported by Dandelion.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public enum AssetType {

   // Raw CSS
   css("css", "text/css", "css", head),

   // Less CSS
   less("less", "text/css", "css", head),

   // Raw JavaScript
   js("js", "application/javascript", "js", body);

   private String sourceExtension;
   private String contentType;
   private String targetExtension;
   private AssetDomPosition defaultDom;

   private AssetType(String sourceExtension, String contentType, String targetExtension, AssetDomPosition defaultDom) {
      this.sourceExtension = sourceExtension;
      this.contentType = contentType;
      this.targetExtension = targetExtension;
      this.defaultDom = defaultDom;
   }

   public String getSourceExtension() {
      return this.sourceExtension;
   }

   public String getContentType() {
      return this.contentType;
   }

   public String getTargetExtension() {
      return this.targetExtension;
   }

   public AssetDomPosition getDefaultDom() {
      return this.defaultDom;
   }

   /**
    * @return all asset extensions supported by Dandelion.
    */
   public static List<String> getCompatibleExtensions() {
      List<String> retval = new ArrayList<String>();
      for (AssetType assetType : values()) {
         Collections.addAll(retval, assetType.getSourceExtension());
      }
      return retval;
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
