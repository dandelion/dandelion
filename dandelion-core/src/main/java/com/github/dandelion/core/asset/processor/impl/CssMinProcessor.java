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

import java.io.Reader;
import java.io.Writer;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.processor.AbstractAssetProcessor;
import com.github.dandelion.core.asset.processor.CompatibleAssetType;
import com.github.dandelion.core.asset.processor.ProcessingContext;
import com.github.dandelion.core.asset.processor.vendor.CssCompressor;

/**
 * <p>
 * CSS processor based on the {@link CssCompressor} implementation.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@CompatibleAssetType(types = AssetType.css)
public class CssMinProcessor extends AbstractAssetProcessor {

   @Override
   public String getProcessorKey() {
      return "cssmin";
   }

   @Override
   public void doProcess(Reader reader, Writer writer, ProcessingContext processingContext) throws Exception {
      new CssCompressor(reader).compress(writer, -1);
      writer.flush();
   }
}