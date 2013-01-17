package com.github.dandelion.core.api.asset;

import java.util.List;

/**
 * Public Api to define a Loader for Asset
 */
public interface AssetLoader {
    List<AssetsComponent> loadAssets();
}
