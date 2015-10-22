package com.leidos.xchangecore.core.infrastructure.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.leidos.xchangecore.core.infrastructure.status.Status;
import com.leidos.xchangecore.core.infrastructure.status.StatusEvent;
import com.leidos.xchangecore.core.infrastructure.status.StatusEventListener;
import com.leidos.xchangecore.core.infrastructure.status.StatusEventMonitor;
import com.usersmarts.util.DirectoryWatcher;
import com.usersmarts.util.DirectoryWatcher.Change;

/**
 * RuleEngine
 * 
 * Compilation of a set of Rule objects which can be used to determine how to handle a StatusEvent
 * 
 * @author Santhosh Amanchi - Image Matters, LLC
 * @created
 */
public class RuleEngine
    implements InitializingBean, StatusEventMonitor, DirectoryWatcher.Listener, DisposableBean {

    Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private List<Rule> rules;

    private Map<String, List<Rule>> rulesMap = new HashMap<String, List<Rule>>();

    private Map<String, Status> status;

    private File rulesFile;

    private Map<String, TimerTask> componentTimerTasks = new HashMap<String, TimerTask>();

    Timer timer = new Timer();

    List<Timer> timers = new ArrayList<Timer>();
    List<TimerTask> tasks = new ArrayList<TimerTask>();

    private List<StatusEventListener> listeners = new ArrayList<StatusEventListener>();

    private DirectoryWatcher directoryWatcher;

    private Timer watcherTimer = null;

    private long delay = 5000;

    boolean updated = false;

    public boolean isUpdated() {

        return updated;
    }

    public void setUpdated(boolean updated) {

        this.updated = updated;
    }

    public long getDelay() {

        return delay;
    }

    public void setDelay(long delay) {

        this.delay = delay;
    }

    public long getFrequency() {

        return frequency;
    }

    public void setFrequency(long frequency) {

        this.frequency = frequency;
    }

    private long frequency = 30000;

    /**
     * Default Constructor
     */
    public RuleEngine() {

    }

    /**
     * @param rules
     * @param rulesFile
     */
    public RuleEngine(List<Rule> rules, File rulesFile) {

        this.rules = rules;
        this.rulesFile = rulesFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        initializeRules(getRulesFile());
        if (getRulesFile().exists()) {
            directoryWatcher = new DirectoryWatcher(getRulesFile().getParentFile(),
                                                    this,
                                                    "rules.xml",
                                                    true);
            watcherTimer = new Timer();
            watcherTimer.schedule(directoryWatcher, getDelay(), getFrequency());
            timers.add(watcherTimer);
            tasks.add(directoryWatcher);
        }
    }

    public List<Rule> getRules() {

        return rules;
    }

    public File getRulesFile() {

        return rulesFile;
    }

    /**
     * @param event StatusEvent event generated for a component
     * @param status Status current status of the component referenced in the event
     * @return Status resulting status for the component
     */
    public Status getStatus(StatusEvent event, Status status) {

        while (isUpdated()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.toString());
            }
        }
        List<Rule> rules = rulesMap.get(event.getComponentId().toLowerCase());
        if (rules == null) {
            return status;
        }
        for (Rule rule : rules) {
            boolean testEvent = rule.test(event); // test component,event and message values
            if (hasTimer(event.getComponentId())) {
                componentTimerTasks.get(event.getComponentId()).cancel(); // cancel timer that was
                // set for no event since
                // timeout
                componentTimerTasks.remove(event.getComponentId());
            }
            if (testEvent) {
                boolean inState = rule.isInState(status); // check whether status is same as
                // beginStatus of rule
                if (inState) {
                    Status newStatus = rule.getNewStatus();
                    if (rule.hasTimeOut()) { // if rule has timeout set timer for no event since
                        // timeout
                        setTimerForNoEvent(event.getComponentId(),
                            rule.getTimeOut(),
                            "No Error/Warn event since " + rule.getTimeOut() + " milliseconds");
                    }
                    return newStatus;
                }
            }
        }
        if (!event.getEvent().equals("TIMEOUT")) {
            if (hasTimer(event.getComponentId())) {
                componentTimerTasks.get(event.getComponentId()).cancel(); // cancel timer that was
                // set for no event since
                // timeout
                componentTimerTasks.remove(event.getComponentId());
            }
            setTimerForNoEvent(event.getComponentId(),
                25000,
                "No Event since 25000 milliseconds (No rule matched the last Event)");
        }
        return status;
    }

    private void setTimerForNoEvent(final String componentId,
                                    final int timeOut,
                                    final String message) {

        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                StatusEvent timeOutEvent = new StatusEvent(componentId,
                                                           "TIMEOUT",
                                                           message,
                                                           new Date(System.currentTimeMillis()));
                notifyOnEvent(timeOutEvent);

            }
        };
        componentTimerTasks.put(componentId, task);
        timer.schedule(task, timeOut);
        timers.add(timer);
        tasks.add(task);
    }

    private boolean hasTimer(String componentId) {

        TimerTask timerTask = componentTimerTasks.get(componentId);
        if (timerTask != null)
            return true;
        return false;
    }

    public void initializeRules(File rulesFile) {

        rulesMap = new HashMap<String, List<Rule>>();
        rules = new ArrayList<Rule>();
        status = new HashMap<String, Status>();
        if (!rulesFile.exists() || !rulesFile.canRead()) {
            log.warn("Could not locate or open file containing health and status rules" +
                     rulesFile.getAbsolutePath());
            return;
        }
        //        log.info("Using rules file:"+ rulesFile.getAbsolutePath());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(rulesFile);
            parseStatus(doc);
            parseRules(doc);
        } catch (Throwable t) {
            log.error("Failed to parse health and status rules from " + rulesFile.getAbsolutePath(),
                t);
        }
        setUpdated(false);
    }

    private void parseRules(Document doc) {

        // parse the rule.xml file
        NodeList ruleNodeList = doc.getElementsByTagName("rule");
        int len = ruleNodeList.getLength();
        for (int i = 0; i < len; i++) {
            Element nodeElem = (Element) ruleNodeList.item(i);
            String componentTag = nodeElem.getAttribute("component");
            String[] components = componentTag.split(",");
            String event = nodeElem.getAttribute("event");
            String begin = nodeElem.getAttribute("beginStatus");
            Status beginStatus = status.get(begin);
            String end = nodeElem.getAttribute("endStatus");
            Status endStatus = status.get(end);
            Node ruleNode = nodeElem.getFirstChild();
            String message = null;
            String timeOut = null;
            String[] keyWords = null;
            Node msgNode = ruleNode.getNextSibling();
            Node timeoutNode = nodeElem.getLastChild().getPreviousSibling();
            if (ruleNode != null) {
                // get the message keywords
                if (msgNode != null) {
                    message = msgNode.getNodeName();
                    keyWords = msgNode.getFirstChild().getNodeValue().split(" ");
                }
                if (timeoutNode != null && timeoutNode.getFirstChild() != null) {
                    timeOut = timeoutNode.getFirstChild().getNodeValue();
                }
            }
            for (String component : components) {
                // store rules based on component
                Rule rule = new Rule(component, event, beginStatus, endStatus, message);
                if (keyWords != null)
                    rule.addKeyWords(keyWords);
                rule.setTimeOut(timeOut);
                if (rulesMap.get(component.toLowerCase()) == null) {
                    rulesMap.put(component.toLowerCase(), new ArrayList<Rule>());
                }
                rulesMap.get(component.toLowerCase()).add(rule);
            }
        }
    }

    private void parseStatus(Document doc) throws Exception {

        // parse the statuses mentioned in rules.xml file
        doc.getDocumentElement().normalize();
        NodeList statusNodeList = doc.getElementsByTagName("status");
        int len = statusNodeList.getLength();
        for (int i = 0; i < len; i++) {
            Element nodeElem = (Element) statusNodeList.item(i);
            String name = nodeElem.getAttribute("name");
            String category = nodeElem.getAttribute("category");
            status.put(name, new Status(name, category));
        }

    }

    public void setRules(List<Rule> rules) {

        this.rules = rules;
    }

    public void setRulesFile(File rulesFile) {

        this.rulesFile = rulesFile;
    }

    @Override
    public void notifyOnEvent(StatusEvent event) {

        for (StatusEventListener listener : listeners) {
            listener.handleStatusEvent(event);
        }
    }

    @Override
    public void addListener(StatusEventListener listener) {

        this.listeners.add(listener);

    }

    @Override
    public void removeListener(StatusEventListener listener) {

    }

    @Override
    public void doPost(LoggingEvent event) {

    }

    @Override
    public void onChange(File file, Change change) {

        if (Change.DELETED.equals(change)) {
        } else if (Change.MODIFIED.equals(change)) {
            //            log.info("Rules File has been modified");
            setUpdated(true);
            initializeRules(file);
        }
    }

    public Status getCoreStatus(StatusEvent coreStatusEvent, Status beginStatus) {

        String message = coreStatusEvent.getMessage();
        List<Rule> coreRules = rulesMap.get(coreStatusEvent.getComponentId().toLowerCase());
        for (Rule coreRule : coreRules) {
            boolean containsEvent = coreRule.contiansEvent(message);
            if (containsEvent) {
                boolean levelCondition = coreRule.isMessage(message);
                if (levelCondition) {
                    boolean inState = coreRule.isInState(beginStatus);
                    if (inState) {
                        return coreRule.getEndStatus();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void destroy() throws Exception {

        if (!tasks.isEmpty()) {
            for (TimerTask task : tasks) {
                task.cancel();
            }
            tasks.clear();
        }
        if (!timers.isEmpty()) {
            for (Timer timer : timers) {
                timer.cancel();
            }
            timers.clear();
        }
    }
}
