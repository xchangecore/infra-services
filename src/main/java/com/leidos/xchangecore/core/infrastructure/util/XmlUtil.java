package com.leidos.xchangecore.core.infrastructure.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;

public class XmlUtil {

    public static final XmlOptions normal = new XmlOptions().setSavePrettyPrint().setLoadStripWhitespace();

    public static final XmlOptions innerOnly = new XmlOptions().setSavePrettyPrint().setSaveInner();

    public static String Document2String(Document doc) throws IOException, TransformerException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(baos,
            "UTF-8")));
        return baos.toString("UTF-8");
    }

    public static String getDOMString(Document doc) {

        String s = null;
        final TransformerFactory tfactory = TransformerFactory.newInstance();
        try {
            final Transformer xform = tfactory.newTransformer();
            final Source src = new DOMSource(doc);
            final StringWriter writer = new StringWriter();
            final Result result = new StreamResult(writer);
            xform.transform(src, result);
            s = writer.toString();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }

    public static String getTextFromAny(XmlObject object) {

        return ((SimpleValue) object).getStringValue();

        /* ddh 04/13/2015
        final XmlCursor c = object.newCursor();
        final String text = c.getTextValue();
        c.dispose();
        return text;
         */
    }

    public static final void substitute(XmlObject parentObject,
                                        String subNamespace,
                                        String subTypeName,
                                        SchemaType subSchemaType,
                                        XmlObject theObject) {

        final XmlObject subObject = parentObject.substitute(new QName(subNamespace, subTypeName),
                                                            subSchemaType);
        if (subObject != parentObject)
            subObject.set(theObject);
    }

}
