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
 * 3. Neither the name of Dandelion nor the names of its contributors
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

package com.github.dandelion.core.asset.web;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Request Context used to store all needed assets by scopes during the pages processing
 */
public class AssetsRequestContext {
    private List<String> scopes;
    private boolean alreadyRendered;
    private List<String> excludedScopes;
    private List<String> excludedAssets;
    private AssetParameters parameters;

    private AssetsRequestContext() {
        this.scopes = new ArrayList<String>();
        this.excludedScopes = new ArrayList<String>();
        this.excludedAssets = new ArrayList<String>();
        this.parameters = new AssetParameters();
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
     * Fluent exclude for scopes
     * @param scopes scopes (separated by comma)
     * @return this context
     */
    public AssetsRequestContext excludeScopes(String scopes) {
        if(scopes == null) return this;
        return excludeScopes(scopes.split(","));
    }

    /**
     * Fluent exclude for scopes
     * @param scopes scopes
     * @return this context
     */
    private AssetsRequestContext excludeScopes(String... scopes) {
        this.excludedScopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Fluent exclude for assets
     * @param assets scopes (separated by comma)
     * @return this context
     */
    public AssetsRequestContext excludeAssets(String assets) {
        if(assets == null) return this;
        return excludeAssets(assets.split(","));
    }

    /**
     * Fluent exclude for assets
     * @param assets scopes
     * @return this context
     */
    private AssetsRequestContext excludeAssets(String... assets) {
        this.excludedAssets.addAll(Arrays.asList(assets));
        return this;
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
     * @return all stored scopes in this context
     */
    public String[] getScopes(boolean withoutExcludedScopes) {
        List<String> _scopes = new ArrayList<String>(scopes);
        if(withoutExcludedScopes)
            _scopes.removeAll(excludedScopes);
        return _scopes.toArray(new String[_scopes.size()]);
    }

    /**
     * @return all scopes to remove
     */
    public String[] getExcludedScopes() {
        return excludedScopes.toArray(new String[excludedScopes.size()]);
    }

    /**
     * @return all assets to remove
     */
    public String[] getExcludedAssets() {
        return excludedAssets.toArray(new String[excludedAssets.size()]);
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

    /**
     * Add a parameter value on a specific asset name in Global Group
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @return this context
     */
    public AssetsRequestContext addParameter(String assetName, String parameter, Object value) {
        return addParameter(assetName, parameter, value, AssetParameters.GLOBAL_GROUP);
    }

    /**
     * Add a parameter value on a specific asset name in a group
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @param groupId ID of the group of assets (can be null - aka global group)
     * @return this context
     */
    public AssetsRequestContext addParameter(String assetName, String parameter, Object value, String groupId) {
        if(groupId == null) groupId = AssetParameters.GLOBAL_GROUP;
        parameters.add(assetName, parameter, value, groupId);
        return this;
    }

    /**
     * @return the parameter/value from template asset names.
     */
    public AssetParameters getParameters() {
        return parameters;
    }
}
