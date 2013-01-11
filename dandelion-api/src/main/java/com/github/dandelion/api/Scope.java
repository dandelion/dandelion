package com.github.dandelion.api;

public class Scope {

	String name;
	String parent;
	boolean loadedByDefault;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public boolean isLoadedByDefault() {
		return loadedByDefault;
	}
	public void setLoadedByDefault(boolean loadedByDefault) {
		this.loadedByDefault = loadedByDefault;
	}
	
	
}
