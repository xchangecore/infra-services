package com.leidos.xchangecore.core.infrastructure.util;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.opengis.gml.x32.CircleByCenterPointType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.usersmarts.jts.GeoGeometryFactory;
import com.usersmarts.util.Coerce;
import com.usersmarts.util.DOMUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

public class GeoUtil {

    static Logger log = LoggerFactory.getLogger(GeoUtil.class);

    private static GeoGeometryFactory geometryFactory = new GeoGeometryFactory();

    public static final String EPSG4326 = "EPSG:4326";

    public static CircleByCenterPointType getCircle(String srsName, String circleString) {

        CircleByCenterPointType gCircle = CircleByCenterPointType.Factory.newInstance();
        gCircle.setNumArc(new BigInteger("1"));

        // separate the point and the radius
        String[] tokens = circleString.split(" ");

        // separate the lat and lon
        String[] latLon = tokens[0].split(",");
        if (latLon.length != 2) {
            return null;
        }

        List<String> pointList = new ArrayList<String>();
        pointList.add(latLon[0]);
        pointList.add(latLon[1]);
        gCircle.addNewPos().setListValue(pointList);
        gCircle.getPos().setSrsName(srsName);

        // set the radius
        if (tokens.length == 2) {
            gCircle.addNewRadius().setStringValue(tokens[1]);
        } else {
            gCircle.addNewRadius().setStringValue("0.0");
        }
        if (gCircle.getRadius() != null) {
            // Default to miles
            gCircle.getRadius().setUom("SMI");
        }
        return gCircle;
    }

    public static PointType getPoint(String srsName, String latLon) {

        PointType point = PointType.Factory.newInstance();
        point.setId(UUIDUtil.getID(DigestConstant.S_Point));
        if (srsName != null) {
            point.setSrsName(srsName);
        }
        point.getPos().setStringValue(latLon);
        return point;
    }

    // convert the array of polygon's points into array of Polygons
    // so far, i only returned the first one
    // TODO - return all the polygons ???
    public static PolygonType getPolygon(String srsName, String polygonString) {

        PolygonType gPolygon = PolygonType.Factory.newInstance();
        gPolygon.setId(UUIDUtil.getID(DigestConstant.S_Polygon));
        if (srsName != null) {
            gPolygon.setSrsName(srsName);
        }

        String[] coords = polygonString.split(" ");
        if (coords != null && coords.length >= 3) {
            LinearRingType linearRing = getLinearRingFromPolygonCoordinateString(coords);
            if (linearRing != null) {
                XmlUtil.substitute(gPolygon.addNewExterior().addNewAbstractRing(),
                    InfrastructureNamespaces.NS_GML,
                    DigestConstant.S_LinearRing,
                    LinearRingType.type,
                    linearRing);
                // If there were not at least 3 points in the polygon then return null because it
                // is not a well formed polygon
            } else {
                gPolygon = null;
            }
        } else {
            gPolygon = null;
        }
        return gPolygon;
    }

    private static LinearRingType getLinearRingFromPolygonCoordinateString(String[] coords) {

        List<Coordinate> coordList = new ArrayList<Coordinate>();

        for (String point : coords) {
            String[] points = point.split(",");
            if (points.length == 2) {
                Coordinate coord = new Coordinate(Double.parseDouble(points[0]),
                                                  Double.parseDouble(points[1]));
                coordList.add(coord);
            }

        }

        // check if the polygon is valid and closed
        Coordinate[] coordinates = new Coordinate[coordList.size()];
        coordList.toArray(coordinates);
        LinearRing lr = null;
        try {
            lr = geometryFactory.createLinearRing(coordinates);
        } catch (IllegalArgumentException e) {
            log.error("Illegal polygon: " + Arrays.toString(coords));
            return null;
        }

        if (!lr.isValid()) {
            return null;
        }

        if (!lr.isClosed()) {
            Coordinate coord = lr.getCoordinateN(0);
            coordList.add(coord);
        }

        return getLinearRingFromCoordinateArray(coordList);
    }

    private static LinearRingType getLinearRingFromCoordinateArray(List<Coordinate> coordinates) {

        LinearRingType linearRing = LinearRingType.Factory.newInstance();

        for (Coordinate coordinate : coordinates) {
            List<String> pointList = new ArrayList<String>();
            pointList.add(Double.toString(coordinate.x));
            pointList.add(Double.toString(coordinate.y));
            linearRing.addNewPos().setListValue(pointList);
        }

        return linearRing;
    }

    /**
     * Parses a coordinate from an incident's XML representation
     * 
     * @param parent Element
     * @return Coordinate
     */
    protected static Coordinate parseCoordinateUsingDOM(Element parent) {

        Element coord = DOMUtils.getChild(parent, "LocationTwoDimensionalGeographicCoordinate");
        if (coord == null)
            coord = parent;

        Element lon = DOMUtils.getChild(coord, "GeographicCoordinateLongitude");
        Double lonDegree = Coerce.toDouble(DOMUtils.getChildText(lon, "LongitudeDegreeValue"), null);
        Double lonMinute = Coerce.toDouble(DOMUtils.getChildText(lon, "LongitudeMinuteValue"), 0.0);
        Double lonSecond = Coerce.toDouble(DOMUtils.getChildText(lon, "LongitudeSecondValue"), 0.0);

        Element lat = DOMUtils.getChild(coord, "GeographicCoordinateLatitude");
        Double latDegree = Coerce.toDouble(DOMUtils.getChildText(lat, "LatitudeDegreeValue"), null);
        Double latMinute = Coerce.toDouble(DOMUtils.getChildText(lat, "LatitudeMinuteValue"), 0.0);
        Double latSecond = Coerce.toDouble(DOMUtils.getChildText(lat, "LatitudeSecondValue"), 0.0);

        // didn't specify coordinates properly
        if (lonDegree == null || latDegree == null) {
            log.warn("Coordinates were not specified properly in Incident work product, "
                     + "unable to determine location for default map");
            return null;
        }

        // convert to decimal degrees
        int sign = (int) (lonDegree / Math.abs(lonDegree));
        lonDegree = sign * (Math.abs(lonDegree) + (lonMinute / 60.0) + (lonSecond / 3600.0));

        sign = (int) (latDegree / Math.abs(latDegree));
        latDegree = sign * (Math.abs(latDegree) + (latMinute / 60.0) + (latSecond / 3600.0));

        return new Coordinate(lonDegree, latDegree);
    }

    public static String[] toDegMinSec(double d) {

        int degrees = (int) d;
        d = Math.abs(d - degrees) * 60;
        int minutes = (int) d;
        double seconds = ((d - minutes) * 60) + 0.005;
        String[] ret = new String[3];
        ret[0] = String.valueOf(degrees);
        ret[1] = String.valueOf(minutes);
        ret[2] = String.valueOf(seconds).substring(0, 5);
        return ret;
    }

    protected static String fromDegMinSec(String degrees, String minutes, String seconds) {

        Double dDegree = Coerce.toDouble(degrees, null);
        Double dMinute = Coerce.toDouble(minutes, 0.0);
        Double dSecond = Coerce.toDouble(seconds, 0.0);

        int sign = (int) (dDegree / Math.abs(dDegree));
        dDegree = sign * (Math.abs(dDegree) + (dMinute / 60.0) + (dSecond / 3600.0));

        String doubleString = Double.valueOf(dDegree).toString();
        // int index = doubleString.indexOf(".");

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(5);
        nf.setMinimumFractionDigits(5);

        return nf.format(dDegree);
    }

    protected static double toDouble(String doubleString) throws NumberFormatException {

        return Double.parseDouble(doubleString);
    }

}
