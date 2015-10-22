package com.leidos.xchangecore.core.infrastructure.util;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;

import com.saic.precis.x2009.x06.structures.WorkProductDocument;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class BoundingBoxTest {

    private boolean intersects(Double[][] boundingBox, Geometry geom) {

        System.out.println("BoundingBox: ");
        for (int i = 0; i < 5; i++) {
            System.out.println("Coordinate: (" + boundingBox[i][0] + ", " + boundingBox[i][1] + ")");
        }

        if (geom instanceof Polygon) {
            System.out.println("geo type isIt's a Polygon: ");
            return GeometryUtil.intersects(boundingBox, (Polygon) geom);
        } else if (geom instanceof Point) {
            System.out.println("geo type is a Point: [" + geom + "]");
            return GeometryUtil.contains(boundingBox, (Point) geom);
        }
        return true;
    }

    private boolean isMatched(String regexp, String content) {

        final boolean isNegative = regexp.startsWith("!");
        if (isNegative) {
            regexp = regexp.substring(1);
        }

        String re = regexp.replaceAll("\\*", ".\\*");
        re = "(?i:" + re + ")";
        final boolean isMatched = content.matches(re);
        return isNegative ? !isMatched : isMatched;
    }

    @Test
    public void testBoundingBox() throws XmlException, IOException {

        final Double[][] boundingBox = new Double[5][2];
        final Double south = 38.00;
        final Double west = -76.00;
        final Double north = 39.00;
        final Double east = -77.00;
        boundingBox[0][0] = west;
        boundingBox[0][1] = south;
        boundingBox[1][0] = west;
        boundingBox[1][1] = north;
        boundingBox[2][0] = east;
        boundingBox[2][1] = north;
        boundingBox[3][0] = east;
        boundingBox[3][1] = south;
        boundingBox[4][0] = west;
        boundingBox[4][1] = south;

        final String filename = "src/test/resources/workproduct/Incident.1.xml";
        final WorkProductDocument wpd = WorkProductDocument.Factory.parse(new File(filename));

        final Geometry geometry = DigestHelper.getFirstGeometry(WorkProductHelper.getDigestElement(wpd.getWorkProduct()));
        final boolean insideBoundingBox = intersects(boundingBox, geometry);
        System.out.println("inside the bounding box ? " + (insideBoundingBox ? " true " : " false"));
    }

    @Test
    public void testMetaCharacter() {

        final String content = "Store is closed and Not open";

        String regexp = "*close*";
        System.out.println("\"" + regexp + "\"" +
            (isMatched(regexp, content) ? " matched " : " not matched ") + "\"" +
            content + "\"");
        regexp = "*open";
        System.out.println("\"" + regexp + "\"" +
            (isMatched(regexp, content) ? " matched " : " not matched ") + "\"" +
            content + "\"");

        regexp = "close*";
        System.out.println("\"" + regexp + "\"" +
            (isMatched(regexp, content) ? " matched " : " not matched ") + "\"" +
            content + "\"");
        regexp = "!*close*";
        System.out.println("\"" + regexp + "\"" +
            (isMatched(regexp, content) ? " matched " : " not matched ") + "\"" +
            content + "\"");
        regexp = "!*close";
        System.out.println("\"" + regexp + "\"" +
            (isMatched(regexp, content) ? " matched " : " not matched ") + "\"" +
            content + "\"");

        regexp = "!close*";
        System.out.println("\"" + regexp + "\"" +
            (isMatched(regexp, content) ? " matched " : " not matched ") + "\"" +
            content + "\"");
    }
}
