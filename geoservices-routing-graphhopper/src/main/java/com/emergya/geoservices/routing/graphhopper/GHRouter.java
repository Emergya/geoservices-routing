/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.emergya.geoservices.routing.graphhopper;

import com.emergya.geoservices.routing.Router;
import com.emergya.geoservices.routing.wsdl.FindShortestPath;
import com.emergya.geoservices.routing.wsdl.FindShortestPathResponse;

/**
 *
 * @author lroman
 */
public interface GHRouter extends Router {

    @Override
    public FindShortestPathResponse createRoute(FindShortestPath parameters);
    
    
}
