package com.leidos.xchangecore.core.infrastructure.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.receivers.varia.LogFilePatternReceiver;
import org.apache.log4j.spi.LoggingEvent;

import com.leidos.xchangecore.core.infrastructure.status.StatusEventMonitor;

public class LogFileMonitor
    extends LogFilePatternReceiver {

    public class LogFileMonitorRunnable
        implements Runnable {

        LogFileMonitor monitor;

        public LogFileMonitorRunnable(LogFileMonitor monitor) {

            this.monitor = monitor;
        }

        @Override
        public void run() {

            monitor.initialize();
            while (reader == null) {
                getLogger().info("attempting to load file: " + getFileURL());
                try {
                    reader = new BufferedReader(new InputStreamReader(new URL(getFileURL()).openStream()));
                } catch (FileNotFoundException fnfe) {
                    getLogger().error("file not available - will try again in 10 seconds");
                    synchronized (this) {
                        try {
                            wait(10000);
                        } catch (InterruptedException ie) {
                        }
                    }
                } catch (IOException ioe) {
                    getLogger().warn("unable to load file", ioe);
                    return;
                }
            }
            try {
                process(reader);
            } catch (IOException ioe) {
                getLogger().info("stream closed");
            }
        }

        public void stop() {

            setTailing(false);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Logger.getLogger(getClass()).error(e.getMessage());
            }
        }

    }

    private BufferedReader reader;

    private String name = "Unknown";

    private List<StatusEventMonitor> monitors = new ArrayList<StatusEventMonitor>();

    private String TOMCAT_TIMESTAMP = "MMM d, yyyy HH:mm:ss a";

    private String OPENFIRE_TIMESTAMP = "yyyy.MM.d HH:mm:ss";

    private String UICDS_TIMESTAMP = "HH:mm:ss";

    private String OPENDS_TIMESTAMP = "dd/MMM/yyyy:HH:mm:ss Z";

    private Map<String, LoggingEvent> lastEvents = new HashMap<String, LoggingEvent>();

    public LogFileMonitor() {

    }

    public LogFileMonitor(String name, String logPattern, boolean tailing, Level level, File logFile) {

        this.name = name;
        // Set LogFilePatternReceiver Properties
        if (name.equals("tomcat")) {
            setTimestampFormat(TOMCAT_TIMESTAMP);
        } else if (name.equals("openfire")) {
            setTimestampFormat(OPENFIRE_TIMESTAMP);
        }
        setFileURL("file:///" + logFile.getAbsolutePath());
        setName(name);
        setLogFormat(logPattern);
        SimpleLoggerRepository logRepository = new SimpleLoggerRepository();
        logRepository.setThreshold(level);
        setLoggerRepository(logRepository);
        setTailing(tailing);
    }

    public void addMonitors(StatusEventMonitor monitor) {

        this.monitors.add(monitor);
    }

    @Override
    public void doPost(LoggingEvent event) {

        // Receive Log events of log file
        if (!isAsSevereAsThreshold(event.getLevel())) {
            return;
        }
        event.setProperty("fileUrl", getFileURL());
        event.setProperty("timeStamp", getTimestampFormat());
        if (lastEvents.get(name + "-" + getFileURL()) == null) {
            processEvent(event);
            lastEvents.put(name + "-" + getFileURL(), event);
        } else {
            if (isLatestEvent(event)) {
                processEvent(event);
                lastEvents.put(name + "-" + getFileURL(), event);
            }
        }
    }

    private Date getEventDate(LoggingEvent event) {

        // Parse timestamp from loggingevent
        Date date = new Date(event.getTimeStamp());
        String relTime = event.getProperty("RELATIVETIME");
        SimpleDateFormat dateFormat = new SimpleDateFormat(event.getProperty("timeStamp"));
        if (name.equals("uicds")) {
            dateFormat = new SimpleDateFormat(UICDS_TIMESTAMP);
        } else if (name.equals("opends")) {
            dateFormat = new SimpleDateFormat(OPENDS_TIMESTAMP);
        }
        try {
            if (relTime != null) {
                if (Character.isDigit(relTime.charAt(0))) {
                    if (name.equals("openfire")) {
                        if (relTime.length() < 12) {
                            String time = (String) event.getMessage();
                            relTime = relTime + " " + time.substring(0, 9).trim();
                        }
                    }
                    date = dateFormat.parse(relTime);
                } else
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public String getName() {

        return name;
    }

    protected boolean isLatestEvent(LoggingEvent event) {

        // check if the event is latest
        if (name.equals("uicds")) {
            return true;
        }
        Date previousEventDate = getEventDate(lastEvents.get(name + "-" + getFileURL()));
        Date currentEventDate = getEventDate(event);
        if (currentEventDate != null && previousEventDate != null) {
            if (currentEventDate.after(previousEventDate) ||
                currentEventDate.equals(previousEventDate)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private void processEvent(LoggingEvent event) {

        for (StatusEventMonitor monitor : monitors) {
            monitor.doPost(event);
        }
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

}
