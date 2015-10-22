/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.messages;

import com.saic.precis.x2009.x06.base.AssociatedGroupsDocument.AssociatedGroups;
import com.saic.precis.x2009.x06.base.IdentifierType;
import com.saic.precis.x2009.x06.structures.WorkProductIdentificationDocument;
import com.saic.precis.x2009.x06.structures.WorkProductPropertiesDocument;

/**
 * @author roger
 * 
 */
public class ProductChangeNotificationMessage {

    public static enum ChangeIndicator {
        Publish, Delete,
    };

    private ChangeIndicator changeIndicator;
    private WorkProductIdentificationDocument identification;
    private WorkProductPropertiesDocument properties;

    public ProductChangeNotificationMessage() {

    }

    public ProductChangeNotificationMessage(WorkProductIdentificationDocument identification,
                                            WorkProductPropertiesDocument properties,
                                            ChangeIndicator changeIndicator) {

        super();
        this.identification = identification;
        this.properties = properties;
        this.changeIndicator = changeIndicator;
    }

    public ChangeIndicator getChangeIndicator() {

        return changeIndicator;
    }

    public WorkProductIdentificationDocument getIdentification() {

        return identification;
    }

    public String getInterestGroupID() {

        if (this.properties != null &&
            this.properties.getWorkProductProperties() != null &&
            this.properties.getWorkProductProperties().getAssociatedGroups() != null &&
            this.properties.getWorkProductProperties().getAssociatedGroups().getIdentifierArray().length > 0) {
            return this.properties.getWorkProductProperties().getAssociatedGroups().getIdentifierArray(0).getStringValue();
        } else {
            return null;
        }
    }

    public String getProductID() {

        if (identification != null && identification.getWorkProductIdentification() != null &&
            identification.getWorkProductIdentification().getIdentifier() != null &&
            identification.getWorkProductIdentification().getIdentifier().getStringValue() != null) {
            return identification.getWorkProductIdentification().getIdentifier().getStringValue();
        } else {
            return null;
        }
    }

    public WorkProductPropertiesDocument getProperties() {

        return properties;
    }

    public String getType() {

        if (identification != null && identification.getWorkProductIdentification() != null &&
            identification.getWorkProductIdentification().getType() != null &&
            identification.getWorkProductIdentification().getType().getStringValue() != null) {
            return identification.getWorkProductIdentification().getType().getStringValue();
        } else {
            return null;
        }
    }

    public void setChangeIndicator(ChangeIndicator changeIndicator) {

        this.changeIndicator = changeIndicator;
    }

    public void setIdentification(WorkProductIdentificationDocument identification) {

        this.identification = identification;
    }

    public void setInterestGroupID(String newIdentifier) {

        if (this.properties == null) {
            this.properties = WorkProductPropertiesDocument.Factory.newInstance();
        }

        if (this.properties.getWorkProductProperties() == null) {
            this.properties.addNewWorkProductProperties();
        }

        if (this.properties.getWorkProductProperties().getAssociatedGroups() == null) {
            this.properties.getWorkProductProperties().addNewAssociatedGroups();
        }

        AssociatedGroups groups = this.properties.getWorkProductProperties().getAssociatedGroups();
        IdentifierType[] identifiers = groups.getIdentifierArray();
        boolean found = false;
        for (IdentifierType identifier : identifiers) {
            if (identifier.getStringValue().equals(newIdentifier)) {
                found = true;
                break;
            }
        }
        if (found == false) {
            groups.addNewIdentifier().setStringValue(newIdentifier);
        }
    }

    public void setProperties(WorkProductPropertiesDocument properties) {

        this.properties = properties;
    }
}
