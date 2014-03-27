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
package com.github.dandelion.core.asset.processor.spi;

import java.io.Reader;
import java.io.Writer;

import com.github.dandelion.core.Context;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;

/**
 * <p>
 * Abstract superclass for all asset processors. Mostly used to handle exception
 * thrown by processor.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractAssetProcessor implements AssetProcessor {

	protected Context context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initProcessor(Context context) {
		this.context = context;
	}

	/**
	 * <p>
	 * Wrapper method for the actual {@link #doProcess(Asset, Reader, Writer)}
	 * method which handle exceptions.
	 */
	@Override
	public void process(Asset asset, Reader reader, Writer writer, Context context) {
		this.context = context;
		try {
			doProcess(asset, reader, writer);
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder("An exception occurred while applying the processor ");
			sb.append(getProcessorKey());
			sb.append(" on the asset ");
			sb.append(asset.toLog());
			throw new DandelionException(sb.toString(), e);
		}
	}

	protected abstract void doProcess(Asset asset, Reader reader, Writer writer) throws Exception;
}
