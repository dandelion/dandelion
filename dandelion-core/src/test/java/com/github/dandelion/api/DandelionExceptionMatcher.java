package com.github.dandelion.api;

import com.github.dandelion.core.api.DandelionError;
import com.github.dandelion.core.api.DandelionException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.HashMap;
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
            if(!entry.getValue().equals(e.get(entry.getKey()))) return false;
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
