package com.leidos.xchangecore.core.infrastructure.service.impl;

import java.util.Set;

import com.leidos.xchangecore.core.infrastructure.model.ExtendedMetadata;

/**
 * The InterestGroupInfo class provides methods for setting and getting interest group information.
 *
 * @ssdd
 */
public class InterestGroupInfo {

    private String interestGroupID;
    private String interestGroupType;
    private String interestGroupSubType;
    private String name;
    private String description;
    private String owningCore;
    private Set<ExtendedMetadata> extendedMetadata;

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
     * @return an ArrayList of coded extended metadata entries
     */
    public Set<ExtendedMetadata> getExtendedMetadata() {

        return extendedMetadata;
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
     * Gets the interest group sub type.
     *
     * @return the interest group sub type
     * @ssdd
     */
    public String getInterestGroupSubType() {

        return interestGroupSubType;
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
     * Gets the name.
     *
     * @return the name
     * @ssdd
     */
    public String getName() {

        return name;
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
     * Sets the description.
     *
     * @param description the new description
     * @ssdd
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * add a list of coded extended metadata
     * @param extendedMetadata
     */
    public void setExtendedMetadata(Set<ExtendedMetadata> extendedMetadata) {

        this.extendedMetadata = extendedMetadata;
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
     * Sets the interest group sub type.
     *
     * @param interestGroupSubType the new interest group sub type
     * @ssdd
     */
    public void setInterestGroupSubType(String interestGroupSubType) {

        this.interestGroupSubType = interestGroupSubType;
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
     * Sets the name.
     *
     * @param name the new name
     * @ssdd
     */
    public void setName(String name) {

        this.name = name;
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

    @Override
    public String toString() {

        return new String("Interest Group Id: " + getInterestGroupID() + ", Name: " + getName() +
                          ", Type: " + getInterestGroupType() + ", OwningCore: " + getOwningCore());
    }
}
