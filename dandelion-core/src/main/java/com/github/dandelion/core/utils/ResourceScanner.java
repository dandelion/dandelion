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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities used for classpath scanning.
 */
public final class ResourceScanner {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(ResourceScanner.class);

	/**
	 * Internal set used as buffer.
	 */
	private static Set<String> resources = new HashSet<String>();

	/**
	 * <p>
	 * Get a first from its name by scanning the classpath.
	 * 
	 * <p>
	 * Note that the scanning is not recursive.
	 * 
	 * @param path
	 *            The virtual path in which to scan.
	 * @param nameCondition
	 *            Name that should match with the scanned resource names.
	 * @return all matching resource names.
	 * @throws IOException
	 *             If any I/O error occur during the resource scanning.
	 */
	public static String getResource(String path, String nameCondition) throws IOException {
		Set<String> resources = getResources(path, null, nameCondition, null, null, false);
		if (resources.isEmpty()) {
			return null;
		}
		return resources.toArray(new String[1])[0];
	}

	/**
	 * <p>
	 * Get all resources that matches the given confitions by scanning the
	 * classpath.
	 * 
	 * @param path
	 *            The virtual path in which to scan.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param prefixCondition
	 *            The prefix condition to be applied on the resource name.
	 * @param suffixCondition
	 *            The suffix condition to be applied on the resource name;
	 * @param recursive
	 *            Indicates whether the scanning is recursive or not.
	 * @return all matching resource names.
	 * @throws IOException
	 *             If any I/O error occur during the resource scanning.
	 */
	public static Set<String> getResources(String path, List<String> excludedPaths, String prefixCondition,
			String suffixCondition, boolean recursive) throws IOException {
		return getResources(path, excludedPaths, null, prefixCondition, suffixCondition, recursive);
	}

	/**
	 * <p>
	 * Get all resources that matches the given confitions by scanning the
	 * classpath.
	 * 
	 * @param path
	 *            The virtual path in which to scan.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param nameCondition
	 *            Name that should match with the scanned resource names.
	 * @param prefixCondition
	 *            The prefix condition to be applied on the resource name.
	 * @param suffixCondition
	 *            The suffix condition to be applied on the resource name;
	 * @param recursive
	 *            Indicates whether the scanning is recursive or not.
	 * @return all matching resource names.
	 * @throws IOException
	 *             If any I/O error occur during the resource scanning.
	 */
	private static Set<String> getResources(String path, List<String> excludedPaths, String nameCondition,
			String prefixCondition, String suffixCondition, boolean recursive) throws IOException {

		LOG.trace("Scanning for resources at '{}'...", path);

		resources.addAll(scanForResources(path, excludedPaths, nameCondition, prefixCondition, suffixCondition,
				recursive));

		Set<String> scannedResources = new HashSet<String>(resources);

		LOG.trace("Found {} files in {}", scannedResources.size(), path);

		// Clear the temporary set for the next scanning
		resources.clear();
		return scannedResources;
	}

	/**
	 * <p>
	 * Scans for resources that matches the given conditions inside the
	 * classpath.
	 * 
	 * @param path
	 *            The virtual path in which to scan.
	 * @param excludedPaths
	 *            List of paths which will be excluded during the classpath
	 *            scanning.
	 * @param nameCondition
	 *            Name that should match with the scanned resource names.
	 * @param prefixCondition
	 *            The prefix condition to be applied on the resource name.
	 * @param suffixCondition
	 *            The suffix condition to be applied on the resource name;
	 * @param recursive
	 *            Indicates whether the scanning is recursive or not.
	 * @return all matching resource names.
	 * @throws IOException
	 *             If any I/O error occur during the resource scanning.
	 */
	private static Set<String> scanForResources(String path, List<String> excludedPaths, String nameCondition,
			String prefixCondition, String suffixCondition, boolean recursive) throws IOException {

		Set<String> resourcesString = new HashSet<String>();

		Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

		while (resources.hasMoreElements()) {

			URL resource = resources.nextElement();

			// resources extraction with file protocol
			if ("file".equals(resource.getProtocol())) {
				resourcesString.addAll(extractResourcesOnFileSystem(path, excludedPaths, nameCondition,
						prefixCondition, suffixCondition, resource, recursive));
			}

			// or jar protocol
			if ("jar".equals(resource.getProtocol()) || "zip".equals(resource.getProtocol())) {
				resourcesString.addAll(extractResourcesOnJarFile(path, excludedPaths, nameCondition, prefixCondition,
						suffixCondition, resource, recursive));
			}
		}

		return resourcesString;
	}

