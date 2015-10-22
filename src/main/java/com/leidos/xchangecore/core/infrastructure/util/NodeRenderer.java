package com.leidos.xchangecore.core.infrastructure.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.saxon.xqj.SaxonXQDataSource;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.usersmarts.util.DirectoryWatcher;
import com.usersmarts.util.DirectoryWatcher.Change;

public class NodeRenderer
    implements ServletContextAware, DirectoryWatcher.Listener {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String configPath;

    private File configDirectory;

    public File getConfigDirectory() {

        return configDirectory;
    }

    public void setConfigDirectory(File configDirectory) {

        this.configDirectory = configDirectory;
    }

    private DirectoryWatcher directoryWatcher;

    private Timer watcherTimer = null;

    private long frequency = 20000;

    private long delay = 5000;

    private Node configDom = null;

    private HashMap<String, Query> preparedQueries = new HashMap<String, Query>();

    public NodeRenderer() {

    }

    private String buildDispatchXQ(File transform, Properties properties) {

        URI uri = transform.toURI();
        String path = uri.getPath();
        String dispatchXQ = "import module namespace ns = '" + properties.getProperty("modulens") +
                            "' " + "	at '" + path + "/" + properties.getProperty("xquery") + "'; " +
                            "declare variable $config external; " +
                            "declare variable $properties external; " +
                            "declare variable $workproducts external; " + "let $b:='bar' " +
                            "return " + "  ns:render($config, $properties, $workproducts)";
        return dispatchXQ;
    }

    public void buildPreparedQuery(File transform) {

        log.info("Preparing renderer: " + transform.getName());

        String dispatchXQ = null;
        XQPreparedExpression query = null;
        Properties properties = null;
        File xqueryFile, propsFile = null;

        XQDataSource ds = null;
        XQConnection conn = null;
        try {
            ds = new SaxonXQDataSource();
            conn = ds.getConnection();
        } catch (XQException e) {
            log.error(e.getMessage());
        }

        if (conn != null) {
            // loading the properties object
            properties = new Properties();
            try {
                propsFile = new File(transform, "properties");
                if (propsFile.exists()) {
                    // open fileInputStream
                    FileInputStream fis = new FileInputStream(propsFile.getAbsoluteFile());

                    // load the properties file
                    properties.load(fis);

                    // close fileInputStream
                    fis.close();
                } else {
                    log.warn("No properties file found.  Setting default values.");
                }
                // fill in any missing properties...
                if (properties.getProperty("mediatype") == null) {
                    properties.put("mediatype", "application/octet-stream");
                }
                if (properties.getProperty("method") == null) {
                    properties.put("method", "text");
                }
                if (properties.getProperty("xquery") == null) {
                    properties.put("xquery", "render.xquery");
                }

                if (properties.getProperty("modulens") != null) {

                    // check to see if the renderer exists
                    xqueryFile = new File(transform, properties.getProperty("xquery"));
                    if (xqueryFile.exists()) {
                        // compile the query
                        dispatchXQ = buildDispatchXQ(transform, properties);
                        query = conn.prepareExpression(dispatchXQ);

                        if (query != null) {
                            // add it to the map.
                            preparedQueries.put(transform.getName().toLowerCase(),
                                new Query(properties, query));
                        } else {
                            log.error("Preparation failed. Query failed to compile.");
                        }

                    } else {
                        log.error("Preparation failed. XQquery file " +
                                  properties.getProperty("xquery") + " was not found.");
                    }

                } else {
                    log.error("Preparation failed. Module namespace is not set.");
                }

            } catch (XQException e) {
                log.error(e.getMessage());
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        } else {
            log.error("Preparation failed. XQConnection was null");
        }
    }

    public String domNodeToString(Node node) {

        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer;
        StringWriter buffer = new StringWriter();

        try {
            transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(buffer));
        } catch (TransformerConfigurationException e) {
            log.error(e.getMessage());
            //e1.printStackTrace();
        } catch (TransformerException e) {
            log.error(e.getMessage());
            //e1.printStackTrace();
        }
        return buffer.toString();
    }

    public Map<String, Object> exec(Node sourceData,
                                    Map<String, String[]> propertiesMap,
                                    String formatName) {

        Map<String, Object> map = new HashMap<String, Object>();

        log.debug("Executing query " + formatName);

        Query query = preparedQueries.get(formatName);

        Node sourceNode = null;
        try {
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setNamespaceAware(true);
            DocumentBuilder docBuilder;

            docBuilder = dfactory.newDocumentBuilder();

            DOMSource source = new DOMSource(sourceData);
            StringWriter xmlAsWriter = new StringWriter();
            StreamResult sresult = new StreamResult(xmlAsWriter);

            TransformerFactory.newInstance().newTransformer().transform(source, sresult);

            // write changes
            ByteArrayInputStream inputStream;

            inputStream = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8"));

            sourceNode = docBuilder.parse(inputStream);

            if (query != null) {

                try {
                    map.put("properties", query.getProperties());

                    query.getXQPreparedExpression().bindNode(new QName("config"),
                        this.configDom,
                        null);
                    query.getXQPreparedExpression().bindNode(new QName("properties"),
                        mapToXMLNode(propertiesMap),
                        null);
                    query.getXQPreparedExpression().bindNode(new QName("workproducts"),
                        sourceNode,
                        null);

                    XQResultSequence result = query.getXQPreparedExpression().executeQuery();

                    if (result != null) {
                        result.next();
                        StringWriter sw = new StringWriter();
                        if (query.getProperties().getProperty("method").equals("xml")) {
                            //							System.out.println("method = xml");
                            Transformer t = TransformerFactory.newInstance().newTransformer();
                            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                            t.setOutputProperty(OutputKeys.INDENT, "yes");
                            t.transform(new DOMSource(result.getNode()), new StreamResult(sw));
                        } else {
                            System.out.println("method != xml");
                            sw.append(result.getAtomicValue());
                        }
                        map.put("output", sw.toString());
                    } else {
                        log.error("The query result set returned null.");
                        map.put("output", "The query result set returned null.");
                    }

                } catch (XQException e) {
                    log.error(e.getMessage());
                    map.put("output", "An XQuery error occurred");
                } catch (TransformerException e) {
                    log.error(e.getMessage());
                    map.put("output", "A transformation error occurred");
                }
            } else {
                map.put("output", "The specified renderer " + formatName + " was not found.");
            }

        } catch (SAXException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (IOException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (TransformerConfigurationException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (TransformerException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (TransformerFactoryConfigurationError e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (ParserConfigurationException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        }

        return map;
    }

    private Node mapToXMLNode(Map<String, String[]> map) {

        Node propertiesNode = null;
        StringBuilder xml = new StringBuilder();

        xml.append("<props xmlns=\"util:properties\">");
        for (String key : map.keySet()) {
            for (String value : map.get(key)) {
                xml.append("<prop name=\"" + key + "\" value=\"" + value + "\"/>");
            }
        }
        xml.append("</props>");

        try {
            propertiesNode = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream((new String(xml)).getBytes())).getDocumentElement();
        } catch (SAXException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (IOException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        } catch (ParserConfigurationException e1) {
            log.error(e1.getMessage());
            //e1.printStackTrace();
        }

        return propertiesNode;
    }

    public String getConfigPath() {

        return configPath;
    }

    public Node getConfigDom() {

        return configDom;
    }

    public long getDelay() {

        return delay;
    }

    public long getFrequency() {

        return frequency;
    }

    public HashMap<String, Query> getPreparedQueries() {

        return preparedQueries;
    }

    public void loadConfiguration() {

        log.info("Instantiating Render Engine...");
        // initialize config file
        File configDirectory = getConfigDirectory();
        try {
            log.info("Reading configDom from " + configDirectory.getAbsolutePath() + "/config.xml");
            File configFile = new File(configDirectory, "config.xml");
            configDom = XmlObject.Factory.parse(configFile).getDomNode();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        // initialize prepared queries
        log.info("Initializing prepared queries");
        preparedQueries = new HashMap<String, Query>();

        // iterate through sub-directory names of transforms directory
        FileFilter dirFilter = new FileFilter() {

            public boolean accept(File file) {

                return file.isDirectory();
            }
        };

        File[] transforms = (new File(configDirectory, "transforms")).listFiles(dirFilter);

        for (File transform : transforms) {
            buildPreparedQuery(transform);
        }
    }

    @Override
    public void onChange(File file, Change change) {

        String fileName = file.getName();
        if (fileName.equals("config.xml")) {
            loadConfiguration();
        } else {
            updateQueries(file, change);
        }
    }

    public void setConfigPath(String configPath) {

        this.configPath = configPath;
    }

    public void setConfigDom(Node configDom) {

        this.configDom = configDom;
    }

    public void setDelay(long delay) {

        this.delay = delay;
    }

    public void setFrequency(long frequency) {

        this.frequency = frequency;
    }

    public void setPreparedQueries(HashMap<String, Query> preparedQueries) {

        this.preparedQueries = preparedQueries;
    }

    @Override
    public void setServletContext(ServletContext context) {

        String path = context.getRealPath(getConfigPath());
        File configDirectory = new File(path);
        if (configDirectory.exists()) {
            log.debug("Watching for new renderers in " + configDirectory.getPath());
            setConfigDirectory(configDirectory);
            loadConfiguration();
            File transformsDirectory = new File(configDirectory, "transforms");
            startDirectoryWatcher(transformsDirectory);
        } else {
            log.debug("Could not find transforms directory " + configDirectory.getPath());
        }
    }

    private void startDirectoryWatcher(File directory) {

        directoryWatcher = new DirectoryWatcher(directory, this, null, true);
        watcherTimer = new Timer();
        watcherTimer.schedule(directoryWatcher, delay, frequency);
    }

    private void updateQueries(File file, Change change) {

        String fileName = file.getName();
        if (Change.DELETED.equals(change)) {
            log.info("Deleted: " + fileName);
            if (preparedQueries.containsKey(fileName)) {
                preparedQueries.remove(fileName);
            }
        } else if (Change.ADDED.equals(change) || Change.MODIFIED.equals(change)) {
            log.info(change.toString() + ": " + fileName);
            if (file.isDirectory()) {
                buildPreparedQuery(file);
            }
        }
    }

    private class Query {

        Properties properties = null;
        XQPreparedExpression query = null;

        public Query(Properties properties, XQPreparedExpression query) {

            this.properties = properties;
            this.query = query;
        }

        public XQPreparedExpression getXQPreparedExpression() {

            return this.query;
        }

        public void setXQPreparedExpression(XQPreparedExpression query) {

            this.query = query;
        }

        public Properties getProperties() {

            return properties;
        }

        public void setProperties(Properties properties) {

            this.properties = properties;
        }
    }
}
