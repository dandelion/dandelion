package com.github.dandelion.fakedomain;

import com.github.dandelion.core.api.asset.Asset;
import com.github.dandelion.core.api.asset.AssetType;
import com.github.dandelion.core.api.asset.AssetsComponent;
import com.github.dandelion.core.api.asset.AssetsLoader;

import java.util.ArrayList;
import java.util.List;

import static org.fest.util.Collections.list;

public class AssetsFakeLoader implements AssetsLoader {
    @Override
    public List<AssetsComponent> loadAssets() {
        return list(
            new AssetsComponent("default", "default", new ArrayList<Asset>()),
            new AssetsComponent("fake", "default", list(
                new Asset("name", "version", AssetType.js, "remoteURL", "localPath"),
                new Asset("name2", "version2", AssetType.js, "remoteURL2", "localPath2")
            ))
        );
    }
}
