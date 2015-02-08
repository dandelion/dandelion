/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
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
package com.github.dandelion.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * <p>
 * Collection of utilities to ease working with {@link Properties}.
 * </p>
 * 
 * @author Romain Lespinasse
 * @author Thibault Duchateau
 * @since 0.2.0
 */
public class PropertiesUtils {

   /**
    * <p>
    * Returns a filtered list of properties which begin with the supplied
    * prefix.
    * </p>
    * 
    * @param prefix
    *           The prefix used to filter the properties.
    * @param properties
    *           The properties source.
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
    * <p>
    * Returns a {@link Properties} instance, loaded from the specified
    * {@code filePath}, absolute path in the file system.
    * </p>
    * 
    * <p>
    * The {@link Properties} instance is loaded using the specified named
    * {@code charset} .
    * </p>
    * 
    * @param filePath
    *           The absolute file path to use in order to load the
    *           {@link Properties} instance.
    * @param charset
    *           The named charset to use when creating the
    *           {@link InputStreamReader}.
    * @return a loaded instance of {@link Properties}.
    * @throws FileNotFoundException
    *            if the file doesn't exist.
    * @throws UnsupportedEncodingException
    *            if the named charset is not supported
    * @throws IOException
    *            if any other error occurred when reading from the input stream.
    */
   public static Properties loadFromFileSystem(String filePath, String charset) throws FileNotFoundException,
         UnsupportedEncodingException, IOException {

      FileInputStream fis = null;
      InputStreamReader isr = null;
      Properties properties = null;
      try {
         fis = new FileInputStream(filePath);
         isr = new InputStreamReader(fis, charset);
         properties = new Properties();
         properties.load(isr);
      }
      finally {
         try {
            if (fis != null) {
               fis.close();
            }
         }
         catch (IOException e) {
            // Nothing to do
         }
         try {
            if (isr != null) {
               isr.close();
            }
         }
         catch (IOException e) {
            // Nothing to do
         }
      }

      return properties;
   }

   /**
    * <p>
    * Returns a {@link Properties} instance, loaded from the specified
    * {@code filePath} and using the context {@link ClassLoader} of the current
    * thread.
    * </p>
    * 
    * <p>
    * The {@link Properties} instance is loaded using the named {@code charset}
    * .
    * </p>
    * 
    * @param filePath
    *           The relative file path inside the classpath.
    * @param charset
    *           The named charset to use when creating the
    *           {@link InputStreamReader}.
    * @return a loaded instance of {@link Properties}.
    * @throws FileNotFoundException
    *            if the file doesn't exist.
    * @throws UnsupportedEncodingException
    *            if the named charset is not supported
    * @throws IOException
    *            if any other error occurred when reading from the input stream.
    */
   public static Properties loadFromClasspath(String filePath, String encoding) throws UnsupportedEncodingException,
         IOException {

      InputStream is = null;
      InputStreamReader isr = null;
      Properties properties;

      try {
         is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
         isr = new InputStreamReader(is, encoding);
         properties = new Properties();
         properties.load(isr);
      }
      finally {
         try {
            if (is != null) {
               is.close();
            }
         }
         catch (IOException e) {
            // Nothing to do
         }
         try {
            if (isr != null) {
               isr.close();
            }
         }
         catch (IOException e) {
            // Nothing to do
         }
      }

      return properties;
   }

   /**
    * Converts the supplied bundle to a Properties.
    * 
    * @param bundle
    *           The ResourceBundle to convert.
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

   /**
    * <p>
    * Returns a set from the specified {@code values}, using "," as a default
    * delimiter to perform the splip operation.
    * </p>
    * 
    * @param values
    *           a {@link String} containing the element to return as a
    *           {@link List} and containing "," as a default delimiter.
    * @return a set of {@link String}s.
    */
   public static Set<String> propertyAsSet(String values) {
      return new HashSet<String>(propertyAsList(values, ","));
   }

   /**
    * <p>
    * Returns a list from the specified {@code values}, using "," as a default
    * delimiter to perform the splip operation.
    * </p>
    * 
    * @param values
    *           a {@link String} containing the element to return as a
    *           {@link List} and containing "," as a default delimiter.
    * @return a list of {@link String}s.
    */
   public static List<String> propertyAsList(String values) {
      return propertyAsList(values, ",");
   }

   /**
    * <p>
    * Returns a list from the specified {@code values}, using the specified
    * delimiter to perform the splip operation.
    * </p>
    * 
    * @param values
    *           a {@link String} containing the element to return as a
    *           {@link List}.
    * @param delimiter
    *           The delimiter to use to perform the split operation against the
    *           {@link String}.
    * @return a list of {@link String}s.
    */
   public static List<String> propertyAsList(String values, String delimiter) {
      if (values == null || values.isEmpty()) {
         return Collections.emptyList();
      }
      List<String> retval = new ArrayList<String>();
      for (String val : values.trim().split(delimiter)) {
         retval.add(val.trim());
      }
      return retval;
   }
}
