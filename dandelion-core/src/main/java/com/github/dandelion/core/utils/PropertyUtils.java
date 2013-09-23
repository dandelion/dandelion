package com.github.dandelion.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertyUtils {

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
}
