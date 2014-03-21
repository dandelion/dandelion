package com.github.dandelion.core.bundle.loader;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.github.dandelion.core.Context;

public class AbstractBundleLoaderTest {

	private Context context;
	
	@Before
	public void setup(){
		context = new Context();
	}
	
	@Test
	public void should_not_load_resource_in_submodule_folder() {
		ModuleWithSubmoduleAssetJsonLoader loader = new ModuleWithSubmoduleAssetJsonLoader();
		assertThat(loader.loadBundles(context)).hasSize(1);
	}
}
