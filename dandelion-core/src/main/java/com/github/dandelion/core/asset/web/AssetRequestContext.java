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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.config.Configuration;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * Main user-side entry point for manipulating the assets graph associated to
 * the current {@link HttpServletRequest}.
 * 
 * <p>
 * The assets graph can be manipulated in many ways:
 * <ul>
 * <li>By adding/removing bundle(s) (using the bundle names) <br/>
 * For example:
 * 
 * <pre>
 * AssetRequestContext.get(request).addBundle(&quot;myBundle&quot;);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * AssetRequestContext.get(request).addBundles(&quot;myBundle1&quot;, &quot;myBundle2&quot;);
 * </pre>
 * 
 * </li>
 * <li>By excluding asset(s) (using the asset names) <br/>
 * For example:
 * 
 * <pre>
 * AssetRequestContext.get(request).addBundle(&quot;myBundle1&quot;).excludeAsset(&quot;assetName1&quot;);
 * </pre>
 * 
 * </li>
 * <li>By parameterizing asset(s). <br/>
 * For example:
 * 
 * <pre>
 * AssetRequestContext.get(request).addBundle(&quot;myBundle1&quot;).addParameter(&quot;assetName1&quot;, &quot;paramName1&quot;, &quot;paramValue1&quot;);
 * </pre>
 * 
 * </li>
 * </ul>
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class AssetRequestContext {

	/**
	 * List of bundle to activate for the current request
	 */
	private List<String> bundles;

	/**
	 * List of bundle to exclude from the current request
	 */
	private List<String> excludedBundles;

	/**
	 * List of assets to exclude from the current request
	 */
	private List<String> excludedAssets;

	/**
	 * List of asset parameters
	 */
	private Map<String, Map<String, Object>> parameters;

	/**
	 * Private constructor.
	 */
	private AssetRequestContext() {
		this.bundles = new ArrayList<String>();
		this.excludedBundles = new ArrayList<String>();
		this.excludedAssets = new ArrayList<String>();
		this.parameters = new HashMap<String, Map<String, Object>>();
	}

	/**
	 * <p>
	 * Returns the {@link AssetRequestContext} associated to the passed
	 * {@link ServletRequest}.
	 * 
	 * <p>
	 * If it doesn't exist, a new instance is created and stored as a request
	 * attribute.
	 * 
	 * @param servletRequest
	 *            The servlet request in which is stored the
	 *            {@link AssetRequestContext}.
	 * @return the instance of {@link AssetRequestContext} associated with the
	 *         current servlet request.
	 */
	public static AssetRequestContext get(ServletRequest servletRequest) {
		Object attribute = servletRequest.getAttribute(AssetRequestContext.class.getCanonicalName());
		if (attribute == null || !(attribute instanceof AssetRequestContext)) {
			attribute = new AssetRequestContext();
			((AssetRequestContext) attribute).addBundles(Configuration.getBundleIncludes());
			servletRequest.setAttribute(AssetRequestContext.class.getCanonicalName(), attribute);
		}
		return AssetRequestContext.class.cast(attribute);
	}

	/**
	 * <p>
	 * Adds the given comma-separated list of bundle to the current
	 * {@link AssetRequestContext}.
	 * 
	 * @param bundles
	 *            A comma-separated list of bundles.
	 * @return the current {@link AssetRequestContext} updated with the active
	 *         bundle.
	 */
	public AssetRequestContext addBundles(String bundles) {
		if (bundles == null || bundles.isEmpty()) {
			return this;
		}
		return addBundles(bundles.split(","));
	}

	/**
	 * <p>
	 * Adds the given bundle array to the current {@link AssetRequestContext}.
	 * 
	 * @param bundles
	 *            An array containing the bundle names.
	 * @return the current {@link AssetRequestContext}.
	 */
	public AssetRequestContext addBundles(String... bundles) {
		this.bundles.addAll(Arrays.asList(bundles));
		return this;
	}

	/**
	 * <p>
	 * Adds the given collection of bundles to the current
	 * {@link AssetRequestContext}.
	 * 
	 * @param bundles
	 *            A collection of bundle names.
	 * @return the current {@link AssetRequestContext}.
	 */
	public AssetRequestContext addBundles(Collection<String> bundles) {
		for (String bundle : bundles) {
			addBundle(bundle);
		}
		return this;
	}

	/**
	 * <p>
	 * Adds the given array of enum to the current {@link AssetRequestContext}.
	 * <p>
	 * All enums are first processed by replacing "_" by "-" and by lowercasing
	 * its value.
	 * 
	 * @param bundles
	 *            An array containing the enums.
	 * @return the current {@link AssetRequestContext}.
	 */
	public AssetRequestContext addBundles(Enum<?>... bundles) {
		for (Enum<?> bundle : bundles) {
			addBundle(bundle);
		}
		return this;
	}

	/**
	 * <p>
	 * Adds the given bundle name to the current {@link AssetRequestContext}.
	 * 
	 * @param bundle
	 *            The bundle name to add.
	 * @return the current {@link AssetRequestContext#}
	 */
	public AssetRequestContext addBundle(String bundle) {
		this.bundles.add(bundle.trim());
		return this;
	}

	/**
	 * <p>
	 * Adds the given enum (representing a bundle name) to the current
	 * {@link AssetRequestContext}.
	 * 
	 * @param bundle
	 *            The enum to add.
	 * @return the current {@link AssetRequestContext}.
	 */
	public AssetRequestContext addBundle(Enum<?> bundle) {
		addBundle(bundle.toString().toLowerCase().replace("_", "-"));
		return this;
	}

	/**
	 * @return all bundle names stored in the current
	 *         {@link AssetRequestContext}.
	 */
	public String[] getBundles(boolean withoutExcludedBundles) {
		List<String> bundles = new ArrayList<String>(this.bundles);
		if (withoutExcludedBundles) {
			bundles.removeAll(excludedBundles);
		}
		return bundles.toArray(new String[bundles.size()]);
	}

	/**
	 * <p>
	 * Excludes a comma-separated list of bundle names from the current
	 * {@link AssetRequestContext}.
	 * 
	 * @param bundles
	 *            A comma-separated list of bundle names to exclude.
	 * @return the current {@link AssetRequestContext}.
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
	 * Fluent exclude for asset names
	 * 
	 * @param assetNames
	 *            asset names (separated by comma)
	 * @return this context
	 */
	public AssetRequestContext excludeAssets(String assetNames) {
		if (StringUtils.isNotBlank(assetNames)) {
			return excludeAssets(assetNames.split(","));
		}
		else {
			return this;
		}
	}

	/**
	 * Fluent exclude for asset names
	 * 
	 * @param assetNames
	 *            asset names
	 * @return this context
	 */
	private AssetRequestContext excludeAssets(String... assetNames) {
		for (String assetName : assetNames) {
			this.excludedAssets.add(assetName.trim().toLowerCase());
		}
		return this;
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
}
