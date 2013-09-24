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

import java.util.Locale;
import java.util.Properties;

import com.github.dandelion.core.DandelionException;

/**
 * <p>
 * Interface for all configuration loaders.
 * 
 * <p>
 * The default implementation is the {@link StandardConfigurationLoader} class
 * but it can be replaced by another class that implements this interface thanks
 * to the {@link DatatablesConfigurator}.
 * 
 * @author Thibault Duchateau
 * @since 0.3.0
 */
public interface ConfigurationLoader {

	public final static String DT_DEFAULT_PROPERTIES = "dandelion/dandelion.properties";
	public final static String DT_USER_PROPERTIES = "dandelion";

	/**
	 * <p>
	 * Load the default configuration from the internal properties file and
	 * both:
	 * <ul>
	 * <li>stores the properties inside a class field</li>
	 * <li>returns the properties if they need to be used outside of the class</li>
	 * </ul>
	 * 
	 * @return the default properties
	 * @throws ConfigurationLoadingException
	 *             if the default properties cannot be loader.
	 */
	public Properties loadDefaultConfiguration() throws DandelionException;

	/**
	 * <p>
	 * Load the user configuration which can be localized thanks to the given
	 * locale.
	 * <p>
	 * Once the bundle loaded, it is converted into Properties.
	 * 
	 * @param locale
	 *            The current locale used to load the right properties file.
	 * @return the ResourceBundle containing the user configuration.
	 */
	public Properties loadUserConfiguration(Locale locale);
}