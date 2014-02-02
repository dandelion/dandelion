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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.utils.ClassUtils;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * The {@link DandelionConfigurator} is used to pick up different classes in
 * charge of the configuration loading, instantiate them and cache them.
 * 
 * <ul>
 * <li>The configuration loader, in charge of loading default and user
 * properties</li>
 * </ul>
 * <p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 * @see ConfigurationLoader
 * @see StandardConfigurationLoader
 */
public class DandelionConfigurator {

	// Logger
	private static Logger LOG = LoggerFactory.getLogger(DandelionConfigurator.class);

	private static ConfigurationLoader configurationLoader;

	/**
	 * <p>
	 * Returns an implementation of {@link ConfigurationLoader} using the
	 * following strategy:
	 * <ol>
	 * <li>Check first if the <code>dandelion.confloader.class</code> system
	 * property is set and tries to instantiate it</li>
	 * <li>Otherwise, instantiate the {@link StandardConfigurationLoader} based
	 * on property files</li>
	 * </ol>
	 * 
	 * @return an implementation of {@link ConfigurationLoader}.
	 */
	public static ConfigurationLoader getConfigurationLoader() {

		if (configurationLoader == null) {

			LOG.debug("Initializing the configuration loader...");

			if (StringUtils.isNotBlank(System.getProperty(ConfigurationLoader.DANDELION_CONFLOADER_CLASS))) {
				Class<?> clazz;
				try {
					clazz = ClassUtils.getClass(System.getProperty(ConfigurationLoader.DANDELION_CONFLOADER_CLASS));
					configurationLoader = (ConfigurationLoader) ClassUtils.getNewInstance(clazz);
				}
				catch (Exception e) {
					LOG.warn(
							"Unable to instantiate the configured {} due to a {} exception. Falling back to the default one.",
							ConfigurationLoader.DANDELION_CONFLOADER_CLASS, e.getClass().getName(), e);
				}
			}

			if (configurationLoader == null) {
				configurationLoader = new StandardConfigurationLoader();
			}
		}

		return configurationLoader;
	}

	static void clear() {
		configurationLoader = null;
	}
}