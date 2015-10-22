package com.leidos.xchangecore.core.infrastructure.util;

import java.util.Set;

import org.apache.xmlbeans.XmlCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.resourceInstanceService.ResourceInstance;
import org.w3.x2005.x08.addressing.AttributedURIType;

import com.leidos.xchangecore.core.infrastructure.model.CodeSpaceValueType;
import com.leidos.xchangecore.core.infrastructure.model.ResourceInstanceModel;
import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.precis.x2009.x06.base.IdentifierType;

public class ResourceInstanceUtil {

    static Logger log = LoggerFactory.getLogger(ResourceInstanceUtil.class);

    public static ResourceInstance copyProperties(ResourceInstanceModel model, int count) {

        //
        ResourceInstance ris = ResourceInstance.Factory.newInstance();
        ris.addNewID().setLabel(model.getLabel());
        ris.getID().setStringValue(model.getIdentifier());
        ris.setDescription(model.getDescription());
        ris.addNewSourceIdentification().setCoreID(model.getOwningCore());
        IdentifierType localResourceID = IdentifierType.Factory.newInstance();
        localResourceID.setStringValue(model.getLocalResourceID());
        ris.getSourceIdentification().setLocalResourceID(localResourceID);

        // keywords
        if (model.getCvts() != null && model.getCvts().size() > 0) {
            for (CodeSpaceValueType keyword : model.getCvts()) {
                CodespaceValueType cvt = ris.addNewKeyword();
                cvt.setCodespace(keyword.getCodeSpace());
                cvt.setLabel(keyword.getLabel());
                cvt.setStringValue(keyword.getValue());
            }
        }

        // endpoints
        Set<String> endpoints = model.getEndpoints();
        if (endpoints != null && endpoints.size() > 0) {
            ris.addNewEndpoints();
            for (String iface : endpoints) {
                AttributedURIType address = ris.getEndpoints().addNewEndpoint().addNewAddress();
                address.setStringValue(iface);
                XmlCursor xc = address.newCursor();
                xc.insertAttributeWithValue("notificationCount",
                    ServiceNamespaces.NS_NotificationService,
                    Integer.toString(count));
                xc.dispose();
            }
        }

        // profiles
        Set<String> profiles = model.getProfiles();
        if (profiles != null && profiles.size() > 0) {
            ris.addNewProfileIDs();
            for (String profile : profiles) {
                ris.getProfileIDs().addNewProfileID().setStringValue(profile);
            }
        }
        return ris;
    }
}
