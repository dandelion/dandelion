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

import com.github.dandelion.core.asset.web.data.AssetName;
import com.github.dandelion.core.asset.web.data.AssetScope;
import com.github.dandelion.core.config.Configuration;

import javax.servlet.ServletRequest;
import java.util.*;

/**
 * Request Context used to store all needed assets by scopes during the pages processing
 */
public class AssetsRequestContext {
    private List<String> scopes;
    private boolean alreadyRendered;
    private List<String> excludedScopes;
    private List<String> excludedAssets;
    private Map<String, Map<String, Object>> parameters;

    private AssetsRequestContext() {
        this.scopes = new ArrayList<String>();
        this.excludedScopes = new ArrayList<String>();
        this.excludedAssets = new ArrayList<String>();
        this.parameters = new HashMap<String, Map<String, Object>>();
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
            ((AssetsRequestContext) attribute).addScopes(Configuration.getProperties().getProperty("assets.included.scopes"));
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
     * Fluent exclude for scopes
     * @param scopes scopes
     * @return this context
     */
    private AssetsRequestContext excludeScopes(AssetScope... scopes) {
        for(AssetScope scope:scopes) {
            excludeScope(scope);
        }
        return this;
    }

    /**
     * Fluent exclude for scopes (as Object with toString())
     * @param scopes scopes
     * @return this context
     */
    private AssetsRequestContext excludeScopes(Object... scopes) {
        for(Object scope:scopes) {
            excludeScope(scope);
        }
        return this;
    }

    /**
     * Fluent exclude for scope
     * @param scope scope
     * @return this context
     */
    private AssetsRequestContext excludeScope(String scope) {
        this.excludedScopes.add(scope);
        return this;
    }

    /**
     * Fluent exclude for scope
     * @param scope scope
     * @return this context
     */
    private AssetsRequestContext excludeScope(AssetScope scope) {
        this.excludedScopes.add(scope.toString());
        return this;
    }

    /**
     * Fluent exclude for scope (as Object with toString())
     * @param scope scope
     * @return this context
     */
    private AssetsRequestContext excludeScope(Object scope) {
        this.excludedScopes.add(scope.toString());
        return this;
    }

    /**
     * Fluent exclude for asset names
     * @param assetNames asset names (separated by comma)
     * @return this context
     */
    public AssetsRequestContext excludeAssets(String assetNames) {
        if(assetNames == null) return this;
        return excludeAssets(assetNames.split(","));
    }

    /**
     * Fluent exclude for asset names
     * @param assetNames asset names
     * @return this context
     */
    private AssetsRequestContext excludeAssets(String... assetNames) {
        this.excludedAssets.addAll(Arrays.asList(assetNames));
        return this;
    }

    /**
     * Fluent exclude for asset names
     * @param assetNames asset names
     * @return this context
     */
    private AssetsRequestContext excludeAssets(AssetName... assetNames) {
        for(Object asset:assetNames) {
            excludeAsset(asset);
        }
        return this;
    }

    /**
     * Fluent exclude for asset names (as Object with toString())
     * @param assetNames asset names
     * @return this context
     */
    private AssetsRequestContext excludeAssets(Object... assetNames) {
        for(Object asset:assetNames) {
            excludeAsset(asset);
        }
        return this;
    }

    /**
     * Fluent exclude for asset name
     * @param assetName asset name
     * @return this context
     */
    private AssetsRequestContext excludeAsset(String assetName) {
        this.excludedAssets.add(assetName);
        return this;
    }

    /**
     * Fluent exclude for asset name
     * @param assetName asset name
     * @return this context
     */
    private AssetsRequestContext excludeAsset(AssetName assetName) {
        this.excludedAssets.add(assetName.toString());
        return this;
    }

    /**
     * Fluent exclude for asset name (as Object with toString())
     * @param assetName asset name
     * @return this context
     */
    private AssetsRequestContext excludeAsset(Object assetName) {
        this.excludedAssets.add(assetName.toString());
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
     * Fluent adder for scopes
     * @param scopes scopes
     * @return this context
     */
    public AssetsRequestContext addScopes(AssetScope ... scopes) {
        for(AssetScope scope:scopes) {
            addScope(scope);
        }
        return this;
    }

    /**
     * Fluent adder for scopes (as Object with toString())
     * @param scopes scopes
     * @return this context
     */
    public AssetsRequestContext addScopes(Object ... scopes) {
        for(Object scope:scopes) {
            addScope(scope);
        }
        return this;
    }

    /**
     * Fluent adder for scope
     * @param scope scope
     * @return this context
     */
    public AssetsRequestContext addScope(String scope) {
        this.scopes.add(scope);
        return this;
    }

    /**
     * Fluent adder for scope
     * @param scope scope
     * @return this context
     */
    public AssetsRequestContext addScope(AssetScope scope) {
        addScope(scope.toString());
        return this;
    }

    /**
     * Fluent adder for scope (as Object with toString())
     * @param scope scope
     * @return this context
     */
    public AssetsRequestContext addScope(Object scope) {
        addScope(scope.toString());
        return this;
    }

    /**
     * @return all stored scopes in this context
     */
    public String[] getScopes(boolean withoutExcludedScopes) {
        List<String> _scopes = new ArrayList<String>(scopes);
        if(withoutExcludedScopes) {
            _scopes.removeAll(excludedScopes);
        }
        return _scopes.toArray(new String[_scopes.size()]);
    }

    /**
     * @return all scopes to remove
     */
    public String[] getExcludedScopes() {
        return excludedScopes.toArray(new String[excludedScopes.size()]);
    }

    /**
     * @return all asset names to remove
     */
    public String[] getExcludedAssets() {
        return excludedAssets.toArray(new String[excludedAssets.size()]);
    }

    /**
     * @return <code>true</code> if this context his already rendered in the response
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
     * Add a parameter value on a specific asset name
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @return this context
     */
    public AssetsRequestContext addParameter(String assetName, String parameter, Object value) {
        return addParameter(assetName, parameter, value, false);
    }

    /**
     * Add a parameter value on a specific asset name
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @return this context
     */
    public AssetsRequestContext addParameter(AssetName assetName, String parameter, Object value) {
        return addParameter(assetName.toString(), parameter, value, false);
    }

    /**
     * Add a parameter value on a specific asset name (as Object with toString())
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @return this context
     */
    public AssetsRequestContext addParameter(Object assetName, String parameter, Object value) {
        return addParameter(assetName.toString(), parameter, value, false);
    }
    
    /**
     * Add a parameter value on a specific asset name
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @param replaceIfExists replace the parameter if he exists already
     * @return this context
     */
    public AssetsRequestContext addParameter(String assetName, String parameter, Object value, boolean replaceIfExists) {
        if(!parameters.containsKey(assetName)) {
            parameters.put(assetName, new HashMap<String, Object>());
        }

        if(!parameters.get(assetName).containsKey(parameter)){
            parameters.get(assetName).put(parameter, value);
        } else if(replaceIfExists) {
            parameters.get(assetName).put(parameter, value);
        }
        return this;
    }

    /**
     * Add a parameter value on a specific asset name
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @param replaceIfExists replace the parameter if he exists already
     * @return this context
     */
    public AssetsRequestContext addParameter(AssetName assetName, String parameter, Object value, boolean replaceIfExists) {
        return addParameter(assetName.toString(), parameter, value, replaceIfExists);
    }

    /**
     * Add a parameter value on a specific asset name (as Object with toString())
     *
     * @param assetName asset name
     * @param parameter parameter
     * @param value value
     * @param replaceIfExists replace the parameter if he exists already
     * @return this context
     */
    public AssetsRequestContext addParameter(Object assetName, String parameter, Object value, boolean replaceIfExists) {
        return addParameter(assetName.toString(), parameter, value, replaceIfExists);
    }

    /**
     * Get the parameters for a asset name
     * @param assetName asset name
     * @return the parameter of the asset name, or empty map
     */
    public Map<String, Object> getParameters(String assetName) {
        if(!parameters.containsKey(assetName)) {
            return Collections.emptyMap();
        }
        return parameters.get(assetName);
    }

    /**
     * Get the parameters for a asset name
     * @param assetName asset name
     * @return the parameter of the asset name, or empty map
     */
    public Map<String, Object> getParameters(AssetName assetName) {
        return getParameters(assetName.toString());
    }

    /**
     * Get the parameters for a asset name (as Object with toString())
     * @param assetName asset name
     * @return the parameter of the asset name, or empty map
     */
    public Map<String, Object> getParameters(Object assetName) {
        return getParameters(assetName.toString());
    }

    /**
     * Get the value of the parameter for the asset name
     * @param assetName asset name
     * @param parameter parameter
     * @param <T> type of the value (aka TypeOfValue value = context.getParameterValue(...) )
     * @return the value of the parameter, or <code>null</code> value
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValue(String assetName, String parameter) {
        Map<String, Object> values = getParameters(assetName);
        if(!values.containsKey(parameter)) {
            return null;
        }
        return (T) values.get(parameter);
    }

    /**
     * Get the value of the parameter for the asset name
     * @param assetName asset name
     * @param parameter parameter
     * @param <T> type of the value (aka TypeOfValue value = context.getParameterValue(...) )
     * @return the value of the parameter, or <code>null</code> value
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValue(AssetName assetName, String parameter) {
        return getParameterValue(assetName.toString(), parameter);
    }

    /**
     * Get the value of the parameter for the asset name (as Object with toString())
     * @param assetName asset name
     * @param parameter parameter
     * @param <T> type of the value (aka TypeOfValue value = context.getParameterValue(...) )
     * @return the value of the parameter, or <code>null</code> value
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValue(Object assetName, String parameter) {
        return getParameterValue(assetName.toString(), parameter);
    }
}
