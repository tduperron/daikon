package org.talend.logging.audit.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.logging.audit.Context;

public class DefaultContextImpl extends LinkedHashMap<String, String> implements Context {

    public DefaultContextImpl() {
        super(Collections.<String, String> emptyMap());
    }

    public DefaultContextImpl(Map<String, String> context) {
        super(context);
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}
