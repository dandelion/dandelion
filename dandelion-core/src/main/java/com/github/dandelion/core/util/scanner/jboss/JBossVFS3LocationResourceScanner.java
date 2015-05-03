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
package com.github.dandelion.core.util.scanner.jboss;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.PathUtils;
import com.github.dandelion.core.util.scanner.LocationResourceScanner;

/**
 * <p>
 * ClassPathLocationScanner for JBoss VFS v3.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class JBossVFS3LocationResourceScanner implements LocationResourceScanner {

   private static final String VFS3_PACKAGE = "org.jboss.vfs.";

   private static Class<?> VFS_CLASS;
   private static Method VFS_METHOD_GET_CHILD;
   private static Method VIRTUALFILE_METHOD_GET_CHILDREN_RECURSIVELY;
   private static Method VIRTUALFILE_METHOD_GET_PATH_NAME;

   static {
      try {
         VFS_CLASS = Class.forName(VFS3_PACKAGE + "VFS");
         Class<?> virtualFileClass = Class.forName(VFS3_PACKAGE + "VirtualFile");

         VFS_METHOD_GET_CHILD = VFS_CLASS.getMethod("getChild", String.class);
         VIRTUALFILE_METHOD_GET_CHILDREN_RECURSIVELY = virtualFileClass.getMethod("getChildrenRecursively");
         VIRTUALFILE_METHOD_GET_PATH_NAME = virtualFileClass.getMethod("getPathName");

      }
      catch (Exception e) {
         StringBuilder message = new StringBuilder("Could not detect JBoss VFS3 classes");
         throw new DandelionException(message.toString(), e);
      }
   }

   public Set<String> findResourcePaths(String location, URL resourceUrl) throws IOException {

      Set<String> resourceNames = null;

      try {
         String filePath = PathUtils.toFilePath(resourceUrl);

         String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
         if (!classPathRootOnDisk.endsWith("/")) {
            classPathRootOnDisk = classPathRootOnDisk + "/";
         }

         resourceNames = new TreeSet<String>();

         Object root = VFS_METHOD_GET_CHILD.invoke(VFS_CLASS, filePath);

         // Everything is retrieved: folder and files
         List<?> children = (List<?>) VIRTUALFILE_METHOD_GET_CHILDREN_RECURSIVELY.invoke(root);

         for (Object file : children) {
            String pathName = (String) VIRTUALFILE_METHOD_GET_PATH_NAME.invoke(file);
            resourceNames.add(pathName.substring(classPathRootOnDisk.length()));
         }

      }
      catch (Exception e) {
         throw new DandelionException("JBoss VFS v3 call failed", e);
      }
      return resourceNames;
   }
}
