package com.github.dandelion.core.api.asset;

import com.github.dandelion.core.asset.AssetsStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a set of asset link to Scope/Parent Scope
 */
public class AssetsComponent {
	
	private String scope = AssetsStorage.ROOT_SCOPE;
	private String parent = AssetsStorage.ROOT_SCOPE;
	private List<Asset> assets = new ArrayList<Asset>();

    public AssetsComponent() {
    }

    public AssetsComponent(String scope, String parent, List<Asset> assets) {
        this.scope = scope;
        this.parent = parent;
        this.assets = assets;
    }

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

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

    @Override
    public String toString() {
        return "AssetsComponent [scope=" + scope + ", parent=" + parent + ", assets=" + assets + "]";
    }
}
