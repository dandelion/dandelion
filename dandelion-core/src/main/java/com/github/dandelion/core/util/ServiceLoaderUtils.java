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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * <p>
 * Collection of utilities to ease working with {@link ServiceLoader}.
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class ServiceLoaderUtils {

   /**
    * <p>
    * Loads all available providers for the passed SPI (Service Provider
    * Interface) and returns them as an {@link ArrayList}.
    * 
    * @param spi
    *           The service provider interface used to load existing
    *           implementations.
    * @return a list of providers.
    */
   public static <S> List<S> getProvidersAsList(Class<S> spi) {

      Validate.notNull(spi, "The SPI (Service Provider Interface) must be specified");

      ServiceLoader<S> services = ServiceLoader.load(spi);
      List<S> retval = new ArrayList<S>();
      for (S service : services) {
         retval.add(service);
      }
      return retval;
   }

   /**
    * Prevents instantiation.
    */
   private ServiceLoaderUtils() {
   }
}
