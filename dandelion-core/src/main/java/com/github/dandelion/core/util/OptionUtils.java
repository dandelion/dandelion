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

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.option.DefaultOptionProcessingContext;
import com.github.dandelion.core.option.Option;
import com.github.dandelion.core.option.OptionProcessingContext;
import com.github.dandelion.core.option.OptionProcessor;

/**
 * <p>
 * Utilities for dealing with {@link Option}s.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 */
public final class OptionUtils {

   protected static Logger logger = LoggerFactory.getLogger(OptionUtils.class);

   public static final String DDL_DT_REQUESTATTR_TABLES = "ddl-dt-tables";

   /**
    * <p>
    * Processed the passed {@code finalOptions} map with the corresponding
    * {@link OptionProcessor}. Once processed, the entry is updated with the
    * processed value, most of the time a typed value.
    * </p>
    * 
    * @param finalOptions
    *           The map of option whose values are to be processed.
    * @param request
    *           The current request.
    */
   public static void processOptions(Map<Option<?>, Object> finalOptions, HttpServletRequest request) {

      if (finalOptions != null) {

         for (Entry<Option<?>, Object> entry : finalOptions.entrySet()) {
            OptionProcessor optionProcessor = entry.getKey().getProcessor();
            OptionProcessingContext pc = new DefaultOptionProcessingContext(entry, request,
                  optionProcessor.isBundleGraphUpdatable());
            optionProcessor.process(pc);
         }
      }
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private OptionUtils() {
      throw new AssertionError();
   }
}