package com.leidos.xchangecore.core.infrastructure.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.uicds.resourceProfileService.Interest;

import com.saic.precis.x2009.x06.base.NamespaceMapItemType;
import com.saic.precis.x2009.x06.base.NamespaceMapType;

public class FilterUtil
    implements ServiceNamespaces {

    public static final String WS_NOTIFICATION = "http://docs.oasis-open.org/wsn/b-2";

    public static FilterType get(Interest interest) throws XmlException {

        XmlOptions xo = new XmlOptions();
        xo.setSaveInner();
        XmlCursor ic = interest.newCursor();
        FilterType filter = FilterType.Factory.parse(ic.xmlText(xo));
        ic.dispose();
        return filter;
    }

    public static Map<String, String> getNamespaceMap(FilterType what) {

        HashMap<String, String> namespaceMap = new HashMap<String, String>();

        QName topicExpressionNode = new QName(NS_ProfileService, "NamespaceMap");
        XmlObject[] namespaceMapObjs = what.selectChildren(topicExpressionNode);
        if (namespaceMapObjs.length == 0) {
            namespaceMapObjs = what.selectPath("./*/NamespaceMap");
            if (namespaceMapObjs.length == 0) {
                topicExpressionNode = new QName(WS_NOTIFICATION, "NamespaceMap");
                namespaceMapObjs = what.selectChildren(topicExpressionNode);
            }
        }

        NamespaceMapType nsMap;
        if (namespaceMapObjs.length == 0) {
            XmlCursor x = what.newCursor();
            while (x.currentTokenType() != XmlCursor.TokenType.ENDDOC) {
                if (x.isStart()) {
                    if (x.getName().getLocalPart().equalsIgnoreCase("NamespaceMap")) {
                        x.toNextToken();
                        try {
                            // log.debug("1: "+x.xmlText());
                            nsMap = NamespaceMapType.Factory.parse(x.xmlText());
                            if (nsMap.sizeOfItemArray() > 0) {
                                for (NamespaceMapItemType ns : nsMap.getItemArray()) {
                                    namespaceMap.put(ns.getPrefix(), ns.getURI());
                                }
                            }
                        } catch (XmlException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                x.toNextToken();
            }
            x.dispose();
        } else if (namespaceMapObjs.length > 0) {
            try {
                // log.debug("2 "+namespaceMapObjs[0].toString());
                nsMap = NamespaceMapType.Factory.parse(namespaceMapObjs[0].toString());
                for (NamespaceMapItemType ns : nsMap.getItemArray()) {
                    namespaceMap.put(ns.getPrefix(), ns.getURI());
                }
            } catch (XmlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return namespaceMap;
    }

    public static String getTopic(FilterType what) {

        String productType = "";
        QName topicExpressionNode = new QName(NS_ProfileService, "TopicExpression");
        XmlObject[] topics = what.selectChildren(topicExpressionNode);
        if (topics.length == 0) {
            topics = what.selectPath("./*/TopicExpression");
        }
        if (topics.length == 0) {
            topicExpressionNode = new QName(WS_NOTIFICATION, "TopicExpression");
            topics = what.selectChildren(topicExpressionNode);
        }
        if (topics.length == 0) {
            XmlCursor x = what.newCursor();
            while (x.currentTokenType() != XmlCursor.TokenType.ENDDOC) {
                if (x.isStart()) {
                    if (x.getName().getLocalPart().equalsIgnoreCase("TopicExpression")) {
                        productType = x.getTextValue();
                        break;
                    }
                }
                x.toNextToken();
            }
            x.dispose();
        } else if (topics.length > 0) {
            XmlCursor xc = topics[0].newCursor();
            productType = xc.getTextValue();
            xc.dispose();
        }
        productType = productType.trim();
        return productType;
    }

    public static String getXPath(FilterType what) {

        String xPath = "";

        QName contentQname = new QName(NS_ProfileService, "MessageContent");
        XmlObject[] xPathObjs = what.selectChildren(contentQname);
        if (xPathObjs.length == 0) {
            contentQname = new QName(WS_NOTIFICATION, "MessageContent");
            xPathObjs = what.selectChildren(contentQname);
        }
        if (xPathObjs.length == 0) {
            xPathObjs = what.selectPath("./*/MessageContent");
        }

        if (xPathObjs.length > 0) {
            String xpath = xPathObjs[0].xmlText();
            XmlCursor xc = xPathObjs[0].newCursor();
            xc.toNextAttribute();
            xPath = xc.getTextValue();
            xc.dispose();
        }

        return xPath;
    }
}
