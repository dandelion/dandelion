package com.github.dandelion.fakedomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.github.dandelion.core.bundle.loader.AbstractBundleLoader;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.util.Lists.newArrayList;
import static org.assertj.core.util.Sets.newLinkedHashSet;

public class AssetFakeLoader extends AbstractBundleLoader {
	
	@Override
	public List<BundleStorageUnit> loadBundles() {
		Map<String, String> locations = new HashMap<String, String>();
		locations.put("remote", "remoteURL");
		locations.put("local", "localPath");
		Map<String, String> locations2 = new HashMap<String, String>();
		locations.put("remote", "remoteURL2");
		locations.put("local", "localPath2");

		AssetStorageUnit fakeAsset1 = new AssetStorageUnit();
		fakeAsset1.setName("name1");
		fakeAsset1.setVersion("version");
		fakeAsset1.setLocations(locations);
		
		AssetStorageUnit fakeAsset2 = new AssetStorageUnit();
		fakeAsset2.setName("name2");
		fakeAsset2.setVersion("version");
		fakeAsset2.setLocations(locations2);
		
		return newArrayList(
				new BundleStorageUnit("default", new HashSet<AssetStorageUnit>()),
				new BundleStorageUnit("fake", newLinkedHashSet(fakeAsset1, fakeAsset2)));
	}

	@Override
	public String getName() {
		return "fake";
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
