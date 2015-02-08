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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.Beta;
import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.processor.AbstractAssetProcessor;
import com.github.dandelion.core.asset.processor.CompatibleAssetType;
import com.github.dandelion.core.asset.processor.ProcessingContext;
import com.github.dandelion.core.asset.processor.support.CssUrlRewriter;
import com.github.dandelion.core.util.StringBuilderUtils;

/**
 * <p>
 * Processes all relative paths in the given content, line by line and replace
 * it with an absolute path.
 * </p>
 * <p>
 * The given url modified according to the number of occurrences of ".." counted
 * in the line.
 * </p>
 * <p>
 * For example, if the CSS file is loaded from:
 * </p>
 * 
 * <pre>
 * http://cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/css/jquery.dataTables.css
 * </pre>
 * 
 * and if it contains:
 * 
 * <pre>
 * .paginate_enabled_previous { background: url('../images/back_enabled.png') no-repeat top left; }
 * </pre>
 * 
 * The line will be replaced by:
 * 
 * <pre>
 * .paginate_enabled_previous { background: url('http://cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/images/back_enabled.png') no-repeat top left; }
 * </pre>
 * 
 * @param content
 *           The content of the asset to process.
 * @param url
 *           The original URL that will be used to build the absolute path.
 * @return the processed content that should contain only absolute paths.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
@Beta
@CompatibleAssetType(types = AssetType.css)
public class CssUrlRewritingProcessor extends AbstractAssetProcessor {

   private static final Logger LOG = LoggerFactory.getLogger(CssUrlRewritingProcessor.class);
   private CssUrlRewriter urlRewriter = new CssUrlRewriter();

   @Override
   public String getProcessorKey() {
      return "cssurlrewriting";
   }

   @Override
   protected void doProcess(Reader reader, Writer writer, ProcessingContext processingContext) throws Exception {
      Asset asset = processingContext.getAsset();
      String contextPath = processingContext.getRequest().getContextPath();
      LOG.debug("Processing {}", asset.toLog());
      urlRewriter.setContextPath(contextPath);

      StringBuilder assetContent = StringBuilderUtils.toStringBuilder(reader);

      BufferedWriter bufferedWriter = null;
      try {
         LOG.debug("  Old location: \"{}\"", asset.getConfigLocation());
         LOG.debug("  New location: \"{}\"", asset.getFinalLocation());

         bufferedWriter = new BufferedWriter(writer);
         bufferedWriter.write(urlRewriter.rewriteUrl("/" + contextPath + asset.getConfigLocation(),
               asset.getFinalLocation(), assetContent.toString()).toString());
      }
      catch (IOException e) {
         LOG.error("An error occurred when processing relative paths inside the asset " + asset.toLog());
         throw DandelionException.wrap(e);
      }
      finally {
         try {
            if (reader != null)
               reader.close();
         }
         catch (IOException e) {
            // Should never happen
            LOG.error("An error occurred when processing relative paths inside the asset " + asset.toLog());
            throw DandelionException.wrap(e);
         }

         // Flush and closes the stream
         bufferedWriter.close();
      }
   }
}
