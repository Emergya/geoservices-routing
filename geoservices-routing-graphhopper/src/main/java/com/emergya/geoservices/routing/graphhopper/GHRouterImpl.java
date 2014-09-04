package com.emergya.geoservices.routing.graphhopper;

import com.emergya.geoservices.routing.utils.PointUtils;
import com.emergya.geoservices.routing.wsdl.Edge;
import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse.Messages;
import com.emergya.geoservices.routing.wsdl.Message;
import com.emergya.geoservices.routing.wsdl.Point;
import com.emergya.geoservices.routing.wsdl.Step;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.http.GraphHopperWeb;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPlace;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.referencing.CRS;
import org.geotools.geometry.jts.JTS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author lroman
 */
@Service
public class GHRouterImpl implements GHRouter {

    @Value("${geoservices.routing.graphhopper.url}")
    private String GRAPHHOPPER_URL;

    private GraphHopperAPI gh;

    @Override
    public FindShortestPathResponse createRoute(FindShortestPath parameters) {

        if (gh == null) {
            gh = new GraphHopperWeb();
            gh.load(GRAPHHOPPER_URL);
        }

        GHResponse route = null;
        FindShortestPathResponse response = new FindShortestPathResponse();
        try {
            route = gh.route(createGHRequest(parameters));
        } catch (FactoryException | TransformException ex) {
            Message message = new Message();
            message.setText("Transform coordinates has failed");
            Messages messages = new FindShortestPathResponse.Messages();
            response.setMessages(messages);
            Logger.getLogger(GHRouterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        FindShortestPathResponse.Path path = new FindShortestPathResponse.Path();
        response.setPath(path);

        List<Step> steps = path.getStep();

        Iterator<Instruction> instructions = route.getInstructions().iterator();

        int i = 0;

        double accTime = 0.0;
        double accDistance = 0.0;

        while (instructions.hasNext()) {
            Instruction instruction = instructions.next();

            Step step = new Step();
            step.setOrder(i);

            String instructionText = instruction.getName();
            // We need to clean it a bit:
            int ontoIndex = instructionText.indexOf(_ONTO_);
            if (ontoIndex >= 0) {
                instructionText = instructionText.substring(ontoIndex + _ONTO_.length());
            }

            if (instructionText.equals("Continue")) {
                instructionText = "Unknown";
            }

            step.setName(instructionText);
            step.setType(7);

            accDistance += instruction.getDistance();
            step.setDistance(accDistance);

            accTime += instruction.getTime() / 1000;
            step.setTime(accTime);

            List<Edge> edges = step.getEdge();

            steps.add(step);

            i++;
        }

        return response;
    }
    private static final String _ONTO_ = " onto ";

    private GHRequest createGHRequest(FindShortestPath parameters) throws FactoryException, TransformException {
        GHRequest request = new GHRequest(
                toGHPlace(parameters.getStartPoint()),
                toGHPlace(parameters.getTargetPoint()));

        return request;
    }

    private GHPlace toGHPlace(Point p) throws FactoryException, MismatchedDimensionException, TransformException {
        double[] coordinates = PointUtils.toCoordinates(p);

        Coordinate c = new Coordinate(coordinates[0], coordinates[1]);
        Coordinate[] coord = new Coordinate[1];
        coord[0] = c;
        CoordinateSequence coorSeq = new CoordinateArraySequence(coord);

        GeometryFactory geometryFactory = new GeometryFactory();
        geometryFactory.createPoint(coorSeq);

        Geometry geometry = new com.vividsolutions.jts.geom.Point(coorSeq, geometryFactory);

        String srs = p.getSrsName();
        CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + srs);
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        Geometry targetGeometry = JTS.transform(geometry, transform);
        
        
        com.vividsolutions.jts.geom.Point point = targetGeometry.getCentroid();
        System.out.println("EPSG inicial " + srs +':' + p.getCoordinates());
        System.out.println("EPSG final 4326:" + point);
        GHPlace gHPlace = new GHPlace(point.getX(), point.getY());

        return gHPlace;
    }

}
