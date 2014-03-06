package com.github.dandelion.core.bundle;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.DandelionExceptionMatcher;

public class CycleDetectorTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	private BundleStorage bundleStorage;
	private List<Bundle> bundlesToAdd;

	@Before
	public void setup() {
		bundlesToAdd = new LinkedList<Bundle>();

		Bundle b1 = new Bundle("b1", null);
		b1.addDependency("b2");

		Bundle b2 = new Bundle("b2", null);
		b1.addDependency("b1");

		Bundle b3 = new Bundle("b3", null);
		b1.addDependency("b2");

		bundlesToAdd.add(b1);
		bundlesToAdd.add(b2);
		bundlesToAdd.add(b3);

		bundleStorage = new BundleStorage();
	}

	@Test
	public void should_throw_an_exception_because_of_the_cycle() {

		expectedEx.expect(DandelionException.class);
		expectedEx.expect(new DandelionExceptionMatcher(BundleStorageError.CYCLE_DETECTED).set("cycle", "b1"));
		bundleStorage.loadBundles(bundlesToAdd);
	}
}
