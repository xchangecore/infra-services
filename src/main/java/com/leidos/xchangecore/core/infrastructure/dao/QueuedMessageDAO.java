package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

public interface QueuedMessageDAO {

    public List<String> getMessagesByCorename(String corename);

    public boolean removeMessage(String corename, String message);

    public void removeMessagesForCore(String corename);

    public void saveMessage(String corename, String message);
}
