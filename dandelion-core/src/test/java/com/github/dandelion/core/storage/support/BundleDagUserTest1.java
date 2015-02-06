package com.github.dandelion.core.storage.support;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.storage.AssetStorageUnit;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.storage.BundleStorageUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BundleDagUserTest1 {

   private BundleStorage bundleStorage;

   @Before
   public void setup() {
      List<BundleStorageUnit> bundlesToAdd = new LinkedList<BundleStorageUnit>();

      Set<AssetStorageUnit> asu1 = new LinkedHashSet<AssetStorageUnit>();
      asu1.add(new AssetStorageUnit("asset1_1", "1.0.0", AssetType.js));
      asu1.add(new AssetStorageUnit("asset1_2", "1.0.0", AssetType.js));
      BundleStorageUnit b1 = new BundleStorageUnit("b1", asu1);

      Set<AssetStorageUnit> assets2 = new LinkedHashSet<AssetStorageUnit>();
      assets2.add(new AssetStorageUnit("asset2_1", "1.0.0", AssetType.js));
      BundleStorageUnit b2 = new BundleStorageUnit("b2", assets2);
      b2.addDependency("b1");

      Set<AssetStorageUnit> assets3 = new LinkedHashSet<AssetStorageUnit>();
      assets3.add(new AssetStorageUnit("asset3_1", "1.0.0", AssetType.js));
      assets3.add(new AssetStorageUnit("asset3_2", "1.0.0", AssetType.css));
      assets3.add(new AssetStorageUnit("asset3_3", "1.0.0", AssetType.css));
      BundleStorageUnit b3 = new BundleStorageUnit("b3", assets3);
      b3.addDependency("b1");

      Set<AssetStorageUnit> assets4 = new LinkedHashSet<AssetStorageUnit>();
      assets4.add(new AssetStorageUnit("asset4_1", "1.0.0", AssetType.css));
      BundleStorageUnit b4 = new BundleStorageUnit("b4", assets4);
      b4.addDependency("b2");
      b4.addDependency("b3");

      bundlesToAdd.add(b3);
      bundlesToAdd.add(b2);
      bundlesToAdd.add(b1);
      bundlesToAdd.add(b4);

      bundleStorage = new BundleStorage();
      bundleStorage.storeBundles(bundlesToAdd);
   }

   @Test
   public void should_override_all_assets_of_bundle_b1_and_leave_untouched_all_others_bundles() {

      Set<AssetStorageUnit> assetsBu1 = new LinkedHashSet<AssetStorageUnit>();
      assetsBu1.add(new AssetStorageUnit("asset1_1", "2.0.0", AssetType.js));
      assetsBu1.add(new AssetStorageUnit("asset1_2", "2.0.0", AssetType.js));
      BundleStorageUnit bu1 = new BundleStorageUnit("b1", assetsBu1);

      bundleStorage.storeBundles(Arrays.asList(bu1));

      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b1");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b1");

      Set<AssetStorageUnit> assetsB1 = bundleStorage.getBundleDag().getVertex("b1").getAssetStorageUnits();
      assertThat(assetsB1).extracting("name").containsExactly("asset1_1", "asset1_2");
      assertThat(assetsB1).extracting("version").containsExactly("2.0.0", "2.0.0");

      Set<AssetStorageUnit> assetsB2 = bundleStorage.getBundleDag().getVertex("b2").getAssetStorageUnits();
      assertThat(assetsB2).extracting("name").containsExactly("asset2_1");
      assertThat(assetsB2).extracting("version").containsExactly("1.0.0");

      Set<AssetStorageUnit> assetsB3 = bundleStorage.getBundleDag().getVertex("b3").getAssetStorageUnits();
      assertThat(assetsB3).extracting("name").containsExactly("asset3_1", "asset3_2", "asset3_3");
      assertThat(assetsB3).extracting("version").containsExactly("1.0.0", "1.0.0", "1.0.0");

      Set<AssetStorageUnit> assetsB4 = bundleStorage.getBundleDag().getVertex("b4").getAssetStorageUnits();
      assertThat(assetsB4).extracting("name").containsExactly("asset4_1");
      assertThat(assetsB4).extracting("version").containsExactly("1.0.0");
   }
}
