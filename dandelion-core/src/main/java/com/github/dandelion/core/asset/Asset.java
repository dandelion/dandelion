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

import java.util.Map;

/**
 * Definition of an Asset
 * <ul>
 *     <li>Name of asset (aka Key)</li>
 *     <li>Version of asset</li>
 *     <li>Type of asset</li>
 *     <li>Possibles locations of assets like :
 *         <ul>
 *             <li>Remote access (aka CDN, Static Content Server, Any url)</li>
 *             <li>Local access (Classpath)</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class Asset implements Cloneable {
	String name;
	String version;
	AssetType type;
	Map<String, String> locations;
    int storagePosition = -1;

    /**
     * Declare an empty asset
     */
    public Asset() {
    }

    /**
     * Enforce the declaration of a full asset
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
    public Map<String, String> getLocations() {
        return locations;
    }
    public void setLocations(Map<String, String> locations) {
        this.locations = locations;
    }

    /**
     * Validate this asset
     * @return <code>true</code> if the asset is valid
     */
    public boolean isValid() {
        if (name == null) return false;
        if (version == null) return false;
        if (type == null) return false;
        if (locations == null) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Asset asset = (Asset) o;
        if (!name.equals(asset.name)) return false;
        if (!type.equals(asset.type)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode() + type.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public String toString() {
		return "Asset [name=" + name + ", version=" + version + ", type=" + type + ", locations=[" + locations + "]";
	}

    String equalsKey() {
        return name+"_"+type;
    }

    @Override
    protected Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch(CloneNotSupportedException e) {
            e.printStackTrace(System.err);
        }
        return o;
    }
}
