package com.github.dandelion.core.utils;

import java.util.*;

public class PropertiesUtils {

	/**
     * Filter properties which begin with a given prefix.
     * @param prefix prefix string
     * @param properties properties to filter
     * @return a filtered list of values
     */
    public static List<String> propertyBeginWith(String prefix, Properties properties) {
        List<String> values = new ArrayList<String>();
        for(String key:properties.stringPropertyNames()) {
            if(key.startsWith(prefix)) {
                values.add(properties.getProperty(key));
            }
        }
        return values;
    }

    public static Properties bundleToProperties(ResourceBundle bundle){
        Properties properties = new Properties();

        if(bundle != null){
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                properties.put(key, bundle.getString(key));
            }
        }
        return properties;
    }
}
