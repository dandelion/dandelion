/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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

import com.github.dandelion.core.utils.DandelionScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Dandelion Configuration (dandelion/dandelion*.properties)
 */
public class Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    static Properties configuration;

    public static Properties getProperties() {
        if(configuration == null) {
            loadConfiguration();
        }
        return configuration;
    }

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    synchronized private static void loadConfiguration() {
        if(configuration == null) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                String mainResource = DandelionScanner.getResource("dandelion", "dandelion.properties");
                Set<String> otherResources = DandelionScanner.getResources("dandelion", "dandelion", "properties");
                otherResources.remove(mainResource);

                // configure with all custom properties
                configuration = new Properties();
                for(String resource:otherResources) {
                    configuration.load(classLoader.getResourceAsStream(resource));
                }

                // override with main properties
                Properties mainProperties = new Properties();
                if(mainResource != null)
                    mainProperties.load(classLoader.getResourceAsStream(mainResource));
                    configuration.putAll(mainProperties);
            } catch (IOException e) {
                LOG.error("Assets configurator can't access/read to some file under 'dandelion/dandelion*.properties'");
            }
        }
    }

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
