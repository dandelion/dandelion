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
package com.github.dandelion.core.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;

/**
 * Utilities used for classpath scanning.
 */
public final class ResourceScanner {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(ResourceScanner.class);

	/**
	 * <p>
	 * Finds the logical path of the first resource that matches the given
	 * {@code resourceName} by scanning the classpath under the given
	 * {@code location}.
	 * 
	 * <p>
	 * By default, no other condition but the name will be applied to the
	 * resource name and the classpath scanning won't be recursive.
	 * 
	 * @param location
	 *            The classpath location where to scan.
	 * @param nameFilter
	 *            The name of the resource to look for.
	 * @return The logical path of the resource if found, otherwise {@code null}
	 *         .
	 * @throws IOException
	 *             if something goes wrong during the scanning.
	 */
	public static String findResourcePath(String location, String nameFilter) throws IOException {
		Set<String> resourcePaths = scanForResourcePaths(location, null, nameFilter, null, null, false);
		if (resourcePaths.isEmpty()) {
			return null;
		}
		else {
			return resourcePaths.iterator().next();
		}
	}

	/**
	 * <p>
	 * Finds the virtual path of all resources that match the given conditions
	 * by scanning the classpath under the given {@code location}.
	 * 
	 * @param location
	 *            The classpath location where to scan.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param nameFilter
	 *            The name of the resource to look for.
	 * @param recursive
	 *            Whether the scanning should be recursive or not.
	 * @return A set of resource paths that match the given conditions.
	 * @throws IOException
	 *             if something goes wrong during the scanning.
	 */
	public static Set<String> findResourcePaths(String location, List<String> excludedPaths, String nameFilter,
			boolean recursive) throws IOException {
		return scanForResourcePaths(location, excludedPaths, nameFilter, null, null, recursive);
	}
	
	/**
	 * <p>
	 * Finds the virtual path of all resources that match the given conditions
	 * by scanning the classpath under the given {@code location}.
	 * 
	 * @param location
	 *            The classpath location where to scan.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param prefixFilter
	 *            The prefix condition to be applied on the resource name.
	 * @param suffixFilter
	 *            The suffix condition to be applied on the resource name.
	 * @param recursive
	 *            Whether the scanning should be recursive or not.
	 * @return A set of resource paths that match the given conditions.
	 * @throws IOException
	 *             if something goes wrong during the scanning.
	 */
	public static Set<String> findResourcePaths(String location, List<String> excludedPaths, String prefixFilter,
			String suffixFilter, boolean recursive) throws IOException {
		return scanForResourcePaths(location, excludedPaths, null, prefixFilter, suffixFilter, recursive);
	}

	/**
	 * <p>
	 * Scans for all resources that match the given confitions by scanning the
	 * classpath.
	 * 
	 * @param location
	 *            The classpath location where to scan.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param nameFilter
	 *            The name of the resource to look for.
	 * @param prefixFilter
	 *            The prefix condition to be applied on the resource name.
	 * @param suffixFilter
	 *            The suffix condition to be applied on the resource name.
	 * @param recursive
	 *            Whether the scanning should be recursive or not.
	 * @return A set of resource paths that match the given conditions.
	 * @throws IOException
	 *             if something goes wrong during the scanning.
	 * @throws DandelionException
	 *             if the URL protocol used to access the resource is not
	 *             supported. This may happen with the JBoss VFS which is still
	 *             not supported.
	 */
	private static Set<String> scanForResourcePaths(String location, List<String> excludedPaths, String nameFilter,
			String prefixFilter, String suffixFilter, boolean recursive) throws IOException {

		LOG.trace("Scanning for resources at '{}'...", location);

		Set<String> resourcePaths = new HashSet<String>();

		Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(location);

		while (urls.hasMoreElements()) {

			URL url = urls.nextElement();
			if ("file".equals(url.getProtocol())) {

				// Computes the physical root of the classpath to later
				// determine the resource path more easily
				String resourcePath = URLDecoder.decode(url.getPath(), "UTF-8");
				if (resourcePath.endsWith("/") && location.length() > 1) {
					resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
				}
				String classpathPhysicalRoot = resourcePath.substring(0, resourcePath.length() - location.length());

				// Gets the folder in which files will be scanned
				File folder = new File(resourcePath);

				resourcePaths.addAll(scanForResourcePathsInFileSystem(folder, classpathPhysicalRoot, recursive));
			}
			else if ("jar".equals(url.getProtocol()) 
					|| "zip".equals(url.getProtocol()) // Weblogic
					|| "wsjar".equals(url.getProtocol())) // Websphere
			{

				resourcePaths.addAll(scanForResourcePathsInJarFile(url));
			}
			else {
				throw new DandelionException(ResourceScannerError.UNSUPPORTED_PROTOCOL).set("", url.getProtocol());
			}
		}

		LOG.debug("{} resources found before filtering", resourcePaths.size());
		return filterResourcePaths(resourcePaths, excludedPaths, nameFilter, prefixFilter, suffixFilter);
	}

