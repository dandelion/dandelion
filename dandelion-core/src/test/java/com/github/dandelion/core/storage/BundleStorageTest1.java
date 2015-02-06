package com.github.dandelion.core.storage;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.dandelion.core.asset.AssetType;

import static org.assertj.core.api.Assertions.assertThat;

public class BundleStorageTest1 {

   private BundleStorage bundleStorage;

   @Before
   public void setup() {
      List<BundleStorageUnit> bundlesToAdd = new LinkedList<BundleStorageUnit>();

      Set<AssetStorageUnit> assets1 = new LinkedHashSet<AssetStorageUnit>();
      assets1.add(new AssetStorageUnit("asset1_1", "1.0.0", AssetType.js));
      assets1.add(new AssetStorageUnit("asset1_2", "1.0.0", AssetType.js));
      BundleStorageUnit b1 = new BundleStorageUnit("b1", assets1);

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
   public void should_have_set_the_right_edges() {
      assertThat(bundleStorage.getBundleDag().hasEdge("b2", "b1"));
      assertThat(bundleStorage.getBundleDag().hasEdge("b3", "b1"));
      assertThat(bundleStorage.getBundleDag().hasEdge("b4", "b2"));
      assertThat(bundleStorage.getBundleDag().hasEdge("b4", "b3"));
   }

   @Test
   public void should_return_all_necessary_bundles_when_requesting_b1() {
      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b1");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b1");
   }

   @Test
   public void should_return_all_necessary_bundles_when_requesting_b2() {
      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b2");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b1", "b2");
   }

   @Test
   public void should_return_all_necessary_bundles_when_requesting_b3() {
      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b3");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b1", "b3");
   }

   @Test
   public void should_return_all_necessary_bundles_when_requesting_b4() {
      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b4");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b1", "b2", "b3", "b4");
   }
}