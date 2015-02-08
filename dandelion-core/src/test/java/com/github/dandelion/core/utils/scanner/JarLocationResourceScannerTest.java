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
package com.github.dandelion.core.utils.scanner;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.dandelion.core.util.scanner.JarLocationResourceScanner;

import static org.assertj.core.api.Assertions.assertThat;

public class JarLocationResourceScannerTest {

   private JarLocationResourceScanner scanner = new JarLocationResourceScanner();

   @Test
   public void should_return_scanned_resources_from_jar() throws IOException {

      Enumeration<URL> urls = Thread.currentThread().getContextClassLoader()
            .getResources("scanning-jar/test-1.0.0.jar");
      Set<String> resourcePaths = new HashSet<String>();

      while (urls.hasMoreElements()) {
         URL url = urls.nextElement();
         resourcePaths.addAll(scanner.findResourcePaths("dandelion", url));
      }

      assertThat(resourcePaths).contains("dandelion/bundle1.json", "dandelion/bundle2.json", "dandelion/");
   }

   @Test
   public void should_return_nothing_when_scanning_in_an_unexisting_folder_in_jar() throws IOException {

      Enumeration<URL> urls = Thread.currentThread().getContextClassLoader()
            .getResources("scanning-jar/test-1.0.0.jar");
      Set<String> resourcePaths = new HashSet<String>();

      while (urls.hasMoreElements()) {
         URL url = urls.nextElement();
         resourcePaths.addAll(scanner.findResourcePaths("unexisting-folder", url));
      }

      assertThat(resourcePaths).isEmpty();
   }
}
