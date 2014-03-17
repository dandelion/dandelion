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
package com.github.dandelion.core.asset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.dandelion.core.asset.locator.spi.AssetLocator;
import com.github.dandelion.core.storage.AssetStorageUnit;

/**
 * <p>
 * Representation of an asset.
 * 
 * <p>
 * Contrary to a {@link AssetStorageUnit}, an {@link Asset} contains more fields
 * because it has been resolved by the {@link AssetMapper}.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 */
public class Asset {

	/**
	 * Arbitrary name given to the asset.
	 */
	private String name;

	/**
	 * Version of the asset.
	 */
	private String version;

	/**
	 * Type of the asset.
	 */
	private AssetType type;

	/**
	 * Position where the asset must be injected.
	 */
	private AssetDomPosition dom;

	/**
	 * Key of the locator used to get the asset.
	 */
	private String configLocationKey;

	/**
	 * Raw location of the asset, corresponding to the selected location key.
	 */
	private String configLocation;

	/**
	 * Computed location of the asset, using the right {@link AssetLocator}.
	 */
	private String finalLocation;

	/**
	 * Various HTML attributes of the HTML tag.
	 */
	private Map<String, String> attributes;

	/**
	 * Various HTML attributes which only needs names.
	 */
	private String[] attributesOnlyName;

	// Internal attribute
	private String cacheKey;

	public Asset() {
	}

	/**
	 * Enforce the declaration of a full asset (mandatory fields).
	 * 
	 * @param name
	 *            name
	 * @param version
	 *            version
	 * @param type
	 *            type
	 * @param locations
	 *            locations of source
	 */
	public Asset(String name, String version, AssetType type, Map<String, String> locations) {
		this.name = name;
		this.version = version;
		this.type = type;
	}

	public Asset(String name, String version, AssetType type, String location) {
		this.name = name;
		this.version = version;
		this.type = type;
		this.finalLocation = location;
	}

	protected Asset(String name, String version, AssetType type, AssetDomPosition dom, String location) {
		this.name = name;
		this.version = version;
		this.type = type;
		this.dom = dom;
		this.finalLocation = location;
	}

	public Asset(String name, String version, AssetType type) {
		this.name = name;
		this.version = version;
		this.type = type;
	}

	public Asset(AssetStorageUnit asu) {
		this.name = asu.getName();
		this.type = asu.getType();
		this.version = asu.getVersion();
		this.dom = asu.getDom();
		this.attributes = asu.getAttributes();
		this.attributesOnlyName = asu.getAttributesOnlyName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public AssetType getType() {
		return type;
	}

	public void setType(AssetType type) {
		this.type = type;
	}

	public String getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	public String getConfigLocationKey() {
		return configLocationKey;
	}

	public void setConfigLocationKey(String configLocationKey) {
		this.configLocationKey = configLocationKey;
	}

	public String getFinalLocation() {
		return finalLocation;
	}

	public void setFinalLocation(String finalLocation) {
		this.finalLocation = finalLocation;
	}

	public AssetDomPosition getDom() {
		return dom;
	}

	public void setDom(AssetDomPosition dom) {
		this.dom = dom;
	}

	public Map<String, String> getAttributes() {
		// if (attributes == null)
		// return Collections.emptyMap();
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String[] getAttributesOnlyName() {
		if (attributesOnlyName == null)
			return new String[0];
		return attributesOnlyName;
	}

	public void setAttributesOnlyName(String[] attributesOnlyName) {
		this.attributesOnlyName = attributesOnlyName;
	}

	/**
	 * Validate this asset
	 * 
	 * @return <code>true</code> if the asset is valid
	 */
	public boolean isValid() {
		return name != null && version != null && type != null && finalLocation != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Asset other = (Asset) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public String getAssetKey() {
		return name + "." + type;
	}

	public Asset clone() {
		return new Asset(name, version, type, dom, finalLocation);
	}

	public void addAttribute(String attributeName, String attributeValue) {
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}

		attributes.put(attributeName, attributeValue);
	}

	public void addAttribute(String attributeName) {
		if (attributesOnlyName == null) {
			attributesOnlyName = new String[] { attributeName };
		}
		else {
			Arrays.copyOf(attributesOnlyName, attributesOnlyName.length + 1);
			attributesOnlyName[attributesOnlyName.length] = attributeName;
		}
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	@Override
	public String toString() {
		return "Asset [name=" + name + ", version=" + version + ", type=" + type + ", dom=" + dom + ", configLocation="
				+ configLocation + ", configLocationKey=" + configLocationKey + ", finalLocation=" + finalLocation
				+ ", attributes=" + attributes + ", attributesOnlyName=" + Arrays.toString(attributesOnlyName) + "]";
	}

	public String toLog() {
		return name + " (" + type + ", v" + version + ")";
	}
}
