package com.github.dandelion.core.asset;

/**
 * An asset can't be define with the detach scope as his scope, only as his parent scope
 */
public class DetachScopeNotAllowedException extends RuntimeException {
    String detachScope;

    public DetachScopeNotAllowedException(String detachScope) {
        this.detachScope = detachScope;
    }

    public String getDetachScope() {
        return detachScope;
    }
}
