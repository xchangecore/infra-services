package com.leidos.xchangecore.core.infrastructure.util;

import gov.ucore.ucore.x20.AgentEventRelationshipType;
import gov.ucore.ucore.x20.AgentRefType;
import gov.ucore.ucore.x20.CauseOfRelationshipType;
import gov.ucore.ucore.x20.CircleByCenterPointType;
import gov.ucore.ucore.x20.CollectionType;
import gov.ucore.ucore.x20.ContentMetadataType;
import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.DigestType;
import gov.ucore.ucore.x20.EntityLocationExtendedRelationshipType;
import gov.ucore.ucore.x20.EntityLocationRelationshipType;
import gov.ucore.ucore.x20.EntityRefType;
import gov.ucore.ucore.x20.EntityType;
import gov.ucore.ucore.x20.EventLocationRelationshipType;
import gov.ucore.ucore.x20.EventRefType;
import gov.ucore.ucore.x20.EventType;
import gov.ucore.ucore.x20.GeoLocationType;
import gov.ucore.ucore.x20.IdentifierType;
import gov.ucore.ucore.x20.LineStringType;
import gov.ucore.ucore.x20.LocationRefType;
import gov.ucore.ucore.x20.LocationType;
import gov.ucore.ucore.x20.OrganizationType;
import gov.ucore.ucore.x20.PointType;
import gov.ucore.ucore.x20.PolygonType;
import gov.ucore.ucore.x20.RelationshipType;
import gov.ucore.ucore.x20.SimplePropertyType;
import gov.ucore.ucore.x20.ThingRefType;
import gov.ucore.ucore.x20.ThingType;
import gov.ucore.ucore.x20.TimeInstantType;
import gov.ucore.ucore.x20.WhatType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.LinearRingType;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.usersmarts.geo.gml.GMLDomModule;
import com.usersmarts.xmf2.Configuration;
import com.usersmarts.xmf2.MarshalContext;
import com.vividsolutions.jts.geom.Geometry;

