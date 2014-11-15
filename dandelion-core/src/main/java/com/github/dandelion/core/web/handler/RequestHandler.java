/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
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

/**
 * <p>
 * Interface for all handlers intended to process requests and/or responses.
 * </p>
 * <p>
 * It is worth noting that:
 * </p>
 * <ul>
 * <li>
 * The request/response processing can occur before the invokation of the
 * {@link FilterChain} or after, depending on the value returned by the
 * {@link #isAfterChaining()} method.</li>
 * <li>
 * If multiple request handlers are configured, they will be executed in a
 * particular order, depending on the value returned by the {@link #getRank()}
 * method. A handler with a rank set to 1 will be executed before another
 * handler with a rank set to 2.</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public interface RequestHandler extends Comparable<RequestHandler> {

	/**
	 * <p>
	 * Whether the handler is applicable in this context or not.
	 * </p>
	 * 
	 * @param context
	 *            The wrapper object holding the context.
	 * @return {@code true} if the handler can be executed in the context,
	 *         otherwise {@code false}.
	 */
	boolean isApplicable(RequestHandlerContext context);

	/**
	 * @return {@code true} if the request handler should be executed after the
	 *         call of the
	 *         {@link FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
	 *         method, {@code false} otherwise.
	 */
	boolean isAfterChaining();

	/**
	 * @return the rank of the handler.
	 */
	int getRank();

	/**
	 * <p>
	 * TODO
	 * </p>
	 * 
	 * @param context
	 * @param response
	 * @return
	 */
	byte[] handle(RequestHandlerContext context, byte[] response);
}
