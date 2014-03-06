package com.github.dandelion.core.bundle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BundleStorageTest3 {

	private BundleStorage bundleStorage;
	private List<Bundle> bundlesToAdd;

	@Before
	public void setup() {
		bundlesToAdd = new LinkedList<Bundle>();

		Bundle b1 = new Bundle("b1");
		b1.addDependency("b3");
		b1.addDependency("b4");

		Bundle b2 = new Bundle("b2");
		b2.addDependency("b4");
		b2.addDependency("b5");

		Bundle b3 = new Bundle("b3");
		b3.addDependency("b7");
		b3.addDependency("b8");

		Bundle b4 = new Bundle("b4");
		b4.addDependency("b8");
		b4.addDependency("b9");

		Bundle b5 = new Bundle("b5");
		b5.addDependency("b9");

		Bundle b6 = new Bundle("b6");
		b6.addDependency("b9");

		Bundle b7 = new Bundle("b7");
		Bundle b8 = new Bundle("b8");
		Bundle b9 = new Bundle("b9");
		Bundle b10 = new Bundle("b10");
		b10.addDependency("b12");

		Bundle b11 = new Bundle("b11");
		Bundle b12 = new Bundle("b12");

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
		bundleStorage.loadBundles(bundlesToAdd);
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
		List<String> bundlesToLoad = bundleStorage.getBundleDag().bundlesFor("b1");
		assertThat(bundlesToLoad).containsExactly("b7", "b8", "b3", "b9", "b4", "b1");
	}

	@Test
	public void should_return_all_necessary_bundles_when_requesting_b2() {
		List<String> bundlesToLoad = bundleStorage.getBundleDag().bundlesFor("b2");
		assertThat(bundlesToLoad).containsExactly("b8", "b9", "b4", "b5", "b2");
	}

	@Test
	public void should_return_all_necessary_bundles_when_requesting_b10() {
		List<String> bundlesToLoad = bundleStorage.getBundleDag().bundlesFor("b10");
		assertThat(bundlesToLoad).containsExactly("b12", "b10");
	}
}
