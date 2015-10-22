package com.leidos.xchangecore.core.infrastructure.rule;

import java.util.ArrayList;
import java.util.List;

import com.leidos.xchangecore.core.infrastructure.status.Status;
import com.leidos.xchangecore.core.infrastructure.status.StatusEvent;

/**
 * Rule
 * 
 * POJO containing a set of variables defining tests 
 * for a StatusEvent
 * 
 * @author Santhosh Amanchi - Image Matters, LLC
 * @created 
 */
public class Rule {

    /** identifier of the UICDS component being tested */
    private String componentId = null;

    /** type of status event (ie, log level) */
    private String event = null;

    /** status the component must be in to be qualified for this test */
    private Status beginStatus = null;

    /** resulting status if the component passes the test */
    private Status endStatus = null;

    /** contents of the status event (ie, log message) to test */
    private String message = null;

    private List<String> keyWords = new ArrayList<String>();

    public List<String> getKeyWords() {

        return keyWords;
    }

    /** length of time to wait before automatically passing a test
     * (Needs more description) */
    private String timeOut = null;

    public Rule() {

    }

    public Rule(String componentId, String event, Status beginStatus, Status endStatus,
                String message) {

        this.componentId = componentId;
        this.event = event;
        this.beginStatus = beginStatus;
        this.endStatus = endStatus;
        this.message = message;
    }

    /**
     * @param event StatusEvent
     * @return boolean true if the test passes, false otherwise
     */
    public boolean test(StatusEvent event) {

        String component = event.getComponentId();
        String evt = event.getEvent();
        String msg = event.getMessage();
        if (isEvent(evt) && evt.equals("TIMEOUT")) {
            return true;
        }
        if (isComponent(component) && isEvent(evt) && isMessage(msg))
            return true;
        return false;
    }

    public void addKeyWords(String[] keyWords) {

        for (String keyWord : keyWords) {
            this.keyWords.add(keyWord);
        }
    }

    private boolean containsAll(String msg) {

        boolean containsAll = true;
        for (String keyWord : keyWords) {
            containsAll = containsAll && msg.toLowerCase().contains(keyWord.toLowerCase());
        }
        return containsAll;
    }

    private boolean containsNone(String msg) {

        boolean containsNone = true;
        for (String keyWord : keyWords) {
            containsNone = containsNone && !(msg.toLowerCase().contains(keyWord.toLowerCase()));
        }
        return containsNone;
    }

    private boolean contiansAny(String msg) {

        boolean containsAny = false;
        for (String keyWord : keyWords) {
            containsAny = containsAny || msg.toLowerCase().contains(keyWord.toLowerCase());
        }
        return containsAny;
    }

    public boolean contians(String msg) {

        boolean contains = false;
        for (String keyWord : keyWords) {
            contains = contains || msg.toLowerCase().contains(keyWord.toLowerCase());
        }
        return contains;
    }

    public Status getBeginStatus() {

        return beginStatus;
    }

    public String getComponentId() {

        return componentId;
    }

    public Status getEndStatus() {

        return endStatus;
    }

    public String getEvent() {

        return event;
    }

    public String getMessage() {

        return message;
    }

    public Status getNewStatus() {

        return endStatus;
    }

    public boolean isComponent(String id) {

        if (this.componentId != null) {
            return this.componentId.equalsIgnoreCase(id);
        } else {
            return false;
        }
    }

    public boolean isEvent(String event) {

        return (this.event != null) ? this.event.contains(event) : false;
    }

    public boolean isInState(Status status) {

        if (beginStatus == null)
            return false;
        boolean name = status.getName().equalsIgnoreCase(beginStatus.getName());
        return name;
    }

    public boolean isMessage(String msg) {

        if (message != null) {
            if (message.equals("containsAny")) {
                return contiansAny(msg); // (exception || failed || error);
            } else if (message.equals("containsNone")) {
                return containsNone(msg); // (!exception && !failed && !error);
            } else if (message.equals("containsAll")) {
                return containsAll(msg); // (exception && failed && error);
            }
        }
        return false;
    }

    public void setBeginStatus(Status beginStatus) {

        this.beginStatus = beginStatus;
    }

    public void setComponentId(String componentId) {

        this.componentId = componentId;
    }

    public void setEndStatus(Status endStatus) {

        this.endStatus = endStatus;
    }

    public void setEvent(String event) {

        this.event = event;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public int getTimeOut() {

        try {
            return Integer.parseInt(timeOut);
        } catch (Throwable t) {
            return -1;
        }
    }

    public boolean hasTimeOut() {

        if (timeOut != null && timeOut != "") {
            return true;
        }
        return false;
    }

    public void setTimeOut(String timeOut) {

        this.timeOut = timeOut;
    }

    public boolean contiansEvent(String message) {

        boolean containsEvent = false;
        containsEvent = message.toLowerCase().contains(event.toLowerCase()) ? true : false;
        return containsEvent;
    }

}
