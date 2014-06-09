package com.github.dandelion.core.bundle.loader;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AbstractBundleLoaderTest {

	@Test
	public void should_not_load_resource_in_submodule_folder() {
		ModuleWithSubmoduleAssetJsonLoader loader = new ModuleWithSubmoduleAssetJsonLoader();
		assertThat(loader.loadBundles()).hasSize(1);
	}
}
