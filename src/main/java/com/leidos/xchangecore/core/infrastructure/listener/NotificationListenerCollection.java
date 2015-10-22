package com.leidos.xchangecore.core.infrastructure.listener;

import java.util.ArrayList;

/**
 * 
 * @author bonnerad
 * 
 * @param <T>
 */
@SuppressWarnings({
    "unchecked",
    "serial"
})
public class NotificationListenerCollection<T>
    extends ArrayList<NotificationListener<T>> {

    public void fireChangeEvent(T message) {

        for (NotificationListener not : this) {
            not.onChange(message);
        }
    }
}
