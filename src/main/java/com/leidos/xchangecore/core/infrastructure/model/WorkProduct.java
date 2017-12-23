package com.leidos.xchangecore.core.infrastructure.model;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.util.WorkProductConstants;

import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.IdentifierType;
import gov.ucore.ucore.x20.ThingType;

/**
 * A work product is a typed resource that contains data in one of several standard data formats
 * about some aspect of an incident.
 * 
 * @author topher
 * @ssdd
 */
@Entity
public class WorkProduct implements Serializable, WorkProductConstants {

    private static final long serialVersionUID = -7909684672536211321L;
    private static final String UICDSXmlMimeType = "application/uicds+xml";

    private static Logger logger = LoggerFactory.getLogger(WorkProduct.class);

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Field(index = Index.TOKENIZED)
    private String productID;

    @Field(index = Index.TOKENIZED)
    private String productType;

    private String productTypeVersion;

    @Field(index = Index.TOKENIZED)
    private Integer productVersion;

    private String checksum;

    @Column(name = "AssociatedInterestGroupIDs")
    /*
    @CollectionOfElements(fetch = FetchType.EAGER)
    private Set<String> associatedInterestGroupIDs = new HashSet<String>();
    */
    private String associatedInterestGroupIDs = "";

    @Field(index = Index.TOKENIZED)
    private Date createdDate = new Date();

    @Field(index = Index.TOKENIZED)
    private String createdBy;

    @Field(index = Index.TOKENIZED)
    private Date updatedDate;

    @Field(index = Index.TOKENIZED)
    private String updatedBy;

    private String mimeType;

    @Lob
    private Blob digest;

    @Lob
    private Blob product;

    private Integer size;

    private String state = State_Active;

    /**
     * Instantiates a new work product.
     * 
     * @ssdd
     */
    public WorkProduct() {

    }

    /**
     * Instantiates a new work product with existed work product content
     * 
     * @param wp the wp
     * @ssdd
     */
    public WorkProduct(WorkProduct wp) {

        this();

        if (wp.getFirstAssociatedInterestGroupID() != null) {
            associatedInterestGroupIDs = wp.getFirstAssociatedInterestGroupID();
        }

        setChecksum(wp.getChecksum());
        setCreatedBy(wp.getCreatedBy());
        setCreatedDate(wp.getCreatedDate());
        setActive(wp.getActive());
        setMimeType(wp.getMimeType());
        // Create a new one for the new instances otherwise it is linked to the current entry
        if (wp.getDigest() != null) {
            setDigest((DigestDocument) wp.getDigest().copy());
        }

        setProductID(wp.getProductID());
        setProductType(wp.getProductType());
        setProductVersion(wp.getProductVersion());
        setSize(wp.getSize());
        setUpdatedBy(wp.getUpdatedBy());
        setUpdatedDate(wp.getUpdatedDate());

        setProduct(wp.getProduct().copy());
    }

    public void associateInterestGroup(String interestGroupID) {

        // logger.debug("associateInterestGroup: " + interestGroupID);
        if (!associatedInterestGroupIDs.contains(interestGroupID)) {
            // logger.debug("associateInterestGroup: before [" + associatedInterestGroupIDs + "]");
            if (associatedInterestGroupIDs.length() == 0) {
                associatedInterestGroupIDs = new String(interestGroupID);
            } else {
                associatedInterestGroupIDs = new String(associatedInterestGroupIDs + " " + interestGroupID);
            }
            // logger.debug("associateInterestGroup: after [" + associatedInterestGroupIDs + "]");
        }
    }

    /**
     * Gets the active./get
     * 
     * @return the active
     * @ssdd
     */
    public boolean getActive() {

        return state.equalsIgnoreCase(State_Active);
    }

    /**
     * Gets the associated interest group ids.
     * 
     * @return the associated interest group ids
     * @ssdd
     */
    public Set<String> getAssociatedInterestGroupIDs() {

        return id2Set();
    }

    /**
     * Gets the checksum.
     * 
     * @return the checksum
     * @ssdd
     */
    public String getChecksum() {

        return checksum;
    }

    /**
     * Gets the created by.
     * 
     * @return the created by
     * @ssdd
     */
    public String getCreatedBy() {

        return createdBy;
    }

    /**
     * Gets the created date.
     * 
     * @return the created date
     * @ssdd
     */
    public Date getCreatedDate() {

        return createdDate;
    }

    /**
     * Gets the digest.
     * 
     * @return the digest
     * @ssdd
     */
    public DigestDocument getDigest() {

        DigestDocument digestDocument = null;

        if (digest != null) {
            try {
                InputStream is = digest.getBinaryStream();

                if (is != null && is.available() > 0) {
                    digestDocument = DigestDocument.Factory.parse(is);
                }
            } catch (Exception e) {
                logger.error("cannot parse digest:\n" + e.getMessage());
            }
        }

        return digestDocument;
    }

