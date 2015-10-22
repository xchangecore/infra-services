package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import com.leidos.xchangecore.core.dao.GenericDAO;
import com.leidos.xchangecore.core.infrastructure.model.Log;

public interface LoggerDAO
    extends GenericDAO<Log, Integer> {

    Log logRequest(Log log);

    List<Log> findByHostname(String hostname);

    List<Log> findByLogger(String logger);
}