	/**
	 * Extract resources on file system
	 * 
	 * @param path
	 *            path of the resource folder
	 * @param resource
	 *            resource url
	 * @throws IOException
	 */
	private static Set<String> extractResourcesOnFileSystem(String path, List<String> excludedPaths,
			String nameCondition, String prefixCondition, String suffixCondition, URL resource, boolean recursive)
			throws IOException {

		Set<String> extractedResources = new HashSet<String>();
		String resourcePath = URLDecoder.decode(resource.getPath(), "UTF-8");
		File folder = new File(resourcePath);
		if (!folder.isDirectory()) {
			return Collections.emptySet();
		}

		File[] files = folder.listFiles();

		if (files != null) {

			for (File file : files) {

				if (file.canRead() && file.isFile()
						&& isAnAuthorizedResource(file.getName(), nameCondition, prefixCondition, suffixCondition)) {
					extractedResources.add(path + "/" + file.getName());
				}
				// Only extract resource in subdirectories if recursive mode is
				// enabled
				else if (recursive && file.canRead() && file.isDirectory()
						&& isAnAuthorizedFolder(path + "/" + file.getName(), excludedPaths)) {

					// Recursive call to fill the resource set
					resources.addAll(scanForResources(path + "/" + file.getName(), excludedPaths,
							nameCondition, prefixCondition, suffixCondition, recursive));
				}
			}
		}

		return extractedResources;
	}

	/**
	 * Extract resources on jar file
	 * 
	 * @param path
	 *            path of the resource folder
	 * @param resource
	 *            resource url
	 * @throws IOException
	 *             If I/O errors occur
	 */
	private static Set<String> extractResourcesOnJarFile(String path, List<String> excludedPaths,
			String nameCondition, String prefixCondition, String suffixCondition, URL resource, boolean recursive)
			throws IOException {

		Set<String> extractedResources = new HashSet<String>();
		URLConnection con = resource.openConnection();

		if (!(con instanceof JarURLConnection)) {
			return Collections.emptySet();
		}
		JarURLConnection jarCon = (JarURLConnection) con;
		jarCon.setUseCaches(false);
		JarFile jarFile = jarCon.getJarFile();
		try {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				String entryName = entries.nextElement().getName();

				if (isAnAuthorizedFolder(entryName, excludedPaths)
						&& isAnAuthorizedResource(entryName, nameCondition, prefixCondition, suffixCondition)) {
					extractedResources.add(entryName);
				}
			}
		} finally {
			jarFile.close();
		}

		return extractedResources;
	}

	/**
	 * <p>
	 * Test whether the passed {@code resourceName} is authorized according to
	 * the passed filters.
	 * 
	 * @param resourceName
	 *            The resource name on which filters must be applied.
	 * @param nameCondition
	 *            If not blank, the {@code resourceName} must match this name
	 *            (case ignored).
	 * @param prefixCondition
	 *            If not blank, the {@code resourceName} must match this prefix.
	 * @param suffixCondition
	 *            If not blank, the {@code resourceName} must match this suffix.
	 * @return {@code true} if the resource is authorized, otherwise
	 *         {@code false}.
	 */
	private static boolean isAnAuthorizedResource(String resourceName, String nameCondition, String prefixCondition,
			String suffixCondition) {

		String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);

		if (StringUtils.isBlank(nameCondition) && StringUtils.isBlank(prefixCondition)
				&& StringUtils.isBlank(suffixCondition)) {
			return true;
		}

		// if name condition is set, it's the only test on resources.
		if (StringUtils.isNotBlank(nameCondition)) {
			if (nameCondition.equalsIgnoreCase(fileName)) {
				return true;
			}
		} else {
			// otherwise prefix and suffix conditions are verified
			if (suffixCondition == null && fileName.startsWith(prefixCondition)) {
				return true;
			} else if (prefixCondition == null && fileName.endsWith(suffixCondition)) {
				return true;
			} else if (prefixCondition != null && suffixCondition != null && fileName.startsWith(prefixCondition)
					&& fileName.endsWith(suffixCondition)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>
	 * Test whether the passed {@code folderName} is authorized according to the
	 * passed list of folder names to exclude.
	 * 
	 * @param path
	 *            The folder name that must not be present in the list of
	 *            excluded folders.
	 * @param excludedFolders
	 *            List of folders to exclude.
	 * @return {@code true} if the folder is authorized, otherwise {@code false}
	 *         .
	 */
	private static boolean isAnAuthorizedFolder(String path, List<String> excludedFolders) {
		if (excludedFolders != null) {
			for (String excludedFolder : excludedFolders) {
				if (path.contains(excludedFolder)) {
					return false;
				}
			}
		}

		return true;
	}
}