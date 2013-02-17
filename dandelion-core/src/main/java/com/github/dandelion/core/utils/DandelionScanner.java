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
 * 3. Neither the name of DataTables4j nor the names of its contributors
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.Thread.currentThread;

/**
 * Scanner for resources in Dandelion Folder
 */
public class DandelionScanner {
    static final String dandelionFolderPath = "dandelion";
    static Set<String> resourcesSet;

    /**
     * Get the resource by this name in dandelion folder
     *
     * @param nameCondition name of the resource
     * @return the found resource
     * @throws IOException If I/O errors occur
     */
    public static String getResource(String nameCondition) throws IOException {
        Set<String> resources = getResources(nameCondition, null, null);
        if (resources.isEmpty()) return null;
        return resources.toArray(new String[1])[0];
    }

    /**
     * Get resources who matches the conditions in dandelion folder
     *
     * @param prefixCondition resources prefix
     * @param suffixCondition resources suffix
     * @return the matched resources
     * @throws IOException If I/O errors occur
     */
    public static Set<String> getResources(String prefixCondition, String suffixCondition) throws IOException {
        return getResources(null, prefixCondition, suffixCondition);
    }

    /**
     * Get resources who matches the conditions in dandelion folder
     *
     * @param nameCondition   name of the resource
     * @param prefixCondition resources prefix
     * @param suffixCondition resources suffix
     * @return the matched resources
     * @throws IOException If I/O errors occur
     */
    private static Set<String> getResources(String nameCondition, String prefixCondition, String suffixCondition) throws IOException {
        // load resources only if needed
        if (resourcesSet == null) loadResources();
        // filter the loaded resources with conditions
        return filterResources(nameCondition, prefixCondition, suffixCondition);
    }

    /**
     * Load resources in dandelion folder
     *
     * @throws IOException If I/O errors occur
     */
    synchronized private static void loadResources() throws IOException {
        if (resourcesSet != null) return;
        resourcesSet = new HashSet<String>();
        Enumeration<URL> resources = resourcesInDandelionFolder();
        if (!resources.hasMoreElements()) return;
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            // resources extraction with file protocol
            extractResourcesOnFileSystem(resource);
            // or jar protocol
            extractResourcesOnJarFile(resource);
        }
    }

    /**
     * Extract resources on file system
     *
     * @param resource resource url
     */
    private static void extractResourcesOnFileSystem(URL resource) {
        if ("file".equals(resource.getProtocol())) {
            String resourcePath = resource.getPath();
            File folder = new File(resourcePath);
            // dandelion folder need to be ... a folder
            if (!folder.isDirectory()) return;

            int rootPathLength = resourcePath.substring(0, resourcePath.length()
                    - dandelionFolderPath.length()).length();
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.canRead()) {
                    if (!file.isDirectory()) {
                        resourcesSet.add(file.getAbsolutePath().substring(rootPathLength));
                    }
                }
            }
        }
    }

    /**
     * Extract resources on jar file
     *
     * @param resource resource url
     * @throws IOException If I/O errors occur
     */
    private static void extractResourcesOnJarFile(URL resource) throws IOException {
        if ("jar".equals(resource.getProtocol())) {
            URLConnection con = resource.openConnection();
            if (!(con instanceof JarURLConnection)) return;
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(false);
            JarFile jarFile = jarCon.getJarFile();
            try {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    String entryName = entries.nextElement().getName();
                    if (entryName.startsWith(dandelionFolderPath)) {
                        resourcesSet.add(entryName);
                    }
                }
            } finally {
                jarFile.close();
            }
        }
    }

    /**
     * Filter resources who match condition
     *
     * @param nameCondition   name condition
     * @param prefixCondition prefix condition
     * @param suffixCondition suffix condition
     * @return filtered resources
     */
    private static Set<String> filterResources(String nameCondition, String prefixCondition, String suffixCondition) {
        Set<String> _filteredResources = new HashSet<String>();
        for (String _resource : resourcesSet) {
            String fileName = _resource.substring(_resource.lastIndexOf("/") + 1);
            // if name condition is set, it's the only test on resources.
            if (nameCondition != null) {
                if (nameCondition.equalsIgnoreCase(fileName)) {
                    _filteredResources.add(_resource);
                }
            } else {
                // otherwise prefix and suffix conditions are verified
                if (suffixCondition == null && fileName.startsWith(prefixCondition)) {
                    _filteredResources.add(_resource);
                } else if (prefixCondition == null && fileName.endsWith(suffixCondition)) {
                    _filteredResources.add(_resource);
                } else if (prefixCondition != null && suffixCondition != null
                        && fileName.startsWith(prefixCondition) && fileName.endsWith(suffixCondition)) {
                    _filteredResources.add(_resource);
                }
            }
        }
        return _filteredResources;
    }

    /**
     * @return all resources from dandelion folder
     * @throws IOException If I/O errors occur
     */
    private static Enumeration<URL> resourcesInDandelionFolder() throws IOException {
        return currentThread().getContextClassLoader().getResources(dandelionFolderPath);
    }
}
