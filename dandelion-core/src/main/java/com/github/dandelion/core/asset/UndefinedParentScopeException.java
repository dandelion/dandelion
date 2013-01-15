package com.github.dandelion.core.asset;

/**
 * An asset can't have a parent scope who don't already exists
 */
public class UndefinedParentScopeException extends RuntimeException {

    public UndefinedParentScopeException() {
    }
}
