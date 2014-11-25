package com.emergya.geoservices.routing.engine;

import javax.annotation.PostConstruct;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.logging.Logger;

/**
 *
 * @author marcos
 */
@Component
public class RoutingHandler {

    private static final Logger LOG = Logger.getLogger(RoutingHandler.class.getName());

    @Value("${geoservices.routing.graphhopper.osmFilePath}")
    private String OSM_FILE_PATH;
    @Value("${geoservices.routing.graphhopper.graphPath}")
    private String GRAPH_PATH;
    @Value("${geoservices.routing.graphhopper.vehicle}")
    private String VEHICLE;
    @Value("${geoservices.routing.graphhopper.weighting}")
    private String WEIGHTING;
    @Value("${geoservices.routing.graphhopper.srs}")
    private String GRAPHHOPER_SRS;
    @Value("${geoservices.routing.graphhopper.locale}")
    private String LOCALE;
    @Value("${geoservices.routing.graphhopper.useLonLat}")
    private boolean USE_LON_LAT = false;

    private GraphHopper hopper;

    public GHResponse getRoute(String[] startPointValue, String[] targetPointValue, String type_wighting) throws MismatchedDimensionException, FactoryException, TransformException {
        // Set points
        GHRequest request = new GHRequest(toGHPlace(startPointValue), toGHPlace(targetPointValue));

        request.setVehicle(this.VEHICLE);
        // Set language
        request.setLocale(this.LOCALE);
        // Set weighting
        if (type_wighting != null) {
            request.setWeighting(type_wighting);
        } else {
            request.setWeighting(this.WEIGHTING);
        }

        request.putHint("calcPoints", true);
        request.putHint("instructions", true);

        return hopper.route(request);
    }

    @PostConstruct
    public void init() {
        hopper = new GraphHopper().forServer();
        //hopper.setEnableTurnRestrictions(true);
        hopper.disableCHShortcuts();
        hopper.setInMemory(true);
        hopper.setOSMFile(this.OSM_FILE_PATH);
        hopper.setGraphHopperLocation(this.GRAPH_PATH);
        hopper.setEncodingManager(new EncodingManager(this.VEHICLE));

        hopper.importOrLoad();
    }

    private GHPoint toGHPlace(String[] point) throws FactoryException, MismatchedDimensionException, TransformException {

        String srs = point[0];
        String coordinate = point[1];
        String[] pointValue = this.getPoint(coordinate);
        Coordinate c = null;
        if (srs.equalsIgnoreCase(this.GRAPHHOPER_SRS)) {
            c = new Coordinate(Double.valueOf(pointValue[1]), Double.valueOf(pointValue[0]));
        } else {
            c = new Coordinate(Double.valueOf(pointValue[0]), Double.valueOf(pointValue[1]));
        }

        Coordinate[] coord = new Coordinate[1];
        coord[0] = c;

        CoordinateSequence coorSeq = new CoordinateArraySequence(coord);

        GeometryFactory geometryFactory = new GeometryFactory();
        geometryFactory.createPoint(coorSeq);

        Geometry geometry = new com.vividsolutions.jts.geom.Point(coorSeq, geometryFactory);

        CoordinateReferenceSystem sourceCRS = CRS.decode(srs);
        CoordinateReferenceSystem targetCRS = CRS.decode(this.GRAPHHOPER_SRS);
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        Geometry targetGeometry = JTS.transform(geometry, transform);

        

        com.vividsolutions.jts.geom.Point jtsPoint = targetGeometry.getCentroid();
        GHPoint gHPlace;
        if (USE_LON_LAT && !this.GRAPHHOPER_SRS.equals(srs)) {
            gHPlace = new GHPoint(jtsPoint.getY(), jtsPoint.getX());
        } else {
            gHPlace = new GHPoint(jtsPoint.getX(), jtsPoint.getY());
        }
        
        LOG.info(String.format("Source point: %s; target point: %s", geometry, gHPlace));

        return gHPlace;
    }

    private String[] getPoint(String pointValue) {
        String[] point = new String[2];
        if (pointValue.indexOf(",") > -1) {
            point = pointValue.split(",");
        }
        return point;
    }
}
