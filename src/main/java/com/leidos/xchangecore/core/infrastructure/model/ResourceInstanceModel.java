package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.HashSet;
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
 * The ResourceInstance data model.
 *
 * @ssdd
 */

@Entity
// @Table(name = "RESOURCE_INSTANCE")
public class ResourceInstanceModel
implements Serializable {

    private static final long serialVersionUID = 4631735818419898973L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // identifier and label are the UICDS unique identifier
    @Column(name = "IDENTIFIER", unique = true)
    @Field(index = Index.TOKENIZED)
    private String identifier;

    @Column(name = "LABEL")
    @Field(index = Index.TOKENIZED)
    private String label;

    // resourceID and owningCore identify the resource with the source that owns
    // it
    @Column(name = "RESOURCEID")
    @Field(index = Index.TOKENIZED)
    private String resourceID;

    @Column(name = "LOCALRESOURCEID")
    @Field(index = Index.TOKENIZED)
    private String localResourceID;

    @Column(name = "OWNINGCORE")
    @Field(index = Index.TOKENIZED)
    private String owningCore;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PROFILES")
    @CollectionOfElements(fetch = FetchType.EAGER)
    private Set<String> profiles = new HashSet<String>();

    //use set to replace that below map<string,string>
    @OneToMany(cascade = {
        CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();

    /*
    @CollectionOfElements
    private Map<String, String> keywords = new HashMap<String, String>();
     */

    // TODO: need to store Description (what size of text field do we need to
    // store for this?)

    @CollectionOfElements
    @Lob
    private Set<String> endpoints = new HashSet<String>();

    //FLI added on 11/30/2011
    //private int notMsgCount;

    public Set<CodeSpaceValueType> getCvts() {

        return cvts;
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
     * Gets the endpoints.
     *
     * @return the endpoints
     * @ssdd
     */
    public Set<String> getEndpoints() {

        return endpoints;
    }

    /**
     * Gets the id.
     *
     * @return the id
     * @ssdd
     */
    public Integer getId() {

        return id;
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     * @ssdd
     */
    public String getIdentifier() {

        return identifier;
    }

    /**
     * Gets the label.
     *
     * @return the label
     * @ssdd
     */
    public String getLabel() {

        return label;
    }

    /**
     * Gets the local resource id.
     *
     * @return the local resource id
     * @ssdd
     */
    public String getLocalResourceID() {

        return localResourceID;
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
     * Gets the profiles.
     *
     * @return the profiles
     * @ssdd
     */
    public Set<String> getProfiles() {

        return profiles;
    }

    /**
     * Gets the resource id.
     *
     * @return the resource id
     * @ssdd
     */
    public String getResourceID() {

        return resourceID;
    }

    public void setCvts(Set<CodeSpaceValueType> cvts) {

        this.cvts = cvts;
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
     * Sets the endpoints.
     *
     * @param endpoints the new endpoints
     * @ssdd
     */
    public void setEndpoints(Set<String> endpoints) {

        this.endpoints = endpoints;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     * @ssdd
     */
    public void setId(Integer id) {

        this.id = id;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the new identifier
     * @ssdd
     */
    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     * @ssdd
     */
    public void setLabel(String label) {

        this.label = label;
    }

    /**
     * Sets the local resource id.
     *
     * @param localResourceID the new local resource id
     * @ssdd
     */
    public void setLocalResourceID(String localResourceID) {

        this.localResourceID = localResourceID;
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
     * Sets the profiles.
     *
     * @param profiles the new profiles
     * @ssdd
     */
    public void setProfiles(Set<String> profiles) {

        this.profiles = profiles;
    }

    /**
     * Sets the resource id.
     *
     * @param resourceID the new resource id
     * @ssdd
     */
    public void setResourceID(String resourceID) {

        this.resourceID = resourceID;
    }

    /* comment out for now
    public int getNotMsgCount() {
    	return notMsgCount;
    }

    public void setNotMsgCount(int notMsgCount) {
    	this.notMsgCount = notMsgCount;
    }
     */
}