    /**
     * Gets the first associated interest group id.
     * 
     * @return the first associated interest group id
     * @ssdd
     */
    public String getFirstAssociatedInterestGroupID() {

        if (associatedInterestGroupIDs.length() > 0) {
            int index = associatedInterestGroupIDs.indexOf(" ");
            return index == -1 ? associatedInterestGroupIDs : associatedInterestGroupIDs.substring(0, index);
        } else {
            return null;
        }
    }

    /**
     * Returns the the primary key of the WorkProduct.
     * 
     * @return the id
     * @ssdd
     */
    public Integer getId() {

        return id;
    }

    public String getMetadata() {

        StringBuffer sb = new StringBuffer();
        sb.append("[WorkProduct]:\n");
        if (getId() != null) {
            sb.append("\tID: " + getId() + "\n");
        }
        sb.append("\tproductID: " + getProductID() + "\n");
        if (getFirstAssociatedInterestGroupID() != null) {
            sb.append("\tIGID: " + getFirstAssociatedInterestGroupID() + "\n");
        }
        sb.append("\tversion: " + getProductVersion() + "\n");
        sb.append("\ttype: " + getProductType() + "\n");
        sb.append("\tlastUpdatedBy: " + getUpdatedBy() + "\n");
        sb.append("\tlastUpdatedDate: " + getUpdatedDate() + "\n");
        sb.append("\tcreateBy: " + getCreatedBy() + "\n");
        sb.append("\tcreatedDate: " + getCreatedDate() + "\n");
        sb.append("\tchecksum: " + getChecksum() + "\n");
        sb.append("\tsize: " + getSize() + " Kilobytes\n");
        sb.append("\tstatus: " + state + "\n");

        return sb.toString();
    }

    /**
     * Gets the mime type.
     * 
     * @return the mime type
     * @ssdd
     */
    public String getMimeType() {

        return mimeType;
    }

    /**
     * Gets the product.
     * 
     * @return the product
     * @ssdd
     */
    public XmlObject getProduct() {

        XmlObject o = null;
        if (product != null) {
            try {
                InputStream is = product.getBinaryStream();

                if (is != null && is.available() > 0) {
                    o = XmlObject.Factory.parse(is);
                }
            } catch (Exception e) {
                logger.error("cannot parse product:\n" + e.getMessage());
            }
        }

        return o;
    }

    /**
     * Gets the product id.
     * 
     * @return the product id
     * @ssdd
     */
    public String getProductID() {

        return productID;
    }

    /**
     * Gets the product type.
     * 
     * @return the product type
     * @ssdd
     */
    public String getProductType() {

        return productType;
    }

    /**
     * Gets the product type version.
     * 
     * @return the product type version
     * @ssdd
     */
    public String getProductTypeVersion() {

        return productTypeVersion;
    }

    /**
     * Gets the product version.
     * 
     * @return the product version
     * @ssdd
     */
    public Integer getProductVersion() {

        return productVersion;
    }

    public Integer getSize() {

        return size;
    }

    /**
     * Gets the updated by.
     * 
     * @return the updated by
     * @ssdd
     */
    public String getUpdatedBy() {

        return updatedBy;
    }

    /**
     * Gets the updated date.
     * 
     * @return the updated date
     * @ssdd
     */
    public Date getUpdatedDate() {

        return updatedDate;
    }

    @Override
    public int hashCode() {

        return new String(productID + checksum).hashCode();
    }

    private Set<String> id2Set() {

        Set<String> idSet = new HashSet<String>();
        String[] igIDs = associatedInterestGroupIDs.split(" ", -1);
        for (String igID : igIDs) {
            if (igID.length() > 0)
                idSet.add(igID.trim());
        }
        // logger.debug("id2Set: [" + associatedInterestGroupIDs + "] to set of " + idSet.size() + "IGIDs");
        return idSet;
    }

    /**
     * Check whether the requstor is the creator
     * 
     * @param user
     * @return
     */
    public boolean isCreator(String user) {

        String[] tokens = this.createdBy.split("@", -1);
	logger.debug("WorkProduct.isCreator: Creator: [" + tokens[0].trim() + "], user: [" + user.trim() + "]");
        if (tokens[0].trim().equalsIgnoreCase(user.trim()))
            return true;

        return false;
    }

    /**
     * Checks if is active.
     * 
     * @return true, if is active
     * @ssdd
     */
    public boolean isActive() {

        return state.equalsIgnoreCase(State_Active);
    }

    /**
     * Checks if is uICDS xml.
     * 
     * @return true, if is uICDS xml
     * @ssdd
     */
    public boolean isUICDSXml() {

        return mimeType.equals(UICDSXmlMimeType);
    }

