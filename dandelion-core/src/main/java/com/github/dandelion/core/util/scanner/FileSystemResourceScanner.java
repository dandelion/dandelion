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
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Scans for resources within the local file system.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 */
public final class FileSystemResourceScanner {

   private static final Logger LOG = LoggerFactory.getLogger(FileSystemResourceScanner.class);
   
   public final static String PREFIX = "file:";
   
   /**
    * <p>
    * Finds the path of all resources that match the given conditions by
    * scanning the file systme under the given {@code rootLocation}.
    * </p>
    * 
    * @param rootLocation
    *           The location where to scan.
    * @param nameFilter
    *           The name of the resource to look for.
    * @return A set of resource paths that match the given conditions.
    * @throws IOException
    *            if something goes wrong during the scanning.
    */
   public static Set<String> findResourcePaths(String rootLocation, String nameFilter) {
      return scanForResourcePaths(rootLocation, nameFilter, null);
   }

   /**
    * <p>
    * Finds the path of all resources that match the given conditions by
    * scanning the file systme under the given {@code rootLocation}.
    * </p>
    * 
    * @param rootLocation
    *           The location where to scan.
    * @param excludedPaths
    *           List of paths which will be excluded during the scanning.
    * @param nameFilter
    *           The name of the resource to look for.
    * @return A set of resource paths that match the given conditions.
    * @throws IOException
    *            if something goes wrong during the scanning.
    */
   public static Set<String> findResourcePaths(String rootLocation, String nameFilter, Set<String> excludedPaths) {
      return scanForResourcePaths(rootLocation, nameFilter, excludedPaths);
   }

   private static Set<String> scanForResourcePaths(String rootLocation, String nameFilter, Set<String> excludedPaths) {

      Set<String> result = new HashSet<String>();
      String normalizedRootLocation = rootLocation.replace("file:", "").replaceAll("\\\\", "/");

      doScanForResourcePaths(normalizedRootLocation, excludedPaths, nameFilter, result);

      return filterResourcePaths(normalizedRootLocation, result, excludedPaths, nameFilter);
   }

   private static void doScanForResourcePaths(String rootLocation, Set<String> excludedPaths, String nameFilter,
         Set<String> result) {

      File rootDir = new File(rootLocation).getAbsoluteFile();

      File[] dirContents = rootDir.listFiles();

      if (dirContents == null) {
         LOG.warn("Could not retrieve contents of directory \"{}\"", rootDir.getAbsolutePath());
         return;
      }
      for (File content : dirContents) {
         String currentPath = content.getAbsolutePath().replaceAll("\\\\", "/");

         result.add(currentPath);
         if (content.isDirectory()) {
            if (!content.canRead()) {
               LOG.debug("Skipping subdirectory \"{}\" because the application is not allowed to read the directory",
                     currentPath);
            }
            else {
               // Recursively scan within subdirectories
               doScanForResourcePaths(currentPath, excludedPaths, nameFilter, result);
            }
         }
      }
   }

   /**
    * <p>
    * Filters the given set of resource paths in multiple ways:
    * </p>
    * <ul>
    * <li>If the resource path contains any of the given {@code excludedPaths},
    * the resource path will be filtered out.</li>
    * <li>If the {@code nameFilter} is used, the resource is only filtered on
    * its name. Suffix and prefix have no effect.</li>
    * <li>If either {@code prefixFilter} or {@code suffixFilter} or both are
    * used, the resource won't be filtered on its name at all.</li>
    * </ul>
    * 
    * @param resourcePaths
    *           The scanned resource paths.
    * @param excludedPaths
    *           List of paths which will be excluded during the classpath
    *           scanning.
    * @param nameFilter
    *           The name of the resource to look for.
    * @param prefixFilter
    *           The prefix condition to be applied on the resource name.
    * @param suffixFilter
    *           The suffix condition to be applied on the resource name;
    * @return A set of resource paths that match the given conditions.
    */
   private static Set<String> filterResourcePaths(String location, Set<String> resourcePaths,
         Set<String> excludedPaths, String nameFilter) {
      Set<String> filteredResources = new HashSet<String>();

      LOG.trace("Filtering scanned resources...");
      for (String resourcePath : resourcePaths) {

         if (isPathAuthorized(resourcePath, location, excludedPaths)) {

            String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);

            if (StringUtils.isNotBlank(nameFilter)) {
               if (nameFilter.equalsIgnoreCase(resourceName)) {
                  filteredResources.add(resourcePath);
               }
            }
         }
      }

      LOG.trace("{} resources found after filtering", filteredResources.size());
      return filteredResources;
   }

   /**
    * <p>
    * Tests whether the given {@code path} is authorized according to the passed
    * list of paths to exclude.
    * </p>
    * 
    * @param path
    *           The path name that must not be present in the list of excluded
    *           paths.
    * @param authorizedLocation
    *           Current location being scanned by the scanner.
    * @param excludedPaths
    *           List of paths which will be excluded during the classpath
    *           scanning.
    * @return {@code true} if the path is authorized, otherwise {@code false}.
    */
   private static boolean isPathAuthorized(String resourcePath, String authorizedLocation, Set<String> excludedPaths) {

      if (excludedPaths != null) {
         for (String excludedFolder : excludedPaths) {
            if (resourcePath.contains(excludedFolder)) {
               return false;
            }
         }
         return true;
      }
      else if (resourcePath.startsWith(authorizedLocation)) {
         return true;
      }
      else {
         return false;
      }
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private FileSystemResourceScanner() {
      throw new AssertionError();
   }
}
