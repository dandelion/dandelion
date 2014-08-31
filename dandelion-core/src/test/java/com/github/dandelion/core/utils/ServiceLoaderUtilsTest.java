package com.github.dandelion.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import com.github.dandelion.core.bundle.loader.spi.BundleLoader;

public class ServiceLoaderUtilsTest {

	@Test
	public void should_return_all_bundleLoader_as_list(){
		assertThat(ServiceLoaderUtils.getProvidersAsList(BundleLoader.class)).isInstanceOf(ArrayList.class);
		assertThat(ServiceLoaderUtils.getProvidersAsList(BundleLoader.class)).hasSize(3);
	}
}
