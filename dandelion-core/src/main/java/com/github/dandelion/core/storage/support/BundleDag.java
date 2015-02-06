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
package com.github.dandelion.core.storage.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.storage.BundleStorageUnit;

/**
 * <p>
 * Implementation of a directed acyclic graph adapted to Dandelion.
 * </p>
 * <p>
 * In this graph, all verticies are {@link BundleStorageUnit}.
 * </p>
 * <p>
 * Part of this code has been kindly borrowed and adapted from Michal Maczka,
 * who contributed a DAG implementation to <a
 * href="http://plexus.codehaus.org/plexus-utils">Plexus Utils</a>.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class BundleDag {

   private Map<String, BundleStorageUnit> vertexMap = new HashMap<String, BundleStorageUnit>();
   private List<BundleStorageUnit> vertexList = new ArrayList<BundleStorageUnit>();

   public List<BundleStorageUnit> getVerticies() {
      return vertexList;
   }

   public Set<String> getBundleNames() {
      return vertexMap.keySet();
   }

   public BundleStorageUnit addVertexIfNeeded(BundleStorageUnit bsu) {
      return addVertexIfNeeded(bsu.getName());
   }

   public BundleStorageUnit addVertexIfNeeded(String bundleName) {
      BundleStorageUnit retValue = null;

      // Check if vertex is already in the DAG
      if (vertexMap.containsKey(bundleName)) {
         retValue = vertexMap.get(bundleName);
      }
      else {
         retValue = new BundleStorageUnit(bundleName);
         vertexMap.put(bundleName, retValue);
         vertexList.add(retValue);
      }

      return retValue;
   }

   public void addEdge(BundleStorageUnit from, BundleStorageUnit to) {

      from.addEdgeTo(to);
      to.addEdgeFrom(from);

      List<BundleStorageUnit> cycle = BundleCycleDetector.introducesCycle(to);

      if (cycle != null) {
         StringBuilder sb = new StringBuilder("A cycle has been detected in the asset graph for the bundle ");
         sb.append(from.getName());
         sb.append(".");
         throw new DandelionException(sb.toString());
      }
   }

   public void removeEdge(BundleStorageUnit from, BundleStorageUnit to) {
      from.removeEdgeTo(to);
      to.removeEdgeFrom(from);
   }

   public Map<String, BundleStorageUnit> getVertexMap() {
      return this.vertexMap;
   }

   public BundleStorageUnit getVertex(String bundleName) {
      BundleStorageUnit retValue = vertexMap.get(bundleName);
      return retValue;
   }

   public boolean hasEdge(String bundleName1, String bundleName2) {
      BundleStorageUnit b1 = getVertex(bundleName1);
      BundleStorageUnit b2 = getVertex(bundleName2);
      return b1.getChildren().contains(b2);
   }

   /**
    * @param bundleName
    * @return
    */
   public List<String> getChildLabels(String bundleName) {
      BundleStorageUnit vertex = getVertex(bundleName);
      return vertex.getChildNames();
   }

   /**
    * Indicates if there is at least one edge leading to or from vertex of given
    * label
    * 
    * @return <code>true</true> if this vertex is connected with other vertex,<code>false</code>
    *         otherwise
    */
   public boolean isConnected(String bundleName) {
      BundleStorageUnit vertex = getVertex(bundleName);
      return vertex.isConnected();

   }
}