package com.leidos.xchangecore.core.infrastructure.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkProductXsltType {

    private List<String> types;

    private Map<String, Map<String, String>> xsltMap;

    public WorkProductXsltType() {

    }

    public List<String> getTypes() {

        return types;
    }

    public Map<String, Map<String, String>> getXsltMap() {

        return xsltMap;
    }

    public void setTypes(List<String> types) {

        this.types = types;
        this.xsltMap = new HashMap<String, Map<String, String>>();
        for (String type : types) {
            this.xsltMap.put(type, new HashMap<String, String>());
        }
    }

    public void setXsltMap(Map<String, Map<String, String>> xsltMap) {

        this.xsltMap = xsltMap;
    }

}
