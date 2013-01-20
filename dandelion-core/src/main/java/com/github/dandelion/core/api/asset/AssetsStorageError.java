package com.github.dandelion.core.api.asset;

import com.github.dandelion.core.api.DandelionError;

/**
 * Possible Errors for 'Assets Storage'
 */
public enum AssetsStorageError implements DandelionError {
    /**
     * An asset can't be add twice in the same scope (same name)
     */
    ASSET_ALREADY_EXISTS_IN_SCOPE(100),
    /**
     * An asset can't be add with 'Detached Scope' as his scope,
     * 'Detached Scope' is only allowed as a Parent Scope
     */
    DETACH_SCOPE_NOT_ALLOWED(101),
    /**
     * An asset can't have a couple of Scope/Parent Scope
     * when his scope is already associated to another parent scope
     */
    PARENT_SCOPE_INCOMPATIBILITY(102),
    /**
     * An asset can't have a parent scope who don't already exists
     */
    UNDEFINED_PARENT_SCOPE(103);

    private final int number;

    private AssetsStorageError(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

}
