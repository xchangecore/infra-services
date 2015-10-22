package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The InterestGroup data model.
 * @ssdd
 */
@Entity
// @Table(name = "INTEREST_GROUP")
public class InterestGroup
    implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "INTERESTGROUP_ID")
    @Field(index = Index.TOKENIZED)
    private String interestGroupID;

    @Column(name = "INTERESTGROUP_TYPE")
    @Field(index = Index.TOKENIZED)
    private String interestGroupType;

    @Column(name = "INTERESTGROUP_SUBTYPE")
    @Field(index = Index.TOKENIZED)
    private String interestGroupSubtype;

    @Column(name = "NAME")
    @Field(index = Index.TOKENIZED)
    private String name;

    @Lob
    @Column(name = "DESCRIPTION")
    @Field(index = Index.TOKENIZED)
    private String description;

    @Column(name = "OWNING_CORE")
    @Field(index = Index.TOKENIZED)
    private String owningCore;

    @Column(name = "SHARING_STATUS")
    @Field(index = Index.TOKENIZED)
    private String sharingStatus;

    //
    @Column(name = "EXTENDED_METADATA")
    @OneToMany(cascade = CascadeType.ALL)
    private Set<ExtendedMetadata> extendedMetadata = new HashSet<ExtendedMetadata>();

    // for shared incidents only, i.e. incidents owned by this core and shared
    // with other cores
    @Column(name = "SHARED_CORE_LIST")
    @CollectionOfElements(fetch = FetchType.EAGER)
    private Set<String> sharedCoreList = new HashSet<String>();
    // end of shared incident data

    // for joined incidents only, i. e. incidents owned by another core but is
    // joined to by this
    // core
    @Column(name = "JOINED_WPTYPE_LIST")
    @CollectionOfElements(fetch = FetchType.EAGER)
    private Set<String> joinedWpTypeList = new HashSet<String>();

    @Column(name = "OWNER_PROPERTIES")
    @Lob
    private byte[] ownerProperties = new byte[0];

    private boolean active = true;

    // end of joined incident data

    public InterestGroup() {

    }

    /**
     * Instantiates a new interest group.
     *
     * @param interestGroupID the interest group id
     * @ssdd
     */
    public InterestGroup(String interestGroupID) {

        setInterestGroupID(interestGroupID);
    }

    /**
     * Gets the description.
     *
     * @return the description
     * @ssdd
     */
    public String getDescription() {

        return description;
    }

    /**
     *
     * @return a Set of extended metadata
     */
    public Set<ExtendedMetadata> getExtendedMetadata() {

        return extendedMetadata;
    }

    public Integer getId() {

        return id;
    }

    /**
     * Gets the interest group id.
     *
     * @return the interest group id
     * @ssdd
     */
    public String getInterestGroupID() {

        return interestGroupID;
    }

    /**
     * Gets the interest group subtype.
     *
     * @return the interest group subtype
     * @ssdd
     */
    public String getInterestGroupSubtype() {

        return interestGroupSubtype;
    }

    /**
     * Gets the interest group type.
     *
     * @return the interest group type
     * @ssdd
     */
    public String getInterestGroupType() {

        return interestGroupType;
    }

    /**
     * Gets the joined wp type list.
     *
     * @return the joined wp type list
     * @ssdd
     */
    public List<String> getJoinedWpTypeList() {

        return new ArrayList<String>(joinedWpTypeList);
    }

    /**
     * Gets the name.
     *
     * @return the name
     * @ssdd
     */
    public String getName() {

        return name;
    }

    /**
     * Gets the owner properties.
     *
     * @return the owner properties
     * @ssdd
     */
    public String getOwnerProperties() {

        // System.out.println("getOwnerProperties: props=[" +
        // ownerProperties.toString() + "]");
        return new String(ownerProperties);
    }

    /**
     * Gets the owning core.
     *
     * @return the owning core
     * @ssdd
     */
    public String getOwningCore() {

        return owningCore;
    }

    /**
     * Gets the shared core list.
     *
     * @return the shared core list
     * @ssdd
     */
    public Set<String> getSharedCoreList() {

        return sharedCoreList;
    }

    /**
     * Gets the sharing status.
     *
     * @return the sharing status
     * @ssdd
     */
    public String getSharingStatus() {

        return sharingStatus;
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     * @ssdd
     */
    public boolean isActive() {

        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the new active
     * @ssdd
     */
    public void setActive(boolean active) {

        this.active = active;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     * @ssdd
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     *
     * @param extendedMetadata
     */
    public void setExtendedMetadata(Set<ExtendedMetadata> extendedMetadata) {

        this.extendedMetadata = extendedMetadata;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Sets the interest group id.
     *
     * @param interestGroupID the new interest group id
     * @ssdd
     */
    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    /**
     * Sets the interest group subtype.
     *
     * @param interestGroupSubtype the new interest group subtype
     * @ssdd
     */
    public void setInterestGroupSubtype(String interestGroupSubtype) {

        this.interestGroupSubtype = interestGroupSubtype;
    }

    /**
     * Sets the interest group type.
     *
     * @param interestGroupType the new interest group type
     * @ssdd
     */
    public void setInterestGroupType(String interestGroupType) {

        this.interestGroupType = interestGroupType;
    }

    /**
     * Sets the joined wp type list.
     *
     * @param joinedWpTypeList the new joined wp type list
     * @ssdd
     */
    public void setJoinedWpTypeList(List<String> joinedWpTypeList) {

        this.joinedWpTypeList = new HashSet<String>(joinedWpTypeList);
    }

    /**
     * Sets the joined wp type list.
     *
     * @param joinedWpTypeList the new joined wp type list
     * @ssdd
     */
    public void setJoinedWpTypeList(Set<String> joinedWpTypeList) {

        this.joinedWpTypeList = joinedWpTypeList;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     * @ssdd
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Sets the owner properties.
     *
     * @param ownerProperties the new owner properties
     * @ssdd
     */
    public void setOwnerProperties(byte[] ownerProperties) {

        this.ownerProperties = ownerProperties;
    }

    /**
     * Sets the owner properties.
     *
     * @param ownerProperties the new owner properties
     * @ssdd
     */
    public void setOwnerProperties(String ownerProperties) {

        this.ownerProperties = ownerProperties.getBytes();
        // System.out.println("setOwnerProperties: props=[" +
        // ownerProperties.toString() + "]");
    }

    /**
     * Sets the owning core.
     *
     * @param owningCore the new owning core
     * @ssdd
     */
    public void setOwningCore(String owningCore) {

        this.owningCore = owningCore;
    }

    /**
     * Sets the shared core list.
     *
     * @param sharedCoreList the new shared core list
     * @ssdd
     */
    public void setSharedCoreList(Set<String> sharedCoreList) {

        this.sharedCoreList = sharedCoreList;
    }

    /**
     * Sets the sharing status.
     *
     * @param sharingStatus the new sharing status
     * @ssdd
     */
    public void setSharingStatus(String sharingStatus) {

        this.sharingStatus = sharingStatus;
    }

    @Override
    public String toString() {

        final StringBuffer sb = new StringBuffer();
        sb.append("[InterestGroup]:\n");
        sb.append("\tID: " + id + "\n");
        sb.append("\tinterestGroupID: " + interestGroupID + "\n");
        sb.append("\tinterestGroupType: " + interestGroupType + "\n");
        sb.append("\tinterestGroupSubType: " + interestGroupSubtype + "\n");
        sb.append("\tname: " + name + "\n");
        sb.append("\tsharingStatus: " + sharingStatus + "\n");
        sb.append("\towningCore: " + owningCore + "\n");
        sb.append("\tactive: " + (active ? "Active" : "Inactive") + "\n");

        return sb.toString();
    }

}
