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
package com.github.dandelion.extras.bower;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;
import com.github.dandelion.core.util.PathUtils;

public class Test {

   public static void main(String[] args) {
      BowerConfiguration bowerConf = null;
      ObjectMapper mapper = new ObjectMapper();
      try {
         bowerConf = mapper
               .readValue(
                     new File(
                           "C:\\Users\\thibz\\workspaces\\dandelion\\Dandelion\\dandelion-core-samples\\core-thymeleaf-bower\\bower_components\\datatables\\bower.json"),
                     BowerConfiguration.class);
      }
      catch (JsonGenerationException e) {
         e.printStackTrace();
      }
      catch (JsonMappingException e) {
         e.printStackTrace();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      System.out.println(bowerConf);
      BundleStorageUnit bsu = toDandelionBundles(bowerConf);
      System.out.println(bsu);
   }
   
   public static BundleStorageUnit toDandelionBundles(BowerConfiguration bowerConf) {
      
      Set<AssetStorageUnit> asus = new HashSet<AssetStorageUnit>();
      
      BundleStorageUnit bsu = new BundleStorageUnit();
      bsu.setName(bowerConf.getName());
      bsu.setDependencies(new ArrayList<String>(bowerConf.getDependencies().keySet()));
      for(String mainAsset : bowerConf.getMain()) {
         
         if(mainAsset.endsWith("css") || mainAsset.endsWith("js")) {
            AssetStorageUnit asu = new AssetStorageUnit();
            asu.setName(PathUtils.extractLowerCasedName(mainAsset));
            asu.setVersion(bowerConf.getVersion());
            asu.setVendor(true);
            
            Map<String, String> locations = new HashMap<String, String>();
            locations.put("webapp", bowerConf.getName() + "/" + mainAsset);
            asu.setLocations(locations);
            asus.add(asu);
         }
      }
      
      bsu.setAssetStorageUnits(asus);
      return bsu;
   }
}
