package com.leidos.xchangecore.core.infrastructure.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ShareRule data model.
 *
 * @ssdd
 */
@Entity
// @Embeddable
public class ShareRule {

    static Logger logger = LoggerFactory.getLogger(ShareRule.class);

    @Id
    @Column(name = "SHARE_RULE_ID")
    @GeneratedValue
    private Integer id;

    private boolean enabled;
    private String ruleID;

    // @ManyToOne(optional = true)
    @ManyToOne(targetEntity = Agreement.class)
    @JoinColumn(name = "AGREEMENT_ID", nullable = false)
    private Agreement agreement;

    // @Embedded
    // @OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    // @org.hibernate.annotations.Cascade(value =
    // org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    // private CodeSpaceValueType interestGroup;

    private String interestGroupCodeSpace;
    private String interestGroupLabel;
    private String interestGroupValue;

    // @Embedded
    // @OneToMany(mappedBy = "shareRule")
    @OneToMany(cascade = {
        CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    Set<CodeSpaceValueType> workProducts;

    @Column(name = "EXTENDED_METADATA")
    @OneToMany(cascade = {
        CascadeType.ALL
    })
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<ExtendedMetadata> extendedMetadata = new HashSet<ExtendedMetadata>();

    @Column(name = "PROXIMITY")
    private String remoteCoreProximity;

    @Column(name = "SHARE_ON_NO_LOC")
    private String shareOnNoLoc;

    @Override
    public boolean equals(Object obj) {

        final ShareRule shareRuleObject = (ShareRule) obj;

        final StringBuffer hash = new StringBuffer();
        // Interest Group Type
        if (!("".equals(interestGroupCodeSpace) | ("".equals(interestGroupValue)))) {
            hash.append(interestGroupCodeSpace);
            hash.append(interestGroupValue);
        }

        // Extended Metadata
        if (extendedMetadata != null) {
            for (final ExtendedMetadata data : extendedMetadata) {
                hash.append(data.getCode());
                hash.append(data.getCodespace());
                hash.append(data.getValue());
            }
        }

        // Proximity
        if (!("".equals(remoteCoreProximity))) {
            hash.append(remoteCoreProximity);
        }

        // compute share rule object hash
        final StringBuffer shareRuleObjectHash = new StringBuffer();
        // Interest Group Type
        if (!("".equals(shareRuleObject.getInterestGroup().getCodeSpace()) | ("".equals(shareRuleObject.getInterestGroup().getValue())))) {
            shareRuleObjectHash.append(shareRuleObject.getInterestGroup().getCodeSpace());
            shareRuleObjectHash.append(shareRuleObject.getInterestGroup().getValue());
        }

        // Extended Metadata
        if (shareRuleObject.extendedMetadata != null) {
            for (final ExtendedMetadata data : shareRuleObject.extendedMetadata) {
                shareRuleObjectHash.append(data.getCode());
                shareRuleObjectHash.append(data.getCodespace());
                shareRuleObjectHash.append(data.getValue());
            }
        }

        // Proximity
        if (!("".equals(shareRuleObject.remoteCoreProximity))) {
            shareRuleObjectHash.append(shareRuleObject.remoteCoreProximity);
        }

        return (hash.equals(shareRuleObjectHash));
    }

    /**
     * Gets the agreement.
     *
     * @return the agreement
     * @ssdd
     */
    public Agreement getAgreement() {

        return agreement;
    }

    public Set<ExtendedMetadata> getExtendedMetadata() {

        if (extendedMetadata == null) {
            extendedMetadata = new HashSet<ExtendedMetadata>();
        }
        return extendedMetadata;
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
     * Gets the interest group.
     *
     * @return the interest group
     * @ssdd
     */
    public CodeSpaceValueType getInterestGroup() {

        final CodeSpaceValueType c = new CodeSpaceValueType();
        c.setCodeSpace(interestGroupCodeSpace);
        c.setLabel(interestGroupLabel);
        c.setValue(interestGroupValue);
        return c;
    }

    public String getRemoteCoreProximity() {

        return remoteCoreProximity;
    }

    /**
     * Gets the rule id.
     *
     * @return the rule id
     * @ssdd
     */
    public String getRuleID() {

        return ruleID;
    }

    public String getShareOnNoLoc() {

        return shareOnNoLoc;
    }

    /**
     * Gets the work products.
     *
     * @return the work products
     * @ssdd
     */
    public Set<CodeSpaceValueType> getWorkProducts() {

        if (workProducts == null) {
            workProducts = new HashSet<CodeSpaceValueType>();
        }
        return workProducts;
    }

    @Override
    public int hashCode() {

        final StringBuffer hash = new StringBuffer();
        // Interest Group Type
        if (!("".equals(interestGroupCodeSpace) | "".equals(interestGroupValue))) {
            hash.append(interestGroupCodeSpace);
            hash.append(interestGroupValue);
        }

        // Extended Metadata
        if (extendedMetadata != null) {
            for (final ExtendedMetadata data : extendedMetadata) {
                hash.append(data.getCode());
                hash.append(data.getCodespace());
                hash.append(data.getValue());
            }
        }

        // Proximity
        if (!("".equals(remoteCoreProximity))) {
            hash.append(remoteCoreProximity);
        }

        return hash.hashCode();
    }

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     * @ssdd
     */
    public boolean isEnabled() {

        return enabled;
    }

    /**
     * Sets the agreement.
     *
     * @param agreement the new agreement
     * @ssdd
     */
    public void setAgreement(Agreement agreement) {

        this.agreement = agreement;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the new enabled
     * @ssdd
     */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public void setExtendedMetadata(Set<ExtendedMetadata> extendedMetadata) {

        logger.debug("setExtendedMetadata: count: " + extendedMetadata.size());
        this.extendedMetadata = getExtendedMetadata();
        this.extendedMetadata.clear();
        for (final ExtendedMetadata em : extendedMetadata) {
            logger.debug("setExtendedMetadata: adding " + em);
            this.extendedMetadata.add(em);
        }
        logger.debug("setExtendedMetadata: added: " + this.extendedMetadata.size());
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
     * Sets the interest group.
     *
     * @param interestGroup the new interest group
     * @ssdd
     */
    public void setInterestGroup(CodeSpaceValueType interestGroup) {

        interestGroupCodeSpace = interestGroup.getCodeSpace();
        interestGroupLabel = interestGroup.getLabel();
        interestGroupValue = interestGroup.getValue();
    }

    public void setRemoteCoreProximity(String remoteCoreProximity) {

        this.remoteCoreProximity = remoteCoreProximity;
    }

    /**
     * Sets the rule id.
     *
     * @param ruleID the new rule id
     * @ssdd
     */
    public void setRuleID(String ruleID) {

        this.ruleID = ruleID;
    }

    public void setShareOnNoLoc(String shareOnNoLoc) {

        this.shareOnNoLoc = shareOnNoLoc;
    }

    /**
     * Sets the work products.
     *
     * @param workProducts the new work products
     * @ssdd
     */
    public void setWorkProducts(Set<CodeSpaceValueType> workProducts) {

        this.workProducts = getWorkProducts();
        this.workProducts.clear();
        for (final CodeSpaceValueType workProduct : workProducts) {
            // workProduct.setShareRule(this);
            this.workProducts.add(workProduct);
        }
        // this.workProducts = workProducts;
    }

}