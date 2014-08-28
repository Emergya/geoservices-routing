package com.emergya.geoservices.routing;

import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;

/**
 *
 * @author lroman
 */
public interface Router {
    public FindShortestPathResponse createRoute(FindShortestPath parameters);
}
