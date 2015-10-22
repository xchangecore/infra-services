package com.leidos.xchangecore.core.infrastructure.controller;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.io.Resource;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ImportXsdSchema
    implements XsdSchema {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    static {
        documentBuilderFactory.setNamespaceAware(true);
    }

    private String schemaLocation;
    private String targetNamespace;

    private Resource xsdResource;

    /**
     * Create a new instance of the {@link SimpleXsdSchema} class.
     * <p/>
     * A subsequent call to the {@link #setXsd(Resource)} method is required.
     */
    public ImportXsdSchema() {

    }

    @Override
    public XmlValidator createValidator() {

        try {
            return XmlValidatorFactory.createValidator(xsdResource,
                XmlValidatorFactory.SCHEMA_W3C_XML);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getSchemaLocation() {

        return schemaLocation;
    }

    @Override
    public Source getSource() {

        DOMSource result = new DOMSource();
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElementNS(SCHEMA_NAMESPACE, "xsd:schema");
            root.setAttribute("xmlns:xsd", SCHEMA_NAMESPACE);
            root.setAttribute("targetNamespace", targetNamespace);
            Element imprt = doc.createElementNS(SCHEMA_NAMESPACE, "xsd:import");
            imprt.setAttribute("schemaLocation", schemaLocation);
            imprt.setAttribute("namespace", targetNamespace);
            root.appendChild(imprt);

            doc.appendChild(root);
            result.setNode(doc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public String getTargetNamespace() {

        return targetNamespace;
    }

    public void setSchemaLocation(String schemaLocation) {

        this.schemaLocation = schemaLocation;
    }

    public void setTargetNamespace(String targetNamespace) {

        this.targetNamespace = targetNamespace;
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer("ImportXsdSchema");
        buffer.append('{');
        buffer.append(getTargetNamespace());
        buffer.append('}');
        return buffer.toString();
    }

}