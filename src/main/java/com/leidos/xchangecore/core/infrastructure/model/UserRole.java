package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The UserRole data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "USER_ROLES")
public class UserRole
    implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "USER_PROFILE_ID")
    @Field(index = Index.TOKENIZED)
    private String userRefId;

    @Column(name = "ROLE_PROFILE_ID")
    @Field(index = Index.TOKENIZED)
    private String roleRefId;

    public UserRole() {

    }

    /**
     * Instantiates a new user role.
     *
     * @param userRefID the user ref id
     * @param roleRefID the role ref id
     * @ssdd
     */
    public UserRole(String userRefID, String roleRefID) {

        setUserRefId(userRefID);
        setRoleRefId(roleRefID);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserRole other = (UserRole) obj;
        if (roleRefId == null) {
            if (other.roleRefId != null) {
                return false;
            }
        } else if (!roleRefId.equals(other.roleRefId)) {
            return false;
        }
        if (userRefId == null) {
            if (other.userRefId != null) {
                return false;
            }
        } else if (!userRefId.equals(other.userRefId)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the role ref id.
     *
     * @return the role ref id
     * @ssdd
     */
    public String getRoleRefId() {

        return roleRefId;
    }

    /**
     * Gets the user ref id.
     *
     * @return the user ref id
     * @ssdd
     */
    public String getUserRefId() {

        return userRefId;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((roleRefId == null) ? 0 : roleRefId.hashCode());
        result = (prime * result) + ((userRefId == null) ? 0 : userRefId.hashCode());
        return result;
    }

    /**
     * Sets the role ref id.
     *
     * @param roleRefId the new role ref id
     * @ssdd
     */
    public void setRoleRefId(String roleRefId) {

        this.roleRefId = roleRefId;
    }

    /**
     * Sets the user ref id.
     *
     * @param userRefId the new user ref id
     * @ssdd
     */
    public void setUserRefId(String userRefId) {

        this.userRefId = userRefId;
    }

}
