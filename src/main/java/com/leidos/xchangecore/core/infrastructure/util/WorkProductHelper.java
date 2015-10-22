package com.leidos.xchangecore.core.infrastructure.util;

import gov.ucore.ucore.x20.DataItemStatusType;
import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.DigestType;
import gov.ucore.ucore.x20.DisseminationCriteriaDocument.DisseminationCriteria;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import mil.dod.metadata.mdr.ns.ddms.x20.PointOfContactType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.workProductService.WorkProductPublicationResponseDocument;
import org.uicds.workProductService.WorkProductPublicationResponseType;

import x0.messageStructure1.DataOwnerMetadataType;
import x0.messageStructure1.DomainAttributeType;
import x0.messageStructure1.PackageMetadataType;
import x0.messageStructure1.StructuredPayloadType;
import x0.messageStructure1.impl.StructuredPayloadMetadataTypeImpl;

import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.impl.ProductPublicationStatus;
import com.saic.precis.x2009.x06.base.AssociatedGroupsDocument.AssociatedGroups;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.base.IdentifierType;
import com.saic.precis.x2009.x06.base.ProcessingStateType;
import com.saic.precis.x2009.x06.base.ProcessingStatusType;
import com.saic.precis.x2009.x06.base.PropertiesType;
import com.saic.precis.x2009.x06.base.StateType;
import com.saic.precis.x2009.x06.payloads.binary.BinaryContentDocument;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;
import com.saic.precis.x2009.x06.structures.WorkProductProcessingStatusDocument;

