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

package com.github.dandelion.core.asset.web;

import com.github.dandelion.core.asset.Asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean to store parameters/values for an usage when the assets are rendered.
 */
public class AssetParameters {
    private Map<String, Map<String, Object>> parameters;
    private Map<String, List<String>> groupIds;

    public AssetParameters() {
        this.parameters = new HashMap<String, Map<String, Object>>();
        this.groupIds = new HashMap<String, List<String>>();
    }

    public List<String> getGroupIds(Asset asset) {
        return groupIds.get(asset.getName());
    }

    public Map<String, Object> getParameters(Asset asset, String groupId) {
        return parameters.get(groupId + "|" + asset.getName());
    }

    public void add(String assetName, String parameter, Object value, String groupId) {
        // group
        if (!groupIds.containsKey(assetName)) {
            groupIds.put(assetName, new ArrayList<String>());
        }
        groupIds.get(assetName).add(groupId);

        // parameter/value
        String key = groupId + "|" + assetName;
        if(!parameters.containsKey(key)) {
            parameters.put(key, new HashMap<String, Object>());
        }
        parameters.get(key).put(parameter, value);
    }
}
