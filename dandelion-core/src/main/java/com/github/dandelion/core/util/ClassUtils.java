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
package com.github.dandelion.core.util;

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

   /**
    * <p>
    * Return the default ClassLoader to use: typically the thread context
    * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
    * class will be used as fallback.
    * </p>
    * <p>
    * Call this method if you intend to use the thread context ClassLoader in a
    * scenario where you clearly prefer a non-null ClassLoader reference: for
    * example, for class path resource loading (but not necessarily for
    * {@code Class.forName}, which accepts a {@code null} ClassLoader reference
    * as well).
    * </p>
    * 
    * @return the default ClassLoader (only {@code null} if even the system
    *         ClassLoader isn't accessible)
    * @see Thread#getContextClassLoader()
    * @see ClassLoader#getSystemClassLoader()
    */
   public static ClassLoader getDefaultClassLoader() {
      ClassLoader cl = null;
      try {
         cl = Thread.currentThread().getContextClassLoader();
      }
      catch (Throwable ex) {
         // Cannot access thread context ClassLoader - falling back...
      }
      if (cl == null) {
         // No thread context class loader -> use class loader of this class.
         cl = ClassUtils.class.getClassLoader();
         if (cl == null) {
            // getClassLoader() returning null indicates the bootstrap
            // ClassLoader
            try {
               cl = ClassLoader.getSystemClassLoader();
            }
            catch (Throwable ex) {
               // Cannot access system ClassLoader - oh well, maybe the caller
               // can live with null...
            }
         }
      }
      return cl;
   }
}