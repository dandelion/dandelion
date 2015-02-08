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
package com.github.dandelion.core.asset.processor.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.processor.AbstractAssetProcessor;
import com.github.dandelion.core.asset.processor.CompatibleAssetType;
import com.github.dandelion.core.asset.processor.ProcessingContext;
import com.github.dandelion.core.asset.processor.vendor.JSMin;
import com.github.dandelion.core.util.ReaderInputStream;
import com.github.dandelion.core.util.WriterOutputStream;

/**
 * <p>
 * JS processor based on the {@link JSMin} implementation.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@CompatibleAssetType(types = AssetType.js)
public class JsMinProcessor extends AbstractAssetProcessor {

   @Override
   public String getProcessorKey() {
      return "jsmin";
   }

   @Override
   public void doProcess(Reader reader, Writer writer, ProcessingContext processingContext) throws Exception {
      InputStream is = new ReaderInputStream(reader, processingContext.getContext().getConfiguration().getEncoding());
      OutputStream os = new WriterOutputStream(writer, processingContext.getContext().getConfiguration().getEncoding());
      try {
         new JSMin(is, os).jsmin();
      }
      catch (Exception e) {
         throw e;
      }
      finally {
         is.close();
         os.close();
      }
   }
}
