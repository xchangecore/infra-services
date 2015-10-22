package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.coreConfig.CoreConfigListType;
import org.uicds.coreConfig.CoreConfigType;
import org.uicds.coreConfig.CoreStatusType;
import org.uicds.coreConfig.CoreStatusType.Enum;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.externalDataSourceConfig.ExternalDataSourceConfigListType;
import org.uicds.externalDataSourceConfig.ExternalDataSourceConfigType;
import org.uicds.externalToolConfig.ExternalToolConfigListType;
import org.uicds.externalToolConfig.ExternalToolConfigType;
import org.uicds.serviceConfig.ServiceConfigListType;
import org.uicds.serviceConfig.ServiceConfigType;
import org.uicds.sosConfig.SOSConfigListType;
import org.uicds.sosConfig.SOSConfigType;

import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.dao.ExternalDataSourceConfigDAO;
import com.leidos.xchangecore.core.infrastructure.dao.PublishedProductDAO;
import com.leidos.xchangecore.core.infrastructure.dao.RegisteredServiceDAO;
import com.leidos.xchangecore.core.infrastructure.dao.SubscribedProductDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidXpathException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;
import com.leidos.xchangecore.core.infrastructure.listener.NotificationListener;
import com.leidos.xchangecore.core.infrastructure.messages.AgreementRosterMessage;
import com.leidos.xchangecore.core.infrastructure.messages.CoreRosterMessage;
import com.leidos.xchangecore.core.infrastructure.messages.CoreStatusUpdateMessage;
import com.leidos.xchangecore.core.infrastructure.model.ExternalDataSourceConfig;
import com.leidos.xchangecore.core.infrastructure.model.PublishedProduct;
import com.leidos.xchangecore.core.infrastructure.model.RegisteredService;
import com.leidos.xchangecore.core.infrastructure.model.SubscribedProduct;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsService;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.PubSubService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;

/**
 * The DirectoryService implementation
 *
 * @author UICDS team
 * @since 1.0
 * @see com.leidos.xchangecore.core.infrastructure.model.ExternalDataSourceConfig ExternalDataSourceConfig
 *      Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.PublishedProduct PublishedProduct Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.RegisteredService RegisteredService Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.SubscribedProduct SubscribedProduct Data Model
 * @see com.leidos.xchangecore.core.infrastructure.model.WorkProduct WorkProduct Data Model
 * @ssdd
 */
