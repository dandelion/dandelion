package com.github.dandelion.core.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BundleStorageTest3 {

   private BundleStorage bundleStorage;
   private List<BundleStorageUnit> bundlesToAdd;

   @Before
   public void setup() {
      bundlesToAdd = new LinkedList<BundleStorageUnit>();

      BundleStorageUnit b1 = new BundleStorageUnit("b1");
      b1.addDependency("b3");
      b1.addDependency("b4");

      BundleStorageUnit b2 = new BundleStorageUnit("b2");
      b2.addDependency("b4");
      b2.addDependency("b5");

      BundleStorageUnit b3 = new BundleStorageUnit("b3");
      b3.addDependency("b7");
      b3.addDependency("b8");

      BundleStorageUnit b4 = new BundleStorageUnit("b4");
      b4.addDependency("b8");
      b4.addDependency("b9");

      BundleStorageUnit b5 = new BundleStorageUnit("b5");
      b5.addDependency("b9");

      BundleStorageUnit b6 = new BundleStorageUnit("b6");
      b6.addDependency("b9");

      BundleStorageUnit b7 = new BundleStorageUnit("b7");
      BundleStorageUnit b8 = new BundleStorageUnit("b8");
      BundleStorageUnit b9 = new BundleStorageUnit("b9");
      BundleStorageUnit b10 = new BundleStorageUnit("b10");
      b10.addDependency("b12");

      BundleStorageUnit b11 = new BundleStorageUnit("b11");
      BundleStorageUnit b12 = new BundleStorageUnit("b12");

      bundlesToAdd.add(b1);
      bundlesToAdd.add(b2);
      bundlesToAdd.add(b3);
      bundlesToAdd.add(b4);
      bundlesToAdd.add(b5);
      bundlesToAdd.add(b6);
      bundlesToAdd.add(b7);
      bundlesToAdd.add(b8);
      bundlesToAdd.add(b9);
      bundlesToAdd.add(b10);
      bundlesToAdd.add(b11);
      bundlesToAdd.add(b12);

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
      assertThat(bundlesToLoad).extracting("name").containsExactly("b7", "b8", "b3", "b9", "b4", "b1");
   }

   @Test
   public void should_return_all_necessary_bundles_when_requesting_b2() {
      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b2");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b8", "b9", "b4", "b5", "b2");
   }

   @Test
   public void should_return_all_necessary_bundles_when_requesting_b10() {
      Set<BundleStorageUnit> bundlesToLoad = bundleStorage.bundlesFor("b10");
      assertThat(bundlesToLoad).extracting("name").containsExactly("b12", "b10");
   }
}
