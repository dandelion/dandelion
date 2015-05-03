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
package com.github.dandelion.core.web.handler;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Handler in charge of preprocessing requests or postprocessing server
 * responses.
 * </p>
 * <p>
 * Handlers are designed using a slightly modified version of the chain of
 * responsibility pattern:
 * </p>
 * 
 * <ul>
 * <li>All handlers are grouped in two chains depending on the
 * {@link #isAfterChaining()} method. In other words, a first chain is in charge
 * of preprocessing requests and another one is in charge of postprocessing
 * responses</li>
 * <li>All handlers are assembled into a chain using their {@link #getRank()}
 * method</li>
 * <li>Each handler is executed only if the
 * {@link #isApplicable(HandlerContext)} method returns {@code true}</li>
 * <li>Each handler can decide to stop the chain depending on the return value
 * of the {@link #doHandle(HandlerContext)} method</li>
 * </ul>
 * <p>
 * If you wish to add a custom handler, you should prefer extending the
 * {@link AbstractHandlerChain} instead of implementing this interface.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public interface HandlerChain extends Comparable<HandlerChain> {

   /**
    * @return {@code true} if the handler should be executed after the call of
    *         the
    *         {@link FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
    *         method, {@code false} otherwise.
    */
   boolean isAfterChaining();

   /**
    * @return the rank of the handler in a chain. A low rank indicates that the
    *         handler will be executed first.
    */
   int getRank();

   /**
    * <p>
    * Whether the handler is applicable in the provided context or not.
    * </p>
    * 
    * @param context
    *           The wrapper object holding the context in which a
    *           {@link HttpServletRequest} is preprocessed or a
    *           {@link HttpServletResponse} is postprocessed.
    * @return {@code true} if the handler can be executed in the context,
    *         otherwise {@code false}.
    */
   boolean isApplicable(HandlerContext context);

   /**
    * <p>
    * Sets the next handler to be executed in the chain of responsibility.
    * </p>
    * 
    * @param nextHandler
    *           The next handler to be executed.
    */
   void setNext(HandlerChain nextHandler);

   /**
    * <p>
    * Start point of the chain. Handlers are executed depending on the
    * {@link #isApplicable(HandlerContext)} method and can decide whether the
    * next handler can be executed or not.
    * </p>
    * 
    * @param context
    *           The context in which the handler is executed.
    */
   void doHandle(HandlerContext context);
}
