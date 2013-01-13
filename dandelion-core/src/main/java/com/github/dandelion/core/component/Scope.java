package com.github.dandelion.core.component;

public class Scope {

	String name;
	boolean loadedByDefault;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isLoadedByDefault() {
		return loadedByDefault;
	}
	public void setLoadedByDefault(boolean loadedByDefault) {
		this.loadedByDefault = loadedByDefault;
	}
}
