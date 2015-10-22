package com.leidos.xchangecore.core.infrastructure.service;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidXpathException;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.saic.precis.x2009.x06.base.IdentificationType;

/**
 * The Work Product Service provides operations to manage work products and their relationships to
 * Interest Groups (i.e. incidents).
 * 
 * @ssdd
 */
// @Transactional
public interface WorkProductService {

    /** The Constant WorkProductIdUrn. */
    public static final String WorkProductIdUrn = "urn:uicds:workproduct:id";

    /** The Constant WorkProductTypeUrn. */
    public static final String WorkProductTypeUrn = "urn:uicds:workproduct:type";

    /** The Constant WorkProductVersionUrn. */
    public static final String WorkProductVersionUrn = "urn:uicds:workproduct:version";

    /** The Constant WorkProductSubmitterIdUrn. */
    public static final String WorkProductSubmitterIdUrn = "urn:uicds:workproduct:submitterId";

    /** The Constant WorkProductChecksumUrn. */
    public static final String WorkProductChecksumUrn = "urn:uicds:workproduct:version";

    /** The Constant workProductOwnerUrn. */
    public static final String workProductOwnerUrn = "urn:uicds:workproduct:owner";

    /** The Constant createdDate. */
    public static final String createdDate = "urn:uicds:workproduct:createdDate";

    /** The Constant createdBy. */
    public static final String createdBy = "urn:uicds:workproduct:createdBy";

    /** The Constant updateedDate. */
    public static final String updatedDate = "urn:uicds:workproduct:updatedDate";

    /** The Constant updateedBy. */
    public static final String updatedBy = "urn:uicds:workproduct:updatedBy";

    /** The Constant workProductSize. */
    public static final String workProductSize = "urn:uicds:workproduct:size";

    /** The Constant editorControl. */
    public static final String editorControl = "urn:uicds:workproduct:editorControl";

    /** The Constant PRODUCT_SERVICE_NAME. */
    public static final String PRODUCT_SERVICE_NAME = "ProductService";

    /** The Constant NiemIdQName. */
    public static final QName NiemIdQName = new QName("http://niem.gov/niem/structures/2.0", "id");

    /** The Constant ACTPrefix. */
    public static final String ACTPrefix = "ACT_";

    /**
     * Archive product.
     * 
     * @param identifier the identifier
     * 
     * @return the product publication status
     * @ssdd
     */
    public ProductPublicationStatus archiveProduct(IdentificationType identifier);

    /**
     * Associate work product to interest group.
     * 
     * @param workProductId the work product id
     * @param interestGroupID the interest group id
     * 
     * @return the string
     * @ssdd
     */
    public String associateWorkProductToInterestGroup(String workProductId, String interestGroupID);

    /**
     * Close product.
     * 
     * @param productID the product id
     * 
     * @return the product publication status
     * @ssdd
     */
    public ProductPublicationStatus closeProduct(IdentificationType productID);

    /**
     * Delete work product.
     * 
     * @param productID the product id
     * 
     * @return the product publication status
     * @ssdd
     */
    public ProductPublicationStatus deleteWorkProduct(String productID);

    /**
     * Delete work product without notify.
     * 
     * @param productID the product id
     * 
     * @return the product publication status
     * @ssdd
     */
    public ProductPublicationStatus deleteWorkProductWithoutNotify(String productID);

    /**
     * Find by interest group and type.
     * 
     * @param interestGroupID the interest group id
     * @param productType the product type
     * 
     * @return the list< work product>
     * @ssdd
     */
    public List<WorkProduct> findByInterestGroupAndType(String interestGroupID, String productType);

    /**
     * Get all versions of a work product
     * 
     * @param productID
     * @return
     */
    public List<WorkProduct> getAllVersionsOfProduct(String productID);

    /**
     * Gets the associated work product list.
     * 
     * @param interestGroupId the interest group id
     * 
     * @return the associated work product list
     * @ssdd
     */
    public WorkProduct[] getAssociatedWorkProductList(String interestGroupId);

    /**
     * Gets the product.
     * 
     * @param pkgId the pkg id
     * 
     * @return the product
     * @ssdd
     */
    public WorkProduct getProduct(IdentificationType pkgId);

    /**
     * Gets the product.
     * 
     * @param productID the product id
     * 
     * @return the product
     * @ssdd
     */
    public WorkProduct getProduct(String productID);

    /**
     * Gets the product by type and x query.
     * 
     * @param productType the product type
     * @param query the query
     * @param namespaceMap the namespace map
     * 
     * @return the product by type and x query
     * @throws InvalidXpathException
     * @ssdd
     */
    public List<WorkProduct> getProductByTypeAndXQuery(String productType,
                                                       String query,
                                                       Map<String, String> namespaceMap)
        throws InvalidXpathException;

    /**
     * Gets the producti by version.
     * 
     * @param productID the product id
     * @param productVersion the product version
     * 
     * @return the producti by version
     * @ssdd
     */
    public WorkProduct getProductiByVersion(String productID, Integer productVersion);

    /**
     * Gets the product identification.
     * 
     * @param productID the product id
     * 
     * @return the product identification
     * @ssdd
     */
    public IdentificationType getProductIdentification(String productID);

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
    public List<String> getProductIDListByTypeAndXQuery(String productType,
                                                        String query,
                                                        Map<String, String> namespaceMap)
        throws InvalidXpathException;

    /**
     * Gets the service name.
     * 
     * @return the service name
     * @ssdd
     */
    public String getServiceName();

    // 2014/05/28 - TTHURSTON
    public boolean isAuthenticatedUserAnAdmin(String authenticatedUser);

    // 2014/04/14 - TTHURSTON
    public boolean isAuthenticatedUserTheOwner(String authenticatedUser, String igID);

    /**
     * Checks if is deleted.
     * 
     * @param productID the product id
     * 
     * @return true, if is deleted
     * @ssdd
     */
    public boolean isDeleted(String productID);

    /**
     * Checks if is existed.
     * 
     * @param productID the product id
     * 
     * @return true, if is existed
     * @ssdd
     */
    public boolean isExisted(String productID);

    /**
     * List all work products.
     * 
     * @return the list< work product>
     * @ssdd
     */
    public List<WorkProduct> listAllWorkProducts();

    /**
     * List by product type.
     * 
     * @param type the type
     * 
     * @return the list< work product>
     * @ssdd
     */
    public List<WorkProduct> listByProductType(String type);

    /**
     * Publish produc requestt from joined core.
     * 
     * @param product the product
     * @param userID the user id
     * 
     * @return the product publication status
     * @ssdd
     */
    public ProductPublicationStatus publishProducRequesttFromJoinedCore(WorkProduct product,
                                                                        String userID);

    /**
     * Publish product.
     * 
     * @param workProduct the work product
     * 
     * @return the product publication status
     * @ssdd
     */
    public ProductPublicationStatus publishProduct(WorkProduct workProduct);

    /**
     * Publish product from owner.
     * 
     * @param product the product
     * @ssdd
     */
    public void publishProductFromOwner(WorkProduct product);

    public void purgeWorkProduct(String productID);

    /**
        * X path executed.
        * 
        * @param productID the product id
        * @param xPath the x path
        * @param namespaceMap the namespace map
        * 
        * @return true, if successful
        * 
        * @throws InvalidXpathException the invalid xpath exception
        * @ssdd
        */
    public boolean xPathExecuted(String productID, String xPath, Map<String, String> namespaceMap)
        throws InvalidXpathException;

}
