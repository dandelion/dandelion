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
package com.github.dandelion.core.option;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.DandelionException;

/**
 * <p>
 * Super interface for all {@link Option} processors.
 * </p>
 * <p>
 * Basically, a processor processes a String value, converts it into the typed
 * value and potentially performs other things.
 * </p>
 * <p>
 * Some processors accept a special syntax allowing to load one or more
 * Dandelion bundles to the current {@link HttpServletRequest}. This syntax will
 * be processed only if {@link #isBundleGraphUpdatable()} returns {@code true}.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.1.0
 */
public interface OptionProcessor {

   /**
    * <p>
    * Processes the {@code Entry<Option<?>, Object>} contained in the passed
    * {@link OptionProcessingContext}, using the processor associated with the
    * {@link Option}.
    * </p>
    * 
    * @param processingContext
    *           Holder for all information needed during the option processing.
    * @throws DandelionException
    *            if something goes wrong during the processing of the option
    *            value.
    */
   public void process(OptionProcessingContext processingContext);

   /**
    * @return {@code true} if the option being processed can update the bundle
    *         graph, {@code false} otherwise.
    */
   public boolean isBundleGraphUpdatable();
}
