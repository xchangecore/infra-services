package com.leidos.xchangecore.core.infrastructure.status;

import org.apache.log4j.spi.LoggingEvent;

public interface StatusEventMonitor {

    public void notifyOnEvent(StatusEvent event);

    public void doPost(LoggingEvent event);

    public void addListener(StatusEventListener listener);

    public void removeListener(StatusEventListener listener);

}
