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
package com.github.dandelion.core.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * Collection of utilities to ease working with reflection.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class ClassUtils {

   /**
    * <p>
    * Get a Java class from its name.
    * 
    * @param className
    *           The name of the class to load.
    * @return The loaded class.
    * @throws ClassNotFoundException
    *            if none of the ClassLoaders is able to found the class.
    */
   public static Class<?> getClass(String className) throws ClassNotFoundException {
      try {
         // trying with the default ClassLoader
         return Class.forName(className);
      }
      catch (ClassNotFoundException cnfe) {
         // trying with thread ClassLoader
         Thread thread = Thread.currentThread();
         ClassLoader threadClassLoader = thread.getContextClassLoader();
         return Class.forName(className, false, threadClassLoader);
      }
   }

   /**
    * Instantiates a class and returns the instance.
    * 
    * @param klass
    *           The class to instantiate.
    * @return a new instance of the given class.
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
   public static Object getNewInstance(Class<?> klass) throws InstantiationException, IllegalAccessException {
      return klass.newInstance();
   }

   /**
    * Determine whether the {@link Class} identified by the supplied name is
    * present and can be loaded. Will return {@code false} if either the class
    * or one of its dependencies is not present or cannot be loaded.
    * 
    * @param className
    *           The fully qualified name of the class to check.
    * @return whether the specified class is present or not.
    */
   public static boolean isPresent(String className) {
      try {
         Class.forName(className);
         return true;
      }
      catch (Throwable ex) {
         // Class or one of its dependencies is not present...
         return false;
      }
   }

   public static List<Class<?>> getSubClassesInPackage(String packageName, Class<?> superClass)
         throws ClassNotFoundException, IOException {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      String path = packageName.replace('.', '/');
      Enumeration<URL> resources = classLoader.getResources(path);
      List<String> dirs = new ArrayList<String>();
      while (resources.hasMoreElements()) {
         URL resource = resources.nextElement();
         dirs.add(URLDecoder.decode(resource.getFile(), "UTF-8"));
      }

      TreeSet<String> classes = new TreeSet<String>();
      for (String directory : dirs) {
         classes.addAll(findClasses(directory, packageName));
      }

      ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
      for (String clazz : classes) {
         Class<?> clazzz = Class.forName(clazz);
         if (superClass.isAssignableFrom(clazzz)) {
            classList.add(clazzz);
         }
      }
      return classList;
   }

   public static List<Class<?>> getAllClassesInPackage(String packageName) throws ClassNotFoundException, IOException {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      Enumeration<URL> resources = classLoader.getResources(path);
      List<String> dirs = new ArrayList<String>();
      while (resources.hasMoreElements()) {
         URL resource = resources.nextElement();
         dirs.add(URLDecoder.decode(resource.getFile(), "UTF-8"));
      }
      TreeSet<String> classes = new TreeSet<String>();
      for (String directory : dirs) {
         classes.addAll(findClasses(directory, packageName));
      }
      ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
      for (String clazz : classes) {
         classList.add(Class.forName(clazz));
      }
      return classList;
   }

   private static TreeSet<String> findClasses(String path, String packageName) throws MalformedURLException,
         IOException {
      TreeSet<String> classes = new TreeSet<String>();
      if (path.startsWith("file:") && path.contains("!")) {
         String[] split = path.split("!");
         URL jar = new URL(split[0]);
         ZipInputStream zip = new ZipInputStream(jar.openStream());
         ZipEntry entry;
         while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
               String className = entry.getName().replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
               if (className.startsWith(packageName)) {
                  classes.add(className);
               }
            }
         }
      }
      File dir = new File(path);
      if (!dir.exists()) {
         return classes;
      }
      File[] files = dir.listFiles();
      if (files == null) {
         return classes;
      }
      for (File file : files) {
         if (file.isDirectory()) {
            classes.addAll(findClasses(file.getAbsolutePath(), packageName + "." + file.getName()));
         }
         else if (file.getName().endsWith(".class")) {
            // Build the class name with the package name and the file after
            // removing the .class extension
            String className = packageName + '.'
                  + file.getName().substring(0, file.getName().length() - ".class".length());
            classes.add(className);
         }
      }
      return classes;
   }
}
