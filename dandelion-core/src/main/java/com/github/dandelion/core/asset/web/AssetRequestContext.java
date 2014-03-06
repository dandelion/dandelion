/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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

import java.util.*;

import javax.servlet.ServletRequest;

import com.github.dandelion.core.asset.web.data.AssetName;
import com.github.dandelion.core.asset.web.data.AssetBundle;
import com.github.dandelion.core.config.Configuration;

/**
 * <p>
 * Request context used to store all needed assets by bundle loaded within a
 * page.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetRequestContext {

	private List<String> bundles;
	private List<String> excludedBundles;
	private List<String> excludedAssets;
	private Map<String, Map<String, Object>> parameters;

	private AssetRequestContext() {
		this.bundles = new ArrayList<String>();
		this.excludedBundles = new ArrayList<String>();
		this.excludedAssets = new ArrayList<String>();
		this.parameters = new HashMap<String, Map<String, Object>>();
	}

	/**
	 * Access to the Assets Request context for given servlet request
	 * 
	 * @param servletRequest
	 *            given servlet request
	 * @return Assets Request context for given servlet request
	 */
	public static AssetRequestContext get(ServletRequest servletRequest) {
		Object attribute = servletRequest.getAttribute(AssetRequestContext.class.getCanonicalName());
		if (attribute == null || !(attribute instanceof AssetRequestContext)) {
			attribute = new AssetRequestContext();
			((AssetRequestContext) attribute).addBundles(Configuration.getProperties().getProperty("bundle.include"));
			servletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), attribute);
		}
		return AssetRequestContext.class.cast(attribute);
	}

	/**
	 * Fluent exclude for bundles.
	 * 
	 * @param bundles
	 *            Comma-separated bundles to exclude.
	 * @return this context
	 */
	public AssetRequestContext excludeBundles(String bundles) {
		if (bundles == null || bundles.isEmpty()) {
			return this;
		}
		return excludeBundles(bundles.split(","));
	}

	/**
	 * Fluent exclude for bundles.
	 * 
	 * @param bundles
	 *            bundles
	 * @return this context
	 */
	private AssetRequestContext excludeBundles(String... bundles) {
		this.excludedBundles.addAll(Arrays.asList(bundles));
		return this;
	}

	/**
	 * Fluent exclude for scopes
	 * 
	 * @param bundles
	 *            scopes
	 * @return this context
	 */
	private AssetRequestContext excludeBundles(AssetBundle... bundles) {
		for (AssetBundle bundle : bundles) {
			excludeBundle(bundle);
		}
		return this;
	}

	/**
	 * Fluent exclude for scopes (as Object with toString())
	 * 
	 * @param bundles
	 *            scopes
	 * @return this context
	 */
	private AssetRequestContext excludeBundles(Object... bundles) {
		for (Object bundle : bundles) {
			excludeBundle(bundle);
		}
		return this;
	}

	/**
	 * Fluent exclude for scope
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	private AssetRequestContext excludeBundle(String bundle) {
		this.excludedBundles.add(bundle);
		return this;
	}

	/**
	 * Fluent exclude for scope
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	private AssetRequestContext excludeBundle(AssetBundle bundle) {
		this.excludedBundles.add(bundle.toString());
		return this;
	}

	/**
	 * Fluent exclude for scope (as Object with toString())
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	private AssetRequestContext excludeBundle(Object bundle) {
		this.excludedBundles.add(bundle.toString());
		return this;
	}

	/**
	 * Fluent exclude for asset names
	 * 
	 * @param assetNames
	 *            asset names (separated by comma)
	 * @return this context
	 */
	public AssetRequestContext excludeAssets(String assetNames) {
		if (assetNames == null || assetNames.isEmpty())
			return this;
		return excludeAssets(assetNames.split(","));
	}

	/**
	 * Fluent exclude for asset names
	 * 
	 * @param assetNames
	 *            asset names
	 * @return this context
	 */
	private AssetRequestContext excludeAssets(String... assetNames) {
		this.excludedAssets.addAll(Arrays.asList(assetNames));
		return this;
	}

	/**
	 * Fluent exclude for asset names
	 * 
	 * @param assetNames
	 *            asset names
	 * @return this context
	 */
	private AssetRequestContext excludeAssets(AssetName... assetNames) {
		for (Object asset : assetNames) {
			excludeAsset(asset);
		}
		return this;
	}

	/**
	 * Fluent exclude for asset names (as Object with toString())
	 * 
	 * @param assetNames
	 *            asset names
	 * @return this context
	 */
	private AssetRequestContext excludeAssets(Object... assetNames) {
		for (Object asset : assetNames) {
			excludeAsset(asset);
		}
		return this;
	}

	/**
	 * Fluent exclude for asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @return this context
	 */
	private AssetRequestContext excludeAsset(String assetName) {
		this.excludedAssets.add(assetName);
		return this;
	}

	/**
	 * Fluent exclude for asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @return this context
	 */
	private AssetRequestContext excludeAsset(AssetName assetName) {
		this.excludedAssets.add(assetName.toString());
		return this;
	}

	/**
	 * Fluent exclude for asset name (as Object with toString())
	 * 
	 * @param assetName
	 *            asset name
	 * @return this context
	 */
	private AssetRequestContext excludeAsset(Object assetName) {
		this.excludedAssets.add(assetName.toString());
		return this;
	}

	/**
	 * Fluent adder for bundles.
	 * 
	 * @param bundles
	 *            A string containing comma-separated bundles.
	 * @return this context
	 */
	public AssetRequestContext addBundles(String bundles) {
		if (bundles == null || bundles.isEmpty()) {
			return this;
		}
		return addBundles(bundles.split(","));
	}

	/**
	 * Fluent adder for scopes
	 * 
	 * @param bundles
	 *            scopes
	 * @return this context
	 */
	public AssetRequestContext addBundles(String... bundles) {
		this.bundles.addAll(Arrays.asList(bundles));
		return this;
	}

	/**
	 * Fluent adder for scopes
	 * 
	 * @param bundles
	 *            scopes
	 * @return this context
	 */
	public AssetRequestContext addBundles(AssetBundle... bundles) {
		for (AssetBundle bundle : bundles) {
			addBundle(bundle);
		}
		return this;
	}

	/**
	 * Fluent adder for scopes (as Object with toString())
	 * 
	 * @param bundles
	 *            scopes
	 * @return this context
	 */
	public AssetRequestContext addBundles(Object... bundles) {
		for (Object bundle : bundles) {
			addBundle(bundle);
		}
		return this;
	}

	/**
	 * Fluent adder for scopes (as Object with toString())
	 * 
	 * @param bundles
	 *            scopes
	 * @return this context
	 */
	public AssetRequestContext addBundles(Enum<?>... bundles) {
		for (Enum<?> bundle : bundles) {
			addBundle(bundle);
		}
		return this;
	}

	/**
	 * Fluent adder for scope
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	public AssetRequestContext addBundle(String bundle) {
		this.bundles.add(bundle);
		return this;
	}

	/**
	 * Fluent adder for scope
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	public AssetRequestContext addBundle(AssetBundle bundle) {
		addBundle(bundle.toString());
		return this;
	}

	/**
	 * Fluent adder for scope (as Object with toString())
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	public AssetRequestContext addBundle(Object bundle) {
		addBundle(bundle.toString());
		return this;
	}

	/**
	 * Fluent adder for scope (as Object with toString())
	 * 
	 * @param bundle
	 *            scope
	 * @return this context
	 */
	public AssetRequestContext addBundle(Enum<?> bundle) {
		addBundle(bundle.toString().toLowerCase().replace("_", "-"));
		return this;
	}

	/**
	 * @return all stored scopes in this context
	 */
	public String[] getBundles(boolean withoutExcludedBundles) {
		List<String> _bundles = new ArrayList<String>(bundles);
		if (withoutExcludedBundles) {
			_bundles.removeAll(excludedBundles);
		}
		return _bundles.toArray(new String[_bundles.size()]);
	}

	/**
	 * @return all scopes to remove
	 */
	public String[] getExcludedBundles() {
		return excludedBundles.toArray(new String[excludedBundles.size()]);
	}

	/**
	 * @return all asset names to remove
	 */
	public String[] getExcludedAssets() {
		return excludedAssets.toArray(new String[excludedAssets.size()]);
	}

	/**
	 * Add a parameter value on a specific asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param value
	 *            value
	 * @return this context
	 */
	public AssetRequestContext addParameter(String assetName, String parameter, Object value) {
		return addParameter(assetName, parameter, value, false);
	}

	/**
	 * Add a parameter value on a specific asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param value
	 *            value
	 * @return this context
	 */
	public AssetRequestContext addParameter(AssetName assetName, String parameter, Object value) {
		return addParameter(assetName.toString(), parameter, value, false);
	}

	/**
	 * Add a parameter value on a specific asset name (as Object with
	 * toString())
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param value
	 *            value
	 * @return this context
	 */
	public AssetRequestContext addParameter(Object assetName, String parameter, Object value) {
		return addParameter(assetName.toString(), parameter, value, false);
	}

	/**
	 * Add a parameter value on a specific asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param value
	 *            value
	 * @param replaceIfExists
	 *            replace the parameter if he exists already
	 * @return this context
	 */
	public AssetRequestContext addParameter(String assetName, String parameter, Object value, boolean replaceIfExists) {
		if (!parameters.containsKey(assetName)) {
			parameters.put(assetName, new HashMap<String, Object>());
		}

		if (!parameters.get(assetName).containsKey(parameter)) {
			parameters.get(assetName).put(parameter, value);
		}
		else if (replaceIfExists) {
			parameters.get(assetName).put(parameter, value);
		}
		return this;
	}

	/**
	 * Add a parameter value on a specific asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param value
	 *            value
	 * @param replaceIfExists
	 *            replace the parameter if he exists already
	 * @return this context
	 */
	public AssetRequestContext addParameter(AssetName assetName, String parameter, Object value, boolean replaceIfExists) {
		return addParameter(assetName.toString(), parameter, value, replaceIfExists);
	}

	/**
	 * Add a parameter value on a specific asset name (as Object with
	 * toString())
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param value
	 *            value
	 * @param replaceIfExists
	 *            replace the parameter if he exists already
	 * @return this context
	 */
	public AssetRequestContext addParameter(Object assetName, String parameter, Object value, boolean replaceIfExists) {
		return addParameter(assetName.toString(), parameter, value, replaceIfExists);
	}

	/**
	 * Get the parameters for a asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @return the parameter of the asset name, or empty map
	 */
	public Map<String, Object> getParameters(String assetName) {
		if (!parameters.containsKey(assetName)) {
			return Collections.emptyMap();
		}
		return parameters.get(assetName);
	}

	/**
	 * Get the parameters for a asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @return the parameter of the asset name, or empty map
	 */
	public Map<String, Object> getParameters(AssetName assetName) {
		return getParameters(assetName.toString());
	}

	/**
	 * Get the parameters for a asset name (as Object with toString())
	 * 
	 * @param assetName
	 *            asset name
	 * @return the parameter of the asset name, or empty map
	 */
	public Map<String, Object> getParameters(Object assetName) {
		return getParameters(assetName.toString());
	}

	/**
	 * Get the value of the parameter for the asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param <T>
	 *            type of the value (aka TypeOfValue value =
	 *            context.getParameterValue(...) )
	 * @return the value of the parameter, or <code>null</code> value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParameterValue(String assetName, String parameter) {
		Map<String, Object> values = getParameters(assetName);
		if (!values.containsKey(parameter)) {
			return null;
		}
		return (T) values.get(parameter);
	}

	/**
	 * Get the value of the parameter for the asset name
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param <T>
	 *            type of the value (aka TypeOfValue value =
	 *            context.getParameterValue(...) )
	 * @return the value of the parameter, or <code>null</code> value
	 */
	public <T> T getParameterValue(AssetName assetName, String parameter) {
		return getParameterValue(assetName.toString(), parameter);
	}

	/**
	 * Get the value of the parameter for the asset name (as Object with
	 * toString())
	 * 
	 * @param assetName
	 *            asset name
	 * @param parameter
	 *            parameter
	 * @param <T>
	 *            type of the value (aka TypeOfValue value =
	 *            context.getParameterValue(...) )
	 * @return the value of the parameter, or <code>null</code> value
	 */
	public <T> T getParameterValue(Object assetName, String parameter) {
		return getParameterValue(assetName.toString(), parameter);
	}

	public void clear() {
		this.bundles = new ArrayList<String>();
		this.excludedBundles = new ArrayList<String>();
		this.excludedAssets = new ArrayList<String>();
		this.parameters = new HashMap<String, Map<String, Object>>();
	}
}
