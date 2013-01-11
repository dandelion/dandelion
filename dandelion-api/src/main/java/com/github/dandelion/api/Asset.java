package com.github.dandelion.api;

public class Asset {

	String name;
	String version;
	String location;
	AssetType type;
	
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public AssetType getType() {
		return type;
	}
	public void setType(AssetType type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "Asset [name=" + name + ", version=" + version + ", location=" + location
				+ ", type=" + type + "]";
	}
}
