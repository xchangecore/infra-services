package com.leidos.xchangecore.core.infrastructure.listener;

/**
 * 
 * @author bonnerad
 * 
 * @param <T>
 */
public interface NotificationListener<T> {

    public void onChange(T notificationMessage);

}
