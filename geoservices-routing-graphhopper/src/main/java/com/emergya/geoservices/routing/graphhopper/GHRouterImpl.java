package com.emergya.geoservices.routing.graphhopper;

import com.emergya.geoservices.routing.utils.PointUtils;
import com.emergya.geoservices.routing.wsdl.Edge;
import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse.Messages;
import com.emergya.geoservices.routing.wsdl.Message;
import com.emergya.geoservices.routing.wsdl.Point;
import com.emergya.geoservices.routing.wsdl.ResponsePoint;
import com.emergya.geoservices.routing.wsdl.Step;
import com.emergya.geoservices.routing.wsdl.StepPoint;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.http.GraphHopperWeb;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.gml2.GMLWriter;
import com.vividsolutions.jts.util.Memory;
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
    private Coordinate coordInicio;
    private Coordinate coordFinal;

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
            return response;
        }

        FindShortestPathResponse.Path path = new FindShortestPathResponse.Path();
        response.setPath(path);

        //start and target points
        ResponsePoint startPoint = new ResponsePoint();
        ResponsePoint.Edge edgeStartPoint = new ResponsePoint.Edge();
        edgeStartPoint.setDirection("TF");
        startPoint.setEdge(edgeStartPoint);
        response.setStartPoint(startPoint);

        ResponsePoint targetPoint = new ResponsePoint();
        ResponsePoint.Edge edgeTargetPoint = new ResponsePoint.Edge();
        edgeTargetPoint.setDirection("FT");
        targetPoint.setEdge(edgeTargetPoint);
        response.setTargetPoint(targetPoint);

        //path steps
        List<Step> steps = path.getStep();

        Iterator<Instruction> instructions = route.getInstructions().iterator();

        int i = 0;
        double accTime = 0.0;
        double accDistance = 0.0;
        int size = route.getInstructions().getSize();

        Geometry startProj = null;
        Geometry targetProj = null;

        //último paso no aporta información
        while (instructions.hasNext() && i < size - 1) {
            Instruction instruction = instructions.next();

            //order
            Step step = new Step();
            step.setOrder(i);

            //name
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

            //type
            if (i == 0) {
                step.setType(100);
            } else if (i == size - 1) {
                step.setType(101);
            } else {
                step.setType(7);
            }

            //startNode
            StepPoint startNode = new StepPoint();
            startNode.setId(i);
            step.setStartNode(startNode);

            //edges
            List<Edge> edges = step.getEdge();
            int numberEdges = (instruction.getPoints().getSize()) - 1;

            for (int j = 0; j < numberEdges; j++) {
                Edge edge = new Edge();
                //id
                String string = Integer.toString(i) + Integer.toString(j) + '0';
                int id = Integer.parseInt(string);
                edge.setId(id);
                //geometry
                GHPoint p1;
                GHPoint p2;
                Geometry geom = null;
                String srs = parameters.getStartPoint().getSrsName();

                if (j < numberEdges) {
                    p1 = instruction.getPoints().toGHPoint(j);
                    p2 = instruction.getPoints().toGHPoint(j + 1);
                    try {
                        geom = toLineString(p1, p2, srs);
                        //primer tramo
                        if (i == 0 && j == 0) {
                            startProj = geom;
                        } //último tramo
                        else if (i == size - 2 && j == 0) {
                            targetProj = geom;
                        }
                    } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
                        Logger.getLogger(GHRouterImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    p1 = instruction.getPoints().toGHPoint(numberEdges - 1);
                    p2 = instruction.getPoints().toGHPoint(numberEdges);
                    try {
                        geom = toLineString(p1, p2, srs);

                    } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
                        Logger.getLogger(GHRouterImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                GMLWriter writerGeom = new GMLWriter();
                edge.setGeometry(writerGeom.write(geom));

                edges.add(edge);

            }

            //targetNode
            StepPoint targetNode = new StepPoint();
            targetNode.setId(i + 1);
            step.setTargetNode(targetNode);

            //time
            accTime += instruction.getTime() / 1000;
            step.setTime(accTime);

            //distance
            accDistance += instruction.getDistance();
            step.setDistance(accDistance);

            steps.add(step);

            i++;
        }

        //distFrom and distTo of startPoint
        Coordinate iPointSP = new Coordinate(startProj.getCoordinates()[0].x, startProj.getCoordinates()[0].y);
        Coordinate[] distFromEdgeSP = new Coordinate[2];
        distFromEdgeSP[0] = iPointSP;
        distFromEdgeSP[1] = coordInicio;
        CoordinateSequence coorSeqInicioSP = new CoordinateArraySequence(distFromEdgeSP);
        GeometryFactory geometryFactoryInicioSP = new GeometryFactory();
        geometryFactoryInicioSP.createLineString(coorSeqInicioSP);
        Geometry geomInicioSP = new com.vividsolutions.jts.geom.LineString(coorSeqInicioSP, geometryFactoryInicioSP);
        double distFromSP = geomInicioSP.getLength();
        startPoint.getEdge().setDistFrom(distFromSP);
        System.out.println("StartPoint distFrom: " + distFromSP);

        Coordinate fPointSP = new Coordinate(startProj.getCoordinates()[1].x, startProj.getCoordinates()[1].y);
        Coordinate[] distToEdgeSP = new Coordinate[2];
        distToEdgeSP[0] = fPointSP;
        distToEdgeSP[1] = coordInicio;
        CoordinateSequence coorSeqFinalSP = new CoordinateArraySequence(distToEdgeSP);
        GeometryFactory geometryFactoryFinalSP = new GeometryFactory();
        geometryFactoryFinalSP.createLineString(coorSeqFinalSP);
        Geometry geomFinalSP = new com.vividsolutions.jts.geom.LineString(coorSeqFinalSP, geometryFactoryFinalSP);
        double distToSP = geomFinalSP.getLength();
        startPoint.getEdge().setDistTo(distToSP);
        System.out.println("StartPoint distTo: " + distToSP);
        
         //distFrom and distTo of targetPoint
        Coordinate iPointTP = new Coordinate(targetProj.getCoordinates()[0].x, targetProj.getCoordinates()[0].y);
        Coordinate[] distFromEdgeTP = new Coordinate[2];
        distFromEdgeTP[0] = iPointTP;
        distFromEdgeTP[1] = coordFinal;
        CoordinateSequence coorSeqInicioTP = new CoordinateArraySequence(distFromEdgeTP);
        GeometryFactory geometryFactoryInicioTP = new GeometryFactory();
        geometryFactoryInicioTP.createLineString(coorSeqInicioTP);
        Geometry geomInicioTP = new com.vividsolutions.jts.geom.LineString(coorSeqInicioTP, geometryFactoryInicioTP);
        double distFromTP = geomInicioTP.getLength();
        targetPoint.getEdge().setDistFrom(distFromTP);
        System.out.println("TargetPoint distFrom: " + distFromTP);

        Coordinate fPointTP = new Coordinate(targetProj.getCoordinates()[1].x, targetProj.getCoordinates()[1].y);
        Coordinate[] distToEdgeTP = new Coordinate[2];
        distToEdgeTP[0] = fPointTP;
        distToEdgeTP[1] = coordFinal;
        CoordinateSequence coorSeqFinalTP = new CoordinateArraySequence(distToEdgeTP);
        GeometryFactory geometryFactoryFinalTP = new GeometryFactory();
        geometryFactoryFinalTP.createLineString(coorSeqFinalTP);
        Geometry geomFinalTP = new com.vividsolutions.jts.geom.LineString(coorSeqFinalTP, geometryFactoryFinalTP);
        double distToTP = geomFinalTP.getLength();
        targetPoint.getEdge().setDistTo(distToTP);
        System.out.println("TargetPoint distTo: " + distToTP);

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
        if (coordInicio == null) {
            coordInicio = c;
        } else {
            coordFinal = c;
        }
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
        System.out.println("EPSG inicial " + srs + ':' + p.getCoordinates());
        System.out.println("EPSG final 4326:" + point);
        GHPlace gHPlace = new GHPlace(point.getX(), point.getY());

        return gHPlace;
    }

    private Geometry toLineString(GHPoint p1, GHPoint p2, String srs) throws FactoryException, MismatchedDimensionException, TransformException {
        Coordinate[] coord = new Coordinate[2];
        Coordinate c1 = new Coordinate(p1.getLat(), p1.getLon());
        Coordinate c2 = new Coordinate(p2.getLat(), p2.getLon());
        coord[0] = c1;
        coord[1] = c2;
        CoordinateSequence coorSeq = new CoordinateArraySequence(coord);

        GeometryFactory geometryFact = new GeometryFactory();
        geometryFact.createLineString(coorSeq);

        Geometry geometry = new com.vividsolutions.jts.geom.LineString(coorSeq, geometryFact);

        CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + srs);
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        Geometry edge = JTS.transform(geometry, transform);

        return edge;
    }

}
