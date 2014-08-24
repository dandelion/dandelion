package com.github.dandelion.fakedomain;

import static org.assertj.core.util.Lists.newArrayList;
import static org.assertj.core.util.Sets.newLinkedHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.bundle.loader.spi.AbstractBundleLoader;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

public class AssetFakeLoader extends AbstractBundleLoader {
	
	@Override
	public List<BundleStorageUnit> loadBundles() {
		Map<String, String> locations = new HashMap<String, String>();
		locations.put("remote", "remoteURL");
		locations.put("local", "localPath");
		Map<String, String> locations2 = new HashMap<String, String>();
		locations.put("remote", "remoteURL2");
		locations.put("local", "localPath2");

		return newArrayList(
				new BundleStorageUnit("default", new HashSet<AssetStorageUnit>()),
				new BundleStorageUnit("fake", newLinkedHashSet(new AssetStorageUnit("name", "version", AssetType.js, locations), new AssetStorageUnit("name2", "version2", AssetType.js, locations2))));
	}

	@Override
	public String getName() {
		return "fake";
	}

	@Override
	public boolean isRecursive() {
		return true;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}
}
