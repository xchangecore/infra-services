package com.leidos.xchangecore.core.infrastructure.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext
    implements NamespaceContext {

    private HashMap<String, String> namespaceMap = new HashMap<String, String>();

    public SimpleNamespaceContext(Map<String, String> namespaceMap) {

        this.namespaceMap.putAll(namespaceMap);
    }

    @Override
    public String getNamespaceURI(String prefix) {

        if (prefix == null)
            throw new NullPointerException("null prefix");

        return this.namespaceMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {

        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<?> getPrefixes(String namespaceURI) {

        throw new UnsupportedOperationException();
    }

}
