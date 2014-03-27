/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors 
 * may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.utils.PropertiesUtils;
import com.github.dandelion.core.utils.StringUtils;
import com.github.dandelion.core.utils.UTF8Control;

/**
 * <p>
 * Default implementation of the {@link ConfigurationLoader}.
 * 
 * <p>
 * Note that a custom {@link ConfigurationLoader} can be used thanks to the
 * {@link Context}.
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 * @see ConfigurationLoader
 * @see Context
 * @see ConfigurationError
 */
public class StandardConfigurationLoader implements ConfigurationLoader {

	// Logger
	private static Logger LOG = LoggerFactory.getLogger(StandardConfigurationLoader.class);

	public static final String DANDELION_USER_PROPERTIES = "dandelion";
	public static final String DANDELION_CONFIGURATION = "dandelion.configuration";

	/**
	 * {@inheritDoc}
	 */
	public Properties loadUserConfiguration() {

		LOG.debug("Loading user configuration...");

		ResourceBundle userBundle = null;
		ResourceBundle.clearCache();

		// First check if the resource bundle is externalized
		if (StringUtils.isNotBlank(System.getProperty(DANDELION_CONFIGURATION))) {

			String path = System.getProperty(DANDELION_CONFIGURATION);

			try {
				URL resourceURL = new File(path).toURI().toURL();
				URLClassLoader urlLoader = new URLClassLoader(new URL[] { resourceURL });
				userBundle = ResourceBundle.getBundle(DANDELION_USER_PROPERTIES, Locale.getDefault(), urlLoader,
						new UTF8Control());
				LOG.debug("User configuration loaded");
			}
			catch (MalformedURLException e) {
				LOG.warn("Wrong path to the externalized bundle", e);
			}
			catch (MissingResourceException e) {
				LOG.info("No *.properties file in {}. Trying to lookup in classpath...", path);
			}
		}

		// No system property is set, retrieves the bundle from the classpath
		if (userBundle == null) {
			try {
				// The user bundle is read using UTF-8
				userBundle = ResourceBundle
						.getBundle(DANDELION_USER_PROPERTIES, Locale.getDefault(), new UTF8Control());
				LOG.debug("User configuration loaded");
			}
			catch (MissingResourceException e) {
				// if no resource bundle is found, try using the context
				// classloader
				try {
					userBundle = ResourceBundle.getBundle("dandelion/" + DANDELION_USER_PROPERTIES,
							Locale.getDefault(), Thread.currentThread().getContextClassLoader(), new UTF8Control());
					LOG.debug("User configuration loaded");
				}
				catch (MissingResourceException mre) {
					LOG.debug("No custom configuration. Using default one.");
				}
			}
		}

		return PropertiesUtils.bundleToProperties(userBundle);
	}
}
