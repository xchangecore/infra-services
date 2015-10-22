package com.leidos.xchangecore.core.infrastructure.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class MutableNamespaceContext
    implements NamespaceContext {

    private Map<String, String> mapping = new HashMap<String, String>();

    public String getNamespaceURI(String prefix) {

        String namespaceURI = mapping.get(prefix);
        return namespaceURI;
    }

    public String getPrefix(String namespaceURI) {

        String result = null;
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (entry.getValue().equals(namespaceURI)) {
                result = entry.getKey();
            }
        }
        return result;
    }

    public Iterator<?> getPrefixes(String namespaceURI) {

        return mapping.keySet().iterator();
    }

    public void put(String prefix, String namespaceURI) {

        mapping.put(prefix, namespaceURI);
    }
}
