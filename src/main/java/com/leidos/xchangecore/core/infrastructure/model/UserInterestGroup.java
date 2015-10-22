package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The User-InterestGroup data model.
 * @ssdd
 */
@Entity
// @Table(name = "USER_INTEREST_GROUP")
public class UserInterestGroup
    implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "UIG_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "USER_JID")
    @Field(index = Index.TOKENIZED)
    private String user;

    @Column(name = "INTEREST_GROUP_ID_LIST")
    @CollectionOfElements(fetch = FetchType.EAGER)
    private final Set<String> interestGroupIDList = new HashSet<String>();

    public UserInterestGroup() {

    }

    public boolean addInterestGroup(final String interestGroupID) {

        return interestGroupIDList.add(interestGroupID);
    }

    public List<String> getInterestGroupIDList() {

        return new ArrayList<String>(interestGroupIDList);
    }

    public String getUser() {

        return user;
    }

    public boolean removeInterestGroup(final String interestGroupID) {

        return interestGroupIDList.remove(interestGroupID);
    }

    public void setUser(final String user) {

        this.user = user;
    }
}