public class DirectoryServiceImpl
implements DirectoryService {

    private class AgreementListener
    implements NotificationListener<AgreementRosterMessage> {

        private final Logger logger = LoggerFactory.getLogger(AgreementListener.class);

        @Override
        public void onChange(AgreementRosterMessage message) {

            logger.info("AgreementListener: onChange: agreementID: " + message.getAgreementID());
            for (final String coreName : message.getCores().keySet()) {
                logger.info("   " + coreName + ":" + message.getCores().get(coreName));
                // If we know about this core then only worry about recind
                if (coreStatusMap.containsKey(coreName))
                    switch (message.getCores().get(coreName)) {
                    case RESCIND: {
                        removeCoreFromStatusMap(coreName);
                        sendMessageToConsole(coreName, "remove");
                    }
                    }
                else
                    switch (message.getCores().get(coreName)) {
                    case CREATE: {
                        addCoreToStatusMap(coreName, CoreStatusType.OFFLINE);
                        sendMessageToConsole(coreName, "create");
                    }
                    }
            }
        }

    }

    class RegisterUICDSServiceRequestData {

        public String urn;
        public String serviceName;
        public WorkProductTypeListType publshiedProducts;
        public WorkProductTypeListType subscribedProducts;

        public RegisterUICDSServiceRequestData(String urn, String serviceName,
                                               WorkProductTypeListType publshiedProducts,
                                               WorkProductTypeListType subscribedProducts) {

            this.urn = urn;
            this.serviceName = serviceName;
            this.publshiedProducts = publshiedProducts;
            this.subscribedProducts = subscribedProducts;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(DirectoryServiceImpl.class);

    private RegisteredServiceDAO registeredServiceDAO;

    private CommunicationsService communicationsService;

    private String resource;

    private String overallCoreStatus = "NORMAL";

    private String localCoreJid;

    private PublishedProductDAO publishedProductDAO;

    private SubscribedProductDAO subscribedProductDAO;

    private ExternalDataSourceConfigDAO externalDataSourceConfigDAO;

    private ConfigurationService configurationService;

    private WorkProductService workProductService;

    private PubSubService pubSubService;

    private AgreementDAO agreementDAO;

    // Need to cache UICDS service requests since they can be made before the bean's session is
    // fully initialized
    List<RegisterUICDSServiceRequestData> cachedUICDSServiceRequests = new ArrayList<RegisterUICDSServiceRequestData>();

    // Key: Core Name
    // Value: Core Status (Online/Offline)
    private final HashMap<String, CoreStatusType.Enum> coreStatusMap = new HashMap<String, CoreStatusType.Enum>();

    // Key: Core Name
    // Value: "lat lon"
    private final HashMap<String, String> coreLocationMap = new HashMap<String, String>();

    // Key: sosID
    // Value: sosURN
    private final HashMap<String, String> sosMap = new HashMap<String, String>();

    private synchronized void addCoreToStatusMap(String coreName, Enum updatedStatus) {

        coreStatusMap.put(coreName, updatedStatus);
    }

    private synchronized void addCoreToStatusMap(String coreName,
                                                 Enum updatedStatus,
                                                 String lat,
                                                 String lon) {

        final String location = lat + " " + lon;
        logger.debug("Adding core, " + coreName + ", to status map.  Location: " + location);
        coreStatusMap.put(coreName, updatedStatus);

        coreLocationMap.put(coreName.toLowerCase(), location);
        logger.debug("Added " + coreName + " to status map - retrieved location: " +
            coreLocationMap.get(coreName));
    }

    /**
     * Core roster handler.
     *
     * @param message the message
     * @ssdd
     */
    @Override
    public void coreRosterHandler(CoreRosterMessage message) {

        final Map<String, String> coreStatusUpdateMap = message.getCoreStatusMap();

        final Set<String> coreNames = coreStatusUpdateMap.keySet();

        for (final String coreName : coreNames) {

            final CoreStatusType.Enum updatedStatus = coreStatusUpdateMap.get(coreName).equals(
                "available") ? CoreStatusType.ONLINE : CoreStatusType.OFFLINE;
            logger.info("coreRosterHandler: [" + coreName + "," + updatedStatus.toString() + "]");
            addCoreToStatusMap(coreName, updatedStatus);

            sendMessageToConsole(coreName, "update");
            sendCoreStatus(coreName, updatedStatus.toString());
        }
    }

    /**
     * Core status update handler.
     *
     * @param message the message
     * @ssdd
     */
    @Override
    public void coreStatusUpdateHandler(CoreStatusUpdateMessage message) {

        final String coreName = message.getCoreName();
        final String coreStatus = message.getCoreStatus();

        logger.debug("coreStatusUpdateHandler: coreName: " + coreName + ", status: " + coreStatus);

        if (coreStatus.equals(CoreStatusUpdateMessage.Status_UnSubscribed)) {
            removeCoreFromStatusMap(coreName);
            sendMessageToConsole(coreName, "remove");
        } else {
            logger.info("coreStatusUpdate: [" + coreName + "], status: " + coreStatus +
                ", mutually agreed: " +
                (getAgreementDAO().isRemoteCoreMutuallyAgreed(coreName) ? "true" : "false"));
            final CoreStatusType.Enum updatedStatus = coreStatus.equals(CoreStatusUpdateMessage.Status_Available) &&
                                                      getAgreementDAO().isRemoteCoreMutuallyAgreed(
                                                          coreName) ? CoreStatusType.ONLINE : CoreStatusType.OFFLINE;

            logger.info("coreStatusUpdate: [" + coreName + "," + updatedStatus.toString() + "]");
            if (!("".equals(message.getCoreLatitude()) || "".equals(message.getCoreLongitude())))
                addCoreToStatusMap(coreName, updatedStatus, message.getCoreLatitude(),
                    message.getCoreLongitude());
            else
                addCoreToStatusMap(coreName, updatedStatus);
        }
        sendMessageToConsole(coreName, "update");
        sendCoreStatus(coreName, coreStatus);

        if (coreStatus.contains("available"))
            sendCoreStatus(coreName, coreStatus);
    }

    public AgreementDAO getAgreementDAO() {

        return agreementDAO;
    }

    public CommunicationsService getCommunicationsService() {

        return communicationsService;
    }

    @Override
    public String getConsoleResource() {

        return resource;
    }

    /**
     * Gets the core config.
     *
     * @param coreName the core name
     *
     * @return the core config
     * @ssdd
     */
    @Override
    public synchronized CoreConfigType getCoreConfig(String coreName) {

        // Get list of cores from Communications Service

        logger.debug("getCoreConfig: " + coreName);
        CoreConfigType coreConfig = null;

        if (coreStatusMap.containsKey(coreName)) {
            coreConfig = CoreConfigType.Factory.newInstance();
            coreConfig.setName(coreName);
            coreConfig.setOnlineStatus(coreStatusMap.get(coreName));

            final String coreNameLower = coreName.toLowerCase();
            if (coreLocationMap.containsKey(coreNameLower)) {
                logger.debug("\tcorename: " + coreNameLower);
                logger.debug("\tlocString: " + coreLocationMap.get(coreNameLower));

                final String[] location = coreLocationMap.get(coreNameLower).split("\\s");
                logger.debug("\tlocation.length: " + location.length);
                logger.debug("\tlocation[]: " + location[0] + " " + location[1]);

                if (location.length == 2) {
                    coreConfig.setLatitude(location[0]);
                    coreConfig.setLongitude(location[1]);
                } else {
                    coreConfig.setLatitude("");
                    coreConfig.setLongitude("");
                }
            } else {
                coreConfig.setLatitude("");
                coreConfig.setLongitude("");
            }
        }
        return coreConfig;
    }

    /**
     * Gets the core list.
     *
     * @return the core list
     * @ssdd
     */
    @Override
    public synchronized CoreConfigListType getCoreList() {

        final CoreConfigListType coreList = CoreConfigListType.Factory.newInstance();

        final Set<String> coreNames = coreStatusMap.keySet();

        String localCoreName = "localhost";
        try {
            localCoreName = InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (final UnknownHostException e) {
            logger.error("Cannot get host name: " + e.getMessage());
        }

        for (final String coreName : coreNames) {
            logger.debug("getCoreList: " + coreName);
            final CoreConfigType coreConfig = coreList.addNewCore();
            coreConfig.setName(coreName);
            coreConfig.setLocalCore(coreName.indexOf(localCoreName) != -1 ? true : false);
            coreConfig.setURL(coreName);
            coreConfig.setOnlineStatus(coreStatusMap.get(coreName));

            final String coreNameLower = coreName.toLowerCase();
            if (coreLocationMap.containsKey(coreNameLower)) {
                logger.debug("corename: " + coreNameLower);
                logger.debug("\tlocString: " + coreLocationMap.get(coreNameLower));

                final String[] location = coreLocationMap.get(coreNameLower).split("\\s");
                logger.debug("\tlocation.length: " + location.length);
                logger.debug("\tlocation[]: " + location[0] + " " + location[1]);

                if (location.length == 2) {
                    coreConfig.setLatitude(location[0]);
                    coreConfig.setLongitude(location[1]);
                } else {
                    coreConfig.setLatitude("");
                    coreConfig.setLongitude("");
                }
            } else {
                coreConfig.setLatitude("");
                coreConfig.setLongitude("");
            }
        }

        return coreList;
    }

    /**
     * Gets the core name.
     *
     * @return the core name
     * @ssdd
     */
    @Override
    public String getCoreName() {

        return configurationService.getCoreName();
    }

    /**
     * Gets the external data source list.
     *
     * @param coreName the core name
     *
     * @return the external data source list
     * @ssdd
     */
    @Override
    public ExternalDataSourceConfigListType getExternalDataSourceList(String coreName) {

        // Get list of data sources from database
        final List<ExternalDataSourceConfig> externalDataSources = externalDataSourceConfigDAO.findByCoreName(coreName);

        final ExternalDataSourceConfigListType externalDataSourceList = ExternalDataSourceConfigListType.Factory.newInstance();

        // Construct the return list from the items retrieved from the database
        for (final ExternalDataSourceConfig externalDataSource : externalDataSources) {
            final ExternalDataSourceConfigType externalDataSourceConfig = externalDataSourceList.addNewExternalDataSource();
            externalDataSourceConfig.setURN(externalDataSource.getUrn());
            externalDataSourceConfig.setCoreName(externalDataSource.getCoreName());
        }
        // }

        return externalDataSourceList;
    }

    /**
     * Gets the external tool list.
     *
     * @param coreName the core name
     *
     * @return the external tool list
     * @ssdd
     */
    @Override
    public ExternalToolConfigListType getExternalToolList(String coreName) {

        // Get list of external tools from database
        final Set<RegisteredService> externalTools = registeredServiceDAO.findByServiceTypeAndCoreName(
            RegisteredService.SERVICE_TYPE.EXTERNAL, coreName);

        final ExternalToolConfigListType externalToolList = ExternalToolConfigListType.Factory.newInstance();

        // Construct the return list from the items retrieved from the database
        for (final RegisteredService externalTool : externalTools) {
            final ExternalToolConfigType externalToolConfig = externalToolList.addNewExternalTool();
            externalToolConfig.setURN(externalTool.getURN());
            externalToolConfig.setCoreName(externalTool.getCoreName());
            externalToolConfig.setToolName(externalTool.getServiceName());

            final WorkProductTypeListType publishedProducts = WorkProductTypeListType.Factory.newInstance();
            for (final PublishedProduct publishedProduct : externalTool.getPublishedProducts())
                publishedProducts.addProductType(publishedProduct.getProductType());
            externalToolConfig.setPublishedProducts(publishedProducts);

            final WorkProductTypeListType subscribedProducts = WorkProductTypeListType.Factory.newInstance();
            for (final SubscribedProduct subscribedProduct : externalTool.getSubscribedProducts())
                subscribedProducts.addProductType(subscribedProduct.getProductType());
            externalToolConfig.setSubscribedProducts(subscribedProducts);
        }
        // }

        return externalToolList;
    };

    /**
     * Gets the incident list.
     *
     * @return the incident list
     * @ssdd
     */
    @Override
    public WorkProduct[] getIncidentList() {

        final ArrayList<WorkProduct> workProducts = new ArrayList<WorkProduct>();
        List<String> incidentWPIDs;
        try {
            incidentWPIDs = workProductService.getProductIDListByTypeAndXQuery("Incident", null,
                null);
            for (final String incidentWPID : incidentWPIDs) {
                final WorkProduct wp = workProductService.getProduct(incidentWPID);
                if (wp != null)
                    workProducts.add(wp);
            }
        } catch (final InvalidXpathException e) {
            logger.error("invalid xpath getting product id by list and XQuery");
        }

        final WorkProduct[] products = new WorkProduct[workProducts.size()];
        return workProducts.toArray(products);

    }

    @Override
    public String getLocalCoreJid() {

        return localCoreJid;
    }

    @Override
    public String getOverallCoreStatus() {

        return overallCoreStatus;
    }

    /**
     * Gets the published product type list.
     *
     * @return the published product type list
     * @ssdd
     */
    @Override
    public WorkProductTypeListType getPublishedProductTypeList() {

        final WorkProductTypeListType productList = WorkProductTypeListType.Factory.newInstance();

        final Set<PublishedProduct> publishedProducts = publishedProductDAO.findAllPublishedProducts();
        for (final PublishedProduct prod : publishedProducts)
            productList.addProductType(prod.getProductType());

        return productList;
    }

    /**
     * Gets the service list.
     *
     * @param coreName the core name
     *
     * @return the service list
     * @ssdd
     */
    @Override
    public ServiceConfigListType getServiceList(String coreName) {

        // if (log.isDebugEnabled()) {
        // log.debug("getServiceList (coreName: " + coreName + ")");
        // }

        // process cache requests
        for (final RegisterUICDSServiceRequestData request : cachedUICDSServiceRequests) {
            logger.info("process cached registration request fro service:" + request.serviceName);
            registerUICDSService(request.urn, request.serviceName, request.publshiedProducts,
                request.subscribedProducts);
        }
        cachedUICDSServiceRequests.clear();

        // Get a list of UICDS services from database
        final Set<RegisteredService> services = registeredServiceDAO.findByServiceTypeAndCoreName(
            RegisteredService.SERVICE_TYPE.UICDS, coreName);

        final ServiceConfigListType serviceList = ServiceConfigListType.Factory.newInstance();

        // Need to put result in hash map first to get rid of duplicates created by the many-to-many
        // associations
        final HashSet<RegisteredService> hashList = new HashSet<RegisteredService>();
        for (final RegisteredService service : services)
            hashList.add(service);

        // Construct the return list from the items retrieved from the database
        for (final RegisteredService service : hashList) {
            // if (log.isDebugEnabled()) {
            // log.debug("getServiceList: found service: " + service.getServiceName() + ")");
            // }

            final ServiceConfigType serviceConfig = serviceList.addNewService();
            serviceConfig.setServiceName(service.getServiceName());
            serviceConfig.setCoreName(service.getCoreName());
            serviceConfig.setURN(service.getURN());

            final WorkProductTypeListType publishedProducts = WorkProductTypeListType.Factory.newInstance();
            for (final PublishedProduct publishedProduct : service.getPublishedProducts())
                publishedProducts.addProductType(publishedProduct.getProductType());
            serviceConfig.setPublishedProducts(publishedProducts);

            final WorkProductTypeListType subscribedProducts = WorkProductTypeListType.Factory.newInstance();
            for (final SubscribedProduct subscribedProduct : service.getSubscribedProducts())
                subscribedProducts.addProductType(subscribedProduct.getProductType());
            serviceConfig.setSubscribedProducts(subscribedProducts);
        }

        return serviceList;
    }

    /**
     * Gets the service name by published product type.
     *
     * @param publishedProductType the published product type
     *
     * @return the service name by published product type
     *
     * @throws InvalidProductTypeException the invalid product type exception
     * @ssdd
     */
    @Override
    public String getServiceNameByPublishedProductType(String publishedProductType)
        throws InvalidProductTypeException {

        if (logger.isInfoEnabled())
            logger.info("getServiceNameByPublishedProductType: productType=" + publishedProductType);
        String serviceName = null;

        final Set<PublishedProduct> publishedProductList = publishedProductDAO.findByProductType(publishedProductType);
        if (publishedProductList.size() == 0)
            throw new InvalidProductTypeException();
        else
            for (final PublishedProduct product : publishedProductList) {
                serviceName = product.getPublisher().getServiceName();
                break;
            }
        if (logger.isInfoEnabled())
            logger.info("getServiceNameByPublishedProductType: serviceName=" + serviceName);
        return serviceName;
    }

    /**
     * Gets the list of sensors.
     *
     * @return the sensor list
     * @ssdd
     */
    @Override
    public SOSConfigListType getSOSList() {

        final Set<String> sosIDs = sosMap.keySet();

        final SOSConfigListType sosList = SOSConfigListType.Factory.newInstance();
        for (final String sosID : sosIDs) {
            final SOSConfigType sos = sosList.addNewSos();
            sos.setServiceID(sosID);
            sos.setURN(sosMap.get(sosID));
        }
        return sosList;
    }

    /**
     * Gets the subscribed product type list.
     *
     * @return the subscribed product type list
     * @ssdd
     */
    @Override
    public WorkProductTypeListType getSubscribedProductTypeList() {

        final WorkProductTypeListType productList = WorkProductTypeListType.Factory.newInstance();

        final Set<SubscribedProduct> subscribedProducts = subscribedProductDAO.findAllSubscribedProducts();
        for (final SubscribedProduct prod : subscribedProducts)
            productList.addProductType(prod.getProductType());

        return productList;
    }

    @PostConstruct
    public void init() {

    }

    @Override
    public boolean isRemoteCoreOnline(String remoteJID) {

        final Enum status = coreStatusMap.get(remoteJID);
        return status == null ? false : status.equals(CoreStatusType.ONLINE) ? true : false;
    }

    /**
     * Register external data source.
     *
     * @param urn the urn
     * @ssdd
     */
    @Override
    public void registerExternalDataSource(String urn) {

        if (logger.isDebugEnabled())
            logger.debug("registerExternalDataSource (urn: " + urn + ")");

        // Persist external data source in the database, overriding any existing one
        final ExternalDataSourceConfig externalDataSourceConfig = new ExternalDataSourceConfig(urn,
                                                                                               configurationService.getCoreName());

        // Delete previous registration (should be just one) if one exists
        final List<ExternalDataSourceConfig> dataSources = externalDataSourceConfigDAO.findByUrn(urn);
        for (final ExternalDataSourceConfig dataSource : dataSources)
            externalDataSourceConfigDAO.makeTransient(dataSource);
        externalDataSourceConfigDAO.makePersistent(externalDataSourceConfig);
    }

    /**
     * Register external tool.
     *
     * @param urn the urn
     * @param toolName the tool name
     * @param publishedProducts the published products
     * @param subscribedProducts the subscribed products
     * @ssdd
     */
    @Override
    public void registerExternalTool(String urn,
                                     String toolName,
                                     WorkProductTypeListType publishedProducts,
                                     WorkProductTypeListType subscribedProducts) {

        if (logger.isDebugEnabled())
            logger.debug("registerExternalTool (urn: " + urn + ")");

        // Persist the external tool in the database, overriding any existing one
        final Set<PublishedProduct> publishedList = new HashSet<PublishedProduct>();
        for (Integer i = 0; i < publishedProducts.sizeOfProductTypeArray(); i++) {
            final PublishedProduct publishedProduct = new PublishedProduct(publishedProducts.getProductTypeArray(i));
            publishedList.add(publishedProduct);
        }

        final Set<SubscribedProduct> subscribedList = new HashSet<SubscribedProduct>();
        for (Integer i = 0; i < subscribedProducts.sizeOfProductTypeArray(); i++) {
            final SubscribedProduct subscribedProduct = new SubscribedProduct(subscribedProducts.getProductTypeArray(i));
            subscribedList.add(subscribedProduct);
        }

        final String coreName = configurationService.getCoreName();
        final RegisteredService externalTool = new RegisteredService(urn,
                                                                     toolName,
                                                                     RegisteredService.SERVICE_TYPE.EXTERNAL,
                                                                     coreName,
                                                                     publishedList,
                                                                     subscribedList);

        // Delete previous registration (should be just one) if one exists
        final Set<RegisteredService> tools = registeredServiceDAO.findByServiceNameAndCoreName(
            toolName, coreName);
        for (final RegisteredService tool : tools)
            registeredServiceDAO.makeTransient(tool);
        registeredServiceDAO.makePersistent(externalTool);
    }

    /**
     * Register sos.
     *
     * @param sosID the sos id
     * @param sosURN the sos urn
     * @ssdd
     */
    @Override
    public void registerSOS(String sosID, String sosURN) {

        sosMap.put(sosID, sosURN);
    }

    /**
     * Register uicds service.
     *
     * @param urn the urn
     * @param serviceName the service name
     * @param publishedProducts the published products
     * @param subscribedProducts the subscribed products
     * @ssdd
     */
    @Override
    public void registerUICDSService(String urn,
                                     String serviceName,
                                     WorkProductTypeListType publishedProducts,
                                     WorkProductTypeListType subscribedProducts) {

        // @Transactional(propagation = Propagation.REQUIRES_NEW)
        if (!registeredServiceDAO.isSessionInitialized()) {
            if (logger.isInfoEnabled())
                logger.info("registerUICDSService - session not yer initialized - cache requrest for (serviceName: " +
                    serviceName + ")");

            // buffer up this request for later
            cachedUICDSServiceRequests.add(new RegisterUICDSServiceRequestData(urn,
                serviceName,
                publishedProducts,
                subscribedProducts));
        } else {

            if (logger.isInfoEnabled())
                logger.info("===> registerUICDSService (serviceName: " + serviceName + ")");

            // Persist the external tool in the database if one doesn't already exist
            final Set<PublishedProduct> publishedList = new HashSet<PublishedProduct>();
            for (Integer i = 0; i < publishedProducts.sizeOfProductTypeArray(); i++) {
                if (logger.isInfoEnabled())
                    logger.info("=====> registerUICDSService (publishedProductType: " +
                        publishedProducts.getProductTypeArray(i) + ")");
                final PublishedProduct publishedProduct = new PublishedProduct(publishedProducts.getProductTypeArray(i));
                publishedList.add(publishedProduct);
            }

            final Set<SubscribedProduct> subscribedList = new HashSet<SubscribedProduct>();
            for (Integer i = 0; i < subscribedProducts.sizeOfProductTypeArray(); i++) {
                logger.info("=====> registerUICDSService (subscribedproductType: " +
                    subscribedProducts.getProductTypeArray(i) + ")");
                final SubscribedProduct subscribedProduct = new SubscribedProduct(subscribedProducts.getProductTypeArray(i));
                subscribedList.add(subscribedProduct);
            }

            // Persist the UICDS service in the database overriding any existing one
            final String coreName = configurationService.getCoreName();
            final RegisteredService service = new RegisteredService(urn,
                                                                    serviceName,
                                                                    RegisteredService.SERVICE_TYPE.UICDS,
                                                                    coreName,
                                                                    publishedList,
                                                                    subscribedList);

            // Delete previous registration (should be just one) if one exists
            final Set<RegisteredService> services = registeredServiceDAO.findByServiceNameAndCoreName(
                serviceName, coreName);

            for (final RegisteredService svc : services) {
                if (logger.isDebugEnabled())
                    logger.debug("Remove existing registration for " +
                        svc.getServiceName().toString());
                registeredServiceDAO.makeTransient(svc);
            }

            if (logger.isDebugEnabled())
                logger.debug("adding new registration for " + serviceName);
            registeredServiceDAO.makePersistent(service);

        }
    }

    private synchronized void removeCoreFromStatusMap(String coreName) {

        coreStatusMap.remove(coreName);
        coreLocationMap.remove(coreName);
    }

    private void sendCoreStatus(String coreName, String coreStatus) {

        if (coreStatus.equals("available") || coreStatus.toLowerCase().contains("online")) {
            final String jid = getCoreName();
            String message = "Remote-CoreStatus: [" + jid + "]\n";
            message += "Status: [" + getOverallCoreStatus() + "]";
            final String resource = getConsoleResource();
            String remoteJid = coreName;
            if (!remoteJid.contains("/"))
                remoteJid = coreName + "/" + resource;
            if (resource != null)
                getCommunicationsService().sendXMPPMessage(message, "message", "message", remoteJid);
        }
    }

    private void sendMessageToConsole(String coreName, String operation) {

        String updateMessage = "DirectoryService-CoreStatus:[" + coreName + "]\n";
        updateMessage += "Operation:[" + operation + "]";
        communicationsService.sendXMPPMessage(updateMessage, "message", "message",
            getLocalCoreJid());
    }

    public void setAgreementDAO(AgreementDAO agreementDAO) {

        this.agreementDAO = agreementDAO;
    }

    public void setCommunicationsService(CommunicationsService communicationsService) {

        this.communicationsService = communicationsService;
    }

    public void setConfigurationService(ConfigurationService ds) {

        configurationService = ds;
    }

    @Override
    public void setConsoleResource(String resource) {

        this.resource = resource;
    }

    public void setExternalDataSourceConfigDAO(ExternalDataSourceConfigDAO externalDataSourceConfigDAO) {

        this.externalDataSourceConfigDAO = externalDataSourceConfigDAO;
    }

    @Override
    public void setLocalCoreJid(String localCoreJid) {

        this.localCoreJid = localCoreJid;
    }

    @Override
    public void setOverallCoreStatus(String category) {

        overallCoreStatus = category;
    }

    public void setPublishedProductDAO(PublishedProductDAO publishedProductDAO) {

        this.publishedProductDAO = publishedProductDAO;
    }

    public void setPubSubService(PubSubService pubSubService) {

        this.pubSubService = pubSubService;
    }

    public void setRegisteredServiceDAO(RegisteredServiceDAO registeredServiceDAO) {

        this.registeredServiceDAO = registeredServiceDAO;
    }

    public void setSubscribedProductDAO(SubscribedProductDAO subscribedDAO) {

        subscribedProductDAO = subscribedDAO;
    }

    public void setWorkProductService(WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    /** {@inheritDoc} */
    @Override
    public void systemInitializedHandler(String messgae) {

        logger.debug("systemInitializedHandler: ... start ...");
        logger.debug("systemInitializedHandler: localJID: " + getLocalCoreJid());

        try {
            // call getServiceList so any cached registration requests get processed
            final ServiceConfigListType serviceList = getServiceList(getLocalCoreJid());
            if (logger.isDebugEnabled()) {
                logger.debug("number of registered services=" + serviceList.sizeOfServiceArray());
                logger.debug("DirectoryServiceImpl:systemInitializedHandler - completed");
            }
        } catch (final Throwable e) {
            logger.error("Exception caught while getting service list.   exception=" +
                e.getMessage());
            e.printStackTrace();
        }

        // Subscribe for notifications of changes in agreements
        try {
            pubSubService.addAgreementListener(-1, new AgreementListener());
        } catch (final InvalidProductIDException e) {
            logger.error("Agreement subscription has invalid product id");
        } catch (final NullSubscriberException e) {
            logger.error("Agreement subscription has null subscriber");
        } catch (final EmptySubscriberNameException e) {
            logger.error("Agreement subscription has empty subscriber name");
        }
        logger.debug("systemInitializedHandler: ... done ...");
    }

    /**
     * Unregister external data source.
     *
     * @param urn the urn
     * @ssdd
     */
    @Override
    public void unregisterExternalDataSource(String urn) {

        if (logger.isDebugEnabled())
            logger.debug("unregisterExternalDataSource (urn: " + urn + ")");

        // Delete previous registration (should be just one) if one exists
        final List<ExternalDataSourceConfig> dataSources = externalDataSourceConfigDAO.findByUrn(urn);
        for (final ExternalDataSourceConfig dataSource : dataSources)
            externalDataSourceConfigDAO.makeTransient(dataSource);
    }

    /**
     * Unregister external tool.
     *
     * @param urn the urn
     * @ssdd
     */
    @Override
    public void unregisterExternalTool(String urn) {

        if (logger.isDebugEnabled())
            logger.debug("unregisterExternalTool (urn: " + urn + ")");

        final String coreName = configurationService.getCoreName();

        // Delete previous registration (should be just one) if one exists
        final Set<RegisteredService> tools = registeredServiceDAO.findByUrnAndCoreName(urn,
            coreName);
        for (final RegisteredService tool : tools)
            registeredServiceDAO.makeTransient(tool);
    }

    /**
     * Unregister sos.
     *
     * @param sosID the sos id
     * @ssdd
     */
    @Override
    public void unregisterSOS(String sosID) {

        sosMap.remove(sosID);
    }

    /**
     * Unregister uicds service.
     *
     * @param serviceName the service name
     * @ssdd
     */
    @Override
    public void unregisterUICDSService(String serviceName) {

        if (logger.isDebugEnabled())
            logger.debug("unregisterUICDSService (serviceName: " + serviceName + ")");

        final String coreName = configurationService.getCoreName();

        // Delete previous registration (should be just one) if one exists
        final Set<RegisteredService> services = registeredServiceDAO.findByServiceNameAndCoreName(
            serviceName, coreName);
        for (final RegisteredService svc : services)
            registeredServiceDAO.makeTransient(svc);

    }

}
