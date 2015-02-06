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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.dandelion.core.storage.BundleStorageUnit;

/**
 * <p>
 * Cycle detector used by the {@link BundleDag}.
 * 
 * <p>
 * Part of this code has been kindly borrowed and adapted from Michal Maczka,
 * who contributed a DAG implementation to <a
 * href="http://plexus.codehaus.org/plexus-utils">Plexus Utils</a>.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class BundleCycleDetector {

   private static Integer NOT_VISTITED = new Integer(0);
   private static Integer VISITING = new Integer(1);
   private static Integer VISITED = new Integer(2);

   public static List<BundleStorageUnit> hasCycle(BundleDag graph) {
      List<BundleStorageUnit> verticies = graph.getVerticies();

      Map<BundleStorageUnit, Integer> vertexStateMap = new HashMap<BundleStorageUnit, Integer>();

      List<BundleStorageUnit> retValue = null;

      for (BundleStorageUnit vertex : verticies) {
         if (isNotVisited(vertex, vertexStateMap)) {
            retValue = introducesCycle(vertex, vertexStateMap);

            if (retValue != null) {
               break;
            }
         }
      }

      return retValue;
   }

   /**
    * This method will be called when an edge leading to given vertex was added
    * and we want to check if introduction of this edge has not resulted in
    * apparition of cycle in the graph
    * 
    * @param vertex
    * @param vertexStateMap
    * @return
    */
   public static List<BundleStorageUnit> introducesCycle(BundleStorageUnit vertex,
         Map<BundleStorageUnit, Integer> vertexStateMap) {
      LinkedList<BundleStorageUnit> cycleStack = new LinkedList<BundleStorageUnit>();

      boolean hasCycle = dfsVisit(vertex, cycleStack, vertexStateMap);

      if (hasCycle) {
         BundleStorageUnit firstBsu = cycleStack.getFirst();
         int pos = cycleStack.lastIndexOf(firstBsu);
         List<BundleStorageUnit> cycle = cycleStack.subList(0, pos + 1);
         Collections.reverse(cycle);
         return cycle;
      }

      return null;
   }

   public static List<BundleStorageUnit> introducesCycle(BundleStorageUnit vertex) {
      Map<BundleStorageUnit, Integer> vertexStateMap = new HashMap<BundleStorageUnit, Integer>();
      return introducesCycle(vertex, vertexStateMap);
   }

   private static boolean isNotVisited(BundleStorageUnit vertex, Map<BundleStorageUnit, Integer> vertexStateMap) {
      Integer state = vertexStateMap.get(vertex);
      return (state == null) || NOT_VISTITED.equals(state);
   }

   private static boolean isVisiting(BundleStorageUnit vertex, Map<BundleStorageUnit, Integer> vertexStateMap) {
      Integer state = vertexStateMap.get(vertex);
      return VISITING.equals(state);
   }

   private static boolean dfsVisit(BundleStorageUnit vertex, LinkedList<BundleStorageUnit> cycle,
         Map<BundleStorageUnit, Integer> vertexStateMap) {
      cycle.addFirst(vertex);

      vertexStateMap.put(vertex, VISITING);

      for (BundleStorageUnit v : vertex.getChildren()) {
         if (isNotVisited(v, vertexStateMap)) {
            boolean hasCycle = dfsVisit(v, cycle, vertexStateMap);

            if (hasCycle) {
               return true;
            }
         }
         else if (isVisiting(v, vertexStateMap)) {
            cycle.addFirst(v);

            return true;
         }
      }
      vertexStateMap.put(vertex, VISITED);

      cycle.removeFirst();

      return false;
   }
}