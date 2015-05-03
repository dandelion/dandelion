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
package com.github.dandelion.core.util.scanner;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Scanner for resources located in a JAR file.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class JarLocationResourceScanner implements LocationResourceScanner {

   private static final Logger LOG = LoggerFactory.getLogger(JarLocationResourceScanner.class);

   public Set<String> findResourcePaths(String location, URL resourceUrl) throws IOException {

      JarFile jarFile = getJarFromUrl(resourceUrl);

      try {
         return findResourcePathsFromJarFile(jarFile, location);
      }
      finally {
         jarFile.close();
      }
   }

   /**
    * <p>
    * Retrieves the Jar file represented by the given {@link URL}.
    * </p>
    * 
    * @param locationUrl
    *           The URL of the jar.
    * @return The jar file.
    * @throws IOException
    *            when the jar could not be resolved.
    */
   private JarFile getJarFromUrl(URL locationUrl) throws IOException {

      URLConnection con = locationUrl.openConnection();
      if (con instanceof JarURLConnection) {
         // Should usually be the case for traditional JAR files.
         JarURLConnection jarCon = (JarURLConnection) con;
         jarCon.setUseCaches(false);
         return jarCon.getJarFile();
      }

      // No JarURLConnection -> need to resort to URL file parsing.
      // We'll assume URLs of the format "jar:path!/entry", with the protocol
      // being arbitrary as long as following the entry format.
      // We'll also handle paths with and without leading "file:" prefix.
      String urlFile = locationUrl.getFile();

      int separatorIndex = urlFile.indexOf("!/");
      if (separatorIndex != -1) {
         String jarFileUrl = urlFile.substring(0, separatorIndex);
         if (jarFileUrl.startsWith("file:")) {
            try {
               return new JarFile(new URL(jarFileUrl).toURI().getSchemeSpecificPart());
            }
            catch (URISyntaxException ex) {
               // Fallback for URLs that are not valid URIs (should hardly
               // ever happen).
               return new JarFile(jarFileUrl.substring("file:".length()));
            }
         }
         return new JarFile(jarFileUrl);
      }

      return new JarFile(urlFile);
   }

   /**
    * <p>
    * Find all resource paths located under the passed {@code location} within
    * the passed {@link JarFile}.
    * </p>
    * 
    * @param jarFile
    *           The jar file.
    * @param location
    *           The location to look under.
    * @return The resource paths.
    * @throws java.io.IOException
    *            when reading the jar file failed.
    */
   private Set<String> findResourcePathsFromJarFile(JarFile jarFile, String location) throws IOException {

      location += location.endsWith("/") ? "" : "/";
      Set<String> resourceNames = new TreeSet<String>();

      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
         String entryName = entries.nextElement().getName();
         LOG.trace("Resource path: {}", entryName);
         if (entryName.startsWith(location)) {
            resourceNames.add(entryName);
         }
      }

      return resourceNames;
   }
}
