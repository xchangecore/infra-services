package com.leidos.xchangecore.core.infrastructure.model;

import gov.ucore.ucore.x20.CyberAddressType;
import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.DigestType;
import gov.ucore.ucore.x20.EntityLocationRelationshipType;
import gov.ucore.ucore.x20.EntityType;
import gov.ucore.ucore.x20.EventType;
import gov.ucore.ucore.x20.GeoLocationType;
import gov.ucore.ucore.x20.LocationType;
import gov.ucore.ucore.x20.PhysicalAddressType;
import gov.ucore.ucore.x20.RelationshipType;
import gov.ucore.ucore.x20.ThingType;
import gov.ucore.ucore.x20.WhatType;
import mil.dod.metadata.mdr.ns.ddms.x20.VirtualCoverageType;
import mil.dod.metadata.mdr.ns.ddms.x20.PostalAddressDocument.PostalAddress;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.leidos.xchangecore.core.infrastructure.util.DigestHelper;

/**
 * The DigestBridge data model. Implements a Hibernate bridge that is specific to the UCore Digest
 * elements.
 * 
 * @ssdd
 */
public class DigestBridge
    implements FieldBridge {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Instantiates a new digest bridge.
     */
    public DigestBridge() {

        logger.debug("Creating digest bridge");
    }

    /**
     * Adds the cyber address.
     * 
     * @param cyber the cyber
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addCyberAddress(CyberAddressType cyber, Document doc, LuceneOptions opts) {

        VirtualCoverageType vc = cyber.getVirtualCoverage();
        addField("cyber.protocol", vc.getProtocol(), doc, opts);
        addField("cyber.address", vc.getAddress(), doc, opts);
    }

    /**
     * Adds the event.
     * 
     * @param event the event
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addEvent(EventType event, Document doc, LuceneOptions opts) {

        if (event.getDescriptor() != null) {
            String descriptor = event.getDescriptor().getStringValue();
            addField("digest.event.descriptor", descriptor, doc, opts);
        }
        for (WhatType what : event.getWhatArray()) {
            addWhat(what, doc, opts);
        }
    }

    /**
     * Adds the entity.
     * 
     * @param entity the entity
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addEntity(EntityType entity, Document doc, LuceneOptions opts) {

        if (entity.getDescriptor() != null) {
            String descriptor = entity.getDescriptor().getStringValue();
            addField("digest.entity.descriptor", descriptor, doc, opts);
        }
        for (WhatType what : entity.getWhatArray()) {
            addWhat(what, doc, opts);
        }
    }

    /**
     * Adds the field.
     * 
     * @param name the name
     * @param value the value
     * @param document the document
     * @param options the options
     * @ssdd
     */
    protected void addField(String name, String value, Document document, LuceneOptions options) {

        Field field = new Field(name,
                                value,
                                options.getStore(),
                                options.getIndex(),
                                options.getTermVector());
        document.add(field);
        logger.debug("Adding field: name='" + name + "' value='" + value + "'");
    }

    /**
     * Adds the geo location.
     * 
     * @param geo the geo
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addGeoLocation(GeoLocationType geo, Document doc, LuceneOptions opts) {

        Geometry geometry = DigestHelper.getGeometry(geo);
        if (geometry != null) {
            Envelope bbox = geometry.getEnvelopeInternal();
            addField("where.minx", "" + bbox.getMinX(), doc, opts);
            addField("where.maxx", "" + bbox.getMaxX(), doc, opts);
            addField("where.miny", "" + bbox.getMinY(), doc, opts);
            addField("where.maxy", "" + bbox.getMaxY(), doc, opts);
        }
        // need to parse out gml and compute bounding box
    }

    /**
     * Adds the location.
     * 
     * @param location the location
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addLocation(LocationType location, Document doc, LuceneOptions opts) {

        for (GeoLocationType geo : location.getGeoLocationArray()) {
            addGeoLocation(geo, doc, opts);
        }
        for (PhysicalAddressType physical : location.getPhysicalAddressArray()) {
            addPhysicalAddress(physical, doc, opts);
        }
        for (CyberAddressType cyber : location.getCyberAddressArray()) {
            addCyberAddress(cyber, doc, opts);
        }
    }

    /**
     * Adds the location relationship.
     * 
     * @param where the where
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addLocationRelationship(EntityLocationRelationshipType where,
                                           Document doc,
                                           LuceneOptions opts) {

        // not supported at the moment
        // this is where a location and a time are associated with the ThingType
    }

    /**
     * Adds the physical address.
     * 
     * @param physical the physical
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addPhysicalAddress(PhysicalAddressType physical, Document doc, LuceneOptions opts) {

        PostalAddress postal = physical.getPostalAddress();
        addField("where.city", postal.getCity(), doc, opts);
        addField("where.postalCode", postal.getPostalCode(), doc, opts);
        addField("where.province", postal.getProvince(), doc, opts);
        addField("where.state", postal.getState(), doc, opts);
        for (String street : postal.getStreetArray()) {
            addField("where.street", street, doc, opts);
        }
        addField("where.coundryCode", postal.getCountryCode().getValue(), doc, opts);
    }

    /**
     * Adds the relationships.
     * 
     * @param relationships the relationships
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addRelationships(RelationshipType[] relationships,
                                    Document doc,
                                    LuceneOptions opts) {

        for (RelationshipType relationship : relationships) {
            if (relationship instanceof EntityLocationRelationshipType) {
                addLocationRelationship((EntityLocationRelationshipType) relationship, doc, opts);
            }
        }
    }

    /**
     * Adds the thing types.
     * 
     * @param types the types
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addThingTypes(ThingType[] types, Document doc, LuceneOptions opts) {

        for (ThingType type : types) {
            String nodeName = type.getDomNode().getNodeName();
            if (type instanceof EventType) {
                addEvent((EventType) type, doc, opts);
            } else if (type instanceof LocationType) {
                addLocation((LocationType) type, doc, opts);
            } else if (type instanceof EntityType) {
                addEntity((EntityType) type, doc, opts);
            } else {
                logger.info("Unknown thing type: " + nodeName);
            }
        }
    }

    /**
     * Adds the what.
     * 
     * @param what the what
     * @param doc the doc
     * @param opts the opts
     * @ssdd
     */
    protected void addWhat(WhatType what, Document doc, LuceneOptions opts) {

        addField("digest.event.what", what.getCodespace() + what.getCode(), doc, opts);
    }

    @Override
    public void set(String name, Object value, Document doc, LuceneOptions opts) {

        WorkProduct workProduct = (WorkProduct) value;
        DigestDocument digestDoc = workProduct.getDigest();
        if (digestDoc != null) {
            DigestType digest = digestDoc.getDigest();
            addThingTypes(digest.getThingAbstractArray(), doc, opts);
            // addRelationships(digest.getRelationshipAbstractArray(), doc, opts);
        }
    }

}
