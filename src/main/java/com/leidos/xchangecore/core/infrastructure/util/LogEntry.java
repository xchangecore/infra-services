package com.leidos.xchangecore.core.infrastructure.util;

public class LogEntry {

    public static final String CATEGORY_INCIDENT = "Incident";
    public static final String ACTION_INCIDENT_CREATE = "Create";
    public static final String ACTION_INCIDENT_UPDATE = "Update";
    public static final String ACTION_INCIDENT_CLOSE = "Close";
    public static final String ACTION_INCIDENT_ARCHIVE = "Archive";
    public static final String ACTION_INCIDENT_SHARE = "Share";
    public static final String ACTION_INCIDENT_JOIN = "Join";

    public static final String CATEGORY_WORKPRODUCT = "WorkProduct";
    public static final String ACTION_WORKPRODUCT_CREATE = "Create";
    public static final String ACTION_WORKPRODUCT_UPDATE = "Update";

    public static final String CATEGORY_NOTIFICATION = "Notification";
    public static final String ACTION_NOTIFICATION_POLL = "Poll";

    private String logEntry = "";

    public String getLogEntry() {

        String prefix = "PERF" + "|" + category + "|" + action + "|";
        String entry = null;
        if (category.equals(CATEGORY_INCIDENT)) {
            if (action.equals(ACTION_INCIDENT_CREATE)) {
                entry = "coreName=" + coreName + "|" + "incidentId=" + incidentId + "|" +
                        "incidentType=" + incidentType + "|" + "createdBy=" + createdBy;
            } else if (action.equals(ACTION_INCIDENT_UPDATE)) {
                entry = "coreName=" + coreName + "|" + "incidentId=" + incidentId + "|" +
                        "incidentType=" + incidentType + "|" + "updatedBy=" + updatedBy;
            } else if (action.equals(ACTION_INCIDENT_CLOSE)) {
                entry = "coreName=" + coreName + "|" + "incidentId=" + incidentId + "|" +
                        "incidentType=" + incidentType + "|" + "updatedBy=" + updatedBy;
            } else if (action.equals(ACTION_INCIDENT_ARCHIVE)) {
                entry = "coreName=" + coreName + "|" + "incidentId=" + incidentId + "|" +
                        "incidentType=" + incidentType + "|" + "updatedBy=" + updatedBy;
            } else if (action.equals(ACTION_INCIDENT_SHARE)) {
                entry = "coreName=" + coreName + "|" + "incidentId=" + incidentId + "|" +
                        "shareCoreName=" + shareCoreName;
            } else if (action.equals(ACTION_INCIDENT_JOIN)) {
                entry = "coreName=" + coreName + "|" + "incidentId=" + incidentId + "|" +
                        "joinCoreName=" + joinCoreName;
            }
        } else if (category.equals(CATEGORY_WORKPRODUCT)) {
            if (action.equals(ACTION_WORKPRODUCT_CREATE)) {
                entry = "workProductId=" + workProductId + "|" + "workProductType=" +
                        workProductType + "|" + "workProductSize=" + workProductSize + "|" +
                        "createdBy=" + createdBy;
            } else if (action.equals(ACTION_WORKPRODUCT_UPDATE)) {
                entry = "workProductId=" + workProductId + "|" + "workProductType=" +
                        workProductType + "|" + "workProductSize=" + workProductSize + "|" +
                        "updatedBy=" + updatedBy;
            }
        } else if (category.equals(CATEGORY_NOTIFICATION)) {
            if (action.equals(ACTION_NOTIFICATION_POLL)) {
                entry = "entityId=" + entityId;
            }
        }
        logEntry = prefix + entry;
        return logEntry;
    }

    private String category = "";

    public void setCategory(String category) {

        this.category = (category == null) ? "" : category;
    }

    private String action = "";

    public void setAction(String action) {

        this.action = (action == null) ? "" : action;
    }

    private String coreName = "";

    public void setCoreName(String coreName) {

        this.coreName = (coreName == null) ? "" : coreName;
    }

    private String incidentId = "";

    public void setIncidentId(String incidentId) {

        this.incidentId = incidentId == null ? "" : incidentId;
    }

    private String incidentType = "";

    public void setIncidentType(String incidentType) {

        this.incidentType = (incidentType == null) ? "" : incidentType;
    }

    private String createdBy = "";

    public void setCreatedBy(String createdBy) {

        this.createdBy = (createdBy == null) ? "" : createdBy;
    }

    private String updatedBy = "";

    public void setUpdatedBy(String updatedBy) {

        this.updatedBy = (updatedBy == null) ? "" : updatedBy;
    }

    private String shareCoreName = "";

    public void setShareCoreName(String shareCoreName) {

        this.shareCoreName = (shareCoreName == null) ? "" : shareCoreName;
    }

    private String joinCoreName = "";

    public void setJoinCoreName(String joinCoreName) {

        this.joinCoreName = (joinCoreName == null) ? "" : joinCoreName;
    }

    private String workProductId = "";

    public void setWorkProductId(String workProductId) {

        this.workProductId = workProductId == null ? "" : workProductId;
    }

    private String workProductType = "";

    public void setWorkProductType(String workProductType) {

        this.workProductType = (workProductType == null) ? "" : workProductType;
    }

    private String workProductSize = "";

    public void setWorkProductSize(String workProductSize) {

        this.workProductSize = (workProductSize == null) ? "" : workProductSize;
    }

    private String entityId = "";

    public void setEntityId(String entityId) {

        this.entityId = (entityId == null) ? "" : entityId;
    }
}
