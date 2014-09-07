package com.github.dandelion.core.bundle.loader;

import java.io.File;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterConfig;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.config.StandardConfigurationLoader;

public class AbstractBundleLoaderTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
		String path = new File("src/test/resources/bundle-loading/json/wrong-format/dandelion/".replace("/",
				File.separator)).getAbsolutePath();
		System.setProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION, path);
	}

	@After
	public void teardown() {
		System.clearProperty(StandardConfigurationLoader.DANDELION_CONFIGURATION);
	}

	@Test
	public void should_throw_an_exception_when_trying_to_initialize_context() {

		exception.expect(DandelionException.class);
		exception.expectMessage("The bundle graph is not consistent for the following reasons:");
		exception
				.expectMessage(CoreMatchers
						.containsString("The file 'bundle-loading/json/wrong-format/dandelion/bundle-wrong-format1.json' is wrongly formatted"));
		exception
				.expectMessage(CoreMatchers
						.containsString("The file 'bundle-loading/json/wrong-format/dandelion/bundle-wrong-format2.json' is wrongly formatted"));

		new Context(new MockFilterConfig());
	}
}
