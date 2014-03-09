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
package com.github.dandelion.core.asset.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.processor.impl.AssetLocationProcessor;
import com.github.dandelion.core.asset.processor.spi.AssetProcessor;

/**
 * <p>
 * System in charge of discovering all implementations of
 * {@link com.github.dandelion.core.asset.processor.spi.AssetProcessor}
 * available in the classpath.
 * 
 * @author Romain Lespinasse
 * @since 0.10.0
 */
public final class AssetProcessorSystem {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(AssetProcessorSystem.class);

	private static ServiceLoader<AssetProcessor> apServiceLoader = ServiceLoader.load(AssetProcessor.class);
	private static List<AssetProcessor> processors = new ArrayList<AssetProcessor>();
	private static AssetProcessor starter;

	private static void initializeIfNeeded() {
		if (starter == null) {
			initializeAssetProcessors();
		}
	}

	synchronized private static void initializeAssetProcessors() {
		if (starter != null) {
			return;
		}

		for (AssetProcessor ape : apServiceLoader) {
			processors.add(ape);
			LOG.info("Asset processor found: {}", ape.getClass().getSimpleName());
		}

		Collections.sort(processors);

		AssetProcessor processorEntry = new AssetLocationProcessor();
		LOG.info("Dandelion Assets Processor starter treat {}", processorEntry.getProcessorKey());

		AssetProcessor lastEntry = processorEntry;
		for (AssetProcessor ape : processors) {
			lastEntry.setNextEntry(ape);
			lastEntry = ape;
			LOG.info("Assets processor entry [rank: {}, processorKey: {}]", ape.getRank(), ape.getProcessorKey());
		}

		starter = processorEntry;
	}

	public static AssetProcessor getStarter() {
		initializeIfNeeded();
		return starter;
	}

	public static Set<Asset> process(Set<Asset> assets, HttpServletRequest request) {
		return getStarter().doProcess(assets, request);
	}

	/**
	 * Prevents instantiation.
	 */
	private AssetProcessorSystem() {
	}
}
