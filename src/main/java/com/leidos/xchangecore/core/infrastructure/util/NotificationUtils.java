package com.leidos.xchangecore.core.infrastructure.util;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.uicds.resourceProfileService.Interest;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.MetadataType;

import com.leidos.xchangecore.core.infrastructure.model.InterestElement;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ConfigurationServiceImpl;

public class NotificationUtils {

    static ConfigurationService configurationService = new ConfigurationServiceImpl();

    public static EndpointReferenceType createEndpoint(String entityID) {

        EndpointReferenceType endpoint = EndpointReferenceType.Factory.newInstance();

        // Set the url for the notification service for pull points
        endpoint.addNewAddress().setStringValue(configurationService.getWebServiceBaseURL() + "/" +
                                                entityID);

        // Add the service identification
        MetadataType metadata = endpoint.addNewMetadata();
        XmlCursor xc = metadata.newCursor();
        xc.toNextToken();
        String type = NotificationService.NOTIFICATION_SERVICE_NAME;
        xc.insertElementWithText("scheme", configurationService.getServiceNameURN(type));
        xc.dispose();

        return endpoint;
    }

    public static FilterType getFilterFromInterest(Interest interest) throws XmlException {

        XmlOptions xo = new XmlOptions();
        xo.setSaveInner();
        XmlCursor ic = interest.newCursor();
        FilterType filter = FilterType.Factory.parse(ic.xmlText(xo));
        ic.dispose();
        return filter;
    }

    public static FilterType getFilterFromInterest(InterestElement interest) {

        FilterType filter = FilterType.Factory.newInstance();
        XmlCursor cursor = filter.newCursor();
        cursor.toNextToken();
        cursor.insertElementWithText("TopicExpression", interest.getTopicExpression());
        cursor.toNextToken();
        cursor.dispose();
        // TODO: copy query expression types and namespace maps
        return filter;
    }
    //	Interests interests = profile.addNewInterests();
    //	Interest interest = interests.addNewInterest();
    //
    //	QName topicExpression = new QName(IncidentManagementService.Type);
    //	interest.setTopicExpression(topicExpression.toString());
    //
    //	if (addTopicQuery) {
    //		QueryExpressionType query = interest.addNewMessageContent();
    //		query.setDialect(WSN_XPATH_DIALECT);
    //		XmlCursor queryCursor = query.newCursor();
    //		queryCursor.toNextToken();
    //		queryCursor.toNextToken();
    //		queryCursor.insertChars("//stuff");
    //		queryCursor.dispose();
    //
    //		NamespaceMapType map = interest.addNewNamespaceMap();
    //		NamespaceMapItemType item = map.addNewItem();
    //		item.setPrefix("de");
    //		item.setUri("urn:oasis:names:tc:emergency:EDXL:DE:1.0");
    //
    //		item = map.addNewItem();
    //		item.setPrefix("cap");
    //		item.setUri("urn:oasis:names:tc:emergency:cap:1.1");
    //	}

}
