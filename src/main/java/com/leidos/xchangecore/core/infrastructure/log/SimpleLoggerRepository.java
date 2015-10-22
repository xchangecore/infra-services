package com.leidos.xchangecore.core.infrastructure.log;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;

@SuppressWarnings("deprecation")
public class SimpleLoggerRepository
    implements LoggerRepository {

    private Level thresholdLevel = Level.ALL;

    public SimpleLoggerRepository() {

    }

    @Override
    public void addHierarchyEventListener(HierarchyEventListener listener) {

        // TODO Auto-generated method stub

    }

    @Override
    public void emitNoAppenderWarning(Category cat) {

        // TODO Auto-generated method stub

    }

    @Override
    public Logger exists(String name) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fireAddAppenderEvent(Category logger, Appender appender) {

        // TODO Auto-generated method stub

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getCurrentCategories() {

        // TODO Auto-generated method stub
        return Logger.getCurrentCategories();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getCurrentLoggers() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Logger getLogger(String name) {

        // TODO Auto-generated method stub
        return Logger.getLogger(name);
    }

    @Override
    public Logger getLogger(String name, LoggerFactory factory) {

        // TODO Auto-generated method stub
        return Logger.getLogger(name, factory);
    }

    @Override
    public Logger getRootLogger() {

        // TODO Auto-generated method stub
        return Logger.getRootLogger();
    }

    @Override
    public Level getThreshold() {

        return thresholdLevel;
    }

    public Level getThresholdLevel() {

        return thresholdLevel;
    }

    @Override
    public boolean isDisabled(int level) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetConfiguration() {

        // TODO Auto-generated method stub

    }

    @Override
    public void setThreshold(Level level) {

        this.thresholdLevel = level;
    }

    @Override
    public void setThreshold(String val) {

        Level level = Level.toLevel(val);
        this.thresholdLevel = level;
    }

    @Override
    public void shutdown() {

        // TODO Auto-generated method stub

    }

}
