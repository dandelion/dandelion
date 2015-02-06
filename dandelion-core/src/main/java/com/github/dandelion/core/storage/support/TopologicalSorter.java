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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.dandelion.core.storage.BundleStorageUnit;

/**
 * <p>
 * Java implementation of the Depth-first search algorithm allowing to apply a
 * topological sort on the {@link BundleDag}.
 * 
 * <p>
 * Part of this code has been kindly borrowed and adapted from Michal Maczka,
 * who contributed a DAG implementation to <a
 * href="http://plexus.codehaus.org/plexus-utils">Plexus Utils</a>.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class TopologicalSorter {

   private final static Integer NOT_VISTITED = new Integer(0);
   private final static Integer VISITING = new Integer(1);
   private final static Integer VISITED = new Integer(2);

   public static List<BundleStorageUnit> sort(BundleDag graph) {
      return dfs(graph);
   }

   public static List<BundleStorageUnit> sort(BundleStorageUnit vertex) {
      List<BundleStorageUnit> retValue = new LinkedList<BundleStorageUnit>();

      dfsVisit(vertex, new HashMap<BundleStorageUnit, Integer>(), retValue);

      return retValue;
   }

   private static List<BundleStorageUnit> dfs(BundleDag graph) {
      List<BundleStorageUnit> retValue = new LinkedList<BundleStorageUnit>();
      Map<BundleStorageUnit, Integer> vertexStateMap = new HashMap<BundleStorageUnit, Integer>();

      for (BundleStorageUnit vertex : graph.getVerticies()) {
         if (isNotVisited(vertex, vertexStateMap)) {
            dfsVisit(vertex, vertexStateMap, retValue);
         }
      }

      return retValue;
   }

   private static boolean isNotVisited(BundleStorageUnit vertex, Map<BundleStorageUnit, Integer> vertexStateMap) {
      Integer state = vertexStateMap.get(vertex);

      return (state == null) || NOT_VISTITED.equals(state);
   }

   private static void dfsVisit(BundleStorageUnit vertex, Map<BundleStorageUnit, Integer> vertexStateMap,
         List<BundleStorageUnit> list) {
      vertexStateMap.put(vertex, VISITING);

      for (BundleStorageUnit v : vertex.getChildren()) {
         if (isNotVisited(v, vertexStateMap)) {
            dfsVisit(v, vertexStateMap, list);
         }
      }

      vertexStateMap.put(vertex, VISITED);

      list.add(vertex);
   }
}
