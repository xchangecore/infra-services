package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;

import com.leidos.xchangecore.core.infrastructure.dao.InterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.dao.WorkProductDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidXpathException;
import com.leidos.xchangecore.core.infrastructure.messages.JoinedPublishProductRequestMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductChangeNotificationMessage;
import com.leidos.xchangecore.core.infrastructure.messages.ProductToInterestGroupAssociationMessage;
import com.leidos.xchangecore.core.infrastructure.model.InterestGroup;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.DirectoryService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.util.DocumentUtil;
import com.leidos.xchangecore.core.infrastructure.util.LdapUtil;
import com.leidos.xchangecore.core.infrastructure.util.LogEntry;
import com.leidos.xchangecore.core.infrastructure.util.ServletUtil;
import com.leidos.xchangecore.core.infrastructure.util.UUIDUtil;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductUtil;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;
import com.saic.precis.x2009.x06.structures.WorkProductIdentificationDocument;
import com.saic.precis.x2009.x06.structures.WorkProductPropertiesDocument;

/**
 * The WorkProductService implementation. WorkProducts are maintained in a hibernate model.
 *
 * @see com.leidos.xchangecore.core.infrastructure.model.WorkProduct WorkProduct Data Model
 * @ssdd
 */
public class WorkProductServiceImpl
    implements WorkProductService {

    private final static Logger logger = LoggerFactory.getLogger(WorkProductServiceImpl.class);

    private static final int KiloBytes = 1024;
    private static final String PRODUCT_TYPE_INCIDENT = "Incident";
    private static final String CHARACTER_AT = "@";
    private static final int INDEX_ZERO = 0;

    private InterestGroupDAO interestGroupDAO;
    private WorkProductDAO workProductDAO;
    private DirectoryService directoryService;
    private ConfigurationService configurationService;
    private MessageChannel productAssociationChannel;
    private MessageChannel productChangeNotificationChannel;
    private MessageChannel joinedPublishProductRequestChannel;
    private LdapUtil ldapUtil;

    // private MessageChannel getProductResponseChannel;

    /**
     * Delete and Archive a work product. The work product must be associated with an interest group
     * owned by the local core. The work product must be already closed .
     *
     * @param identifier the identifier
     *
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus archiveProduct(IdentificationType identifier) {

        if ((identifier.getIdentifier() == null) ||
            (identifier.getIdentifier().getStringValue() == null)) {
            return new ProductPublicationStatus("No product Identifier to be archived");
        }

        final String productID = identifier.getIdentifier().getStringValue();
        final WorkProduct product = getWorkProductDAO().findByProductID(productID);
        if (product == null) {
            return new ProductPublicationStatus(productID + " cannot be located in repository");
        }

        // figure out whether this product associate to an interest group or not
        // if it's associated with an IG then check whether this core owns it.

        final Set<String> igIDSet = product.getAssociatedInterestGroupIDs();
        boolean isOwner = false;
        if ((igIDSet != null) && (igIDSet.size() > 0)) {
            for (final String igID : igIDSet) {
                if (getInterestGroupDAO().ownedByCore(igID, getDirectoryService().getLocalCoreJid()) == true) {
                    isOwner = true;
                    break;
                }
            }
            // not the owner
            if (isOwner == false) {
                return new ProductPublicationStatus(getDirectoryService().getCoreName() +
                                                    " is not owner: Cannot archive product: " +
                                                    productID);
            }
        }

        if (product.isActive() == true) {
            return new ProductPublicationStatus(productID + " has to be closed first");
        }

        final ProductPublicationStatus status = deleteWorkProduct(productID);
        // even it's archive, we can still return the latest version of product
        status.setProduct(product);
        return status;
    }

    /**
     * Associate work product to interest group.
     *
     * @param workProductID the work product id
     * @param interestGroupID the interest group id
     *
     * @return the string
     * @ssdd
     */
    @Override
    public String associateWorkProductToInterestGroup(String workProductID, String interestGroupID) {

        final WorkProduct wp = getWorkProductDAO().findByProductID(workProductID);
        if (wp == null) {
            return null;
        }

        // verify that the association doesn't already exist
        if ((interestGroupID != null) &&
            (wp.getAssociatedInterestGroupIDs().contains(interestGroupID) == false)) {
            wp.associateInterestGroup(interestGroupID);
            publishProduct(wp);
            notifyOfWorkProductInterestGroupAssociation(wp,
                                                        interestGroupID,
                                                        ProductToInterestGroupAssociationMessage.AssociationType.Associate);
        }
        return wp.getProductID();
    }

    /**
     * Close product publishes a new version of the product that is marked inactive. The interest
     * group must be owned by the local core.
     *
     * @param productID the product id
     *
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus closeProduct(IdentificationType productID) {

        if (productID == null) {
            return new ProductPublicationStatus("No work product identifier specified");
        }

        final WorkProduct product = getProduct(productID);
        if (product == null) {
            return new ProductPublicationStatus(productID + " cannot be located in repository");
        }

        logger.debug("closeProduct: found: " + product.getMetadata());

        // figure out whether this product associate to an interest group or not
        // if it's associated with an IG then check whether this core owns it.

        final Set<String> igIDSet = product.getAssociatedInterestGroupIDs();
        boolean isOwner = false;

        if (igIDSet != null) {
            if (igIDSet.size() > 0) {
                for (final String igID : igIDSet) {
                    if (getInterestGroupDAO().ownedByCore(igID,
                                                          getDirectoryService().getLocalCoreJid()) == true) {
                        isOwner = true;
                        break;
                    }
                }
                // not the owner
                if (isOwner == false) {
                    return new ProductPublicationStatus(getDirectoryService().getCoreName() +
                                                        " is not owner: Cannot close product: " +
                                                        productID);
                }
            }
        }
        final WorkProduct theProduct = new WorkProduct(product);
        theProduct.setActive(false);
        return publishIt(theProduct, getUserID());
    }

    /**
     * The method will not generate a new version of the work product
     *
     * @param productId
     * @param act
     * @return private ProductPublicationStatus deleteIt(String productId, String act) {
     *         ProductPublicationStatus status = new ProductPublicationStatus();
     *
     *         try { WorkProduct product = getWorkProductDAO().findByProductID(productId);
     *         getWorkProductDAO().makeTransient(product);
     *         status.setStatus(ProductPublicationStatus.SuccessStatus); status.setProduct(product);
     *         return status; } catch (Exception e) { log.error("deleteWorkProduct: " +
     *         e.getMessage()); } return status; }
     */

    private ProductPublicationStatus deleteIt(String productID, boolean needNotify) {

        final ProductPublicationStatus status = new ProductPublicationStatus();
        try {

            final WorkProduct product = getWorkProductDAO().findByProductID(productID);
            if ((product == null) || product.isActive()) {
                return new ProductPublicationStatus(productID + " has NOT been closed yet");
            }

            final WorkProductIdentificationDocument identification = WorkProductIdentificationDocument.Factory.newInstance();
            final WorkProductPropertiesDocument properties = WorkProductPropertiesDocument.Factory.newInstance();

            identification.setWorkProductIdentification(WorkProductHelper.getWorkProductIdentification(product));
            properties.setWorkProductProperties(WorkProductHelper.getWorkProductProperties(product));

            final String interestGroupID = product.getFirstAssociatedInterestGroupID();
            if (needNotify && (interestGroupID != null)) {
                // need to un-associate the product with the interest group
                notifyOfWorkProductInterestGroupAssociation(product,
                                                            interestGroupID,
                                                            ProductToInterestGroupAssociationMessage.AssociationType.Unassociate);
            }

            // notify PubSub of the delete
            if ((identification.isNil() == false) && (properties.isNil() == false)) {
                logger.debug("deleteIt: sending DELETE message for product ID=" + productID);
                notifyOfWorkProductChange(identification,
                                          properties,
                                          ProductChangeNotificationMessage.ChangeIndicator.Delete);
                logger.debug("deleteIt: sending DELETE message for product ID=" + productID +
                             " ... done ...");
            } else {
                logger.error("deleteIt: cannot obtain product type from the closed product ID=" +
                             productID + " DELETE message not sent!");
            }

            status.setStatus(ProductPublicationStatus.SuccessStatus);
        } catch (final Exception e) {
            logger.error("deleteWorkProduct: " + e.getMessage());
            status.setStatus(ProductPublicationStatus.FailureStatus);
            status.setReasonForFailure("deleteWorkProduct failed: " + e.getMessage());
        }

        final List<WorkProduct> productList = getWorkProductDAO().findAllVersionOfProduct(productID);
        for (final WorkProduct p : productList) {
            logger.debug("deleteIt: makeTransient : " + p.getProductID() + "/" + p.getId() + "/");
            getWorkProductDAO().deleteProduct(p.getId());
        }

        return status;
    }

    /**
     * Queries the DAO to get all versions, including closed versions, of the workproduct and marks
     * them as transient. By default, workproducts are de-associated from interest groups with which
     * they are associated.
     *
     * @param productID the product id
     *
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus deleteWorkProduct(String productID) {

        return deleteIt(productID, true);
    }

    /**
     * Delete work product, but does de-associate workproducts from associated interest groups.
     *
     * @param productID the product id
     *
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus deleteWorkProductWithoutNotify(String productID) {

        return deleteIt(productID, false);
    }

    private WorkProduct doPublish(WorkProduct theProduct, boolean doNotifications) {

        WorkProduct product = null;

        logger.debug("doPublish: " + theProduct.getMetadata());

        WorkProductIdentificationDocument identification = null;
        WorkProductPropertiesDocument properties = null;
        try {
            product = getWorkProductDAO().makePersistent(theProduct);
            logger.debug("doPublish: published: " + product.getMetadata());

            identification = WorkProductIdentificationDocument.Factory.newInstance();
            identification.addNewWorkProductIdentification().set(WorkProductHelper.getWorkProductIdentification(product));

            properties = WorkProductPropertiesDocument.Factory.newInstance();
            properties.addNewWorkProductProperties().set(WorkProductHelper.getWorkProductProperties(product));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if ((product != null) && doNotifications) {
            notifyOfWorkProductChange(identification,
                                      properties,
                                      ProductChangeNotificationMessage.ChangeIndicator.Publish);
        }

        return product;
    }

    /**
     * Find by interest group and type.
     *
     * @param interestGroupID the interest group id
     * @param productType the product type
     *
     * @return the list< work product>
     * @ssdd
     */
    @Override
    public List<WorkProduct> findByInterestGroupAndType(String interestGroupID, String productType) {

        return getWorkProductDAO().findByInterestGroupAndType(interestGroupID, productType);
    }

    // public IncidentDAO getIncidentDAO() {
    // return incidentDAO;
    // }

    @Override
    public List<WorkProduct> getAllVersionsOfProduct(String id) {

        if (id != null) {
            return getWorkProductDAO().findAllVersionOfProduct(id);
        } else {
            return null;
        }
    }

    /**
     * Gets the list of work products associated with and interest group.
     *
     * @param interestGroupId the interest group id
     *
     * @return the associated work product list
     * @ssdd
     */
    @Override
    public WorkProduct[] getAssociatedWorkProductList(String interestGroupId) {

        final List<WorkProduct> productList = workProductDAO.findByInterestGroup(interestGroupId);
        WorkProduct[] products = null;
        if ((productList != null) && (productList.size() > 0)) {
            products = new WorkProduct[productList.size()];
        } else {
            products = new WorkProduct[0];
        }
        return productList.toArray(products);
    }

    public ConfigurationService getConfigurationService() {

        return configurationService;
    }

    public DirectoryService getDirectoryService() {

        return directoryService;
    }

    private String getIdAttribute(XmlObject object) {

        String id = null;
        final XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        id = cursor.getAttributeText(NiemIdQName);
        cursor.dispose();
        return id;
    }

    public InterestGroupDAO getInterestGroupDAO() {

        return interestGroupDAO;
    }

    public LdapUtil getLdapUtil() {

        return ldapUtil;
    }

    /**
     * Gets the product using the IdentificationType package id
     *
     * @param pkgId the package id that identifies the work product
     *
     * @return the product
     * @ssdd
     */
    @Override
    public WorkProduct getProduct(IdentificationType pkgId) {

        if (pkgId != null) {
            return getWorkProductDAO().findByWorkProductIdentification(pkgId);
        } else {
            return null;
        }
    }

    /**
     * Gets the product using the work product id string
     *
     * @param id the id
     *
     * @return the product
     * @ssdd
     */
    @Override
    public WorkProduct getProduct(String id) {

        if (id != null) {
            return getWorkProductDAO().findByProductID(id);
        } else {
            return null;
        }
    }

    /**
     * Gets the product association channel.
     *
     * @return the product association channel
     */
    public MessageChannel getProductAssociationChannel() {

        return productAssociationChannel;
    }

    /**
     * Gets the product by type and x query.
     *
     * @param productType the product type
     * @param query the query
     * @param namespaceMap the namespace map
     *
     * @return the product by type and x query
     * @ssdd
     */
    @Override
    public List<WorkProduct> getProductByTypeAndXQuery(String productType,
                                                       String query,
                                                       Map<String, String> namespaceMap)
        throws InvalidXpathException {

        final List<WorkProduct> listOfProducts = new ArrayList<WorkProduct>();
        final List<WorkProduct> products = getWorkProductDAO().findByProductType(productType);
        if ((products != null) && (products.size() > 0)) {
            for (final WorkProduct product : products) {
                if ((query == null) || (query.length() == 0) || (namespaceMap == null) ||
                    (namespaceMap.size() == 0) ||
                    DocumentUtil.exist(query, product.getProduct(), namespaceMap)) {
                    listOfProducts.add(product);
                }
            }
        }
        return listOfProducts;
    }

    /**
     * Gets the product by identifier and version.
     *
     * @param id the id
     * @param productVersion the product version
     *
     * @return the producti by version
     * @ssdd
     */
    @Override
    public WorkProduct getProductiByVersion(String id, Integer productVersion) {

        if (id != null) {
            return getWorkProductDAO().findByProductIDAndVersion(id, productVersion);
        } else {
            return null;
        }
    }

    /**
     * Gets the product identification using the string id.
     *
     * @param id the id
     *
     * @return the product identification
     * @ssdd
     */
    @Override
    public IdentificationType getProductIdentification(String id) {

        if (id != null) {
            final WorkProduct product = getWorkProductDAO().findByProductID(id);
            if (product == null) {
                return null;
            }
            return WorkProductHelper.getWorkProductIdentification(product);
        } else {
            return null;
        }
    }

    /**
     * Gets the product id list by type and x query.
     *
     * @param productType the product type
     * @param query the query
     * @param namespaceMap the namespace map
     *
     * @return the product id list by type and x query
     *
     * @throws InvalidXpathException the invalid xpath exception
     * @ssdd
     */
    @Override
    public List<String> getProductIDListByTypeAndXQuery(String productType,
                                                        String query,
                                                        Map<String, String> namespaceMap)
        throws InvalidXpathException {

        final ArrayList<String> productIDs = new ArrayList<String>();
        final List<WorkProduct> products = getWorkProductDAO().findByProductType(productType);
        if ((products != null) && (products.size() > 0)) {
            for (final WorkProduct product : products) {
                try {
                    if ((query == null) || (query.length() == 0) || (namespaceMap == null) ||
                        (namespaceMap.size() == 0) ||
                        DocumentUtil.exist(query, product.getProduct(), namespaceMap)) {
                        productIDs.add(product.getProductID());
                    }
                } catch (final InvalidXpathException e) {
                    // TODO - need to propagate this ???
                    logger.error(e.getMessage());
                }
            }
        }
        return productIDs;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     * @ssdd
     */
    @Override
    public String getServiceName() {

        return PRODUCT_SERVICE_NAME;
    }

    private String getUserID() {

        String principal = ServletUtil.getPrincipalName();
        if (principal == null) {
            principal = "admin";
            logger.warn("getUserID: user is unset, set to " + principal);
        }

        return new String(principal + "@" + configurationService.getFullyQualifiedHostName());
    }

    public WorkProductDAO getWorkProductDAO() {

        return workProductDAO;
    }

    // 2014/05/28 - TTHURSTON
    @Override
    public boolean isAuthenticatedUserAnAdmin(String authenticatedUser) {

        // strip the server name
        logger.debug("Authenticated User is: " + authenticatedUser);
        if (authenticatedUser.contains(CHARACTER_AT)) {
            authenticatedUser = authenticatedUser.substring(INDEX_ZERO,
                                                            authenticatedUser.indexOf(CHARACTER_AT));
        }

        logger.debug("Retrieving list of admins...");
        // get the list of admins
        final List<String> admins = ldapUtil.getGroupMembersForAdmins();
        logger.debug("Retrieved list of admins... searching now.");
        for (final String anAdmin : admins) {
            if (anAdmin.equals(authenticatedUser)) {
                logger.debug("user: " + authenticatedUser + " is an admin, returning true!");
                return true;
            }
        }
        logger.debug("user: " + authenticatedUser + " is not an admin, returning false!");

        return false;
    }

    // 2014/04/14 - TTHURSTON
    @Override
    public boolean isAuthenticatedUserTheOwner(String authenticatedUser, String igID) {

        // TODO: HACK - removed the server id appended to the owner to pass the authentication check
        // Ideally, should append server name to the principal so that another core cannot close
        final WorkProduct[] workproducts = getAssociatedWorkProductList(igID);
        String owner = "";

        for (final WorkProduct workProduct : workproducts) {
            if (workProduct.getProductType().equals(PRODUCT_TYPE_INCIDENT)) {
                owner = workProduct.getCreatedBy();
                owner = owner.substring(INDEX_ZERO, owner.indexOf(CHARACTER_AT));
                break;
            }
        }

        return owner.equals(authenticatedUser);
    }

    /**
     * Determines if the workproduct id is in among the closed work products
     *
     * @param productID the product id
     *
     * @return true, if is deleted
     * @ssdd
     */
    @Override
    public boolean isDeleted(String productID) {

        final List<WorkProduct> deletedProducts = workProductDAO.findAllClosedVersionOfProduct(productID);
        return deletedProducts.size() > 0 ? true : false;
    }

    /**
     * Looks up a work product by workproduct id string
     *
     * @param productID the product id
     *
     * @return true, if is existed
     * @ssdd
     */
    @Override
    public boolean isExisted(String productID) {

        final WorkProduct product = getWorkProductDAO().findByProductID(productID);
        return product != null;
    }

    /**
     * List all work products.
     *
     * @return the list< work product>
     * @ssdd
     */
    @Override
    public List<WorkProduct> listAllWorkProducts() {

        final List<WorkProduct> products = getWorkProductDAO().findAll();
        return products;
    }

    // public void setGetProductResponseChannel(MessageChannel channel) {
    // getProductResponseChannel = channel;
    // }

    // public void setIncidentDAO(IncidentDAO incidentDAO) {
    // this.incidentDAO = incidentDAO;
    // }

    /**
     * List work products for a given product type.
     *
     * @param type the type
     *
     * @return the list< work product>
     * @ssdd
     */
    @Override
    public List<WorkProduct> listByProductType(String type) {

        return getWorkProductDAO().findByProductType(type);
    }

    private void notifyOfWorkProductChange(WorkProductIdentificationDocument identification,
                                           WorkProductPropertiesDocument properties,
                                           ProductChangeNotificationMessage.ChangeIndicator changeIndicaor) {

        final ProductChangeNotificationMessage notification = new ProductChangeNotificationMessage(identification,
                                                                                                   properties,
                                                                                                   changeIndicaor);

        logger.debug("Work product change notification: " + notification.getProductID());

        final Message<ProductChangeNotificationMessage> message = new GenericMessage<ProductChangeNotificationMessage>(notification);
        try {
            productChangeNotificationChannel.send(message);
        } catch (final Exception e) {
            // System.err.println("notifyOfWorkProductChange Exception sending message: "
            // + e.getMessage());
            logger.error("Exception sending message on productChangeNotificationChannel: " +
                         e.getMessage());
        }
    }

    private void notifyOfWorkProductInterestGroupAssociation(WorkProduct product,
                                                             String interestGroupID,
                                                             ProductToInterestGroupAssociationMessage.AssociationType associationType) {

        logger.debug("Notify Communication Service to " +
                     (associationType == ProductToInterestGroupAssociationMessage.AssociationType.Associate ? "associate" : "de-associate") +
                     " work product to incident");

        final InterestGroup interestGroup = getInterestGroupDAO().findByInterestGroup(interestGroupID);
        // if it's shared work product not need to notify the communication service
        if (interestGroup.getOwningCore().equals(getConfigurationService().getCoreName()) == false) {
            logger.error(product.getProductID() + " is owned by " + interestGroup.getOwningCore() +
                         " and this core is " + getConfigurationService().getCoreName() +
                         ". No notification needed");
            return;
        }

        final ProductToInterestGroupAssociationMessage notification = new ProductToInterestGroupAssociationMessage();
        notification.setAssociationType(associationType);
        notification.setProductId(product.getProductID());
        notification.setProductType(product.getProductType());
        notification.setInterestGroupId(interestGroup.getInterestGroupID());
        notification.setOwningCore(interestGroup.getOwningCore());

        final Message<ProductToInterestGroupAssociationMessage> message = new GenericMessage<ProductToInterestGroupAssociationMessage>(notification);
        logger.debug("sending AssociateProductToIncidentMessage");
        getProductAssociationChannel().send(message);
    }

    private ProductPublicationStatus publishIt(WorkProduct theProduct, String userID) {

        boolean notifyOfAssociation = false;
        final LogEntry logEntry = new LogEntry();

        final ProductPublicationStatus status = new ProductPublicationStatus();

        logger.debug("publishIt: " + theProduct.getMetadata());

        WorkProduct product = null;
        if (theProduct.getProductID() != null) {
            product = getProduct(theProduct.getProductID());
            if ((product != null) && (product.getId() != null)) {
                theProduct = new WorkProduct(theProduct);
                logger.debug("publishIt: make a copy of product: " + theProduct.getMetadata());
            }
        }
        if (product != null) {
            // this is an update, verify that this is valid request, i.e. version number and
            // checksum match those in the current copy in the database

            // make sure the update is based on the current version of the work product-
            if (!theProduct.getProductVersion().equals(product.getProductVersion()) ||
                !theProduct.getChecksum().equals(product.getChecksum())) {

                status.setStatus("Failure");
                final String reason = "Invalid version number and/or checksum: " +
                    " specified version number=" +
                    theProduct.getProductVersion() + "; specified checksum=[" +
                    theProduct.getChecksum() + "]." +
                    "  The current version number=" + product.getProductVersion();
                status.setReasonForFailure(reason);
                return status;
            }
        }

        if (theProduct.getFirstAssociatedInterestGroupID() != null) {
            /*
            InterestGroup interestGroup = getInterestGroupDAO().findByInterestGroup(theProduct.getFirstAssociatedInterestGroupID());
            if (interestGroup == null) {
                logger.error("===>>> Unknown interest group ID  " +
                             theProduct.getFirstAssociatedInterestGroupID() +
                             " provided for association.");
                status.setStatus("Failure");
                String reason = "Unknown interest group ID: " +
                                theProduct.getFirstAssociatedInterestGroupID() +
                                " provided for association.";
                status.setReasonForFailure(reason);
                return status;
            } else
             */
            if ((product == null) ||
                (product.getAssociatedInterestGroupIDs().contains(theProduct.getFirstAssociatedInterestGroupID()) == false)) {
                notifyOfAssociation = true;
            }
        }

        final Date date = new Date();
        // the publish/update request is valid
        if ((product != null) && (product.getProductVersion() != null)) {
            // this is an update to an existing product, increment the version number
            final Integer newVersion = product.getProductVersion() + 1;
            theProduct.setProductVersion(newVersion);
            theProduct.setCreatedBy(product.getCreatedBy());
            theProduct.setCreatedDate(product.getCreatedDate());

            logEntry.setAction(LogEntry.ACTION_WORKPRODUCT_UPDATE);
            logEntry.setUpdatedBy(product.getUpdatedBy());
        } else {
            // first publication, set the product ID, initial version number, creator and the
            // created date

            // this is done for IMS who needs to generate its own product ID first in order to
            // insert the product ID into the incident document to be published
            // TODO: need a better mechanism for doing this so that WorkProductService is the only
            // place the work product ID is generated.
            if ((theProduct.getProductID() == null) || theProduct.getProductID().isEmpty()) {
                theProduct.setProductID(UUIDUtil.getID(theProduct.getProductType()));
            }
            theProduct.setProductVersion(1);
            theProduct.setCreatedBy(userID);
            theProduct.setCreatedDate(date);

            logEntry.setAction(LogEntry.ACTION_WORKPRODUCT_CREATE);
            logEntry.setCreatedBy(theProduct.getCreatedBy());
        }

        theProduct.setUpdatedBy(userID);
        theProduct.setUpdatedDate(date);

        // set size in kilobytes
        // TODO do we really need this ???
        theProduct.setSize(theProduct.getProduct().toString().length() / KiloBytes);

        final String checksum = WorkProductUtil.calculateChecksum(theProduct.getUpdatedDate().toString(),
                                                                  theProduct.getProductVersion(),
                                                                  theProduct.getSize());
        theProduct.setChecksum(checksum);

        if (theProduct.getMimeType() == null) {
            theProduct.setDefaultMimeType();
        }

        final WorkProduct newProduct = doPublish(theProduct, true);

        // close old product if new product was publish successfully and it was not the first
        // version
        if ((newProduct != null) && (product != null) && product.isActive() &&
            (theProduct.getProductVersion() > 1)) {
            product.setActive(false);
            logger.debug("publishIt: deactive: " + product.getMetadata());
            doPublish(product, false);
        }

        if ((newProduct != null) && notifyOfAssociation) {
            // notify Comms of the association
            notifyOfWorkProductInterestGroupAssociation(newProduct,
                                                        newProduct.getFirstAssociatedInterestGroupID(),
                                                        ProductToInterestGroupAssociationMessage.AssociationType.Associate);
        }

        if (newProduct != null) {
            logEntry.setWorkProductId(newProduct.getProductID());
            logEntry.setWorkProductType(newProduct.getProductType());
            logEntry.setWorkProductSize(newProduct.getSize().toString());
            logEntry.setCategory(LogEntry.CATEGORY_WORKPRODUCT);
            logger.info(logEntry.getLogEntry());

            status.setStatus("Success");
            status.setProduct(newProduct);
            return status;
        } else {
            status.setStatus("Failure");
            status.setReasonForFailure("Internal Error: Unable to publish work product");
            return status;
        }
    }

    /**
     * Publish product in response from a request from a joined core.
     *
     * @param wp the wp
     * @param userID the user id
     *
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus publishProducRequesttFromJoinedCore(WorkProduct wp,
                                                                        String userID) {

        // we are the incident owning core, who has just received a joined core's request to update
        // or publish a work product associated to the shared incident.
        // Note: For now we just turn around and publish the work product as requested
        // In the future, it is possible that some involvement from the corresponding UICDS service
        // may be required.
        return publishIt(wp, userID);
    }

    /**
     * Publish product. Verify that the work product is active. If there is an associated incident,
     * then this is a joined core so send the publish request to the owning core. Otherwise this is
     * the owning core, so publish the work product directly.
     *
     * @param theProduct the the product
     *
     * @return the product publication status
     * @ssdd
     */
    @Override
    public ProductPublicationStatus publishProduct(WorkProduct theProduct) {

        logger.debug("publishProduct: " + theProduct.getMetadata());

        // check whether it's Inactive or not
        if ((theProduct != null) && (theProduct.getProductID() != null)) {
            final WorkProduct product = getProduct(theProduct.getProductID());
            if ((product != null) && (product.isActive() == false)) {
                return new ProductPublicationStatus(theProduct.getProductID() + " is inactive");
            }
        }

        if (theProduct.getProductType() == null) {
            logger.error(">>> Error: NULL productType received");
            final ProductPublicationStatus status = new ProductPublicationStatus();
            status.setStatus("Failure");
            status.setReasonForFailure("Internal Error: Unable to publish work product");
            return status;
        }

        InterestGroup interestGroup = null;
        final String userID = getUserID();
        final String interestGroupID = theProduct.getFirstAssociatedInterestGroupID();

        // Check to see if there is an associated incident
        if (interestGroupID != null) {
            interestGroup = getInterestGroupDAO().findByInterestGroup(interestGroupID);
            // interestGroupInfo = interestGroupManagementComponent.getInterestGroup(interestGroupID);
        }

        if ((interestGroup != null) &&
            (interestGroup.getOwningCore().equals(directoryService.getCoreName()) != true)) {
            // We are a joined core (i.e. joined to this incident) and therefore this a
            // publish/update by a joined core.
            // Send the publish request to owning core for approval first.

            logger.debug("This an publish/update by a joined core=" +
                         directoryService.getCoreName() +
                         "    ...   Send update request to owning core  " +
                         interestGroup.getOwningCore() + " for approval first.");

            try {
                final ProductPublicationStatus status = new ProductPublicationStatus();
                final String act = WorkProductUtil.getACT();

                // TODO: Problems!!!
                // the package identification is not set yet
                // Maybe we should consider other ways of streaming work product model across the
                // XMPP connection
                // without having to use precis type
                final WorkProductDocument doc = WorkProductHelper.toWorkProductDocument(theProduct);

                String wpString = null;
                wpString = doc.toString();

                // log.debug("====> wpString=[" + wpString + "]");

                final JoinedPublishProductRequestMessage msg = new JoinedPublishProductRequestMessage();
                msg.setAct(act);
                msg.setUserID(userID);
                msg.setInterestGroupId(theProduct.getFirstAssociatedInterestGroupID());
                msg.setOwningCore(interestGroup.getOwningCore());
                msg.setRequestingCore(directoryService.getCoreName());
                msg.setProductId(theProduct.getProductID());
                msg.setProductType(theProduct.getProductType());
                msg.setWorkProduct(wpString);
                final Message<JoinedPublishProductRequestMessage> message = new GenericMessage<JoinedPublishProductRequestMessage>(msg);
                logger.debug("===>publishProduct:  sending JoinedPublishProductRequestMessage message");
                joinedPublishProductRequestChannel.send(message);

                status.setStatus("Pending");
                status.setAct(act);
                return status;
            } catch (final Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return publishIt(theProduct, userID);
        }
    }

    /**
     * Publish product from owner. This is a joined core that has received publication of a work
     * product associated with a shared incident from the incident owning core
     *
     * @param product the product
     * @ssdd
     */
    @Override
    public void publishProductFromOwner(WorkProduct theProduct) {

        logger.debug("publishProductFromOwner: productID: " + theProduct.getProductID() +
                     ", IGID: " + theProduct.getFirstAssociatedInterestGroupID());
        // Get the current version of the product
        /*
        WorkProduct product = null;
        if (theProduct.getProductID() != null && !theProduct.getProductID().isEmpty()) {
            product = getProduct(theProduct.getProductID());

            //for double secure and use the product id from product.
            if (product != null && product.getProductID() != null) {
                theProduct.setProductID(product.getProductID());
            }
        }
         */
        // publish the new version
        final WorkProduct newProduct = doPublish(theProduct, true);
    }

    /* this is purge when there is orphan incident/work products */
    @Override
    public void purgeWorkProduct(String productID) {

        logger.debug("purgeWorkProduct: " + productID);

        final WorkProduct wp = getProduct(productID);
        final WorkProductIdentificationDocument identification = WorkProductIdentificationDocument.Factory.newInstance();
        final WorkProductPropertiesDocument properties = WorkProductPropertiesDocument.Factory.newInstance();
        identification.setWorkProductIdentification(WorkProductHelper.getWorkProductIdentification(wp));
        properties.setWorkProductProperties(WorkProductHelper.getWorkProductProperties(wp));
        notifyOfWorkProductChange(identification,
                                  properties,
                                  ProductChangeNotificationMessage.ChangeIndicator.Delete);

        final List<WorkProduct> productList = getWorkProductDAO().findAllVersionOfProduct(productID);
        for (final WorkProduct product : productList) {
            logger.debug("purgeWorkProduct: Product:/" + productID + "/, Version:/" +
                         product.getProductVersion() + "/ ...");
            getWorkProductDAO().makeTransient(product);
        }
    }

    public void setConfigurationService(ConfigurationService configurationService) {

        this.configurationService = configurationService;
    }

    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    public void setInterestGroupDAO(InterestGroupDAO interestGroupDAO) {

        this.interestGroupDAO = interestGroupDAO;
    }

    public void setJoinedPublishProductRequestChannel(MessageChannel joinedPublishProductRequestChannel) {

        this.joinedPublishProductRequestChannel = joinedPublishProductRequestChannel;
    }

    public void setLdapUtil(LdapUtil ldapUtil) {

        this.ldapUtil = ldapUtil;
    }

    public void setProductAssociationChannel(MessageChannel productAssociationChannel) {

        this.productAssociationChannel = productAssociationChannel;
    }

    public void setProductChangeNotificationChannel(MessageChannel channel) {

        productChangeNotificationChannel = channel;
    }

    public void setWorkProductDAO(WorkProductDAO workProductDAO) {

        this.workProductDAO = workProductDAO;
    }

    /**
     * X path executed.
     *
     * @param productID the product id
     * @param path the path
     * @param namespaceMap the namespace map
     *
     * @return true, if successful
     *
     * @throws InvalidXpathException the invalid xpath exception
     * @ssdd
     */
    @Override
    public boolean xPathExecuted(String productID, String path, Map<String, String> namespaceMap)
        throws InvalidXpathException {

        return DocumentUtil.exist(path, getProduct(productID).getProduct(), namespaceMap);
    }
}
