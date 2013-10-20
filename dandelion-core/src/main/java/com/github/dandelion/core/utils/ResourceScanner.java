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
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.dandelion.core.DevMode.devModeOverride;
import static java.lang.Thread.currentThread;

/**
 * Scanner for resources in folder
 */
public final class ResourceScanner {
    static Map<String, Set<String>> resourcesSets = new HashMap<String, Set<String>>();

    /**
     * Get the resource by this name in dandelion folder
     *
     * @param folderPath path of the resource folder
     * @param nameCondition name of the resource
     * @return the found resource
     * @throws IOException If I/O errors occur
     */
    public static String getResource(String folderPath, String nameCondition) throws IOException {
        Set<String> resources = getResources(folderPath, nameCondition, null, null);
        if (resources.isEmpty()) return null;
        return resources.toArray(new String[1])[0];
    }

    /**
     * Get resources who matches the conditions in dandelion folder
     *
     * @param folderPath path of the resource folder
     * @param prefixCondition resources prefix
     * @param suffixCondition resources suffix
     * @return the matched resources
     * @throws IOException If I/O errors occur
     */
    public static Set<String> getResources(String folderPath, String prefixCondition, String suffixCondition) throws IOException {
        return getResources(folderPath, null, prefixCondition, suffixCondition);
    }

    /**
     * Get resources who matches the conditions in dandelion folder
     *
     * @param folderPath path of the resource folder
     * @param nameCondition   name of the resource
     * @param prefixCondition resources prefix
     * @param suffixCondition resources suffix
     * @return the matched resources
     * @throws IOException If I/O errors occur
     */
    private static Set<String> getResources(String folderPath, String nameCondition, String prefixCondition, String suffixCondition) throws IOException {
        // load resources only if needed
        if (devModeOverride(resourcesSets.get(folderPath) == null)) loadResources(folderPath);
        // filter the loaded resources with conditions
        return filterResources(folderPath, nameCondition, prefixCondition, suffixCondition);
    }

    /**
     * Load resources in dandelion folder
     *
     * @param folderPath path of the resource folder
     * @throws IOException If I/O errors occur
     */
    synchronized private static void loadResources(String folderPath) throws IOException {
        if (!devModeOverride(resourcesSets.get(folderPath) == null)) return;
        resourcesSets.put(folderPath, new HashSet<String>());
        Enumeration<URL> resources = resourcesInFolder(folderPath);
        if (!resources.hasMoreElements()) return;
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            // resources extraction with file protocol
            extractResourcesOnFileSystem(folderPath, resource);
            // or jar protocol
            extractResourcesOnJarFile(folderPath, resource);
        }
    }

    /**
     * Extract resources on file system
     *
     * @param folderPath path of the resource folder
     * @param resource resource url
     */
    private static void extractResourcesOnFileSystem(String folderPath, URL resource) {
        if ("file".equals(resource.getProtocol())) {
            String resourcePath = resource.getPath();
            File folder = new File(resourcePath);
            // dandelion folder need to be ... a folder
            if (!folder.isDirectory()) return;
            File[] files = folder.listFiles();
            if(files!= null) {
                for (File file : files) {
                    if (file.canRead() && !file.isDirectory()) {
                        resourcesSets.get(folderPath).add(folderPath + File.separator + file.getName());
                    }
                }
            }
        }
    }

    /**
     * Extract resources on jar file
     *
     * @param folderPath path of the resource folder
     * @param resource resource url
     * @throws IOException If I/O errors occur
     */
    private static void extractResourcesOnJarFile(String folderPath, URL resource) throws IOException {
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
                    if (entryName.startsWith(folderPath)) {
                        resourcesSets.get(folderPath).add(entryName);
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
     * @param folderPath path of the resource folder
     * @param nameCondition   name condition
     * @param prefixCondition prefix condition
     * @param suffixCondition suffix condition
     * @return filtered resources
     */
    private static Set<String> filterResources(String folderPath, String nameCondition, String prefixCondition, String suffixCondition) {
        Set<String> _filteredResources = new HashSet<String>();
        for (String _resource : resourcesSets.get(folderPath)) {
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
     * @param folderPath path of the resource folder
     * @return all resources from the folder
     * @throws IOException If I/O errors occur
     */
    private static Enumeration<URL> resourcesInFolder(String folderPath) throws IOException {
        return currentThread().getContextClassLoader().getResources(folderPath);
    }
}
