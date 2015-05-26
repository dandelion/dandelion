/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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
package com.github.dandelion.core.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.support.BundleDag;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Bundle storage unit used by the configured JSON deserializer.
 * <p>
 * This object is also used as a vertex inside the {@link BundleDag}.
 * 
 * @author Thibault Duchateau
 * @version 0.10.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleStorageUnit {

   // Exposed attributes
   private String name;

   private List<String> dependencies;

   private Set<AssetStorageUnit> assetStorageUnits = new LinkedHashSet<AssetStorageUnit>();

   // Internal attributes
   private List<BundleStorageUnit> children = new LinkedList<BundleStorageUnit>();

   private List<BundleStorageUnit> parents = new LinkedList<BundleStorageUnit>();

   private String relativePath;

   private String bundleLoaderOrigin;

   private boolean vendor;

   public BundleStorageUnit() {
   }

   public BundleStorageUnit(String name) {
      this.name = name;
   }

   public BundleStorageUnit(String name, Set<AssetStorageUnit> assetStorageUnits) {
      this.name = name;
      this.assetStorageUnits = assetStorageUnits;
   }

   @JsonProperty(value = "bundle")
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void addDependency(String dep) {
      if (this.dependencies == null) {
         this.dependencies = new ArrayList<String>();
      }
      this.dependencies.add(dep);
   }

   public List<String> getDependencies() {
      return dependencies;
   }

   public void setDependencies(List<String> dependencies) {
      this.dependencies = dependencies;
   }

   @JsonProperty(value = "assets")
   @JsonDeserialize(as = LinkedHashSet.class)
   public Set<AssetStorageUnit> getAssetStorageUnits() {
      return assetStorageUnits;
   }

   public Set<String> getAssetStorageUnitNames() {
      Set<String> asus = new HashSet<String>();
      if (assetStorageUnits != null) {
         for (AssetStorageUnit asu : assetStorageUnits) {
            if (StringUtils.isNotBlank(asu.getName())) {
               asus.add(asu.getName().toLowerCase());
            }
         }
      }
      return asus;
   }

   public Set<String> getJsAssetStorageUnitNames() {
      Set<String> asus = new HashSet<String>();
      if (assetStorageUnits != null) {
         for (AssetStorageUnit asu : assetStorageUnits) {
            if (asu.getType().equals(AssetType.js)) {
               asus.add(asu.getName().toLowerCase());
            }
         }
      }
      return asus;
   }

   public Set<String> getCssAssetStorageUnitNames() {
      Set<String> asus = new HashSet<String>();
      if (assetStorageUnits != null) {
         for (AssetStorageUnit asu : assetStorageUnits) {
            if (asu.getType().equals(AssetType.css)) {
               asus.add(asu.getName().toLowerCase());
            }
         }
      }
      return asus;
   }

   public void setAssetStorageUnits(Set<AssetStorageUnit> assetStorageUnits) {
      this.assetStorageUnits = assetStorageUnits;
   }

   public void addEdgeTo(BundleStorageUnit vertex) {
      children.add(vertex);
   }

   public void removeEdgeTo(BundleStorageUnit vertex) {
      children.remove(vertex);
   }

   public void addEdgeFrom(BundleStorageUnit vertex) {
      parents.add(vertex);
   }

   public void removeEdgeFrom(BundleStorageUnit vertex) {
      parents.remove(vertex);
   }

   public List<BundleStorageUnit> getChildren() {
      return children;
   }

   public List<String> getChildNames() {
      List<String> retValue = new ArrayList<String>(children.size());

      for (BundleStorageUnit vertex : children) {
         retValue.add(vertex.getName());
      }
      return retValue;
   }

   public List<BundleStorageUnit> getParents() {
      return parents;
   }

   public List<String> getParentBundleNames() {
      List<String> retValue = new ArrayList<String>(parents.size());

      for (BundleStorageUnit vertex : parents) {
         retValue.add(vertex.getName());
      }
      return retValue;
   }

   public String getRelativePath() {
      return relativePath;
   }

   public void setRelativePath(String bundleRelativePath) {
      this.relativePath = bundleRelativePath;
   }

   public boolean isLeaf() {
      return children.size() == 0;
   }

   public boolean isRoot() {
      return parents.size() == 0;
   }

   /**
    * <p>
    * Indicates if there is at least one edge leading to or from the current
    * vertex.
    * 
    * @return {@code true} if this vertex is connected with another vertex,
    *         {@code false} otherwise.
    */
   public boolean isConnected() {
      return isRoot() || isLeaf();
   }

   public String getBundleLoaderOrigin() {
      return bundleLoaderOrigin;
   }

   public void setBundleLoaderOrigin(String bundleLoaderOrigin) {
      this.bundleLoaderOrigin = bundleLoaderOrigin;
   }

   public boolean isVendor() {
      return vendor;
   }

   public void setVendor(boolean vendor) {
      this.vendor = vendor;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
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
      BundleStorageUnit other = (BundleStorageUnit) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return name + " [assets=" + assetStorageUnits.size() + ", dependencies=" + dependencies + "]";
   }
}