public class WorkProductHelper
    implements InfrastructureNamespaces {

    static Logger log = LoggerFactory.getLogger(WorkProductHelper.class);

    public static final DigestType getDigestElement(com.saic.precis.x2009.x06.structures.WorkProductDocument.WorkProduct workProduct) {

        DigestType digest = null;
        if (workProduct != null) {
            XmlObject[] objects = workProduct.selectChildren(new QName(NS_UCORE, S_DIGEST));
            if (objects.length > 0) {
                digest = (DigestType) objects[0];
            }
        }
        return digest;
    }

    /**
     * Get the Identification of the given WorkProduct
     * 
     * @param workProduct
     * @return
     */
    public static final IdentificationType getIdentificationElement(com.saic.precis.x2009.x06.structures.WorkProductDocument.WorkProduct workProduct) {

        IdentificationType id = null;
        if (workProduct == null) {
            System.err.println("Trying to get an identification element from a null work product");
        }
        if (workProduct != null && workProduct.getPackageMetadata() != null) {
            XmlObject[] objects = workProduct.getPackageMetadata().selectChildren(new QName(NS_PRECIS_STRUCTURES,
                                                                                            S_PACKAGE_IDENTIFICATION));
            if (objects.length > 0) {
                id = (IdentificationType) objects[0];
            }
        }
        return id;
    }

    public static String getInterestGroupID(WorkProductDocument.WorkProduct wp) {

        if (wp != null) {
            PropertiesType properties = getPropertiesElement(wp);
            if (properties != null) {
                AssociatedGroups groups = properties.getAssociatedGroups();
                if (groups != null && groups.getIdentifierArray(0) != null) {
                    return groups.getIdentifierArray(0).getStringValue();
                }
            }
        }
        return null;
    }

    public static final XmlObject getPayload(StructuredPayloadType payload) {

        XmlObject object = null;
        XmlCursor cursor = payload.newCursor();
        cursor.toFirstChild();
        do {
            if (cursor.getObject() instanceof StructuredPayloadMetadataTypeImpl) {
                continue;
                /*
                try {
                    if ((object = XmlObject.Factory.parse(cursor.xmlText())) instanceof StructuredPayloadMetadataDocumentImpl)
                        continue;
                    break;
                } catch (XmlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                */
            }
        } while (cursor.toNextSibling());

        try {
            object = XmlObject.Factory.parse(cursor.xmlText());
        } catch (Exception e) {
            log.error("Parsing [" + cursor.xmlText() + "] failed: " + e.getMessage());
        }

        cursor.dispose();

        return object;
    }

    public static String getProductID(WorkProductDocument.WorkProduct wp) {

        if (wp != null) {
            IdentificationType identifier = getIdentificationElement(wp);
            if (identifier != null && identifier.getIdentifier() != null) {
                return identifier.getIdentifier().getStringValue();
            }
        }
        return null;
    }

    public static String getProductType(WorkProductDocument.WorkProduct wp) {

        if (wp != null) {
            IdentificationType identifier = getIdentificationElement(wp);
            if (identifier != null && identifier.getType() != null) {
                return identifier.getType().getStringValue();
            }
        }
        return null;
    }

    public static String getProductVersion(WorkProductDocument.WorkProduct wp) {

        if (wp != null) {
            IdentificationType identifier = getIdentificationElement(wp);
            if (identifier != null && identifier.getVersion() != null) {
                return identifier.getVersion().getStringValue();
            }
        }
        return null;
    }

    /**
     * Get the Properties element from the given WorkProduct
     * 
     * @param workProduct
     * @return
     */
    public static final PropertiesType getPropertiesElement(com.saic.precis.x2009.x06.structures.WorkProductDocument.WorkProduct workProduct) {

        PropertiesType properties = null;
        if (workProduct != null && workProduct.getPackageMetadata() != null) {
            XmlObject[] objects = workProduct.getPackageMetadata().selectChildren(new QName(NS_PRECIS_STRUCTURES,
                                                                                            S_PACKAGE_PROPERTIES));
            if (objects.length > 0) {
                properties = (PropertiesType) objects[0];
            }
        }
        return properties;
    }

    public static final IdentificationType getWorkProductIdentification(WorkProduct wp) {

        IdentificationType pkgId = IdentificationType.Factory.newInstance();

        if (wp.getChecksum() != null) {
            pkgId.addNewChecksum().setStringValue(wp.getChecksum());
        }
        if (wp.getProductID() != null) {
            pkgId.addNewIdentifier().setStringValue(wp.getProductID());
        }
        if (wp.getProductType() != null) {
            pkgId.addNewType().setStringValue(wp.getProductType());
        }
        if (wp.getProductVersion() != null) {
            pkgId.addNewVersion().setStringValue(wp.getProductVersion().toString());
        }

        pkgId.setState(wp.getActive() ? StateType.ACTIVE : StateType.INACTIVE);

        return pkgId;
    }

    public static final PropertiesType getWorkProductProperties(WorkProduct wp) {

        PropertiesType properties = PropertiesType.Factory.newInstance();

        Set<String> igIdSet = wp.getAssociatedInterestGroupIDs();

        if (igIdSet != null) //fli added the check since igIdSet can be null!!!!
        {
            if (igIdSet.size() > 0) {
                String[] igIds = wp.getAssociatedInterestGroupIDs().toArray(new String[igIdSet.size()]);
                properties.addNewAssociatedGroups();
                for (String igId : igIds) {
                    properties.getAssociatedGroups().addNewIdentifier().setStringValue(igId);
                }
            }
        }

        if (wp.getCreatedDate() != null) {
            properties.addNewCreated().setDateValue(wp.getCreatedDate());
        }
        if (wp.getCreatedBy() != null) {
            properties.addNewCreatedBy().setStringValue(wp.getCreatedBy());
        }
        if (wp.getUpdatedDate() != null) {
            properties.addNewLastUpdated().setDateValue(wp.getUpdatedDate());
        }
        if (wp.getUpdatedBy() != null) {
            properties.addNewLastUpdatedBy().setStringValue(wp.getUpdatedBy());
        }
        if (wp.getMimeType() != null) {
            properties.addNewMimeType().setStringValue(wp.getMimeType());
        }
        if (wp.getSize() != null) {
            properties.addNewKilobytes().setBigIntegerValue(BigInteger.valueOf(wp.getSize()));
        }

        return properties;
    }

    public static final WorkProduct setWorkProductIdentification(WorkProduct wp,
                                                                 IdentificationType pkgIdentification) {

        if (pkgIdentification == null) {
            return wp;
        }

        if (pkgIdentification.getChecksum() != null) {
            wp.setChecksum(pkgIdentification.getChecksum().getStringValue());
        }
        if (pkgIdentification.getIdentifier() != null) {
            wp.setProductID(pkgIdentification.getIdentifier().getStringValue());
        }
        if (pkgIdentification.getType() != null) {
            wp.setProductType(pkgIdentification.getType().getStringValue());
        }
        if (pkgIdentification.getVersion() != null) {
            wp.setProductVersion(Integer.parseInt(pkgIdentification.getVersion().getStringValue()));
        }

        // to use the work product status
        wp.setActive(wp.getActive());

        return wp;
    }

    public static final WorkProduct toModel(WorkProductDocument.WorkProduct product) {

        if (product == null) {
            return null;
        }

        if (product.sizeOfStructuredPayloadArray() == 0) {
            return null;
        }

        WorkProduct wp = new WorkProduct();

        // StructuredPayload
        StructuredPayloadType payload = product.getStructuredPayloadArray(0);

        // the WorkProductType is the CommunityURI/namespace
        if (payload.getStructuredPayloadMetadata() == null ||
            payload.getStructuredPayloadMetadata().getCommunityURI() == null) {
            return null;
        }
        wp.setProductType(payload.getStructuredPayloadMetadata().getCommunityURI());
        if (payload.getStructuredPayloadMetadata().getCommunityVersion() == null) {
            return null;
        }
        wp.setProductTypeVersion(payload.getStructuredPayloadMetadata().getCommunityVersion());
        wp.setProduct(getPayload(payload));

        String mimeType = null;
        if (wp.getProduct() instanceof BinaryContentDocument) {
            mimeType = ((BinaryContentDocument) wp.getProduct()).getBinaryContent().getMimetype();
        }

        if (mimeType != null) {
            wp.setMimeType(mimeType);
        }

        // WorkProductIdentification
        XmlObject[] metadataArray = product.getPackageMetadata().getPackageMetadataExtensionAbstractArray();
        for (XmlObject metadata : metadataArray) {
            if (metadata instanceof IdentificationType) {
                // WorkProductIdentification
                IdentificationType id = (IdentificationType) metadata;
                if (id.getChecksum() != null) {
                    wp.setChecksum(id.getChecksum().getStringValue());
                }
                if (id.getIdentifier() != null) {
                    wp.setProductID(id.getIdentifier().getStringValue());
                }
                if (id.getType() != null) {
                    wp.setProductType(id.getType().getStringValue());
                }
                if (id.getVersion() != null && !id.getVersion().isNil()) {
                    try {
                        wp.setProductVersion(new Integer(id.getVersion().getStringValue()));
                    } catch (NumberFormatException e) {
                        log.error("Invalid version value");
                    }
                }
                if (id.getState() != null) {
                    wp.setActive(id.getState().equals(StateType.ACTIVE) ? true : false);
                }
            } else if (metadata instanceof PropertiesType) {
                // save the interest group
                PropertiesType props = (PropertiesType) metadata;
                if (props.getCreated() != null) {
                    wp.setCreatedDate(props.getCreated().getDateValue());
                }
                if (props.getCreatedBy() != null) {
                    wp.setCreatedBy(props.getCreatedBy().getStringValue());
                }
                if (props.getKilobytes() != null) {
                    wp.setSize(new Integer(props.getKilobytes().getStringValue()));
                }
                if (props.getLastUpdated() != null) {
                    wp.setUpdatedDate(props.getLastUpdated().getDateValue());
                }
                if (props.getLastUpdatedBy() != null) {
                    wp.setUpdatedBy(props.getLastUpdatedBy().getStringValue());
                }
                if (props.getMimeType() != null) {
                    wp.setMimeType(props.getMimeType().getStringValue());
                }
                if (props.getAssociatedGroups() != null) {
                    Set<String> idSet = new HashSet<String>();
                    IdentifierType[] ids = props.getAssociatedGroups().getIdentifierArray();
                    for (IdentifierType id : ids) {
                        idSet.add(id.getStringValue());
                    }
                    wp.setAssociatedInterestGroupIDs(idSet);
                }
            }
        }

        // Add the digest if it is there
        // we have to wait the last so the associatedInterestGroupIDs can be used as identifier
        if (product.getDigestAbstract() != null) {
            XmlObject d = product.getDigestAbstract().changeType(DigestType.type);
            if (d instanceof DigestType) {
                DigestDocument digest = DigestDocument.Factory.newInstance();
                digest.setDigest((DigestType) d);
                wp.setDigest(digest);
                // System.out.println("Adding Digest: "+digest);
            }
        }

        return wp;
    }

    public static final ProcessingStatusType toProcessingStatusType(ProductPublicationStatus status) {

        ProcessingStatusType pStatus = ProcessingStatusType.Factory.newInstance();

        if (status.getAct() != null) {
            // ACT is set if this is a pending request or if this a response to a pending request
            pStatus.addNewACT().setStringValue(status.getAct());
        }

        if (status.getStatus().equals(ProductPublicationStatus.SuccessStatus)) {
            // fill the WorkProductSummary
            pStatus.setStatus(ProcessingStateType.ACCEPTED);
        } else if (status.getStatus().equals(ProductPublicationStatus.FailureStatus)) {
            // fill the reason
            pStatus.setStatus(ProcessingStateType.REJECTED);
            pStatus.addNewMessage().setStringValue(status.getReasonForFailure());
        } else {
            pStatus.setStatus(ProcessingStateType.PENDING);
        }
        return pStatus;
    }

    public static final WorkProductDocument.WorkProduct toWorkProduct(WorkProduct wp) {

        WorkProductDocument wpd = toWorkProductDocument(wp, false);
        return wpd == null ? null : toWorkProductDocument(wp, false).getWorkProduct();
    }

    public static final WorkProductDocument toWorkProductDocument(WorkProduct wp) {

        return toWorkProductDocument(wp, false);
    }

    private static final WorkProductDocument toWorkProductDocument(WorkProduct wp, boolean summary) {

        if (wp == null) {
            return null;
        }

        WorkProductDocument wpd = WorkProductDocument.Factory.newInstance();
        WorkProductDocument.WorkProduct theWorkProduct = wpd.addNewWorkProduct();

        IdentificationType wpid = getWorkProductIdentification(wp);

        // PackageMetadata
        PackageMetadataType pkgMetadata = theWorkProduct.addNewPackageMetadata();

        pkgMetadata.setDataItemID(wp.getProductID());
        pkgMetadata.setDataItemReferenceID(wp.getProductID());

        // DataItemStatus
        // create the dataItemStatus then substitute it with the Abstract
        DataItemStatusType dataItemStatus = DataItemStatusType.Factory.newInstance();
        dataItemStatus.addNewLabel().setStringValue("UICDS Work Product Status");
        dataItemStatus.setCodespace(UICDS_WORK_PRODUCT_STATUS_CODE_SPACE);
        dataItemStatus.setCode(wpid.getState().toString());
        XmlUtil.substitute(pkgMetadata.addNewDataItemStatusAbstract(),
            NS_UCORE,
            S_DATA_ITEM_STATUS,
            DataItemStatusType.type,
            dataItemStatus);

        DataOwnerMetadataType ownerMetadata = pkgMetadata.addNewDataOwnerMetadata();
        // DataOwnerContact
        PointOfContactType contact = PointOfContactType.Factory.newInstance();
        contact.addNewOrganization().addName(UICDS_WEB_SITE);
        XmlUtil.substitute(ownerMetadata.addNewDataOwnerContactAbstract(),
            NS_UCORE,
            S_DATA_OWNER_CONTACT,
            PointOfContactType.type,
            contact);

        // DataOwnerIdentifier
        contact = PointOfContactType.Factory.newInstance();
        contact.addNewOrganization().addName(URI_UICDS);
        XmlUtil.substitute(ownerMetadata.addNewDataOwnerIdentifierAbstract(),
            NS_UCORE,
            S_DATA_OWNER_IDENTIFIER,
            PointOfContactType.type,
            contact);

        // DataOwnerMeatdataDomainAttribute
        DomainAttributeType domain = DomainAttributeType.Factory.newInstance();
        domain.setDomainName("UICDS EM Domain");
        XmlUtil.substitute(ownerMetadata.addNewDataOwnerMetadataExtensionAbstract(),
            NS_ULEX_STRUCTURE,
            S_DATA_OWNER_METADATA_DOMAIN_ATTRIBUTE,
            DomainAttributeType.type,
            domain);

        // DisseminatinCriteria
        XmlUtil.substitute(pkgMetadata.addNewDisseminationCriteriaAbstract(),
            NS_UCORE,
            S_DISSEMINATION_CRITERIA,
            DisseminationCriteria.type,
            DisseminationCriteria.Factory.newInstance());

        // WorkProductIdentification
        XmlUtil.substitute(pkgMetadata.addNewPackageMetadataExtensionAbstract(),
            NS_PRECIS_STRUCTURES,
            S_PACKAGE_IDENTIFICATION,
            IdentificationType.type,
            wpid);

        // WorkProductProperties
        XmlUtil.substitute(pkgMetadata.addNewPackageMetadataExtensionAbstract(),
            NS_PRECIS_STRUCTURES,
            S_PACKAGE_PROPERTIES,
            PropertiesType.type,
            getWorkProductProperties(wp));

        // Digest part
        if (wp.getDigest() != null) {
            XmlUtil.substitute(theWorkProduct.addNewDigestAbstract(),
                NS_UCORE,
                S_DIGEST,
                DigestType.type,
                wp.getDigest().getDigest());

        }
        // the structuredPayload part
        if (!summary) {
            StructuredPayloadType payload = theWorkProduct.addNewStructuredPayload();
            if (wp.getProduct() != null) {
                payload.set(wp.getProduct());
            } else {
                log.error("Payload was null for work product: " + wp.getProductID());
            }
            payload.addNewStructuredPayloadMetadata().setCommunityURI(wp.getProductType());
            payload.getStructuredPayloadMetadata().setCommunityVersion(wp.getProductTypeVersion() != null ? wp.getProductTypeVersion() : "");
        }

        return wpd;
    }

    public static final WorkProductProcessingStatusDocument toWorkProductProcessingStatus(ProductPublicationStatus status) {

        WorkProductProcessingStatusDocument processingStatus = WorkProductProcessingStatusDocument.Factory.newInstance();
        processingStatus.addNewWorkProductProcessingStatus();
        if (status.getStatus().equals(ProductPublicationStatus.SuccessStatus)) {
            processingStatus.getWorkProductProcessingStatus().setStatus(ProcessingStateType.ACCEPTED);
        } else {
            processingStatus.getWorkProductProcessingStatus().setStatus(ProcessingStateType.REJECTED);
            processingStatus.getWorkProductProcessingStatus().addNewMessage().setStringValue(status.getReasonForFailure());
        }

        return processingStatus;
    }

    public static final WorkProductPublicationResponseDocument toWorkProductPublicatinResponse(String statusStr) {

        // System.out.println("toWorkProductPublicatinResponse : statusStr=[" + statusStr + "]");
        WorkProductPublicationResponseDocument processingResponse = null;

        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                processingResponse = WorkProductPublicationResponseDocument.Factory.parse(statusStr);
            } catch (XmlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }

        return processingResponse;
    }

    public static final WorkProductPublicationResponseType toWorkProductPublicationResponse(ProductPublicationStatus status) {

        WorkProductPublicationResponseType theStatus = WorkProductPublicationResponseType.Factory.newInstance();
        theStatus.addNewWorkProductProcessingStatus();
        if (status.getStatus().equals(ProductPublicationStatus.SuccessStatus)) {
            // fill the WorkProductSummary
            theStatus.addNewWorkProduct();
            theStatus.getWorkProductProcessingStatus().setStatus(ProcessingStateType.ACCEPTED);
            theStatus.getWorkProduct().set(toWorkProductSummary(status.getProduct()));
        } else if (status.getStatus().equals(ProductPublicationStatus.FailureStatus)) {
            // fill the reason
            theStatus.getWorkProductProcessingStatus().setStatus(ProcessingStateType.REJECTED);
            theStatus.getWorkProductProcessingStatus().addNewMessage().setStringValue(status.getReasonForFailure());
        } else {
            // fill the ACTUuid
            theStatus.getWorkProductProcessingStatus().setStatus(ProcessingStateType.PENDING);
        }

        if (status.getAct() != null) {
            // ACT is set if this is a pending request or if this a response to a pending request
            theStatus.getWorkProductProcessingStatus().addNewACT().setStringValue(status.getAct());
        }

        return theStatus;
    }

    public static final WorkProductDocument.WorkProduct toWorkProductSummary(WorkProduct wp) {

        return toWorkProductDocument(wp, true).getWorkProduct();
    }

    public static final String toWorkProductXmlDocument(WorkProduct wp) {

        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(wp);
            oos.close();

            // Get the bytes of the serialized object
            byte[] buf = bos.toByteArray();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /*
     * this is for IdentificationType to trim the trailing carriage-return/new-line
     * to make it complete, the WorkProduct model will need to do the same for the
     * CheckSum and Identifier
    public static IdentificationType trimProductIdentification(IdentificationType pkgId) {

                IdentificationType identification = IdentificationType.Factory.newInstance();

                if (pkgId.getChecksum().getStringValue() != null) {
                    identification.addNewChecksum().setStringValue(
                        pkgId.getChecksum().getStringValue().trim());
                }

                if (pkgId.getIdentifier() != null) {
                    identification.addNewIdentifier().setStringValue(
                        pkgId.getIdentifier().getStringValue().trim());
                }

                if (pkgId.getType() != null) {
                    identification.addNewType().setStringValue(pkgId.getType().getStringValue().trim());
                }

                if (pkgId.getVersion() != null) {
                    identification.addNewVersion().setStringValue(
                        pkgId.getVersion().getStringValue().trim());
                }

                if (pkgId.getState() != null) {
                    identification.setState(pkgId.getState());
                }

                return identification;
            }
        */
}
