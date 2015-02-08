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
 * Collection of utilities to ease detecting whether some libraries are
 * available or not.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public final class LibraryDetector {

   private static Boolean jstlAvailable;
   private static Boolean thymeleafAvailable;
   private static Boolean jbossVFSv2Available;
   private static Boolean jbossVFSv3Available;

   /**
    * @return {@code true} if the JSTL is present in the classpath, otherwise
    *         {@code false}.
    */
   public static boolean isJstlAvailable() {
      if (jstlAvailable == null) {
         jstlAvailable = ClassUtils.isPresent("javax.servlet.jsp.jstl.core.Config");
      }

      return jstlAvailable;
   }

   /**
    * @return {@code true} if Thymeleaf is present in the classpath, otherwise
    *         {@code false}.
    */
   public static boolean isThymeleafAvailable() {
      if (thymeleafAvailable == null) {
         thymeleafAvailable = ClassUtils.isPresent("org.thymeleaf.TemplateEngine");
      }

      return thymeleafAvailable;
   }

   /**
    * @return {@code true} if the JBoss VFS2 infrastructure is present in the
    *         classpath, otherwise {@code false}.
    */
   public static boolean isJBossVFS2Available() {
      if (jbossVFSv2Available == null) {
         jbossVFSv2Available = ClassUtils.isPresent("org.jboss.virtual.VFS");
      }

      return jbossVFSv2Available;
   }

   /**
    * @return {@code true} if the JBoss VFS3 infrastructure is present in the
    *         classpath, otherwise {@code false}.
    */
   public static boolean isJBossVFS3Available() {
      if (jbossVFSv3Available == null) {
         jbossVFSv3Available = ClassUtils.isPresent("org.jboss.vfs.VFS");
      }

      return jbossVFSv3Available;
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private LibraryDetector() {
      throw new AssertionError();
   }
}
