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
package com.github.dandelion.core.asset.processor.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.DandelionException;
import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.processor.CompatibleAssetType;
import com.github.dandelion.core.asset.processor.spi.AbstractAssetProcessor;
import com.github.dandelion.core.utils.StringUtils;

/**
 * <p>
 * Processes all relative paths in the given content, line by line and replace
 * it with an absolute path.
 * <p>
 * The given url modified according to the number of occurrences of ".." counted
 * in the line.
 * 
 * <p>
 * For example, if the CSS file is loaded from:
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
 *            The content of the asset to process.
 * @param url
 *            The original URL that will be used to build the absolute path.
 * @return the processed content that should contain only absolute paths.
 */
@CompatibleAssetType(types = AssetType.css)
public class CssUrlRewritingProcessor extends AbstractAssetProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(CssUrlRewritingProcessor.class);
	private Pattern pattern = Pattern.compile("url\\((.*)\\)", Pattern.CASE_INSENSITIVE);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProcessorKey() {
		return "cssUrlRewriting";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doProcess(Asset asset, Reader reader, Writer writer) throws Exception {

		BufferedReader bufferedReader = new BufferedReader(reader);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);

		try {
			String line = null;

			while ((line = bufferedReader.readLine()) != null) {

				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {

					int lvl = StringUtils.countMatches(line, "..");

					if (lvl > 0) {
						String tmp2 = null;
						int c = lvl + 1;
						for (int i = asset.getFinalLocation().length() - 1; i >= 0; i--) {
							if (c > 0 && asset.getFinalLocation().charAt(i) == '/') {
								tmp2 = asset.getFinalLocation().substring(0, i + 1);
								c--;
							}

							if (c == 0) {
								break;
							}
						}
						line = line.replaceAll("(\\.\\./)+", tmp2);
					}
				}
				bufferedWriter.write(line + '\n');
			}
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
			try {
				if (bufferedWriter != null)
					bufferedWriter.close();
			}
			catch (IOException e) {
				// Should never happen
				LOG.error("An error occurred when processing relative paths inside the asset " + asset.toLog());
				throw DandelionException.wrap(e);
			}
		}

	}
}
