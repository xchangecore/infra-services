package com.leidos.xchangecore.core.infrastructure.util;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidXpathException;

public class DocumentUtil {

    private static DocumentBuilder instance = null;

    public synchronized static DocumentBuilder getInstance() {

        if (instance == null) {
            synchronized (DocumentUtil.class) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                try {
                    instance = factory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    public static boolean exist(String expression,
                                XmlObject content,
                                Map<String, String> namespaceMap) throws InvalidXpathException {

        String result = "";
        Document doc;
        try {
            doc = getInstance().parse(new InputSource(new StringReader(content.xmlText())));

            // normalize the text representation
            doc.normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new SimpleNamespaceContext(namespaceMap));
            result = xpath.evaluate(expression, doc);
        } catch (XPathExpressionException e) {
            throw new InvalidXpathException("Invalid XPath: " + expression + ". Cause: " +
                                            e.getCause().getMessage().trim());
        } catch (Exception e) {
            throw new InvalidXpathException("XPath evaluation: Document: " + e.getMessage());
        }

        return result.length() > 0;
    }
}
