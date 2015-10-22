package com.leidos.xchangecore.core.infrastructure.service;

import org.apache.xmlbeans.XmlObject;

/**
 * The Configuration Services provides methods for retrieving identification
 * information from the local core.
 * 
 * @author roger
 * @ssdd
 */
public interface ConfigurationService {

    /**
     * Gets the core name.
     * 
     * @return the core name
     * @ssdd
     */
    public XmlObject getConfig();

    /**
     * Gets the core name.
     * 
     * @return the core name
     * @ssdd
     */
    public String getCoreName();

    /**
     * Gets the host name.
     * 
     * @return the host name
     * @ssdd
     */
    public String getHostName();

    /**
     * Gets the fully qualified host name.
     * 
     * @return the fully qualified host name
     * @ssdd
     */
    public String getFullyQualifiedHostName();

    /**
     * Gets the service name urn.
     * 
     * @param serviceName the service name
     * 
     * @return the service name urn
     * @ssdd
     */
    public String getServiceNameURN(String serviceName);

    /**
     * Gets the fully qualified service name urn.
     * 
     * @param serviceName the service name
     * 
     * @return the fully qualified service name urn
     * @ssdd
     */
    public String getFullyQualifiedServiceNameURN(String serviceName);

    /**
     * Gets the web service base url.
     * 
     * @return the web service base url
     * @ssdd
     */
    public String getWebServiceBaseURL();

    /**
     * Gets the rest base url.
     * 
     * @return the rest base url
     * @ssdd
     */
    public String getRestBaseURL();
}
