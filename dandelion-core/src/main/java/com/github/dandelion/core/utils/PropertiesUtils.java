package com.github.dandelion.core.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * <p>
 * Utility class used when dealing with Properties.
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public class PropertiesUtils {

	/**
	 * Filter properties which begin with the supplied prefix.
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
}