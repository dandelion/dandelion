package com.github.dandelion.core.asset;

public class Asset {

	String name;
	String version;
	AssetType type;
	String cdn;
	String local;
	
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
	public AssetType getType() {
		return type;
	}
	public void setType(AssetType type) {
		this.type = type;
	}
	public String getCdn() {
		return cdn;
	}
	public void setCdn(String cdn) {
		this.cdn = cdn;
	}
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	@Override
	public String toString() {
		return "Asset [name=" + name + ", version=" + version + ", type=" + type + ", cdn=" + cdn
				+ ", local=" + local + "]";
	}
	
}
