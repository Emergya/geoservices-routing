package com.emergya.geoservices.routing.osrm;

import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;
import com.emergya.geoservices.routing.wsdl.Step;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import net.opengis.xls.v_1_2_0.DetermineRouteRequestType;
import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.RouteInstructionType;
import net.opengis.xls.v_1_2_0.RoutePlanType;
import net.opengis.xls.v_1_2_0.RoutePreferenceType;
import net.opengis.xls.v_1_2_0.WayPointType;
import org.emergya.osrm.OSRM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author lroman
 */
@Service
public class OpenLsOsrmRouterImpl implements OSRMRouter {

    @Autowired(required = false)
    OSRM osrmConnector;
    
    @Value("${geoservices.routing.osrm.url}")
    private String OSRM_URL;


    public FindShortestPathResponse createRoute(FindShortestPath parameters) {
        FindShortestPathResponse response = new FindShortestPathResponse();

        DetermineRouteRequestType openLSRequest = createOpenLsRequest(parameters);
        
        DetermineRouteResponseType opnLSResponse = null;
        try {
            opnLSResponse = osrmConnector.routePlan(openLSRequest, OSRM_URL, Locale.getDefault());
        } catch (IOException | JAXBException | ParseException | InterruptedException ex) {
            Logger.getLogger(OpenLsOsrmRouterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(opnLSResponse!=null) {
            FindShortestPathResponse.Path path = new FindShortestPathResponse.Path();
            response.setPath(path);
            
            List<Step> steps = path.getStep();
            
            List<RouteInstructionType> instructions = opnLSResponse.getRouteInstructionsList().getRouteInstruction();
            for(RouteInstructionType instruction: instructions) {
                Step step = new Step();
                
                instruction.getInstruction();
                
                steps.add(step);
            }
                    
        }

        return response;
    }

    private DetermineRouteRequestType createOpenLsRequest(FindShortestPath parameters) {
        DetermineRouteRequestType request = new DetermineRouteRequestType();   
        
        RoutePlanType routePlan = new RoutePlanType();
        routePlan.setRoutePreference(RoutePreferenceType.FASTEST);
        
        List<WayPointType> wayPoints = routePlan.getWayPointList().getViaPoint();
        WayPointType startPoint = new WayPointType();
        //startPoint.setLocation(new PositionType().setPoint(new PointType()));
        
        
        //startPoint.setLocation();
        
        wayPoints.add(startPoint);
        
        WayPointType endPoint = new WayPointType();
        
        wayPoints.add(endPoint);
        
        request.setRoutePlan(routePlan);
        
        return request;
    }

}
