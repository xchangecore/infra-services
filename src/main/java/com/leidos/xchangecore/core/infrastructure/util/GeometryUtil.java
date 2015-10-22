package com.leidos.xchangecore.core.infrastructure.util;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryUtil {

    public static boolean contains(Double[][] bb, Point point) {

        LinearRing bbLinerRing = new GeometryFactory().createLinearRing(getCoordinateArray(bb));
        Polygon bbPloygon = new GeometryFactory().createPolygon(bbLinerRing, null);
        return point.within(bbPloygon);
    }

    private static Coordinate[] getCoordinateArray(Double[][] coords) {

        List<Coordinate> coordianteList = new ArrayList<Coordinate>();
        for (Double[] coord : coords) {
            coordianteList.add(new Coordinate(coord[0], coord[1]));
        }
        return coordianteList.toArray(new Coordinate[coordianteList.size()]);
    }

    public static boolean intersects(Double[][] bb, Polygon polygon) {

        LinearRing bbLinerRing = new GeometryFactory().createLinearRing(getCoordinateArray(bb));
        Polygon bbPloygon = new GeometryFactory().createPolygon(bbLinerRing, null);

        boolean isTouched = bbPloygon.intersects(polygon);

        return bbPloygon.intersects(polygon);
    }
}
