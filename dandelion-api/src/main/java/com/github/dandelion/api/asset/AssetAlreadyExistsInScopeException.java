package com.github.dandelion.api.asset;

/**
 * An asset can't be add twice in the same scope (same name)
 */
public class AssetAlreadyExistsInScopeException extends RuntimeException {
    Asset originalAsset;

    public AssetAlreadyExistsInScopeException(Asset originalAsset) {
        this.originalAsset = originalAsset;
    }

    public Asset getOriginalAsset() {
        return originalAsset;
    }
}
