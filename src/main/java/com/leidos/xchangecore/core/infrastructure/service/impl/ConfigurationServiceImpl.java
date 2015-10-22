package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;

/**
 * The ConfigurationService Implementation
 *
 * @ssdd
 */
public class ConfigurationServiceImpl
    implements ConfigurationService {

    /** The log. */
    Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    /** The Constant serviceUrnPrefix. */
    static final String serviceUrnPrefix = "urn:xchangecore:service:";

    /** The base url. */
    private static String baseURL = "https://localhost/xchangecore/core/ws/service";

    /** The rest base url. */
    private static String restBaseURL = "https://localhost/xchangecore/api/";

    /** The configuration data */
    private XmlObject config;

    /** The core jid. */
    private String coreJID;

    // set to a default value for the JUnit test
    /** The core name. */
    private String coreName = "xchangecore@localhost";

    private DirectoryService directoryService;

    /**
     * Gets the core name.
     *
     * @return the core name
     * @ssdd
     */
    @Override
    public XmlObject getConfig() {

        /*
        try {
            XQueryService queryService =
                (XQueryService) xmldbConnection.getRootCollection().getService("XQueryService",
                    "1.0");
            String query = "xquery version \"1.0\"; " + "return " + "fn:doc(\"config.xml\")";
            CompiledExpression cquery = queryService.compile(query);
            ResourceSet result = queryService.execute(cquery);
            if (result.getSize() >= 1) {
                ResourceIterator it = result.getIterator();
                Resource resource = it.nextResource();
                return XmlObject.Factory.parse(resource.toString());
            } else
                return null;
        } catch (XMLDBException xmldbe) {
            log.error(xmldbe.getMessage());
            return null;
        } catch (XmlException xmle) {
            log.error(xmle.getMessage());
            return null;
        }
         */
        return null;
    }

    /**
     * Gets the core name.
     *
     * @return the core name
     * @ssdd
     */
    @Override
    public String getCoreName() {

        return coreName;
    }

    public DirectoryService getDirectoryService() {

        return directoryService;
    }

    /**
     * Gets the fully qualified host name.
     *
     * @return the fully qualified host name
     * @ssdd
     */
    @Override
    public String getFullyQualifiedHostName() {

        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            hostname = "Unknown Host";
        }
        return hostname;
    }

    /**
     * Gets the fully qualified service name urn.
     *
     * @param serviceName the service name
     *
     * @return the fully qualified service name urn
     * @ssdd
     */
    @Override
    public String getFullyQualifiedServiceNameURN(String serviceName) {

        return getServiceUrnPrefix() + serviceName;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     * @ssdd
     */
    @Override
    public String getHostName() {

        /*
         * String hostname = null; try { hostname =
         * InetAddress.getLocalHost().getHostName().toLowerCase(); } catch (UnknownHostException e)
         * { e.printStackTrace(); hostname = "Unknown Host"; } return hostname;
         */

        return getFullyQualifiedHostName();
    }

    /**
     * Gets the rest base url.
     *
     * @return the rest base url
     * @ssdd
     */
    @Override
    public String getRestBaseURL() {

        return restBaseURL;
    }

    /**
     * Gets the service name urn.
     *
     * @param serviceName the service name
     *
     * @return the service name urn
     * @ssdd
     */
    @Override
    public String getServiceNameURN(String serviceName) {

        return serviceUrnPrefix + serviceName;
    }

    /**
     * Gets the service urn prefix.
     *
     * @return the service urn prefix
     * @ssdd
     */
    public String getServiceUrnPrefix() {

        return serviceUrnPrefix + getFullyQualifiedHostName() + ".";
    }

    /**
     * Gets the web service base url.
     *
     * @return the web service base url
     * @ssdd
     */
    @Override
    public String getWebServiceBaseURL() {

        return baseURL;
    }

    /**
     * Inits the.
     */
    @PostConstruct
    public void init() {

        String fqn = getFullyQualifiedHostName();
        if (fqn != null && !fqn.isEmpty()) {
            baseURL = baseURL.replace("localhost", fqn);
            restBaseURL = restBaseURL.replace("localhost", fqn);
        }

        if (coreJID != null) {
            coreName = coreJID;
        }
        coreName = coreName.replace("localhost", fqn);
        getDirectoryService().setLocalCoreJid(coreName);
        logger.debug("init: set localJID: " + coreName);
    }

    /**
     * Sets the base url.
     *
     * @param url the new base url
     * @ssdd
     */
    public void setBaseURL(String url) {

        baseURL = url;
    }

    /**
     * Sets the core jid.
     *
     * @param coreJID the new core jid
     * @ssdd
     */
    public void setCoreJID(String coreJID) {

        this.coreJID = coreJID;
    }

    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }
}
