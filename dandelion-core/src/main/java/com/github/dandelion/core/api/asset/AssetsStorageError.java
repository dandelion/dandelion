/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of DataTables4j nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
