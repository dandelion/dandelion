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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;

/**
 * <p>
 * Implementation of a directed acyclic graph adapted to Dandelion
 * {@link Bundle}s and {@link Asset}s.
 * 
 * <p>
 * In this graph, all verticies are {@link Bundle}.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class BundleDag {

	private Map<String, Bundle> vertexMap = new HashMap<String, Bundle>();
	private List<Bundle> vertexList = new ArrayList<Bundle>();

	public List<Bundle> getVerticies() {
		return vertexList;
	}

	public Set<String> getBundleNames() {
		return vertexMap.keySet();
	}

	public Bundle addVertex(Bundle bundle) {

		Bundle retValue = null;

		// check if vertex is alredy in DAG
		if (vertexMap.containsKey(bundle.getName())) {
			retValue = vertexMap.get(bundle.getName());
		}
		else {
			retValue = bundle;
			vertexMap.put(bundle.getName(), retValue);
			vertexList.add(retValue);
		}

		return retValue;
	}

	public Bundle addVertex(String label) {
		Bundle retValue = null;

		// check if vertex is alredy in DAG
		if (vertexMap.containsKey(label)) {
			retValue = vertexMap.get(label);
		}
		else {
			retValue = new Bundle(label);

			vertexMap.put(label, retValue);

			vertexList.add(retValue);
		}

		return retValue;
	}

	public void addEdge(Bundle from, Bundle to) {

		from.addEdgeTo(to);
		to.addEdgeFrom(from);

		List<String> cycle = CycleDetector.introducesCycle(to);

		if (cycle != null) {
			throw new DandelionException(BundleStorageError.CYCLE_DETECTED).set("cycle", from.getName());
		}
	}

	public void removeEdge(Bundle from, Bundle to) {
		from.removeEdgeTo(to);

		to.removeEdgeFrom(from);
	}

	public Map<String, Bundle> getVertexMap() {
		return this.vertexMap;
	}

	public Bundle getVertex(String label) {
		Bundle retValue = (Bundle) vertexMap.get(label);
		return retValue;
	}

	public boolean hasEdge(String bundleName1, String bundleName2) {
		Bundle b1 = getVertex(bundleName1);
		Bundle b2 = getVertex(bundleName2);
		return b1.getChildren().contains(b2);
	}

	/**
	 * @param label
	 * @return
	 */
	public List<String> getChildLabels(String label) {
		Bundle vertex = getVertex(label);

		return vertex.getChildNames();
	}

	/**
	 * Indicates if there is at least one edge leading to or from vertex of
	 * given label
	 * 
	 * @return <code>true</true> if this vertex is connected with other vertex,<code>false</code>
	 *         otherwise
	 */
	public boolean isConnected(String label) {
		Bundle vertex = getVertex(label);

		boolean retValue = vertex.isConnected();

		return retValue;

	}

	/**
	 * Return the list of labels of bundles according to the topological sort.
	 * 
	 * @param bundleName
	 *            The name of the bundle.
	 * 
	 * @return The list of bundle names sorted by a topological order. The list
	 *         also contains the given bundle name, always in last.
	 */
	public List<String> bundlesFor(String bundleName) {
		Bundle vertex = getVertex(bundleName);

		if(vertex != null){
			List<String> retval;
			
			if (vertex.isLeaf()) {
				retval = new ArrayList<String>(1);
				retval.add(bundleName);
			}
			else {
				retval = TopologicalSorter.sort(vertex);
			}
			
			return retval;
		}

		return Collections.emptyList();
	}
}
