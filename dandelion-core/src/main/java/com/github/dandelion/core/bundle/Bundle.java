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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dandelion.core.asset.Asset;

/**
 * <p>Representation of a bundle.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bundle {

	private List<Bundle> children = new LinkedList<Bundle>();
	private List<Bundle> parents = new LinkedList<Bundle>();
	private String name;
	private Set<Asset> assets = new LinkedHashSet<Asset>();
	private List<String> dependencies;

	public Bundle(){
	}
	
	public Bundle(String name) {
		this.name = name;
	}

	public Bundle(String name, Set<Asset> assets) {
		this.name = name;
		this.assets = assets;
	}

	/**
	 * @return
	 */
	@JsonProperty(value = "bundle")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Set<Asset> getAssets() {
		return assets;
	}

	public void setAssets(Set<Asset> assets) {
		this.assets = assets;
	}

	/**
	 * @param vertex
	 */
	public void addEdgeTo(Bundle vertex) {
		children.add(vertex);
	}

	/**
	 * @param vertex
	 */
	public void removeEdgeTo(Bundle vertex) {
		children.remove(vertex);
	}

	/**
	 * @param vertex
	 */
	public void addEdgeFrom(Bundle vertex) {
		parents.add(vertex);
	}

	public void removeEdgeFrom(Bundle vertex) {
		parents.remove(vertex);
	}

	public List<Bundle> getChildren() {
		return children;
	}

	/**
	 * Get the labels used by the most direct children.
	 * 
	 * @return the labels used by the most direct children.
	 */
	public List<String> getChildNames() {
		List<String> retValue = new ArrayList<String>(children.size());

		for (Bundle vertex : children) {
			retValue.add(vertex.getName());
		}
		return retValue;
	}

	/**
	 * Get the list the most direct ancestors (parents).
	 * 
	 * @return list of parents
	 */
	public List<Bundle> getParents() {
		return parents;
	}

	/**
	 * Get the labels used by the most direct ancestors (parents).
	 * 
	 * @return the labels used parents
	 */
	public List<String> getParentBundleNames() {
		List<String> retValue = new ArrayList<String>(parents.size());

		for (Bundle vertex : parents) {
			retValue.add(vertex.getName());
		}
		return retValue;
	}

	/**
	 * Indicates if given vertex has no child
	 * 
	 * @return <code>true</true> if this vertex has no child, <code>false</code>
	 *         otherwise
	 */
	public boolean isLeaf() {
		return children.size() == 0;
	}

	/**
	 * Indicates if given vertex has no parent
	 * 
	 * @return <code>true</true> if this vertex has no parent, <code>false</code>
	 *         otherwise
	 */
	public boolean isRoot() {
		return parents.size() == 0;
	}

	/**
	 * Indicates if there is at least one edee leading to or from given vertex
	 * 
	 * @return <code>true</true> if this vertex is connected with other vertex,<code>false</code>
	 *         otherwise
	 */
	public boolean isConnected() {
		return isRoot() || isLeaf();
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
		Bundle other = (Bundle) obj;
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
		return "Bundle [name=" + name + ", assets=" + assets + ", dependencies=" + dependencies + "]";
	}
}