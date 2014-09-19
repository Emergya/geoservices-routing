package com.emergya.geoservices.routing.ws;

import com.emergya.geoservices.routing.graphhopper.GHRouter;
import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;
import com.emergya.geoservices.routing.wsdl.Header;
import com.emergya.geoservices.routing.wsdl.RoutingService;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation for the WS interface for the routing geoservices.
 *
 * @author lroman
 */
@WebService(serviceName = "RoutingService", targetNamespace = "http://sigem.sitep.com/mrk")
@Stateless()
public class RoutingServiceImpl implements RoutingService {
    
    @Autowired
    private GHRouter ghRouter;

    @Override
    @WebMethod()
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @RequestWrapper(targetNamespace = "http://sigem.sitep.com/mrk")
    public @WebResult(targetNamespace = "http://sigem.sitep.com/mrk")
    FindShortestPathResponse findShortestPath(FindShortestPath parameters) {

        FindShortestPathResponse response = ghRouter.createRoute(parameters);

        response.setHeaders(new FindShortestPathResponse.Headers());

        List<Header> headers = response.getHeaders().getHeader();
        Header header = new Header();
        header.setName("CREATED BY");
        header.setValue(this.getClass().getName());
        headers.add(header);

        header = new Header();
        header.setName("CREATION DATE");
        header.setValue((new Date()).toString());
        headers.add(header);

        return response;
    }
}
