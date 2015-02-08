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
package com.github.dandelion.core.config;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.PropertiesUtils;
import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Default implementation of the {@link ConfigurationLoader}.
 * </p>
 * 
 * <p>
 * Note that a custom {@link ConfigurationLoader} can be used thanks to the
 * {@link Context}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @author Romain Lespinasse
 * @since 0.10.0
 * @see Context
 */
public class StandardConfigurationLoader implements ConfigurationLoader {

   private static Logger logger = LoggerFactory.getLogger(StandardConfigurationLoader.class);

   public static final String DANDELION_USER_PROPERTIES = "dandelion";
   public static final String DANDELION_CONFIGURATION = "dandelion.configuration";

   /**
    * {@inheritDoc}
    */
   public Properties loadUserConfiguration() {

      logger.debug("Loading user configuration...");

      Properties userProperties = null;

      // Get the Dandelion mode
      String activeRawProfile = Profile.getActiveRawProfile();

      // Get the configuration base path, if set
      String dandelionBasePath = System.getProperty(DANDELION_CONFIGURATION);

      // Compute the name of the file to load
      String dandelionFileName = null;
      if (StringUtils.isBlank(activeRawProfile)) {
         dandelionFileName = DANDELION_USER_PROPERTIES + ".properties";
      }
      else if (StringUtils.isNotBlank(activeRawProfile)) {
         dandelionFileName = DANDELION_USER_PROPERTIES + "_" + activeRawProfile + ".properties";
      }

      // Compute the full path
      String dandelionFilePath = null;

      // First check if the resource bundle is externalized
      if (StringUtils.isNotBlank(dandelionBasePath)) {

         if (!dandelionBasePath.endsWith(String.valueOf(File.separatorChar))) {
            dandelionBasePath += File.separator;
         }
         dandelionFilePath = dandelionBasePath + dandelionFileName;
         logger.debug("Trying to load the configuration from \"{}\"", dandelionFilePath);

         try {
            userProperties = PropertiesUtils.loadFromFileSystem(dandelionFilePath, "UTF-8");
         }
         catch (Exception e) {
            StringBuilder error = new StringBuilder("No file \"");
            error.append(dandelionFileName);
            error.append("\" was found in \"");
            error.append(dandelionBasePath);
            error.append("\".");
            if (StringUtils.isNotBlank(activeRawProfile)) {
               throw new DandelionException(error.toString());
            }
            else {
               logger.warn("No file \"{}\" was found in \"{}\". The default configuration will be used.",
                     dandelionFileName, dandelionBasePath);
            }
         }
      }

      // No system property is set, retrieves the bundle from the classpath
      if (userProperties == null) {

         dandelionFilePath = "dandelion/" + dandelionFileName;
         logger.debug("Trying to load the configuration from \"{}\"", dandelionFilePath);

         try {
            userProperties = PropertiesUtils.loadFromClasspath(dandelionFilePath, "UTF-8");
         }
         catch (Exception e) {
            logger.warn("No file \"dandelion.properties\" was found in \"" + dandelionFilePath
                  + "\" (classpath). The default configuration will be used.");
         }
      }

      logger.debug("User configuration loaded");

      return userProperties;
   }
}
