package com.github.dandelion.api;

import java.util.List;

public class Component {

	List<Asset> assets;

	public List<Asset> getAssets() {
		return assets;
	}

	public void setAssets(List<Asset> assets) {
		this.assets = assets;
	}

	@Override
	public String toString() {
		return "Component [assets=" + assets + "]";
	}
	
	
}
