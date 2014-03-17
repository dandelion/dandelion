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
package com.github.dandelion.core.utils;

import java.util.*;

/**
 * <p>
 * Collection of utilities to ease working with {@link Properties}.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public class PropertiesUtils {

	/**
	 * <p>Filters properties which begin with the supplied prefix.
	 * 
	 * @param prefix
	 *            The prefix used to filter the properties.
	 * @param properties
	 *            The properties source.
	 * @return a filtered list of properties.
	 */
	public static List<String> propertyBeginWith(String prefix, Properties properties) {
		List<String> values = new ArrayList<String>();
		for (String key : properties.stringPropertyNames()) {
			if (key.startsWith(prefix)) {
				values.add(properties.getProperty(key));
			}
		}
		return values;
	}

	/**
	 * Converts the supplied bundle to a Properties.
	 * 
	 * @param bundle
	 *            The ResourceBundle to convert.
	 * @return a Properties instance.
	 */
	public static Properties bundleToProperties(ResourceBundle bundle) {
		Properties properties = new Properties();

		if (bundle != null) {
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				properties.put(key, bundle.getString(key));
			}
		}
		return properties;
	}

	public static List<String> propertyAsList(String values, String delimiter) {
		if (values == null || values.isEmpty())
			return null;
		return Arrays.asList(values.split(delimiter));
	}
}
