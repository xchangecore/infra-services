package com.leidos.xchangecore.core.infrastructure.controller;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportPathRewriteXsdSchema
    implements XsdSchema, InitializingBean {

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    private static final QName SCHEMA_QNAME = QNameUtils.createQName(SCHEMA_NAMESPACE,
        "schema",
        "xsd");

    static {
        documentBuilderFactory.setNamespaceAware(true);
    }

    private Element schemaElement;

    private Resource xsdResource;

    /**
     * Create a new instance of the {@link SimpleXsdSchema} class.
     * <p/>
     * A subsequent call to the {@link #setXsd(Resource)} method is required.
     */
    public ImportPathRewriteXsdSchema() {

    }

    /**
     * Create a new instance of the {@link SimpleXsdSchema} class with the specified resource.
     *
     * @param xsdResource the XSD resource; must not be <code>null</code>
     * @throws IllegalArgumentException if the supplied <code>xsdResource</code> is
     *             <code>null</code>
     */
    public ImportPathRewriteXsdSchema(Resource xsdResource) {

        Assert.notNull(xsdResource, "xsdResource must not be null");
        this.xsdResource = xsdResource;
    }

    @Override
    public void afterPropertiesSet() throws ParserConfigurationException, IOException, SAXException {

        Assert.notNull(xsdResource, "'xsd' is required");
        Assert.isTrue(xsdResource.exists(), "xsd '" + xsdResource + "' does not exit");
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        loadSchema(documentBuilder);
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

    @Override
    public Source getSource() {

        return new DOMSource(schemaElement);
    }

    @Override
    public String getTargetNamespace() {

        return schemaElement.getAttribute("targetNamespace");
    }

    private void loadSchema(DocumentBuilder documentBuilder) throws SAXException, IOException {

        Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(xsdResource));
        schemaElement = schemaDocument.getDocumentElement();
        Assert.isTrue(SCHEMA_QNAME.getLocalPart().equals(schemaElement.getLocalName()),
            xsdResource + " has invalid root element : [" + schemaElement.getLocalName() +
                "] instead of [schema]");
        Assert.isTrue(SCHEMA_QNAME.getNamespaceURI().equals(schemaElement.getNamespaceURI()),
            xsdResource + " has invalid root element: [" + schemaElement.getNamespaceURI() +
                "] instead of [" + SCHEMA_QNAME.getNamespaceURI() + "]");
        Assert.hasText(getTargetNamespace(), xsdResource + " has no targetNamespace");
        rewriteImportLocations(schemaElement);
    }

    private void rewriteImportLocations(Element element) {

        NodeList nl = element.getElementsByTagNameNS(SCHEMA_NAMESPACE, "import");
        int length = nl.getLength();
        for (int i = 0; i < length; ++i) {
            Element e = (Element) nl.item(i);
            String schemaLocation = e.getAttribute("schemaLocation");
            if (schemaLocation.startsWith("../")) {
                schemaLocation = schemaLocation.replaceFirst("../", "../xsd/");
            } else if (schemaLocation.matches("\\w+\\.xsd")) {
                schemaLocation = "../xsd/" + schemaLocation;
            }
            e.setAttribute("schemaLocation", schemaLocation);
        }
    }

    /**
     * Set the XSD resource to be exposed by calls to this instances' {@link #getSource()} method.
     *
     * @param xsdResource the XSD resource
     */
    public void setXsd(Resource xsdResource) {

        this.xsdResource = xsdResource;
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer("SimpleXsdSchema");
        buffer.append('{');
        buffer.append(getTargetNamespace());
        buffer.append('}');
        return buffer.toString();
    }

}