    /**
     * Sets the active.
     * 
     * @param active the new active
     * @ssdd
     */
    public void setActive(boolean active) {

        state = active ? State_Active : State_Inactive;
    }

    /**
     * Sets the associated interest group i ds.
     * 
     * @param associatedInterestGroupIDs the new associated interest group i ds
     * @ssdd
     */
    public void setAssociatedInterestGroupIDs(Set<String> idSet) {

        StringBuffer sb = new StringBuffer();
        for (String id : idSet) {
            logger.debug("setAssociatedInterestGroupIDs: adding IGID: " + id);
            sb.append(id);
            sb.append(" ");
        }
        associatedInterestGroupIDs = new String(sb.toString().trim());
        logger.debug("setAssociatedInterestGroupIDs: associatedInterestGroupIDs: [" + associatedInterestGroupIDs + "]");
        logger.debug("setAssociatedInterestGroupIDs: IGID: [" + getFirstAssociatedInterestGroupID() + "]");
    }

    /**
     * Sets the checksum.
     * 
     * @param checksum the new checksum
     * @ssdd
     */
    public void setChecksum(String checksum) {

        this.checksum = checksum;
    }

    /**
     * Sets the created by.
     * 
     * @param createdBy the new created by
     * @ssdd
     */
    public void setCreatedBy(String createdBy) {

        this.createdBy = createdBy;
    }

    /**
     * Sets the created date.
     * 
     * @param createdDate the new created date
     * @ssdd
     */
    public void setCreatedDate(Date createdDate) {

        this.createdDate = createdDate;
    }

    /**
     * Sets the default mime type.
     * 
     * @ssdd
     */
    public void setDefaultMimeType() {

        mimeType = UICDSXmlMimeType;
    }

    /**
     * Sets the digest.
     * 
     * @param digest the new digest
     * @ssdd
     */
    public void setDigest(DigestDocument digestDocument) {

        ThingType thing = digestDocument.getDigest().getThingAbstractArray(0);
        if (thing != null && thing instanceof ThingType && thing.sizeOfIdentifierArray() == 0) {
            IdentifierType id = thing.addNewIdentifier();
            String IGID = getFirstAssociatedInterestGroupID();
            if (IGID == null || IGID.length() == 0) {
                id.addNewLabel().setStringValue("ProductID");
                id.setStringValue(productID);
            } else {
                id.addNewLabel().setStringValue("InterestGroupID");
                id.setStringValue(IGID);
            }
            thing.setIdentifierArray(0, id);
            logger.debug("WorkProduct: after adding Digest.Event.Identifier: " + id.getStringValue());
        }

        digest = Hibernate.createBlob(digestDocument.xmlText().getBytes());
    }

    /**
     * Sets the id of the WorkProduct.
     * 
     * @param id the id
     * @ssdd
     */
    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Sets the mime type.
     * 
     * @param mimeType the new mime type
     * @ssdd
     */
    public void setMimeType(String mimeType) {

        this.mimeType = mimeType;
    }

    /**
     * Sets the product.
     * 
     * @param product the new product
     * @ssdd
     */
    public void setProduct(XmlObject productDocument) {

        product = Hibernate.createBlob(productDocument.xmlText().getBytes());
    }

    /**
     * Sets the product id.
     * 
     * @param productID the new product id
     * @ssdd
     */
    public void setProductID(String productID) {

        this.productID = productID;
    }

    /**
     * Sets the product type.
     * 
     * @param productType the new product type
     * @ssdd
     */
    public void setProductType(String productType) {

        this.productType = productType;
    }

    /**
     * Sets the product type version.
     * 
     * @param productTypeVersion the new product type version
     * @ssdd
     */
    public void setProductTypeVersion(String productTypeVersion) {

        this.productTypeVersion = productTypeVersion;
    }

    /**
     * Sets the product version.
     * 
     * @param productVersion the new product version
     * @ssdd
     */
    public void setProductVersion(Integer productVersion) {

        this.productVersion = productVersion;
    }

    public void setSize(Integer size) {

        this.size = size;
    }

    /**
     * Sets the updated by.
     * 
     * @param updatedBy the new updated by
     * @ssdd
     */
    public void setUpdatedBy(String updatedBy) {

        this.updatedBy = updatedBy;
    }

    /**
     * Sets the updated date.
     * 
     * @param updatedDate the new updated date
     * @ssdd
     */
    public void setUpdatedDate(Date updatedDate) {

        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(getMetadata());

        if (getProduct() != null) {
            sb.append("\tcontent:\n" + getProduct().toString() + "\n");
        }
        if (getDigest() != null) {
            sb.append("\tdigest:\n" + getDigest().toString() + "\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