	/**
	 * <p>
	 * Scans for all resources in the given file system {@code folder}.
	 * 
	 * @param folder
	 *            Folder in which the files will be scanned/listed.
	 * @param classpathPhysicalRoot
	 *            Physical root of the classpath, used to compute the resource
	 *            path.
	 * @param recursive
	 *            Whether the scanning should be recursive or not.
	 * @return A set of non-filtered resource paths.
	 * @throws IOException
	 *             if something goes wrong during the scanning.
	 */
	private static Set<String> scanForResourcePathsInFileSystem(File folder, String classpathPhysicalRoot,
			boolean recursive) throws IOException {

		Set<String> extractedResourcePaths = new HashSet<String>();

		for (File file : folder.listFiles()) {
			if (file.canRead()) {

				if (file.isDirectory() && recursive) {
					extractedResourcePaths.addAll(scanForResourcePathsInFileSystem(file, classpathPhysicalRoot,
							recursive));
				}
				else {
					String filePath = URLDecoder.decode(file.toURI().toURL().getFile(), "UTF-8");
					String resourcePath = filePath.substring(classpathPhysicalRoot.length());
					extractedResourcePaths.add(resourcePath);
				}
			}
		}

		return extractedResourcePaths;
	}

	/**
	 * <p>
	 * Scans for all resources in the given {@code url} that reffers to a JAR
	 * file.
	 * 
	 * @param url
	 *            The URL that reffers to the JAR file in which resources will
	 *            be scanned.
	 * @return A set of non-filtered resource paths.
	 * @throws IOException
	 *             if something goes wrong during the scanning.
	 */
	private static Set<String> scanForResourcePathsInJarFile(URL url) throws IOException {

		Set<String> extractedResourcePaths = new HashSet<String>();

		URLConnection connection = url.openConnection();

		if (connection instanceof JarURLConnection) {

			JarURLConnection jarConnection = (JarURLConnection) connection;
			jarConnection.setUseCaches(false);
			JarFile jarFile = jarConnection.getJarFile();

			try {
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					String resourcePath = entries.nextElement().getName();
					extractedResourcePaths.add(resourcePath);
				}
			}
			finally {
				jarFile.close();
			}
		}

		return extractedResourcePaths;
	}

	/**
	 * <p>
	 * Tests whether the given {@code path} is authorized according to the
	 * passed list of paths to exclude.
	 * 
	 * @param path
	 *            The path name that must not be present in the list of excluded
	 *            paths.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @return {@code true} if the path is authorized, otherwise {@code false}.
	 */
	private static boolean isPathAuthorized(String path, List<String> excludedPaths) {
		if (excludedPaths != null) {
			for (String excludedFolder : excludedPaths) {
				if (path.contains(excludedFolder)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * <p>
	 * Filters the given set of resource paths in multiple ways:
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
	 *            The scanned resource paths.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param nameFilter
	 *            The name of the resource to look for.
	 * @param prefixFilter
	 *            The prefix condition to be applied on the resource name.
	 * @param suffixFilter
	 *            The suffix condition to be applied on the resource name;
	 * @return A set of resource paths that match the given conditions.
	 */
	private static Set<String> filterResourcePaths(Set<String> resourcePaths, List<String> excludedPaths,
			String nameFilter, String prefixFilter, String suffixFilter) {
		Set<String> filteredResources = new HashSet<String>();

		LOG.debug("Filtering scanned resources");
		for (String resourcePath : resourcePaths) {

			if (isPathAuthorized(resourcePath, excludedPaths)) {

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

		LOG.debug("{} resources found after filtering", filteredResources.size());
		return filteredResources;
	}
}