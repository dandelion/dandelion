package com.github.dandelion.core.asset;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    AssetsStorageCase.class,
    AssetsConfiguratorCase.class,
    AssetsDefaultLoaderCase.class
})
public class AssetsSuiteTest {
}
