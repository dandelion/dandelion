package com.github.dandelion.core.asset;

/**
 * An asset can't have a couple of Scope/Parent Scope when his scope is already associated to another parent scope
 */
public class ParentScopeIncompatibilityException extends RuntimeException {
    String scope;
    String parentScope;

    public ParentScopeIncompatibilityException(String scope, String parentScope) {
        this.scope = scope;
        this.parentScope = parentScope;
    }

    public String getScope() {
        return scope;
    }

    public String getParentScope() {
        return parentScope;
    }
}
