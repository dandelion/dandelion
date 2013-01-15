package com.github.dandelion.core.asset;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of a list of assets associated to a scope and his parent scope
 */
class StorageUnit {
    String scope;
    String parentScope;
    List<Asset> assets;

    /**
     * A new container is a scope with his parent scope and a empty list of assets
     * @param scope scope of assets
     * @param parentScope parent of scope
     */
    public StorageUnit(String scope, String parentScope) {
        this.scope = scope;
        this.parentScope = parentScope;
        this.assets = new ArrayList<Asset>();
    }
}
