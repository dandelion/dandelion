/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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
package com.github.dandelion.core.bundle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Depth-first search algorithm allowing to apply a
 * topological sort on the {@link BundleDag}.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class TopologicalSorter {

	private final static Integer NOT_VISTITED = new Integer(0);
	private final static Integer VISITING = new Integer(1);
	private final static Integer VISITED = new Integer(2);

	public static List<String> sort(BundleDag graph) {
		return dfs(graph);
	}

	public static List<String> sort(Bundle vertex) {
		List<String> retValue = new LinkedList<String>();

		dfsVisit(vertex, new HashMap<Bundle, Integer>(), retValue);

		return retValue;
	}

	private static List<String> dfs(BundleDag graph) {
		List<String> retValue = new LinkedList<String>();
		Map<Bundle, Integer> vertexStateMap = new HashMap<Bundle, Integer>();

		for (Bundle vertex : graph.getVerticies()) {
			if (isNotVisited(vertex, vertexStateMap)) {
				dfsVisit(vertex, vertexStateMap, retValue);
			}
		}

		return retValue;
	}

	private static boolean isNotVisited(Bundle vertex, Map<Bundle, Integer> vertexStateMap) {
		Integer state = vertexStateMap.get(vertex);

		return (state == null) || NOT_VISTITED.equals(state);
	}

	private static void dfsVisit(Bundle vertex, Map<Bundle, Integer> vertexStateMap, List<String> list) {
		vertexStateMap.put(vertex, VISITING);

		for (Bundle v : vertex.getChildren()) {
			if (isNotVisited(v, vertexStateMap)) {
				dfsVisit(v, vertexStateMap, list);
			}
		}

		vertexStateMap.put(vertex, VISITED);

		list.add(vertex.getName());
	}
}
