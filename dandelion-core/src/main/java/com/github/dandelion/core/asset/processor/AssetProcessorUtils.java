/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetProcessorUtils {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(AssetProcessorUtils.class);

    public static AssetProcessorEntry getAssetsProcessorStarterEntry() {
        AssetProcessorEntry location = new AssetLocationProcessorEntry();
        LOG.info("Dandelion AssetsProcessor Entry '1' treat ", location.getTreatmentKey());

        AssetProcessorEntry aggregation = new AssetAggregationProcessorEntry();
        location.setNextEntry(aggregation);
        LOG.info("Dandelion AssetsProcessor Entry '2' treat ", aggregation.getTreatmentKey());

        AssetProcessorEntry compression = new AssetCompressionProcessorEntry();
        aggregation.setNextEntry(compression);
        LOG.info("Dandelion AssetsProcessor Entry '3' treat ", compression.getTreatmentKey());

        return location;
    }
}
