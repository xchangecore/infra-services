package com.leidos.xchangecore.core.infrastructure.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * The Agreement data model.
 *
 * @ssdd
 */
@Entity
public class Agreement {

    private static final String JIDType_Group = "groups";
    private static final String JIDType_User = "ids";

    @Id
    @Column(name = "AGREEMENT_ID")
    @GeneratedValue
    private Integer id;

    @Column(columnDefinition = "tinyint default 0")
    private boolean enabled = false;

    @Column(columnDefinition = "tinyint default 0")
    private boolean mutuallyAgreed = false;

    private String remoteCodeSpace;
    private String remoteValue;
    private String localCodeSpace;
    private String localValue;
    private String description;

    @OneToMany(cascade = {
                          CascadeType.ALL
    }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<ShareRule> shareRules;

    @Override
    public boolean equals(Object obj) {

        return ((Agreement) obj).getId() == getId();
    }

    private String extractCorename(final String fullJID) {

        int index = fullJID.indexOf("?");
        return index == -1 ? fullJID : fullJID.substring(0, index);
    }

    public String getDescription() {

        return description;
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

    private Set<String> getIDs(String jid, String type) {

        Set<String> idSet = new HashSet<String>();

        // ignore the core part
        int index = jid.indexOf("?");
        if (index == -1)
            return idSet;

        String jidString = jid.substring(index + 1);

        // separate each ids and groups
        String[] list = jidString.split("&", -1);
        for (String item : list) {
            String[] parts = item.split("=");
            // if the type is not specified the continue
            if (parts[0].equalsIgnoreCase(type) == false)
                continue;

            String[] ids = parts[1].split(",", -1);
            for (String id : ids)
                idSet.add(id);
        }
        return idSet;
    }

    /**
     * Gets the local code space.
     *
     * @return the local code space
     * @ssdd
     */
    public String getLocalCodeSpace() {

        return localCodeSpace;
    }

    /**
     * Gets the local core.
     *
     * @return the local core
     * @ssdd
     */
    public CodeSpaceValueType getLocalCore() {

        CodeSpaceValueType c = new CodeSpaceValueType();
        c.setCodeSpace(localCodeSpace);
        c.setValue(localValue);
        return c;
    }

    public String getLocalCorename() {

        return extractCorename(localValue);
    }

    public Set<String> getLocalGroups() {

        return getIDs(localValue, JIDType_Group);
    }

    public Set<String> getLocalJIDs() {

        return getIDs(localValue, JIDType_User);
    }

    /**
     * Gets the local value.
     *
     * @return the local value
     * @ssdd
     */
    public String getLocalValue() {

        return localValue;
    }

    /**
     * Gets the remote code space.
     *
     * @return the remote code space
     * @ssdd
     */
    public String getRemoteCodeSpace() {

        return remoteCodeSpace;
    }

    /**
     * Gets the remote core.
     *
     * @return the remote core
     * @ssdd
     */
    public CodeSpaceValueType getRemoteCore() {

        CodeSpaceValueType c = new CodeSpaceValueType();
        c.setCodeSpace(remoteCodeSpace);
        c.setValue(remoteValue);
        return c;
    }

    public String getRemoteCorename() {

        return extractCorename(remoteValue);
    }

    public Set<String> getRemoteGroups() {

        return getIDs(remoteValue, JIDType_Group);
    }

    public Set<String> getRemoteJIDs() {

        return getIDs(remoteValue, JIDType_User);
    }

    /**
     * Gets the remote value.
     *
     * @return the remote value
     * @ssdd
     */
    public String getRemoteValue() {

        return remoteValue;
    }

    /**
     * Gets the share rules.
     *
     * @return the share rules
     * @ssdd
     */
    public Set<ShareRule> getShareRules() {

        if (shareRules == null)
            shareRules = new HashSet<ShareRule>();
        return shareRules;
    }

    /*
    @Override
    public int hashCode() {

        return new String(localValue + remoteValue).hashCode();
    }
     */

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     * @ssdd
     */
    public boolean isEnabled() {

        return enabled;
    }

    public boolean isIntraCoreAgreement() {

        return getLocalCorename().equalsIgnoreCase(getRemoteCorename());
    }

    public boolean isMutuallyAgreed() {

        return mutuallyAgreed;
    }

    public void setDescription(String description) {

        this.description = description;
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
     * Sets the local code space.
     *
     * @param localCodeSpace the new local code space
     * @ssdd
     */
    public void setLocalCodeSpace(String localCodeSpace) {

        this.localCodeSpace = localCodeSpace;
    }

    /**
     * Sets the local core.
     *
     * @param localCore the new local core
     * @ssdd
     */
    public void setLocalCore(CodeSpaceValueType localCore) {

        localCodeSpace = localCore.getCodeSpace();
        setLocalValue(localCore.getValue());
    }

    /**
     * Sets the local value.
     *
     * @param localValue the new local value
     * @ssdd
     */
    public void setLocalValue(String localValue) {

        this.localValue = localValue;
    }

    public void setMutuallyAgreed(boolean mutuallyAgreed) {

        this.mutuallyAgreed = mutuallyAgreed;
    }

    /**
     * Sets the remote code space.
     *
     * @param remoteCodeSpace the new remote code space
     */
    public void setRemoteCodeSpace(String remoteCodeSpace) {

        this.remoteCodeSpace = remoteCodeSpace;
    }

    /**
     * Sets the remote core.
     *
     * @param remoteCore the new remote core
     * @ssdd
     */
    public void setRemoteCore(CodeSpaceValueType remoteCore) {

        remoteCodeSpace = remoteCore.getCodeSpace();
        setRemoteValue(remoteCore.getValue());
    }

    /**
     * Sets the remote value.
     *
     * @param remoteValue the new remote value
     * @ssdd
     */
    public void setRemoteValue(String remoteValue) {

        this.remoteValue = remoteValue;
    }

    /**
     * Sets the share rules.
     *
     * @param shareRules the new share rules
     * @ssdd
     */
    public void setShareRules(Set<ShareRule> shareRules) {

        this.shareRules = getShareRules();
        this.shareRules.clear();
        for (ShareRule rule : shareRules) {
            rule.setAgreement(this);
            this.shareRules.add(rule);
        }
        // this.shareRules = shareRules;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("Agreement: [" + getId() + "]");
        sb.append(enabled ? "Active" : "Inactive");
        sb.append("\n");

        sb.append("\tLocalCoreJID: " + getLocalCorename());
        sb.append("\n");
        if (!getLocalGroups().isEmpty()) {
            sb.append("\t\tGroup: ");
            String[] ids = getLocalGroups().toArray(new String[getLocalGroups().size()]);
            for (String id : ids)
                sb.append(id + " ");
            sb.append("\n");
        }
        if (!getLocalJIDs().isEmpty()) {
            sb.append("\t\tJID: ");
            String[] ids = getLocalJIDs().toArray(new String[getLocalJIDs().size()]);
            for (String id : ids)
                sb.append(id + " ");
            sb.append("\n");
        }

        sb.append("\n\tRemoteCoreJID: " + getRemoteCorename());
        sb.append("\n");
        if (!getRemoteGroups().isEmpty()) {
            sb.append("\t\tGroup: ");
            String[] ids = getRemoteGroups().toArray(new String[getRemoteGroups().size()]);
            for (String id : ids)
                sb.append(id + " ");
            sb.append("\n");
        }
        if (!getRemoteJIDs().isEmpty()) {
            sb.append("\t\tJID: ");
            String[] ids = getRemoteJIDs().toArray(new String[getRemoteJIDs().size()]);
            for (String id : ids)
                sb.append(id + " ");
            sb.append("\n");
        }

        return sb.toString();
    }
}