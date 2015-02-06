package com.github.dandelion.core.storage.support;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.storage.BundleStorage;
import com.github.dandelion.core.storage.BundleStorageUnit;

public class CycleDetectorTest {

   @Rule
   public ExpectedException expectedEx = ExpectedException.none();

   private BundleStorage bundleStorage;
   private List<BundleStorageUnit> bundlesToAdd;

   @Before
   public void setup() {
      bundlesToAdd = new LinkedList<BundleStorageUnit>();

      BundleStorageUnit b1 = new BundleStorageUnit("b1", null);
      b1.addDependency("b2");

      BundleStorageUnit b2 = new BundleStorageUnit("b2", null);
      b1.addDependency("b1");

      BundleStorageUnit b3 = new BundleStorageUnit("b3", null);
      b1.addDependency("b2");

      bundlesToAdd.add(b1);
      bundlesToAdd.add(b2);
      bundlesToAdd.add(b3);

      bundleStorage = new BundleStorage();
   }

   @Test
   public void should_throw_an_exception_because_of_the_cycle() {

      expectedEx.expect(DandelionException.class);
      expectedEx.expectMessage("A cycle has been detected in the asset graph for the bundle b1.");
      bundleStorage.storeBundles(bundlesToAdd);
   }
}
