package com.leidos.xchangecore.core.infrastructure.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The ExternalDataSourceConfig data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "EXTERNAL_DATASOURCE_CONFIG")
public class ExternalDataSourceConfig {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "URN")
    private String urn;

    @Column(name = "CORE_NAME")
    private String coreName;

    public ExternalDataSourceConfig() {

    }

    /**
     * Instantiates a new external data source config.
     *
     * @param urn the urn
     * @param coreName the core name
     * @ssdd
     */
    public ExternalDataSourceConfig(String urn, String coreName) {

        setUrn(urn);
        setCoreName(coreName);
    }

    /**
     * Gets the core name.
     *
     * @return the core name
     * @ssdd
     */
    public String getCoreName() {

        return coreName;
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
     * Gets the urn.
     *
     * @return the urn
     * @ssdd
     */
    public String getUrn() {

        return urn;
    }

    /**
     * Sets the core name.
     *
     * @param coreName the new core name
     * @ssdd
     */
    public void setCoreName(String coreName) {

        this.coreName = coreName;
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
     * Sets the urn.
     *
     * @param urn the new urn
     * @ssdd
     */
    public void setUrn(String urn) {

        this.urn = urn;
    }

}
