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

package com.github.dandelion.core.asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean to store parameters/values for an usage when the assets are rendered.
 */
public class AssetsTemplateParameters {
    private List<String> assetsTemplates;
    private Map<String, Map<String, String>> assetsParameters;
    private Map<String, List<String>> groupIds;

    public AssetsTemplateParameters() {
        this.assetsTemplates = new ArrayList<String>();
        this.assetsParameters = new HashMap<String, Map<String, String>>();
        this.groupIds = new HashMap<String, List<String>>();
    }

    public boolean isTemplate(Asset asset) {
        return assetsTemplates.contains(asset.getName());
    }

    public List<String> getGroupIds(Asset asset) {
        return groupIds.get(asset.getName());
    }

    public Map<String, String> getParameters(Asset asset, String groupId) {
        return assetsParameters.get(groupId + "|" + asset.getName());
    }

    public void addTemplateParameter(String assetName, String parameter, String value, String groupId) {
        // name
        assetsTemplates.add(assetName);

        // group
        if (!groupIds.containsKey(assetName)) {
            groupIds.put(assetName, new ArrayList<String>());
        }
        groupIds.get(assetName).add(groupId);

        // parameter/value
        String key = groupId + "|" + assetName;
        if(!assetsParameters.containsKey(key)) {
            assetsParameters.put(key, new HashMap<String, String>());
        }
        assetsParameters.get(key).put(parameter, value);
    }
}
