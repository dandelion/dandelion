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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.dandelion.core.asset.locator.AssetLocator;
import com.github.dandelion.core.storage.AssetStorageUnit;

/**
 * <p>
 * Representation of an asset.
 * </p>
 * <p>
 * Contrary to a {@link AssetStorageUnit}, an {@link Asset} contains more fields
 * because it has been resolved by the {@link AssetMapper}.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.0.1
 */
public class Asset implements Serializable {

   private static final long serialVersionUID = -5249405621430370618L;

   /**
    * Arbitrary name given to the asset.
    */
   private String name;

   /**
    * Version of the asset.
    */
   private String version;

   /**
    * Type of the asset.
    */
   private AssetType type;

   /**
    * Position where the asset must be injected.
    */
   private AssetDomPosition dom;

   /**
    * Key of the locator used to get the asset.
    */
   private String configLocationKey;

   /**
    * Raw location of the asset, corresponding to the selected location key.
    */
   private String configLocation;

   private String processedConfigLocation;

   /**
    * Computed location of the asset, using the right {@link AssetLocator}.
    */
   private String finalLocation;

   /**
    * Various HTML attributes of the HTML tag.
    */
   private Map<String, String> attributes;

   /**
    * Various HTML attributes which only need a name.
    */
   private String[] attributesOnlyName;

   /**
    * Condition to use in a conditionnal comment. (IE 5 to 9)
    */
   private String condition;

   // Internal attribute

   /**
    * The computed storage key of the asset.
    */
   private String storageKey;

   /**
    * The parent bundle.
    */
   private String bundle;

   /**
    * Whether the asset has been loaded by the {@link VendorBundleLoader}.
    */
   private boolean vendor;

   private String generatorUid;
   
   public Asset() {
   }

   /**
    * Enforce the declaration of a full asset (mandatory fields).
    * 
    * @param name
    *           name
    * @param version
    *           version
    * @param type
    *           type
    * @param locations
    *           locations of source
    */
   public Asset(String name, String version, AssetType type, Map<String, String> locations) {
      this.name = name;
      this.version = version;
      this.type = type;
   }

   public Asset(String name, String version, AssetType type, String location) {
      this.name = name;
      this.version = version;
      this.type = type;
      this.finalLocation = location;
   }

   protected Asset(String name, String version, AssetType type, AssetDomPosition dom, String location) {
      this.name = name;
      this.version = version;
      this.type = type;
      this.dom = dom;
      this.finalLocation = location;
   }

   public Asset(String name, String version, AssetType type) {
      this.name = name;
      this.version = version;
      this.type = type;
   }

   public Asset(AssetStorageUnit asu) {
      this.name = asu.getName();
      this.type = asu.getType();
      this.version = asu.getVersion();
      this.dom = asu.getDom();
      this.attributes = asu.getAttributes();
      this.attributesOnlyName = asu.getAttributesOnlyName();
      this.vendor = asu.isVendor();
      this.bundle = asu.getBundle();
      this.condition = asu.getCondition();
   }

   public Asset(String name, String version, AssetType type, AssetDomPosition position) {
      this.name = name;
      this.version = version;
      this.type = type;
      this.dom = position;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public AssetType getType() {
      return type;
   }

   public void setType(AssetType type) {
      this.type = type;
   }

   public String getConfigLocation() {
      return configLocation;
   }

   public void setConfigLocation(String configLocation) {
      this.configLocation = configLocation;
   }

   public String getProcessedConfigLocation() {
      return processedConfigLocation;
   }

   public void setProcessedConfigLocation(String processedConfigLocation) {
      this.processedConfigLocation = processedConfigLocation;
   }

   public String getConfigLocationKey() {
      return configLocationKey;
   }

   public void setConfigLocationKey(String configLocationKey) {
      this.configLocationKey = configLocationKey;
   }

   public String getFinalLocation() {
      return finalLocation;
   }

   public void setFinalLocation(String finalLocation) {
      this.finalLocation = finalLocation;
   }

   public AssetDomPosition getDom() {
      return dom;
   }

   public void setDom(AssetDomPosition dom) {
      this.dom = dom;
   }

   public Map<String, String> getAttributes() {
      return attributes;
   }

   public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
   }

   public String[] getAttributesOnlyName() {
      if (attributesOnlyName == null)
         return new String[0];
      return attributesOnlyName;
   }

   public void setAttributesOnlyName(String[] attributesOnlyName) {
      this.attributesOnlyName = attributesOnlyName;
   }

   /**
    * Validate this asset
    * 
    * @return <code>true</code> if the asset is valid
    */
   public boolean isValid() {
      return name != null && version != null && type != null && finalLocation != null;
   }

   public String getAssetKey() {
      return name + "." + type;
   }

   public void addAttribute(String attributeName, String attributeValue) {
      if (attributes == null) {
         attributes = new HashMap<String, String>();
      }

      attributes.put(attributeName, attributeValue);
   }

   public void addAttribute(String attributeName) {
      if (attributesOnlyName == null) {
         attributesOnlyName = new String[] { attributeName };
      }
      else {
         Arrays.copyOf(attributesOnlyName, attributesOnlyName.length + 1);
         attributesOnlyName[attributesOnlyName.length] = attributeName;
      }
   }

   public String getCondition() {
      return condition;
   }

   public void setCondition(String condition) {
      this.condition = condition;
   }

   public String getStorageKey() {
      return storageKey;
   }

   public void setStorageKey(String cacheKey) {
      this.storageKey = cacheKey;
   }

   public boolean isVendor() {
      return vendor;
   }

   public boolean isNotVendor() {
      return !isVendor();
   }

   public String getBundle() {
      return bundle;
   }

   public void setBundle(String bundle) {
      this.bundle = bundle;
   }

   public String getGeneratorUid() {
      return generatorUid;
   }

   public void setGeneratorUid(String generatorUid) {
      this.generatorUid = generatorUid;
   }

   @Override
   public String toString() {
      return "Asset [name=" + name + ", version=" + version + ", type=" + type + ", dom=" + dom + ", configLocation="
            + configLocation + ", configLocationKey=" + configLocationKey + ", finalLocation=" + finalLocation
            + ", attributes=" + attributes + ", attributesOnlyName=" + Arrays.toString(attributesOnlyName) + "]";
   }

   public String toLog() {
      StringBuilder log = new StringBuilder("\"");
      log.append(name);
      log.append("\" (type: ");
      log.append(type);
      log.append(", location: ");
      log.append(configLocation);
      log.append(", location key: ");
      log.append(configLocationKey);
      log.append(")");
      return log.toString();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Asset other = (Asset) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      if (type != other.type)
         return false;
      return true;
   }
}
