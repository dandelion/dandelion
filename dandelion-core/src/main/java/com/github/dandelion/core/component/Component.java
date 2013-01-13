package com.github.dandelion.core.component;

import java.util.List;

import com.github.dandelion.core.asset.Asset;


public class Component {
	
	private String scope;
	private String parent;
	private List<Asset> assets;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public List<Asset> getAssets() {
		return assets;
	}

	public void setAssets(List<Asset> assets) {
		this.assets = assets;
	}

	@Override
	public String toString() {
		return "Component [scope=" + scope + ", assets=" + assets + "]";
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	
}
