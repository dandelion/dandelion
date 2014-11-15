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
package com.github.dandelion.core.web.handler.impl;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.monitoring.GraphViewer;
import com.github.dandelion.core.web.WebConstants;
import com.github.dandelion.core.web.handler.AbstractRequestHandler;
import com.github.dandelion.core.web.handler.RequestHandlerContext;

/**
 * <p>
 * Post-filtering request handler intended to display the debugger when
 * requested by the user using the following request parameter:
 * {@code ddl-debug}.
 * </p>
 * <p>
 * If so, the {@link HttpServletResponse} is simply overriden with a new page
 * containing the debugger.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.11.0
 */
public class DebuggerPostHandler extends AbstractRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DebuggerPostHandler.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	public boolean isAfterChaining() {
		return true;
	}

	@Override
	public int getRank() {
		return 1;
	}

	@Override
	public boolean isApplicable(RequestHandlerContext context) {
		return context.getContext().getConfiguration().isToolBundleGraphEnabled()
				&& context.getRequest().getParameter(WebConstants.DANDELION_DEBUGGER) != null;
	}

	@Override
	public byte[] handle(RequestHandlerContext context, byte[] response) {

		byte[] newResponse;

		try {
			GraphViewer graphViewer = new GraphViewer(context.getContext());

			String responseAsString = graphViewer.getView(context.getRequest(), context.getResponse());
			newResponse = responseAsString.getBytes(context.getContext().getConfiguration().getEncoding());
		}
		catch (Exception e) {
			throw new DandelionException("An error occured when generating the debug page.", e);
		}

		// The response is overriden with a new one containing the debug page
		return newResponse;
	}
}
