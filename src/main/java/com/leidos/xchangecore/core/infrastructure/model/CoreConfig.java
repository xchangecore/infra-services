package com.leidos.xchangecore.core.infrastructure.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * The CoreConfig data model.
 *
 * @ssdd
 */
@Entity
// @Table(name = "CORE_CONFIG")
public class CoreConfig {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "URL")
    private String url;

    @Column(name = "LOCAL_CORE")
    private boolean localCore;

    @Column(name = "ONLINE_STATUS")
    private String onlineStatus;

    @Column(name = "LATITUDE")
    private String latitude;

    @Column(name = "LONGITUDE")
    private String longitude;

    public CoreConfig() {

    }

    /**
     * Instantiates a new core config.
     *
     * @param name the name
     * @param url the url
     * @param onlineStatus the online status
     * @ssdd
     */
    public CoreConfig(String name, String url, String onlineStatus) {

        this.name = name;
        this.url = url;
        this.onlineStatus = onlineStatus;
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

    public String getLatitude() {

        return latitude;
    }

    /**
     * Gets the local core.
     *
     * @return the local core
     * @ssdd
     */
    public boolean getLocalCore() {

        return localCore;
    }

    public String getLongitude() {

        return longitude;
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
     * Gets the online status.
     *
     * @return the online status
     * @ssdd
     */
    public String getOnlineStatus() {

        return onlineStatus;
    }

    /**
     * Gets the url.
     *
     * @return the url
     * @ssdd
     */
    public String getUrl() {

        return url;
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

    public void setLatitude(String latitude) {

        this.latitude = latitude;
    }

    /**
     * Sets the local core.
     *
     * @param localCore the new local core
     * @ssdd
     */
    public void setLocalCore(boolean localCore) {

        this.localCore = localCore;
    }

    public void setLongitude(String longitude) {

        this.longitude = longitude;
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
     * Sets the online status.
     *
     * @param onlineStatus the new online status
     * @ssdd
     */
    public void setOnlineStatus(String onlineStatus) {

        this.onlineStatus = onlineStatus;
    }

    /**
     * Sets the url.
     *
     * @param url the new url
     * @ssdd
     */
    public void setUrl(String url) {

        this.url = url;
    }

}
