package com.leidos.xchangecore.core.infrastructure.endpoint;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.uicds.workProductService.ArchiveProductRequestDocument;
import org.uicds.workProductService.ArchiveProductResponseDocument;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupRequestDocument;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupResponseDocument;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupResponseDocument.AssociateWorkProductToInterestGroupResponse;
import org.uicds.workProductService.CloseProductRequestDocument;
import org.uicds.workProductService.CloseProductResponseDocument;
import org.uicds.workProductService.GetAssociatedWorkProductListRequestDocument;
import org.uicds.workProductService.GetAssociatedWorkProductListResponseDocument;
import org.uicds.workProductService.GetProductCurrentVersionRequestDocument;
import org.uicds.workProductService.GetProductCurrentVersionResponseDocument;
import org.uicds.workProductService.GetProductRequestDocument;
import org.uicds.workProductService.GetProductResponseDocument;
import org.uicds.workProductService.PublishProductRequestDocument;
import org.uicds.workProductService.PublishProductResponseDocument;

import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.PermissionDeniedException;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.leidos.xchangecore.core.infrastructure.util.ServiceNamespaces;
import com.leidos.xchangecore.core.infrastructure.util.ServletUtil;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * The Work Product Service provides operations to manage work products and their relationships to
 * Interest Groups (i.e. incidents). The services provided are:
 * <ul>
 * <li>Publish a work product of any type that is not managed by the XchangeCore product management
 * services</li>
 * <li>Get a work product</li>
 * <li>Close a work product (mark it as inactive and unable to be updated)</li>
 * <li>Archive a work product (remove it from the core)</li>
 * <li>Associate a work product with an interest group</li>
 * <li>Get a list of work product associated with an interest group Id</li>
 * </ul>
 * <p>
 * XchangeCore work products are defined as a UCore Data Information Package (DIP).
 * <p>
 * <img src="doc-files/WorkProduct.png" />
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * A XchangeCore work product contains metadata and a structured payload. The metadata elements are
 * XchangeCore-generated data, are immutable, and are defined as substitutable elements for a UCore
 * PackageMetadataExtension element. The two XchangeCore metadata elements are:
 * <ul>
 * <li>Work Product Identification
 * <li>Work Product Properties
 * </ul>
 * <p>
 * The Work Product Identification element is the unique identification of a specific version of a
 * work product. It is required for all service requests that target a specific version of a work
 * product and is defined as follows:
 * <p>
 * <img src="doc-files/WorkProductIdentification.png">
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * The Work Product Properties type contains metadata about the work product such as who created it,
 * when it was created, who last updated it, when it was last updated, it's size, an optional mime
 * type, and the list of Interest Groups (i.e. incidents) that this work product is associated with
 * The Work Product Properties element is defined as follows:
 * <p>
 * <img src="doc-files/WorkProductProperties.png">
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * Several of the work product types will have a Digest element. The digest element gives a summary
 * (digest) of the actual work product using UCore who, what, where, when elements. This digest is
 * automatically created by the service that handles the work product. For example the Incident
 * Mangement Service will create and update a digest element for each incident's Incident work
 * product. The UCore specification contains details on the elements that make up a digest. Digests
 * are the main elements that are delivered in a work product notification.
 * <p>
 * The StructuredPayload contains the actual work product content and is a required element for both
 * create and update requests and is defined as follows:
 * <p>
 * <img src="doc-files/StructuredPayload.png">
 * <p>
 * <!-- NEWPAGE -->
 * <p>
 * The StructuredPayload contains:
 * <ul>
 * <li>CommunityURI
 * <li>CommunityVersion
 * <li>payload content
 * </ul>
 * <p>
 * The CommunityURI should be set to a unique schema that identifies the top element for the
 * contents of the StructuredPayload. The schema version number should be placed in the
 * CommunityVersion element.
 * <p>
 * The payload content of the StructutredPayload content types can be of three types:
 * <ul>
 * <li>BinaryContent
 * <li>LinkedContent
 * <li>Any
 * </ul>
 * <p>
 * A binary work product should be encoded via base64 bit encoding and should be contained within a
 * BinaryContentType element defined in the "http://www.saic.com/precis/2009/06/payloads/binary"
 * namespace. The mimeType attribute of the BinaryContentType should be set to a standard mime type
 * that represents the binary data. The BinaryContentType is defined in the
 * precis/2009/06/BinaryPayload.xsd file.
 * <p>
 * A link content work product contains a URL to information and should be contained within a
 * LinkContentType element defined in the "http://www.saic.com/precis/2009/06/payloads/link"
 * namespace. The LinkContentType is defined in the precis/2009/06/BinaryPayload.xsd file.
 * <p>
 * Any arbitrary XML can also be used as the payload. The CommunityURI and CommunityVerions elements
 * should be set as appropriate.
 * <p>
 * Note that any work products of Binary, Link, or Any XML types should also create a UCore digest
 * element to summarize the contents of the work product if applicable.
 * <p>
 * XchangeCore does not currently use the AttachmentLink, RenderingInstructions, or Narrative elements but
 * clients are free to add data to these elements and XchangeCore will preserve this data.
 * <p>
 * A WorkProductProcessingStatus will be returned for publish and update requests. The
 * WorkProductProcessingStatus is defined as follows:
 * <p>
 * <img src="doc-files/WorkProductProcessingStatus.png"">
 * <p>
 * If the Status value is "Accepted" then the operation was accepted by the core. If the Status
 * value is "Pending" then the operation could not be completed in a synchronous manner and the ACT
 * element will contain an asynchronous completion token. A subsequent notification message will
 * contain a WorkProductProcessingStatus element with the identical ACT value and a status of either
 * "Accepted" or "Rejected". If the state of any WorkProductProcessingStatus is "Rejected" then the
 * Message element will contain more details about the reason why the operation was rejected.
 * <p>
 * The main reason to receive a "Rejected" status is that the requested update was posted against
 * the most current version of the work product. All updates to work products must be based on the
 * most current version of the work product. The WorkProductIdentification for the most current
 * version can be obtained by retrieving the list of work products associated with an interest group
 * using the GetAssociatedWorkProductList. Or clients can use the NotificationService in conjunction
 * with the Resource Profile and Resource Instance service to be notified when a new version of a
 * work product is produced and thereby obtain the latest WorkProductIdenification.
 * 
 * @author Daniel Huang
 * @see <a href="../../wsdl/WorkProductService.wsdl">Appendix: WorkProductService.wsdl</a>
 * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
 *      WorkProductService.xsd</a>
 * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
 *      WorkProductData.xsd</a>
 * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
 * @see <a href="../../precis/2009/06/BinaryPayload.xsd">Appendix: BinaryPayload.xsd</a>
 * @see <a href="../../precis/2009/06/LinkPayload.xsd">Appendix: LinkPayload.xsd</a>
 * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
 * @see <a href="https://www.ucore.gov/ucore/">UCore Specification</a>
 * @idd
 * 
 */
