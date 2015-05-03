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

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.util.scanner.UrlResolver;

/**
 * <p>
 * Resolves JBoss VFS3 URLs into standard {@link URL}s using reflection.
 * </p>
 * <p>
 * Ensures compatibility with <b>JBoss AS 6+</b> and <b>WildFly 8</b>.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public class JBossVFS3UrlResolver implements UrlResolver {

   private static final String VFS3_PACKAGE = "org.jboss.vfs.";

   private static Method VFS_METHOD_GET_CHILD;
   private static Method VIRTUALFILE_METHOD_TO_URL;

   static {
      try {
         Class<?> vfsClass = Class.forName(VFS3_PACKAGE + "VFS");
         Class<?> virtualFileClass = Class.forName(VFS3_PACKAGE + "VirtualFile");

         VFS_METHOD_GET_CHILD = vfsClass.getMethod("getChild", URL.class);
         VIRTUALFILE_METHOD_TO_URL = virtualFileClass.getMethod("toURL");
      }
      catch (Exception e) {
         StringBuilder message = new StringBuilder("Could not detect JBoss VFS3 classes");
         throw new DandelionException(message.toString(), e);
      }
   }

   public URL toStandardUrl(URL url) throws IOException {

      URL standardUrl = null;
      try {
         Object child = VFS_METHOD_GET_CHILD.invoke(null, url);
         standardUrl = (URL) VIRTUALFILE_METHOD_TO_URL.invoke(child);
      }
      catch (Exception e) {
         StringBuilder error = new StringBuilder("Unable to resolve the URL \"");
         error.append(url.getPath());
         error.append("\" using JBoss VFS2 classes");
         throw new DandelionException(error.toString(), e);
      }
      return standardUrl;
   }
}
