package com.leidos.xchangecore.core.infrastructure.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * A log is a persisted record of a client's execution. The client will call the Logging webservice,
 * which will be persisted via hibernate and H2.
 *
 * @ssdd
 */
@Entity
// @Table(name = "LOG")
public class Log
    implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @FieldBridge(impl = DateBridge.class)
    // @Column(name = "PUBLISHED")
    // private Date published;

    @Column(name = "LOGGER")
    @Field(index = Index.TOKENIZED)
    private String logger;

    @Column(name = "TIMESTAMP")
    @Field(index = Index.TOKENIZED)
    private Date timestamp;

    @Column(name = "HOSTNAME")
    @Field(index = Index.TOKENIZED)
    private String hostname;

    @Column(name = "LOGGING_TYPE")
    @Field(index = Index.TOKENIZED)
    private String loggingType;

    @Column(name = "MESSAGE")
    @Field(index = Index.TOKENIZED)
    private String message;

    // @Column(name = "TYPE")
    // @Lob
    // private byte[] product;

    // @ManyToOne
    // Incident incident;

    /**
     * Gets the hostname.
     *
     * @return the hostname
     * @ssdd
     */
    public String getHostname() {

        return hostname;
    }

    /**
     * Returns the the primary key of the Log message.
     *
     * @return the id
     *
     * @ssdd
     */
    public Integer getId() {

        return id;
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     * @ssdd
     */
    public String getLogger() {

        return logger;
    }

    /**
     * Gets the logging type.
     *
     * @return the logging type
     * @ssdd
     */
    public String getLoggingType() {

        return loggingType;
    }

    /**
     * Gets the message.
     *
     * @return the message
     * @ssdd
     */
    public String getMessage() {

        return message;
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     * @ssdd
     */
    public Date getTimestamp() {

        return timestamp;
    }

    /**
     * Sets the hostname.
     *
     * @param hostname the new hostname
     * @ssdd
     */
    public void setHostname(String hostname) {

        this.hostname = hostname;
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
     * Sets the logger.
     *
     * @param logger the new logger
     * @ssdd
     */
    public void setLogger(String logger) {

        this.logger = logger;
    }

    /**
     * Sets the logging type.
     *
     * @param loggingType the new logging type
     * @ssdd
     */
    public void setLoggingType(String loggingType) {

        this.loggingType = loggingType;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     * @ssdd
     */
    public void setMessage(String message) {

        this.message = message;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the new timestamp
     * @ssdd
     */
    public void setTimestamp(Date timestamp) {

        this.timestamp = timestamp;
    }
}