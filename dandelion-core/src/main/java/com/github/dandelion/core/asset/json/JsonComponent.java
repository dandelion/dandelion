package com.github.dandelion.core.asset.json;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON component to wrapper Asset Definition
 */
public class JsonComponent {
	
	private String scope = AssetStorage.ROOT_SCOPE;
	private String parent = AssetStorage.ROOT_SCOPE;
	private List<Asset> assets = new ArrayList<Asset>();

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
        return "JsonComponent [scope=" + scope + ", parent=" + parent + ", assets=" + assets + "]";
    }
}
