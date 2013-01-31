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

package com.github.dandelion.core.asset;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Request Context to stock all needed assets by scopes during page processing
 */
public class AssetsRequestContext {
    private List<String> scopes;
    private boolean alreadyRendered;

    private AssetsRequestContext() {
        this.scopes = new ArrayList<String>();
    }

    /**
     * Access to the Assets Request context for given servlet request
     * @param servletRequest given servlet request
     * @return Assets Request context for given servlet request
     */
    public static AssetsRequestContext get(ServletRequest servletRequest) {
        Object attribute = servletRequest.getAttribute(AssetsRequestContext.class.getCanonicalName());
        if(attribute == null || !(attribute instanceof AssetsRequestContext)) {
            attribute = new AssetsRequestContext();
            servletRequest.setAttribute(AssetsRequestContext.class.getCanonicalName(), attribute);
        }
        return AssetsRequestContext.class.cast(attribute);
    }

    /**
     * Fluent adder for scopes
     * @param scopes scopes (separated by comma)
     * @return this context
     */
    public AssetsRequestContext addScopes(String scopes) {
        if(scopes == null) return this;
        return addScopes(scopes.split(","));
    }

    /**
     * Fluent adder for scopes
     * @param scopes scopes
     * @return this context
     */
    public AssetsRequestContext addScopes(String ... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Fluent adder for one scope
     * @param scope scope
     * @return this context
     */
    public AssetsRequestContext addScope(String scope) {
        this.scopes.add(scope);
        return this;
    }

    /**
     * Verify if a scope has been stored in this context
     * @return <code>true</code> if this context have at least one scope stored
     */
    public boolean hasScopes() {
        return !this.scopes.isEmpty();
    }

    /**
     * @return all stored scopes in this context
     */
    public String[] getScopes() {
        return scopes.toArray(new String[scopes.size()]);
    }

    /**
     * @return <code>true</code> if this context his already rendered in the reponse
     */
    public boolean isAlreadyRendered() {
        return alreadyRendered;
    }

    /**
     * Set this context as rendered
     * @return this context
     */
    public AssetsRequestContext hasBeenRendered() {
        this.alreadyRendered = true;
        return this;
    }
}
