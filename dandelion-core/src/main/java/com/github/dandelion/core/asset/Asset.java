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
package com.github.dandelion.core.asset;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of an Asset
 * <ul>
 *     <li>Name of asset (aka Key)</li>
 *     <li>Version of asset</li>
 *     <li>Type of asset</li>
 *     <li>DOM positionning</li>
 *     <li>Possible locations of assets like:
 *         <ul>
 *             <li>Remote access (aka CDN, Static Content Server, Any url)</li>
 *             <li>Local access (Classpath)</li>
 *         </ul>
 *     </li>
 *     <li>Html Attributes (key/value)</li>
 *     <li>Html Attributes (only name)</li>
 * </ul>
 */
public class Asset {
	String name;
	String version;
	AssetType type;
    AssetDOMPosition dom;
    Map<String, String> locations;
    Map<String, String> attributes;
    String[] attributesOnlyName;
    int storagePosition = -1;

    /**
     * Declare an empty asset
     */
    public Asset() {
    }

    /**
     * Enforce the declaration of a full asset (mandatory fields)
     * @param name name
     * @param version version
     * @param type type
     * @param locations locations of source
     */
    public Asset(String name, String version, AssetType type, Map<String, String> locations) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.locations = locations;
    }

    protected Asset(String name, String version, AssetType type, AssetDOMPosition dom, Map<String, String> locations, int storagePosition) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.dom = dom;
        this.locations = locations;
        this.storagePosition = storagePosition;
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
    public AssetDOMPosition getDom() {
        return dom;
    }
    public void setDom(AssetDOMPosition dom) {
        this.dom = dom;
    }
    public Map<String, String> getLocations() {
        return locations;
    }
    public void setLocations(Map<String, String> locations) {
        this.locations = locations;
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    public String[] getAttributesOnlyName() {
        return attributesOnlyName;
    }
    public void setAttributesOnlyName(String[] attributesOnlyName) {
        this.attributesOnlyName = attributesOnlyName;
    }

    /**
     * Validate this asset
     * @return <code>true</code> if the asset is valid
     */
    public boolean isValid() {
        return name != null && version != null && type != null && locations != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Asset asset = (Asset) o;
        return !(name != null ? !name.equals(asset.name) : asset.name != null) && type == asset.type;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public String toString() {
		return "Asset [name=" + name + ", version=" + version + ", type=" + type
                + (dom!=null?", dom=" + dom:"")
                + ", locations=[" + locations + "]";
	}

    public String getAssetKey() {
        return name + "." + type;
    }

    public Asset clone(boolean withoutLocations) {
        return new Asset(name, version, type, dom, withoutLocations?new HashMap<String, String>():locations, storagePosition);
    }

    public void addAttribute(String attributeName, String attributeValue) {
        if(attributes == null) {
            attributes = new HashMap<String, String>();
        }

        attributes.put("data-ddl-asset-" + attributeName, attributeValue);
    }
}
