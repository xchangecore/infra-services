package com.leidos.xchangecore.core.infrastructure.util;

public class LatLonConversion {

    public static String[] toDegMinSec(String decimal) {

        double d = Double.parseDouble(decimal);
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

    public static void main(String[] args) {

        System.out.print("convert " + args[0] + " into ");
        String[] points = toDegMinSec(args[0]);
        System.out.println(points[0] + " " + points[1] + "' " + points[2] + "\"");
    }
}
