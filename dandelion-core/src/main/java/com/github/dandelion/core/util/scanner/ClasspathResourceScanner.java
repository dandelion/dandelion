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
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.ClassUtils;
import com.github.dandelion.core.util.LibraryDetector;
import com.github.dandelion.core.util.StringUtils;
import com.github.dandelion.core.util.scanner.jboss.JBossVFS2UrlResolver;
import com.github.dandelion.core.util.scanner.jboss.JBossVFS3LocationResourceScanner;
import com.github.dandelion.core.util.scanner.jboss.JBossVFS3UrlResolver;
import com.github.dandelion.core.util.scanner.websphere.WebSphereUrlResolver;

/**
 * <p>
 * Scans for resources within the classpath.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class ClasspathResourceScanner {

   private static final Logger LOG = LoggerFactory.getLogger(ClasspathResourceScanner.class);

   public final static String PREFIX = "classpath:";

   /**
    * <p>
    * Finds the logical path of the first resource that matches the given
    * {@code resourceName} by scanning the classpath under the given
    * {@code location}.
    * </p>
    * <p>
    * By default, no other condition but the name will be applied to the
    * resource name.
    * </p>
    * 
    * @param location
    *           The classpath location where to scan.
    * @param nameFilter
    *           The name of the resource to look for.
    * @return The logical path of the resource if found, otherwise {@code null}
    */
   public static String findResourcePath(String location, String nameFilter) {
      Set<String> resourcePaths = scanForResourcePaths(location, null, nameFilter, null, null);
      if (resourcePaths.isEmpty()) {
         return null;
      }
      else {
         return resourcePaths.iterator().next();
      }
   }

   /**
    * <p>
    * Finds the virtual path of all resources that match the given conditions by
    * scanning the classpath under the given {@code location}.
    * </p>
    * 
    * @param location
    *           The classpath location where to scan.
    * @param excludedPaths
    *           List of paths which will be excluded during the classpath
    *           scanning.
    * @param nameFilter
    *           The name of the resource to look for.
    * @return A set of resource paths that match the given conditions.
    */
   public static Set<String> findResourcePaths(String location, Set<String> excludedPaths, String nameFilter) {
      return scanForResourcePaths(location, excludedPaths, nameFilter, null, null);
   }

   /**
    * <p>
    * Finds the virtual path of all resources that match the given conditions by
    * scanning the classpath under the given {@code location}.
    * </p>
    * 
    * @param location
    *           The classpath location where to scan.
    * @param excludedPaths
    *           List of paths which will be excluded during the classpath
    *           scanning.
    * @param prefixFilter
    *           The prefix condition to be applied on the resource name.
    * @param suffixFilter
    *           The suffix condition to be applied on the resource name.
    * @return A set of resource paths that match the given conditions.
    */
   public static Set<String> findResourcePaths(String location, Set<String> excludedPaths, String prefixFilter,
         String suffixFilter) {
      return scanForResourcePaths(location, excludedPaths, null, prefixFilter, suffixFilter);
   }

   /**
    * <p>
    * Scans for all resources that match the given confitions by scanning the
    * classpath using the context {@link ClassLoader} of the current
    * {@link Thread}.
    * </p>
    * 
    * @param location
    *           The classpath location where to scan.
    * @param excludedPaths
    *           List of paths which will be excluded during the classpath
    *           scanning.
    * @param nameFilter
    *           The name of the resource to look for.
    * @param prefixFilter
    *           The prefix condition to be applied on the resource name.
    * @param suffixFilter
    *           The suffix condition to be applied on the resource name.
    * @return A set of resource paths that match the given conditions.
    * @throws DandelionException
    *            if the URL protocol used to access the resource is not
    *            supported.
    */
   private static Set<String> scanForResourcePaths(String location, Set<String> excludedPaths, String nameFilter,
         String prefixFilter, String suffixFilter) {

      LOG.trace("Scanning for resources at '{}'...", location);

      Set<String> resourcePaths = new HashSet<String>();

      Enumeration<URL> urls;
      try {
         urls = ClassUtils.getDefaultClassLoader().getResources(location);
         while (urls.hasMoreElements()) {

            URL url = urls.nextElement();
            LOG.trace("Found URL: {} (protocol:{})", url.getPath(), url.getProtocol());

            UrlResolver urlResolver = createUrlResolver(url.getProtocol());
            LOG.trace("Resolving URL \"{}\" with the resolver {}", url.getPath(), urlResolver.getClass()
                  .getSimpleName());

            URL resolvedUrl = urlResolver.toStandardUrl(url);
            LOG.trace("Resolved URL: \"{}\"", resolvedUrl.getPath());

            String protocol = resolvedUrl.getProtocol();
            LocationResourceScanner classPathLocationScanner = createLocationScanner(protocol);

            resourcePaths.addAll(classPathLocationScanner.findResourcePaths(location, resolvedUrl));
         }
      }
      catch (IOException e) {
         LOG.warn("Unable to scan within the location: {}", location);
      }

      LOG.trace("{} resources found before filtering", resourcePaths.size());
      return filterResourcePaths(location, resourcePaths, excludedPaths, nameFilter, prefixFilter, suffixFilter);
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
            if (resourcePath.startsWith(excludedFolder)) {
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
         Set<String> excludedPaths, String nameFilter, String prefixFilter, String suffixFilter) {
      Set<String> filteredResources = new HashSet<String>();

      LOG.trace("Filtering scanned resources...");
      for (String resourcePath : resourcePaths) {

         if (isPathAuthorized(resourcePath, location, excludedPaths)) {

            String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);

            if (StringUtils.isBlank(nameFilter) && StringUtils.isBlank(prefixFilter)
                  && StringUtils.isBlank(suffixFilter)) {
               filteredResources.add(resourcePath);
               continue;
            }

            // if name condition is set, it's the only test on resources.
            if (StringUtils.isNotBlank(nameFilter)) {
               if (nameFilter.equalsIgnoreCase(resourceName)) {
                  filteredResources.add(resourcePath);
               }
            }
            else {
               // otherwise prefix and suffix conditions are verified
               if (suffixFilter == null && resourceName.startsWith(prefixFilter)) {
                  filteredResources.add(resourcePath);
               }
               else if (prefixFilter == null && resourceName.endsWith(suffixFilter)) {
                  filteredResources.add(resourcePath);
               }
               else if (prefixFilter != null && suffixFilter != null && resourceName.startsWith(prefixFilter)
                     && resourceName.endsWith(suffixFilter)) {
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
    * Creates and returns the appropriate URL resolver for the given {@link URL}
    * protocol.
    * </p>
    * 
    * @param protocol
    *           The protocol of the location url to scan.
    * @return The url resolver for this protocol.
    */
   private static UrlResolver createUrlResolver(String protocol) {

      // Websphere
      if (protocol.startsWith("wsjar")) {
         LOG.trace("Selected URL resolver: {}", WebSphereUrlResolver.class.getSimpleName());
         return new WebSphereUrlResolver();
      }

      // JBoss 5+ / WildFly
      if (protocol.startsWith("vfs") || protocol.startsWith("vfszip")) {

         if (LibraryDetector.isJBossVFS2Available()) {
            LOG.trace("Selected URL resolver: {}", JBossVFS2UrlResolver.class.getSimpleName());
            return new JBossVFS2UrlResolver();
         }

         if (LibraryDetector.isJBossVFS3Available()) {
            LOG.trace("Selected URL resolver: {}", JBossVFS3UrlResolver.class.getSimpleName());
            return new JBossVFS3UrlResolver();
         }
      }

      LOG.trace("Selected URL resolver: {}", StandardUrlResolver.class.getSimpleName());
      return new StandardUrlResolver();
   }

   /**
    * <p>
    * Creates and returns the appropriate resource scanner for the given
    * {@link URL} protocol.
    * </p>
    * 
    * @param protocol
    *           The protocol of the location url to scan.
    * @return The resource scanner for this protocol.
    * @throws DandelionException
    *            if the protocol is not supported.
    */
   private static LocationResourceScanner createLocationScanner(String protocol) {

      if ("file".equals(protocol)) {
         LOG.trace("Selected resource scanner: {}", FileSystemLocationResourceScanner.class.getSimpleName());
         return new FileSystemLocationResourceScanner();
      }

      if ("jar".equals(protocol) || "zip".equals(protocol) // WebLogic
            || "wsjar".equals(protocol) // WebSphere
      ) {
         LOG.trace("Selected resource scanner: {}", JarLocationResourceScanner.class.getSimpleName());
         return new JarLocationResourceScanner();
      }

      // JBoss / WildFly
      if ("vfs".equals(protocol) && LibraryDetector.isJBossVFS3Available()) {
         LOG.debug("Selected resource scanner: {}", JBossVFS3LocationResourceScanner.class.getSimpleName());
         return new JBossVFS3LocationResourceScanner();
      }

      StringBuilder sb = new StringBuilder("The protocol ");
      sb.append(protocol);
      sb.append(" is not supported.");
      throw new DandelionException(sb.toString());
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private ClasspathResourceScanner() {
      throw new AssertionError();
   }
}
