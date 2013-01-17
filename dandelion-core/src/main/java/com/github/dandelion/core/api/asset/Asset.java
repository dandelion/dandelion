package com.github.dandelion.core.api.asset;

/**
 * Definition of an Asset
 * <ul>
 *     <li>Name of asset (aka Key)</li>
 *     <li>Version of asset</li>
 *     <li>Type of asset</li>
 *     <li>Content access
 *         <ul>
 *             <li>Remote access (aka CDN, Static Content Server, Any url)</li>
 *             <li>Local access (Classpath)</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class Asset {
	String name;
	String version;
	AssetType type;
	String remote;
	String local;

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
     * @param remote remote access
     * @param local local access
     */
    public Asset(String name, String version, AssetType type, String remote, String local) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.remote = remote;
        this.local = local;
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
	public String getRemote() {
		return remote;
	}
	public void setRemote(String remote) {
		this.remote = remote;
	}
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}

    /**
     * Validate this asset
     * @return <code>true</code> if the asset is valid
     */
    public boolean isValid() {
        if (name == null) return false;
        if (version == null) return false;
        if (type == null) return false;
        if (remote == null && local == null) return false;
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

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public String toString() {
		return "Asset [name=" + name + ", version=" + version + ", type=" + type + ", remote=" + remote
				+ ", local=" + local + "]";
	}
	
}
