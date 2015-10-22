package com.leidos.xchangecore.core.infrastructure.status;

public interface StatusEventListener {

    public void handleStatusEvent(StatusEvent event);

}
