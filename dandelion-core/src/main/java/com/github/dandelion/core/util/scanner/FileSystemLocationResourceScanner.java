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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.util.PathUtils;

/**
 * <p>
 * Scanner for resources located in the file system.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class FileSystemLocationResourceScanner implements LocationResourceScanner {

   private static final Logger LOG = LoggerFactory.getLogger(FileSystemLocationResourceScanner.class);

   public Set<String> findResourcePaths(String location, URL resourceUrl) throws IOException {

      // Computes the physical root of the classpath to later
      // determine the resource path more easily
      String resourcePath = PathUtils.toFilePath(resourceUrl);
      String classpathPhysicalRoot = resourcePath.substring(0, resourcePath.length() - location.length());

      // Gets the folder in which files will be scanned
      File folder = new File(resourcePath);

      return scanForResourcePathsInFileSystem(folder, classpathPhysicalRoot);
   }

   /**
    * <p>
    * Scans recursively for all resources in the given file system
    * {@code folder}.
    * </p>
    * 
    * @param folder
    *           Folder in which the files will be scanned/listed.
    * @param classpathPhysicalRoot
    *           Physical root of the classpath, used to compute the resource
    *           path.
    * @return A set of non-filtered resource paths.
    * @throws IOException
    *            if something goes wrong during the scanning.
    */
   private Set<String> scanForResourcePathsInFileSystem(File folder, String classpathPhysicalRoot) throws IOException {

      Set<String> extractedResourcePaths = new HashSet<String>();

      for (File file : folder.listFiles()) {
         if (file.canRead()) {

            if (file.isDirectory()) {
               extractedResourcePaths.addAll(scanForResourcePathsInFileSystem(file, classpathPhysicalRoot));
            }
            else {
               String filePath = URLDecoder.decode(file.toURI().toURL().getFile(), "UTF-8");
               String resourcePath = filePath.substring(classpathPhysicalRoot.length());
               LOG.trace("Resource path: {}", resourcePath);
               extractedResourcePaths.add(resourcePath);
            }
         }
      }

      return extractedResourcePaths;
   }
}