public class DigestHelper
implements InfrastructureNamespaces, DigestConstant {

    private static Configuration gmlParseCfg = new Configuration(GMLDomModule.class);

    private static Logger logger = LoggerFactory.getLogger(DigestHelper.class);

    public static boolean containWhatClause(DigestType digest, String whatClause) {

        final Set<ThingType> whats = getThingsByWhatType(digest,
            InfrastructureNamespaces.NS_UCORE_CODESPACE, whatClause);
        return whats.size() > 0;
    }

    public static CauseOfRelationshipType getCauseByEffectID(DigestType digest, String effectID) {

        final RelationshipType[] relationships = digest.getRelationshipAbstractArray();
        for (final RelationshipType relationship : relationships) {
            if (relationship instanceof CauseOfRelationshipType) {
                final CauseOfRelationshipType causeOf = (CauseOfRelationshipType) relationship;
                if (causeOf.getEffect().getRef().size() > 0) {
                    final String effectValue = (String) causeOf.getEffect().getRef().get(0);
                    if (effectValue.equals(effectID)) {
                        return causeOf;
                    }
                }
            }
        }
        return null;
    }

    public static EventType getFirstEventWithActivityNameIdentifier(DigestType digest) {

        final ThingType[] things = digest.getThingAbstractArray();
        for (final ThingType thing : things) {
            if (thing instanceof EventType) {
                final XmlObject[] ids = thing.selectChildren(
                    IdentifierType.type.getName().getNamespaceURI(), "Identifier");
                if (ids.length != 0) {
                    for (final XmlObject object : ids) {
                        if (((IdentifierType) object).getCode().equals("ActivityName")) {
                            return (EventType) thing;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Geometry getFirstGeometry(DigestType digest) {

        for (final ThingType thing : digest.getThingAbstractArray()) {
            if (thing instanceof LocationType) {
                final LocationType location = (LocationType) thing;
                for (final GeoLocationType geo : location.getGeoLocationArray()) {
                    final Geometry geometry = getGeometry(geo);
                    if (geometry != null) {
                        return geometry;
                    }
                }
            }
        }
        return null;
    }

    public static Geometry getGeometry(GeoLocationType geo) {

        Geometry result = null;
        final XmlObject abstr = geo.getGeoLocationAbstract();
        if (abstr instanceof PolygonType) {
            final Node node = ((PolygonType) abstr).getPolygon().getDomNode();
            result = getGeometry(node);
        } else if (abstr instanceof PointType) {
            final Node node = ((PointType) abstr).getPoint().getDomNode();
            result = getGeometry(node);
        } else if (abstr instanceof CircleByCenterPointType) {
            final PointType point = PointType.Factory.newInstance();
            point.addNewPoint().setPos(
                ((CircleByCenterPointType) abstr).getCircleByCenterPoint().getPos());
            final Node node = point.getPoint().getDomNode();
            result = getGeometry(node);
        }
        return result;
    }

    public static Geometry getGeometry(Node node) {

        final MarshalContext ctx = new MarshalContext(gmlParseCfg);
        final Geometry result = (Geometry) ctx.marshal(node);
        return result;
    }

    public static Geometry getGeometryFromLocationByID(DigestType digest, String id) {

        final XmlObject[] elements = digest.selectChildren(NS_UCORE, "Location");
        if ((elements != null) && (elements.length > 0)) {
            for (final XmlObject element : elements) {
                if (element instanceof LocationType) {
                    final LocationType location = (LocationType) element;
                    if (location.getId().equals(id)) {
                        for (final GeoLocationType geo : location.getGeoLocationArray()) {
                            final Geometry geometry = getGeometry(geo);
                            if (geometry != null) {
                                return geometry;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<LocationType> getLocationElements(DigestType digest) {

        final ArrayList<LocationType> list = new ArrayList<LocationType>();
        final XmlObject[] locations = digest.selectChildren(
            gov.ucore.ucore.x20.LocationType.type.getName().getNamespaceURI(), "Location");
        if (locations.length > 0) {
            for (final XmlObject object : locations) {
                final LocationType location = (LocationType) object;
                list.add(location);
            }
        }
        return list;
    }

    public static EventLocationRelationshipType getLocationRelationshipByTypeAndEventID(DigestType digest,
                                                                                        String type,
                                                                                        ThingRefType eventID) {

        final XmlObject[] relationships = digest.selectChildren(
            EventLocationRelationshipType.type.getName().getNamespaceURI(), type);
        for (final XmlObject relationship : relationships) {
            final EventLocationRelationshipType elRelationship = (EventLocationRelationshipType) relationship;
            if (elRelationship.getEventRef().getRef().get(0).equals(eventID.getRef().get(0))) {
                return elRelationship;
            }
        }
        return null;
    }

    public static SimplePropertyType getSimplePropertyFromThing(ThingType thing,
                                                                String codespace,
                                                                String code,
                                                                String label,
                                                                String value) {

        if (thing == null) {
            return null;
        }

        SimplePropertyType result = null;
        final XmlObject[] props = thing.selectChildren(
            SimplePropertyType.type.getName().getNamespaceURI(), "SimpleProperty");
        for (final XmlObject prop : props) {
            final SimplePropertyType property = (SimplePropertyType) prop;
            if (simplePropertyMatches(property, codespace, code, label, value)) {
                result = property;
                break;
            }
        }
        return result;
    }

    public static Set<ThingType> getThingsByWhatType(DigestType digest,
        String codespace,
        String code) {

        final ThingType[] things = digest.getThingAbstractArray();
        final Set<ThingType> results = new HashSet<ThingType>();
        for (final ThingType thing : things) {
            if ((thing instanceof EventType) &&
                objectHasWhatType(codespace, code, null, null, thing)) {
                results.add(thing);
            }
        }
        return results;
    }

    public static String getUCoreWhatType(DigestType digest) {

        return "Event";
    }

    private static boolean isMatched(String regexp, String content) {

        final boolean isNegative = regexp.startsWith("!");
        if (isNegative) {
            regexp = regexp.substring(1);
        }

        String re = regexp.replaceAll("\\*", ".\\*");
        re = "(?i:" + re + ")";
        final boolean isMatched = content.matches(re);
        return isNegative ? !isMatched : isMatched;
    }

    public static boolean objectHasWhatType(String codespace,
                                            String code,
                                            String label,
                                            String value,
                                            XmlObject event) {

        final XmlObject[] props = event.selectChildren(WhatType.type.getName().getNamespaceURI(),
            "What");

        for (final XmlObject prop : props) {

            XmlObject xmlObject = null;
            String stringValue = null;
            if (codespace != null) {
                // Find the SimpleProperty with the correct codespace (there may be more than one)
                if ((xmlObject = prop.selectAttribute(WhatType.type.getName().getNamespaceURI(),
                    "codespace")) == null) {
                    continue;
                }
                stringValue = ((SimpleValue) xmlObject).getStringValue();
                if ((stringValue != null) && (stringValue.equalsIgnoreCase(codespace) == false)) {
                    continue;
                }
            }

            if (code != null) {
                if ((xmlObject = prop.selectAttribute(WhatType.type.getName().getNamespaceURI(),
                    "code")) == null) {
                    continue;
                }
                stringValue = ((SimpleValue) xmlObject).getStringValue();
                if (isMatched(code, stringValue) == false) {
                    continue;
                }
            }

            if (label != null) {
                if ((xmlObject = prop.selectAttribute(WhatType.type.getName().getNamespaceURI(),
                    "label")) == null) {
                    continue;
                }

                stringValue = ((SimpleValue) xmlObject).getStringValue();
                if ((stringValue != null) && (stringValue.equalsIgnoreCase(label) == false)) {
                    continue;
                }
            }

            if (value != null) {
                stringValue = ((SimpleValue) xmlObject).getStringValue();
                if ((stringValue != null) && (stringValue.equalsIgnoreCase(value) == false)) {
                    continue;
                }
            }
            return true;
        }

        return false;
    }

    protected static boolean simplePropertyMatches(SimplePropertyType property,
                                                   String codespace,
                                                   String code,
                                                   String label,
                                                   String value) {

        // must have at least a label
        if (label == null) {
            return false;
        }

        if (property.getLabel().getStringValue().equals(label)) {
            boolean codespaceOK = false;
            boolean codeOK = false;
            boolean valueOK = false;
            if (codespace != null) {
                if (property.getCodespace().equals(codespace)) {
                    codespaceOK = true;
                }
            } else {
                codespaceOK = true;
            }
            if (code != null) {
                if (property.getCode().equals(code)) {
                    codeOK = true;
                }
            } else {
                codeOK = true;
            }
            if (value != null) {
                if (property.getStringValue().equals(value)) {
                    valueOK = true;
                }
            } else {
                valueOK = true;
            }
            return codespaceOK && codeOK && valueOK;
        }
        return false;
    }

    protected DigestDocument digest;

    public DigestHelper() {

        super();
        digest = DigestDocument.Factory.newInstance();
        digest.addNewDigest();
    }

    protected void addCircleToLocation(LocationType location,
                                       net.opengis.gml.x32.CircleByCenterPointType circle) {

        circle.getPos().setSrsName(GeoUtil.EPSG4326);
        final CircleByCenterPointType uCircle = CircleByCenterPointType.Factory.newInstance();
        uCircle.addNewCircleByCenterPoint().set(circle);
        XmlUtil.substitute(location.addNewGeoLocation().addNewGeoLocationAbstract(), NS_UCORE,
            S_CircleByCenterPoint, CircleByCenterPointType.type, uCircle);
    }

    public void addLineStringToLocation(LocationType location,
                                        net.opengis.gml.x32.LineStringType line) {

        line.setSrsName(GeoUtil.EPSG4326);
        final LineStringType uLine = LineStringType.Factory.newInstance();
        uLine.addNewLineString().set(line);
        XmlUtil.substitute(location.addNewGeoLocation().addNewGeoLocationAbstract(), NS_UCORE,
            "LineString", LineStringType.type, uLine);
    }

    public void addPointToLocation(LocationType location, net.opengis.gml.x32.PointType point) {

        final PointType uPoint = PointType.Factory.newInstance();
        uPoint.addNewPoint().set(point);
        XmlUtil.substitute(location.addNewGeoLocation().addNewGeoLocationAbstract(), NS_UCORE,
            S_Point, PointType.type, uPoint);
    }

    public void addPolygonToLocation(LocationType location, net.opengis.gml.x32.PolygonType polygon) {

        polygon.setSrsName(GeoUtil.EPSG4326);
        if (polygon.getExterior().getAbstractRing() instanceof LinearRingType) {
            final LinearRingType ring = (LinearRingType) polygon.getExterior().getAbstractRing();
            for (final DirectPositionType pos : ring.getPosArray()) {
                pos.setSrsName(GeoUtil.EPSG4326);
            }
        }
        final PolygonType uPolygon = PolygonType.Factory.newInstance();
        uPolygon.addNewPolygon().set(polygon);
        XmlUtil.substitute(location.addNewGeoLocation().addNewGeoLocationAbstract(), NS_UCORE,
            S_Polygon, PolygonType.type, uPolygon);
    }

    public void addSimplePropertyToThing(ThingType thing,
                                         String codespace,
                                         String code,
                                         String label,
                                         String value) {

        final SimplePropertyType property = SimplePropertyType.Factory.newInstance();
        if (codespace != null) {
            property.setCodespace(codespace);
        }
        if (code != null) {
            property.setCode(code);
        }
        if (label != null) {
            property.addNewLabel().setStringValue(label);
        }
        if (value != null) {
            property.setStringValue(value);
        }
        thing.addNewSimpleProperty().set(property);
    }

    public byte[] getBytes() {

        return digest.isNil() ? null : digest.toString().getBytes();
    }

    public DigestDocument getDigest() {

        return digest;
    }

    public EventType getEvent(String eventId) {

        final ThingType[] things = digest.getDigest().getThingAbstractArray();
        for (final ThingType thing : things) {
            if (thing.getId().equalsIgnoreCase(eventId.trim()) && (thing instanceof EventType)) {
                return (EventType) thing;
            }
        }
        return null;
    }

    public boolean isNil() {

        return digest.isNil();
    }

    public void setCauseOf(String causeId, String effectId) {

        final CauseOfRelationshipType causeOf = CauseOfRelationshipType.Factory.newInstance();
        causeOf.setId(UUIDUtil.getID("CauseOf"));

        // Set the cause
        final ThingRefType thingRef = ThingRefType.Factory.newInstance();
        final ArrayList<String> theList = new ArrayList<String>();
        theList.add(causeId);
        thingRef.setRef(theList);
        causeOf.setCause(thingRef);

        // Set the effect
        final EventRefType effectRef = EventRefType.Factory.newInstance();
        theList.clear();
        theList.add(effectId);
        effectRef.setRef(theList);
        causeOf.setEffect(effectRef);

        XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(), NS_UCORE, S_CauseOf,
            CauseOfRelationshipType.type, causeOf);
    }

    public void setCircle(LocationType location, net.opengis.gml.x32.CircleByCenterPointType circle) {

        addCircleToLocation(location, circle);
        setLocation(location);
    }

    public void setEntity(EntityType entity) {

        // add an Entity
        XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE, S_Entity,
            EntityType.type, entity);
    }

    public void setEvent(EventType event) {

        // add an Event
        XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE, S_Event,
            EventType.type, event);
    }

    public void setEvent(String eventId,
                         String descriptor,
                         String identifier,
                         String[] codespace,
                         ContentMetadataType metadata,
                         SimplePropertyType property) {

        final EventType event = EventType.Factory.newInstance();
        event.setId(eventId);

        // set the Identifier for the Event
        if (identifier != null) {
            final IdentifierType id = event.addNewIdentifier();
            id.setStringValue(identifier);
            if (codespace.length == 2) {
                id.setCodespace(codespace[0]);
                id.setCode(codespace[1]);
                id.addNewLabel().setStringValue("ID");
            } else if (codespace.length == 3) {
                id.setCodespace(codespace[0]);
                id.setCode(codespace[1]);
                id.addNewLabel().setStringValue(codespace[2]);
            } else {
                id.addNewLabel().setStringValue("label");
            }
        }

        if (descriptor != null) {
            event.addNewDescriptor().setStringValue(descriptor);
        }
        if (metadata != null) {
            event.setMetadata(metadata);
        }
        if (property != null) {
            event.addNewSimpleProperty().set(property);
        }

        this.setEvent(event);
    }

    // The HasDestinationOf Relationship is used to associate an Event with a
    // destination
    // (i.e. RequestResource event with where the resource is requested to go
    // to)
    public void setHasDestinationOf(String eventId, String locationId, Calendar cal) {

        final EntityLocationRelationshipType hasDestinationOf = EntityLocationRelationshipType.Factory.newInstance();
        hasDestinationOf.setId(UUIDUtil.getID(S_HasDestionationOf));

        // add a time instant
        final TimeInstantType time = TimeInstantType.Factory.newInstance();
        time.setValue(cal);

        XmlUtil.substitute(hasDestinationOf.addNewTime().addNewTimeAbstract(), NS_UCORE,
            S_TimeInstant, TimeInstantType.type, time);

        // set the event reference
        final EntityRefType eventRef = EntityRefType.Factory.newInstance();
        final ArrayList<String> theList = new ArrayList<String>();
        theList.add(eventId);
        eventRef.setRef(theList);
        hasDestinationOf.setEntityRef(eventRef);

        // set the location reference
        final LocationRefType locationRef = LocationRefType.Factory.newInstance();
        theList.clear();
        theList.add(locationId);
        locationRef.setRef(theList);
        hasDestinationOf.setLocationRef(locationRef);

        // System.out.println(hasDestinationOf);
        XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(), NS_UCORE,
            S_HasDestionationOf, EntityLocationRelationshipType.type, hasDestinationOf);
    }

    // The InvolvedIn relationship is used to associate an Agent with an Event
    public void setInvolvedIn(String agentId, String eventId, Calendar cal) {

        final AgentEventRelationshipType involvedIn = AgentEventRelationshipType.Factory.newInstance();
        involvedIn.setId(UUIDUtil.getID(S_InvolvedIn));

        // add a time instant
        final TimeInstantType time = TimeInstantType.Factory.newInstance();
        time.setValue(cal);
        XmlUtil.substitute(involvedIn.addNewTime().addNewTimeAbstract(), NS_UCORE, S_TimeInstant,
            TimeInstantType.type, time);

        // set the event reference
        final EventRefType eventRef = EventRefType.Factory.newInstance();
        final ArrayList<String> theList = new ArrayList<String>();
        theList.add(eventId);
        eventRef.setRef(theList);
        involvedIn.setEventRef(eventRef);

        // set the location reference
        final AgentRefType locationRef = AgentRefType.Factory.newInstance();
        theList.clear();
        theList.add(agentId);
        locationRef.setRef(theList);
        involvedIn.setAgentRef(locationRef);

        // System.out.println(hasDestinationOf);
        XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(), NS_UCORE, S_InvolvedIn,
            AgentEventRelationshipType.type, involvedIn);
    }

    public void setLineString(LocationType location, net.opengis.gml.x32.LineStringType line) {

        addLineStringToLocation(location, line);
        setLocation(location);
    }

    // The LocatedAt Relationship is used to associate an Entity with a time and
    // a place.
    public void setLocatedAt(String entityId, String locationId, Calendar cal) {

        final EntityLocationExtendedRelationshipType locatedAt = EntityLocationExtendedRelationshipType.Factory.newInstance();
        locatedAt.setId(UUIDUtil.getID(S_LocatedAt));

        if (cal != null) {
            // TRAC #272
            // UCore's TimeInstant is a union of several date, time, and
            // date-time types
            // The value is converted into a Java Calendar object which, when
            // passed to xmlbeans
            // uses the first match in the union (generally date or date+offset)
            // To overcome this,
            // the following code constructs a valid iso 8601 (xs:datTime)
            // string and then
            // invokes TimeInstantType's parse method to construct the Value
            // element.
            //
            // The following code normalizes the date and time such that a
            // complete
            // datetime object is constructed including a locale offset
            // (timezone).
            // This ensures meaningful sorting and searching within UICDS.

            // normalized iso 8601 datetime string (with offset)
            final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

            // use the current time and offset to fill in any missing blanks.
            final Calendar now = Calendar.getInstance();

            TimeInstantType time = TimeInstantType.Factory.newInstance();
            try {
                // check yyyy
                if (!cal.isSet(Calendar.YEAR)) {
                    cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                }

                // check MM
                if (!cal.isSet(Calendar.MONTH)) {
                    cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                }

                // check dd
                if (!cal.isSet(Calendar.DAY_OF_MONTH)) {
                    cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                }

                // check HH
                if (!cal.isSet(Calendar.HOUR_OF_DAY)) {
                    cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                }

                // check mm
                if (!cal.isSet(Calendar.MINUTE)) {
                    cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                }

                // check ss
                if (!cal.isSet(Calendar.SECOND)) {
                    cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
                }

                // check timezone
                if (cal.getTimeZone() == null) {
                    cal.setTimeZone(now.getTimeZone());
                }

                final StringBuffer buf = new StringBuffer(iso8601Format.format(cal.getTime()));
                buf.insert(buf.length() - 2, ':');
                time = TimeInstantType.Factory.parse(
                    "<Value xmlns=\"http://ucore.gov/ucore/2.0\">" + buf.toString() + "</Value>",
                    null);
            } catch (final Exception e) {
                time.setValue(now);
            }

            XmlUtil.substitute(locatedAt.addNewTime().addNewTimeAbstract(), NS_UCORE,
                S_TimeInstant, TimeInstantType.type, time);
        }

        final EntityRefType entityRef = EntityRefType.Factory.newInstance();
        final ArrayList<String> theList = new ArrayList<String>();
        theList.add(entityId);
        entityRef.setRef(theList);
        locatedAt.setEntityRef(entityRef);

        final LocationRefType locationRef = LocationRefType.Factory.newInstance();
        theList.clear();
        theList.add(locationId);
        locationRef.setRef(theList);
        locatedAt.setLocationRef(locationRef);

        XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(), NS_UCORE, S_LocatedAt,
            EntityLocationExtendedRelationshipType.type, locatedAt);
    }

    public void setLocation(LocationType location) {

        XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE, S_Location,
            LocationType.type, location);
    }

    // The OccursAt Relationship is used to associate an Event (like the forest
    // fire) with a time
    // and place.
    public void setOccursAt(String eventId, String locationId, Calendar cal) {

        final EventLocationRelationshipType occursAt = EventLocationRelationshipType.Factory.newInstance();
        occursAt.setId(UUIDUtil.getID(S_OccursAt));

        if (cal != null) {
            // TRAC #272
            // UCore's TimeInstant is a union of several date, time, and
            // date-time types
            // The value is converted into a Java Calendar object which, when
            // passed to xmlbeans
            // uses the first match in the union (generally date or date+offset)
            // To overcome this,
            // the following code constructs a valid iso 8601 (xs:datTime)
            // string and then
            // invokes TimeInstantType's parse method to construct the Value
            // element.
            //
            // The following code normalizes the date and time such that a
            // complete
            // datetime object is constructed including a locale offset
            // (timezone).
            // This ensures meaningful sorting and searching within UICDS.

            // normalized iso 8601 datetime string (with offset)
            final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

            // use the current time and offset to fill in any missing blanks.
            final Calendar now = Calendar.getInstance();

            TimeInstantType time = TimeInstantType.Factory.newInstance();
            try {
                // check yyyy
                if (!cal.isSet(Calendar.YEAR)) {
                    cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                }

                // check MM
                if (!cal.isSet(Calendar.MONTH)) {
                    cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                }

                // check dd
                if (!cal.isSet(Calendar.DAY_OF_MONTH)) {
                    cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                }

                // check HH
                if (!cal.isSet(Calendar.HOUR_OF_DAY)) {
                    cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
                }

                // check mm
                if (!cal.isSet(Calendar.MINUTE)) {
                    cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
                }

                // check ss
                if (!cal.isSet(Calendar.SECOND)) {
                    cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
                }

                // check timezone
                if (cal.getTimeZone() == null) {
                    cal.setTimeZone(now.getTimeZone());
                }

                final StringBuffer buf = new StringBuffer(iso8601Format.format(cal.getTime()));
                buf.insert(buf.length() - 2, ':');
                time = TimeInstantType.Factory.parse(
                    "<Value xmlns=\"http://ucore.gov/ucore/2.0\">" + buf.toString() + "</Value>",
                    null);
            } catch (final Exception e) {
                time.setValue(now);
            }

            XmlUtil.substitute(occursAt.addNewTime().addNewTimeAbstract(), NS_UCORE, S_TimeInstant,
                TimeInstantType.type, time);
        }

        // set the event reference
        final EventRefType eventRef = EventRefType.Factory.newInstance();
        final ArrayList<String> theList = new ArrayList<String>();
        theList.add(eventId);
        eventRef.setRef(theList);
        occursAt.setEventRef(eventRef);

        // set the location reference
        final LocationRefType locationRef = LocationRefType.Factory.newInstance();
        theList.clear();
        theList.add(locationId);
        locationRef.setRef(theList);
        occursAt.setLocationRef(locationRef);

        XmlUtil.substitute(digest.getDigest().addNewRelationshipAbstract(), NS_UCORE, S_OccursAt,
            EventLocationRelationshipType.type, occursAt);
    }

    public void setOrganization(OrganizationType org) {

        XmlUtil.substitute(digest.getDigest().addNewThingAbstract(), NS_UCORE, S_Organization,
            OrganizationType.type, org);
    }

    public void setPoint(LocationType location, net.opengis.gml.x32.PointType point) {

        addPointToLocation(location, point);
        setLocation(location);
    }

    public void setPolygon(LocationType location, net.opengis.gml.x32.PolygonType polygon) {

        addPolygonToLocation(location, polygon);
        setLocation(location);
    }

    public void setWhatForEvent(WhatType theWhat, String eventId) {

        final ThingType[] things = digest.getDigest().getThingAbstractArray();
        for (final ThingType thing : things) {
            if (thing.getId().equals(eventId)) {

                WhatType[] whats = null;
                if (thing instanceof EventType) {
                    whats = ((EventType) thing).getWhatArray();
                } else if (thing instanceof EntityType) {
                    whats = ((EntityType) thing).getWhatArray();
                } else if (thing instanceof CollectionType) {
                    whats = ((CollectionType) thing).getWhatArray();
                }
                boolean found = false;
                for (final WhatType what : whats) {
                    if (what.equals(theWhat)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (thing instanceof EventType) {
                        ((EventType) thing).addNewWhat().set(theWhat);
                    } else if (thing instanceof EntityType) {
                        ((EntityType) thing).addNewWhat().set(theWhat);
                    } else if (thing instanceof CollectionType) {
                        ((CollectionType) thing).addNewWhat().set(theWhat);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {

        return digest.isNil() ? null : digest.toString();
    }
}
