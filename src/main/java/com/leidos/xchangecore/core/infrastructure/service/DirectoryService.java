package com.leidos.xchangecore.core.infrastructure.service;

import org.springframework.transaction.annotation.Transactional;
import org.uicds.coreConfig.CoreConfigListType;
import org.uicds.coreConfig.CoreConfigType;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.externalDataSourceConfig.ExternalDataSourceConfigListType;
import org.uicds.externalToolConfig.ExternalToolConfigListType;
import org.uicds.serviceConfig.ServiceConfigListType;
import org.uicds.sosConfig.SOSConfigListType;

import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.messages.CoreRosterMessage;
import com.leidos.xchangecore.core.infrastructure.messages.CoreStatusUpdateMessage;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;

/**
 * Manages system information about the local core.
 *
 * @author Aruna Hau
 * @since 1.0
 * @ssdd
 *
 */
@Transactional
public interface DirectoryService {

    /**
     * Handles the message containing a roster of known cores.
     *
     * @param message containing a roster of known cores
     * @see CoreRosterMessage
     * @ssdd
     */
    public void coreRosterHandler(CoreRosterMessage message);

    /**
     * handles the message containing a core status update
     *
     * @param message containing core status update
     * @see CoreStatusUpdateMessage
     * @ssdd
     */
    public void coreStatusUpdateHandler(CoreStatusUpdateMessage message);

    public String getConsoleResource();

    /**
     * Retrieves the configuration of a given core, including its address and online status.
     *
     * @return configuration data
     * @see CoreConfigType
     * @ssdd
     */
    public CoreConfigType getCoreConfig(String coreName);

    /**
     * Retrieves a list of cores, their addresses, and online status.
     *
     * @return list of known cores
     * @see CoreConfigListType
     * @ssdd
     */
    public CoreConfigListType getCoreList();

    /**
     * Retrieves the name of local XchangeCore core
     *
     * @return coreName: String
     * @ssdd
     */
    public String getCoreName();

    /**
     * Retrieves a list of incidents that are active in the XchangeCore core.
     *
     * @return list of incidents
     * @see IncidentInfoListType
     * @ssdd
     */

    /**
     * Retrieves a list of registered external data sources
     *
     * @param coreName String
     * @return list of registered external data sources
     * @see ExternalDataSourceConfigListType
     * @ssdd
     */
    public ExternalDataSourceConfigListType getExternalDataSourceList(String coreName);

    /**
     * Retrieves a list of external tools and their WPS interface URLs that are registered with the
     * XchangeCore core.
     *
     * @param coreName String
     * @return list of external tools
     * @see ExternalToolConfigListType
     * @ssdd
     */
    public ExternalToolConfigListType getExternalToolList(String coreName);

    /**
     * Retrieves a list of incidents that are active in the XchangeCore core.
     *
     * @return list of incidents
     * @see IncidentInfoListType
     * @ssdd
     */
    public WorkProduct[] getIncidentList();

    public String getLocalCoreJid();

    public String getOverallCoreStatus();

    /**
     * Returns a list of all published work product types
     *
     * @return list of all published work product types
     * @see workProductTypeList
     * @ssdd
     */
    public WorkProductTypeListType getPublishedProductTypeList();

    /**
     * Retrieves a list of services that are registered with the XchangeCore core.
     *
     * @param coreName
     * @return list of services
     * @see ServiceConfigListType
     * @ssdd
     */
    public ServiceConfigListType getServiceList(String coreName);

    /**
     * Returns the name of the XchangeCore service which published a given work product type
     *
     * @param publishedProductType the published product type
     * @return service name - the service name
     *
     * @exception InvalidProductTypeException - the specified work product type is not currently
     *                published
     * @ssdd
     */
    public String getServiceNameByPublishedProductType(String publishedProductType)
        throws InvalidProductTypeException;

    /**
     * Retrieves a list of sensors that are registered with the XchangeCore core.
     *
     * @return list of sensors
     * @see SOSConfigListType
     * @ssdd
     */
    public SOSConfigListType getSOSList();

    /**
     * Returns a list of all subscribed work product types
     *
     * @return list of all subscribed work product types
     * @see workProductTypeList
     * @ssdd
     */
    public WorkProductTypeListType getSubscribedProductTypeList();

    boolean isRemoteCoreOnline(String remoteJID);

    /**
     * Registers an external data source that implements the Open Search interface with XchangeCore.
     *
     * @param urn the URL for the external data source
     * @ssdd
     */
    public void registerExternalDataSource(String urn);

    /**
     * Registers an external tool that implements the WPS interface with XchangeCore.
     *
     * @param urn the URL for the external tools
     * @param toolName the tool name
     * @param publishedProducts
     * @see WorkProductTypeListType
     * @param subscribedProducts
     * @see WorkProductTypeListType
     * @ssdd
     */
    public void registerExternalTool(String urn,
                                     String toolName,
                                     WorkProductTypeListType publishedProducts,
                                     WorkProductTypeListType subscribedProducts);

    /**
     * Registers a sensor observation service
     *
     * @param sosID the Sensor Observation Service ID
     * @param sosURN the Sensor Observation Service URN
     * @ssdd
     */
    public void registerSOS(String sensorID, String sosURN);

    /**
     * Registers a XchangeCore service.
     *
     * @param urn the URL for the service
     * @param serviceName the service name
     * @param publishedProducts
     * @see WorkProductTypeListType
     * @param subscribedProducts
     * @see WorkProductTypeListType
     * @ssdd
     */
    public void registerUICDSService(String urn,
                                     String serviceName,
                                     WorkProductTypeListType publishedProducts,
                                     WorkProductTypeListType subscribedProducts);

    public void setConsoleResource(String resource);

    public void setLocalCoreJid(String localCoreJid);

    public void setOverallCoreStatus(String category);

    /**
     * Handles the SystemInitialized message.
     *
     * @param messgae the initialization message
     * @ssdd
     */
    public void systemInitializedHandler(String messgae);

    /**
     * Unregisters an external data source.
     *
     * @param urn the URL for the external data source
     * @ssdd
     */
    public void unregisterExternalDataSource(String urn);

    /**
     * Unregisters an external tool with UICDS.
     *
     * @param urn the URL for the external tool
     * @ssdd
     */
    public void unregisterExternalTool(String urn);

    /**
     * Unregisters a sensor observation service.
     *
     * @param sosID the Sensor Observation Service ID
     * @ssdd
     */
    public void unregisterSOS(String sosID);

    /**
     * Unregisters a UICDS service.
     *
     * @param serviceName the service name
     * @ssdd
     */
    public void unregisterUICDSService(String serviceName);

}
