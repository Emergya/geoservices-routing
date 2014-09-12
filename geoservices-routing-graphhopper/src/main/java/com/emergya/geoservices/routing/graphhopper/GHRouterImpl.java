package com.emergya.geoservices.routing.graphhopper;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.emergya.geoservices.routing.utils.PointUtils;
import com.emergya.geoservices.routing.wsdl.Edge;
import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;
import com.emergya.geoservices.routing.wsdl.Point;
import com.emergya.geoservices.routing.wsdl.RequestPoint;
import com.emergya.geoservices.routing.wsdl.Step;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.http.GraphHopperWeb;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPlace;
import java.util.Iterator;
import java.util.List;
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
       
        if(gh == null) {
            gh = new GraphHopperWeb();
            gh.load(GRAPHHOPPER_URL);
        }

        GHResponse route = gh.route(createGHRequest(parameters));

        FindShortestPathResponse response = new FindShortestPathResponse();
        FindShortestPathResponse.Path path = new FindShortestPathResponse.Path();        
        response.setPath(path);
        
        List<Step> steps = path.getStep();
        
        
        Iterator<Instruction> instructions = route.getInstructions().iterator();
        
        int i =0;
        
        double accTime = 0.0;
        double accDistance = 0.0;
        
        while(instructions.hasNext()) {
            Instruction instruction = instructions.next();
                    
            
            Step step = new Step();
            step.setOrder(i);
            
            String instructionText = instruction.getName();
            // We need to clean it a bit:
            int ontoIndex = instructionText.indexOf(_ONTO_);
            if(ontoIndex>=0) {
                instructionText = instructionText.substring(ontoIndex+ _ONTO_.length());
            }
            
            if(instructionText.equals("Continue")) {
                instructionText = "Unknown";
            }
            
            step.setName(instructionText);
            step.setType(7);
                    
            accDistance += instruction.getDistance();
            step.setDistance(accDistance);
            
            accTime+= instruction.getTime()/1000;            
            step.setTime(accTime);
            
            List<Edge> edges = step.getEdge();
            
            
            
            steps.add(step);
            
            i++;
        }
        
        return response;
    }
    private static final String _ONTO_ = " onto ";

    private GHRequest createGHRequest(FindShortestPath parameters) {
        GHRequest request = new GHRequest(
                toGHPlace(parameters.getStartPoint()),
                toGHPlace(parameters.getTargetPoint()));

        return request;
    }

    private GHPlace toGHPlace(Point p) {
        double[] coordinates = PointUtils.toCoordinates(p);
        GHPlace gHPlace = new GHPlace(coordinates[0], coordinates[1]);

        return gHPlace;
    }

}