@Endpoint
public class WorkProductEndpoint implements ServiceNamespaces {

    private static Logger log = LoggerFactory.getLogger(WorkProductEndpoint.class);

    @Autowired
    private UserInterestGroupDAO userInterestGroupDAO;

    @Autowired
    private WorkProductService productService;

    /**
     * Archive a work product (i.e. remove it from the core and notify all clients that they should
     * archive this data). A work product must be closed before it can be archived.
     * 
     * @param ArchiveProductRequestDocument
     * 
     * @return ArchiveProductResponseDocument
     * @throws InvalidProductIDException 
     * @throws SOAPException 
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "ArchiveProductRequest")
    public ArchiveProductResponseDocument archiveProduct(ArchiveProductRequestDocument request)
        throws DatatypeConfigurationException, InvalidProductIDException, SOAPException {

        ArchiveProductResponseDocument response = ArchiveProductResponseDocument.Factory.newInstance();

        WorkProduct wp = productService.getProduct(request.getArchiveProductRequest().getWorkProductIdentification());
        if (wp == null) {
            throw new InvalidProductIDException();
        }

        if ((wp.getFirstAssociatedInterestGroupID() != null &&
             userInterestGroupDAO.isEligible(ServletUtil.getPrincipalName(), wp.getFirstAssociatedInterestGroupID())) ||
            wp.isCreator(ServletUtil.getPrincipalName())) {
            response.addNewArchiveProductResponse().set(WorkProductHelper.toWorkProductProcessingStatus(productService.archiveProduct(request.getArchiveProductRequest().getWorkProductIdentification())));
        } else {
            throw new SOAPException("Permission Denied");
        }
        return response;
    }

    /**
     * Associates a work product with an interest group.
     * 
     * @param AssociateWorkProductToIncidentRequestDocument
     * 
     * @return AssociateWorkProductToIncidentResponseDocument
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "AssociateWorkProductToInterestGroupRequest")
    public AssociateWorkProductToInterestGroupResponseDocument
           associateWorkProductToInterestGroup(AssociateWorkProductToInterestGroupRequestDocument request)
               throws SOAPException {

        if (request.getAssociateWorkProductToInterestGroupRequest() == null ||
            request.getAssociateWorkProductToInterestGroupRequest().getWorkProductID() == null ||
            request.getAssociateWorkProductToInterestGroupRequest().getWorkProductID().getStringValue() == null ||
            request.getAssociateWorkProductToInterestGroupRequest().getIncidentID() == null ||
            request.getAssociateWorkProductToInterestGroupRequest().getIncidentID().getStringValue() == null) {
            throw new SOAPException("Invalid Work Product ID/Incident ID");
        }

        String wpID = productService.associateWorkProductToInterestGroup(request.getAssociateWorkProductToInterestGroupRequest().getWorkProductID().getStringValue(),
                                                                         request.getAssociateWorkProductToInterestGroupRequest().getIncidentID().getStringValue());

        WorkProduct wp = productService.getProduct(wpID);

        AssociateWorkProductToInterestGroupResponseDocument response = AssociateWorkProductToInterestGroupResponseDocument.Factory.newInstance();
        AssociateWorkProductToInterestGroupResponse res = response.addNewAssociateWorkProductToInterestGroupResponse();
        if (wp != null) {
            res.addNewWorkProduct().set(WorkProductHelper.toWorkProductSummary(wp));
        } else {
            res.addNewWorkProduct();
        }

        return response;

    }

    /**
     * Close a work product (mark it as inactive and unable to be updated).
     * 
     * @param CloseProductRequestDocument
     * 
     * @return CloseProductResponseDocument
     * @throws SOAPException 
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "CloseProductRequest")
    public CloseProductResponseDocument closeProduct(CloseProductRequestDocument request)
        throws DatatypeConfigurationException, InvalidProductIDException, SOAPException {

        CloseProductResponseDocument response = CloseProductResponseDocument.Factory.newInstance();

        WorkProduct wp = productService.getProduct(request.getCloseProductRequest().getWorkProductIdentification());
        if (wp == null) {
            throw new InvalidProductIDException();
        }
        if ((wp.getFirstAssociatedInterestGroupID() != null &&
             userInterestGroupDAO.isEligible(ServletUtil.getPrincipalName(), wp.getFirstAssociatedInterestGroupID())) ||
            wp.isCreator(ServletUtil.getPrincipalName())) {
            response.addNewCloseProductResponse().addNewWorkProductPublicationResponse().set(WorkProductHelper.toWorkProductPublicationResponse(productService.closeProduct(request.getCloseProductRequest().getWorkProductIdentification())));
        } else {
            throw new SOAPException("Permission Denied");
        }
        return response;
    }

    /**
     * Get the work product list associated with an interest group.
     * 
     * @param GetAssociatedWorkProductListRequestDocument
     * 
     * @return GetAssociatedWorkProductListResponseDocument
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "GetAssociatedWorkProductListRequest")
    public GetAssociatedWorkProductListResponseDocument
           getAssociatedWorkProductList(GetAssociatedWorkProductListRequestDocument request)
               throws DatatypeConfigurationException {

        GetAssociatedWorkProductListResponseDocument response = GetAssociatedWorkProductListResponseDocument.Factory.newInstance();
        WorkProduct[] products = productService.getAssociatedWorkProductList(request.getGetAssociatedWorkProductListRequest().getIdentifier().getStringValue());
        List<WorkProduct> productList = new ArrayList<WorkProduct>();
        for (WorkProduct wp : products) {
            if (userInterestGroupDAO.isEligible(ServletUtil.getPrincipalName(),
                                                wp.getFirstAssociatedInterestGroupID())) {
                productList.add(wp);
            }
        }
        if (productList.size() > 0) {
            response.addNewGetAssociatedWorkProductListResponse().addNewWorkProductList();
            for (WorkProduct product : productList) {
                response.getGetAssociatedWorkProductListResponse().getWorkProductList().addNewWorkProduct().set(WorkProductHelper.toWorkProductSummary(product));
            }
        } else {
            response.addNewGetAssociatedWorkProductListResponse().setNil();
        }

        return response;
    }

    /**
     * Get the work product by work product identification.
     * 
     * @param GetProductRequestDocument
     * 
     * @return GetProductResponseDocument
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "GetProductRequest")
    public GetProductResponseDocument getProduct(GetProductRequestDocument request) throws SOAPException {

        GetProductResponseDocument response = GetProductResponseDocument.Factory.newInstance();

        WorkProduct wp = productService.getProduct(request.getGetProductRequest().getWorkProductIdentification());
        if (wp != null) {
            if (userInterestGroupDAO.isEligible(ServletUtil.getPrincipalName(),
                                                wp.getFirstAssociatedInterestGroupID())) {
                WorkProductDocument.WorkProduct theWorkProduct = WorkProductHelper.toWorkProduct(wp);
                response.addNewGetProductResponse().setWorkProduct(theWorkProduct);
            } else {
                throw new SOAPException("Permission Denied");
            }
        } else {
            throw new SOAPException("Work Product does not exist");
        }

        return response;
    }

    /**
     * Get the work product by work product id string.
     * 
     * @param GetProductCurrentVersionRequestDocument
     * 
     * @return GetProductCurrentVersionResponseDocument
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "GetProductCurrentVersionRequest")
    public GetProductCurrentVersionResponseDocument
           getProductCurrentVersion(GetProductCurrentVersionRequestDocument request) throws SOAPException {

        GetProductCurrentVersionResponseDocument response = GetProductCurrentVersionResponseDocument.Factory.newInstance();

        WorkProduct wp = productService.getProduct(request.getGetProductCurrentVersionRequest().getIdentifier().getStringValue());
        if (wp != null) {
            if (userInterestGroupDAO.isEligible(ServletUtil.getPrincipalName(),
                                                wp.getFirstAssociatedInterestGroupID())) {
                WorkProductDocument.WorkProduct theWorkProduct = WorkProductHelper.toWorkProduct(wp);
                response.addNewGetProductCurrentVersionResponse().setWorkProduct(theWorkProduct);
            } else {
                throw new SOAPException("Permission Denied");
            }
        } else {
            throw new SOAPException("Work Product does not exist");
        }

        return response;
    }

    /**
     * Publish the work product (i.e. create a new work product if there is no
     * WorkProductIdentification element or update one if the WorkProductIdentification element is
     * the most current value.
     * 
     * @param PublishProductRequestDocument
     * 
     * @return PublishProductResponseDocument
     * @see <a href="../../services/WorkProduct/0.1/WorkProductService.xsd">Appendix:
     *      WorkProductService.xsd</a>
     * @see <a href="../../services/WorkProduct/0.1/WorkProductData.xsd">Appendix:
     *      WorkProductData.xsd</a>
     * @see <a href="../../precis/2009/06/BaseTypes.xsd">Appendix: BaseTypes.xsd</a>
     * @see <a href="../../precis/2009/06/Structures.xsd">Appendix: Structures.xsd</a>
     * 
     * @idd
     */
    @PayloadRoot(namespace = NS_WorkProductService, localPart = "PublishProductRequest")
    public PublishProductResponseDocument publishProduct(PublishProductRequestDocument request)
        throws DatatypeConfigurationException, SOAPException {

        // first, convert the request into WorkProduct model
        WorkProduct wp = WorkProductHelper.toModel(request.getPublishProductRequest().getWorkProduct());

        // if the request contains specific interest group ID to be associated, associate it to this WorkProduct
        if (request.getPublishProductRequest().getIncidentId() != null) {
            wp.associateInterestGroup(request.getPublishProductRequest().getIncidentId());
        }

        // if the product associated with an interest group then check the permission for this user to see whether
        // he/she can publish it
        if (wp.getFirstAssociatedInterestGroupID() != null &&
            userInterestGroupDAO.isEligible(ServletUtil.getPrincipalName(),
                                            wp.getFirstAssociatedInterestGroupID()) == false) {
            throw new PermissionDeniedException();
        }

        // Publish Product
        ProductPublicationStatus status = productService.publishProduct(wp);

        // Create Response
        PublishProductResponseDocument response = PublishProductResponseDocument.Factory.newInstance();
        response.addNewPublishProductResponse().addNewWorkProductPublicationResponse().set(WorkProductHelper.toWorkProductPublicationResponse(status));

        return response;
    }

    public void setUserInterestGroupDAO(UserInterestGroupDAO userInterestGroupDAO) {

        this.userInterestGroupDAO = userInterestGroupDAO;
    }

    public void setWorkProductService(WorkProductService wps) {

        productService = wps;
    }
}
