package com.leidos.xchangecore.core.infrastructure.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

@Entity
// @Table(name = "NAMESPACE_MAP")
public class NamespaceMap {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PREFIX")
    @Field(index = Index.TOKENIZED)
    private String prefix;

    @Column(name = "URI")
    @Field(index = Index.TOKENIZED)
    private String uri;

    public Integer getId() {

        return id;
    }

    public String getPrefix() {

        return prefix;
    }

    public String getUri() {

        return uri;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public void setPrefix(String prefix) {

        this.prefix = prefix;
    }

    public void setUri(String uri) {

        this.uri = uri;
    }

}
