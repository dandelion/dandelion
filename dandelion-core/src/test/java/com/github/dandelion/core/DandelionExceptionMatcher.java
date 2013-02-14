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
 * 3. Neither the name of DataTables4j nor the names of its contributors
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
package com.github.dandelion.core;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Matcher for test {@link DandelionException}
 */
public class DandelionExceptionMatcher extends BaseMatcher {
    DandelionError error;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public DandelionExceptionMatcher(DandelionError error) {
        this.error = error;
    }

    @Override
    public boolean matches(Object o) {
        if(!(o instanceof DandelionException)) return false;
        DandelionException e = DandelionException.class.cast(o);
        if(e.getErrorCode() != error) return false;
        for(Map.Entry<String,Object> entry:parameters.entrySet()) {
            if(!entry.getValue().equals(e.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("getErrorCode should return ").appendValue(error);
        for(Map.Entry<String,Object> entry:parameters.entrySet()) {
            description.appendText(", get('").appendText(entry.getKey())
                    .appendText("') should return ").appendValue(entry.getValue());
        }
    }

    public DandelionExceptionMatcher set(String field, Object value) {
        parameters.put(field, value);
        return this;
    }
}
