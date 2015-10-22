package com.leidos.xchangecore.core.infrastructure.messages;

import java.util.HashMap;
import java.util.Map;

public class CoreRosterMessage {

    public Map<String, String> coreStatusMap = new HashMap<String, String>();

    public Map<String, String> getCoreStatusMap() {

        return coreStatusMap;
    }

    public void setCoreStatusMap(Map<String, String> coreStatusMap) {

        this.coreStatusMap = coreStatusMap;
    }

    public CoreRosterMessage(Map<String, String> coreStatusMap) {

        setCoreStatusMap(coreStatusMap);
    }
}
