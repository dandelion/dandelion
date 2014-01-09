/*
 * [The "BSD licence"]
 * Copyright (c) 2012 Dandelion
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.utils.PropertiesUtils;
import com.github.dandelion.core.utils.ResourceScanner;
import com.github.dandelion.core.utils.StringUtils;
import com.github.dandelion.core.utils.UTF8Control;

/**
 * <p>
 * Default implementation of the {@link ConfigurationLoader}.
 * 
 * <p>
 * Note that a custom {@link ConfigurationLoader} can be used thanks to the
 * {@link DandelionConfigurator}.
 * 
 * @author Thibault Duchateau
 * @since 0.3.0
 * @see ConfigurationLoader
 * @see DandelionConfigurator
 * @see ConfigurationError
 */
public class StandardConfigurationLoader implements ConfigurationLoader {

    public static final String DANDELION_DEFAULT = "dandelion-default";
    public static final String DANDELION_PROPERTIES = "properties";
    public static final String DANDELION_OTHER = "dandelion";
    public static final String DANDELION_FOLDER = "dandelion";
    // Logger
	private static Logger LOG = LoggerFactory.getLogger(StandardConfigurationLoader.class);

    public final static String DANDELION_USER_PROPERTIES = "dandelion";
    public final static String DANDELION_CONFIGURATION = "dandelion.configuration";

	/**
	 * {@inheritDoc}
	 */
	public Properties loadDefaultConfiguration() throws DandelionException {

		LOG.debug("Loading default configurations...");

		// Initialize properties
		Properties defaultProperties = new Properties();

		// Get default file as stream
		InputStream propertiesStream = null;

		try {
			Reader reader;
            Properties properties;

            String defaultResource = ResourceScanner.getResource(DANDELION_FOLDER, DANDELION_DEFAULT + "." + DANDELION_PROPERTIES);
            if(defaultResource != null) {
                propertiesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultResource);
                reader = new InputStreamReader(propertiesStream, "UTF-8");
                properties = new Properties();
                properties.load(reader);
                defaultProperties.putAll(properties);
            }

			Set<String> resources = ResourceScanner.getResources(DANDELION_FOLDER, null, DANDELION_OTHER, DANDELION_PROPERTIES, false);
			for(String resource : resources) {
                if((DANDELION_FOLDER + File.separator
                        + DANDELION_DEFAULT + "." + DANDELION_PROPERTIES).equals(resource)) {
                    continue;
                }

				propertiesStream = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(resource);
				reader = new InputStreamReader(propertiesStream, "UTF-8");
				properties = new Properties();
                properties.load(reader);
                defaultProperties.putAll(properties);
			}
		} catch (IOException e) {
			throw DandelionException.wrap(e, ConfigurationError.DEFAULT_CONFIGURATION_LOADING)
					.set("default name", DANDELION_DEFAULT + "." + DANDELION_PROPERTIES);
		} finally {
			if (propertiesStream != null) {
				try {
					propertiesStream.close();
				} catch (IOException e) {
					LOG.error("Properties Stream can't be close", e);
				}
			}
		}

		LOG.debug("Default configuration loaded");

		return defaultProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	public Properties loadUserConfiguration() {

		LOG.debug("Loading user configuration...");
		
		ResourceBundle userBundle = null;

		// First check if the resource bundle is externalized
		if (StringUtils.isNotBlank(System.getProperty(DANDELION_CONFIGURATION))) {

			String path = System.getProperty(DANDELION_CONFIGURATION);

			try {
				URL resourceURL = new File(path).toURI().toURL();
				URLClassLoader urlLoader = new URLClassLoader(new URL[] { resourceURL });
				userBundle = ResourceBundle.getBundle(DANDELION_USER_PROPERTIES, Locale.getDefault(), urlLoader, new UTF8Control());
				LOG.debug("User configuration loaded");
			} catch (MalformedURLException e) {
				LOG.warn("Wrong path to the externalized bundle", e);
			} catch (MissingResourceException e) {
				LOG.info("No *.properties file in {}. Trying to lookup in classpath...", path);
			}

		}

		// No system property is set, retrieves the bundle from the classpath
		if (userBundle == null) {
			try {
				// The user bundle is read using UTF-8
				userBundle = ResourceBundle.getBundle(DANDELION_USER_PROPERTIES, Locale.getDefault(), new UTF8Control());
				LOG.debug("User configuration loaded");
			} catch (MissingResourceException e) {
				// if no resource bundle is found, try using the context
				// classloader
				try {
					userBundle = ResourceBundle.getBundle(DANDELION_USER_PROPERTIES, Locale.getDefault(), Thread.currentThread()
							.getContextClassLoader(), new UTF8Control());
					LOG.debug("User configuration loaded");
				} catch (MissingResourceException mre) {
					LOG.debug("No custom configuration. Using default one.");
				}
			}
		}

		return PropertiesUtils.bundleToProperties(userBundle);
	}
}