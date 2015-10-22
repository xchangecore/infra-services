/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.LoggerDAO;
import com.leidos.xchangecore.core.infrastructure.model.Log;

public class LoggerDAOHibernate
    extends GenericHibernateDAO<Log, Integer>
    implements LoggerDAO {

    Logger logger = LoggerFactory.getLogger(LoggerDAOHibernate.class);

    public List<Log> findByHostname(String hostname) {

        Log log = new Log();
        log.setHostname(hostname);
        List<Log> results = findByExample(log);
        return results;
    }

    public List<Log> findByLogger(String logger) {

        Log log = new Log();
        log.setLogger(logger);
        List<Log> results = findByExample(log);
        return results;
    }

    public Log logRequest(Log log) {

        logger.debug("in log Request()...");
        if (logger.isDebugEnabled()) {
            logger.debug("in log Request...");
        }
        Log localLog = new Log();
        localLog = makePersistent(log);
        return localLog;
    }
}