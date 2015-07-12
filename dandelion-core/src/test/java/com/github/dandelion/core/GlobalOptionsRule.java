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
package com.github.dandelion.core;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.dandelion.core.config.DandelionConfig;

/**
 * <p>
 * Custom {@link TestRule} used to initialize global Dandelion options.
 * </p>
 * <p>
 * Usage: put this in a test class to activate the rule
 * </p>
 * 
 * <pre>
 * &#064;Rule
 * public GlobalOptionsRule options = new GlobalOptionsRule();
 * </pre>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 */
public class GlobalOptionsRule implements TestRule {

   public Statement apply(Statement base, Description description) {
      return statement(base);
   }

   private Statement statement(final Statement base) {
      return new Statement() {
         @Override
         public void evaluate() throws Throwable {
            before();
            try {
               base.evaluate();
            }
            finally {
               after();
            }
         }
      };
   }

   /**
    * Override to set up your specific external resource.
    *
    * @throws if
    *            setup fails (which will disable {@code after}
    */
   protected void before() throws Throwable {
      System.setProperty(DandelionConfig.BUNDLE_PRE_LOADERS.getName(), "false");
   }

   /**
    * Override to tear down your specific external resource.
    */
   protected void after() {
      System.clearProperty(DandelionConfig.BUNDLE_PRE_LOADERS.getName());
   }

}
