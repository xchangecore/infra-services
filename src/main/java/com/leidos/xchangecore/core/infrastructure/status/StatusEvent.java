package com.leidos.xchangecore.core.infrastructure.status;

import java.util.Date;

public class StatusEvent {

    private String componentId;

    private String event;

    private String message;

    private Date timestamp;

    public StatusEvent() {

    }

    public StatusEvent(String componentId, String event, String message, Date timestamp) {

        this.componentId = componentId;
        this.event = event;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getComponentId() {

        return componentId;
    }

    public String getEvent() {

        return event;
    }

    public String getMessage() {

        return message;
    }

    public Date getTimestamp() {

        return timestamp;
    }

    public void setComponentId(String componentId) {

        this.componentId = componentId;
    }

    public void setEvent(String event) {

        this.event = event;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public void setTimestamp(Date timestamp) {

        this.timestamp = timestamp;
    }

}
