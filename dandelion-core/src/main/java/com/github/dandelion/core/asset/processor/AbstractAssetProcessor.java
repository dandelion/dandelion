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
package com.github.dandelion.core.asset.processor;

import java.io.Reader;
import java.io.Writer;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;

/**
 * <p>
 * Abstract superclass for all asset processors. Mostly used to handle
 * exceptions thrown by any of the active processors.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public abstract class AbstractAssetProcessor implements AssetProcessor {

   /**
    * <p>
    * Wrapper method for the actual
    * {@link #doProcess(Reader, Writer, ProcessingContext)} method which handle
    * exceptions.
    * </p>
    */
   @Override
   public void process(Reader reader, Writer writer, ProcessingContext processingContext) throws DandelionException {

      try {
         doProcess(reader, writer, processingContext);
      }
      catch (Exception e) {
         StringBuilder sb = new StringBuilder("An exception occurred while applying the processor ");
         sb.append(getProcessorKey());
         sb.append(" on the asset ");
         sb.append(processingContext.getAsset().toLog());
         throw new DandelionException(sb.toString(), e);
      }
   }

   /**
    * <p>
    * Performs the processing of the {@link Asset} stored in the given
    * {@link ProcessingContext} by reading its content from the given
    * {@link Reader} and writing the new content to the {@link Writer}.
    * </p>
    * 
    * @param reader
    *           The reader containig the content to process.
    * @param writer
    *           The destination writer.
    * @param processingContext
    *           The processing context that includes the {@link Asset} to be
    *           processed.
    * @throws Exception
    *            if something goes wrong during the processing of the asset.
    */
   protected abstract void doProcess(Reader reader, Writer writer, ProcessingContext processingContext)
         throws Exception;
}
