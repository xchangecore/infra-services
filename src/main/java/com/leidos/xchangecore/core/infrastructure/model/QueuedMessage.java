package com.leidos.xchangecore.core.infrastructure.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.CollectionOfElements;

@Entity
public class QueuedMessage {

    @Id
    @GeneratedValue
    private Integer id;

    private String corename;

    @Column(name = "MessageSet")
    @Lob
    @CollectionOfElements(fetch = FetchType.EAGER)
    private Set<String> interestGroupSet = new HashSet<String>();

    public QueuedMessage() {

    }

    public QueuedMessage(String corename) {

        this.corename = corename;
    }

    public void addMessage(String message) {

        interestGroupSet.add(message);
    }

    public String getCorename() {

        return corename;
    }

    public Integer getId() {

        return id;
    }

    public Set<String> getMessageSet() {

        return interestGroupSet;
    }

    public void setCorename(String corename) {

        this.corename = corename;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public void setMessageSet(Set<String> interestGroupSet) {

        this.interestGroupSet = interestGroupSet;
    }
}
