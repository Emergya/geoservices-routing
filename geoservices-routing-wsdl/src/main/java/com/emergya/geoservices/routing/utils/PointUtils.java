package com.emergya.geoservices.routing.utils;

import com.emergya.geoservices.routing.wsdl.Point;

/**
 * Utilities for the handling of points.
 * 
 * @author lroman
 */
public abstract class PointUtils {
    public static double[] toCoordinates(Point p) {
        // TODO: Must convert to LatLng.
        String [] coordinates = p.getCoordinates().split(",");
        
        return new double[]{
            Double.parseDouble(coordinates[0].trim()), 
            Double.parseDouble(coordinates[1].trim())};
        
    }
}
