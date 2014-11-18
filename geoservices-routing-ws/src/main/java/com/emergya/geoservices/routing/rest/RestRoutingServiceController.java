package com.emergya.geoservices.routing.rest;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.emergya.geoservices.routing.engine.RoutingHandler;
import com.graphhopper.GHResponse;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.gml2.GMLWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/mrk")
public class RestRoutingServiceController {

    private static final Logger LOG = Logger.getLogger(RestRoutingServiceController.class.getName());
    
    @Value("${geoservices.routing.graphhopper.srs}")
    private String SRS;
    
    @Value("${geoservices.routing.graphhopper.useLonLat}")
    private boolean USE_LON_LAT = false;

    @Autowired
    RoutingHandler handler;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    String test() {
        return "Please try using POST ;)";
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    String restRouting(@RequestBody String payload) {
        // Request
        GHResponse route = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        String[] startPointValue, targetPointValue;
        String srs = "";
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(payload)));
            startPointValue = getCoordinates("startPoint", document);
            srs = startPointValue[0];
            targetPointValue = getCoordinates("targetPoint", document);
            NodeList prioritize = document.getElementsByTagName("toPrioritize");
            String type_weighting = null;
            if (prioritize != null) {
                String type_priority = prioritize.item(0).getTextContent();
                if (type_priority.equalsIgnoreCase("distance")) {
                    type_weighting = "shortest";
                } else if (type_priority.equalsIgnoreCase("time")) {
                    type_weighting = "fastest";
                }
            }
            route = handler.getRoute(startPointValue, targetPointValue, type_weighting);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }

        // Response
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
        Element rootElement = doc.createElement("soapenv:Envelope");
        Attr attr = doc.createAttribute("xmlns:soapenv");
        attr.setValue("http://schemas.xmlsoap.org/soap/envelope/");
        rootElement.setAttributeNode(attr);
        doc.appendChild(rootElement);

        // <soapenv:Body>
        Element body = doc.createElement("soapenv:Body");
        rootElement.appendChild(body);

        // <n:findShortestPathResponse xmlns:n="http://sigem.sitep.com/mrk">
        Element findSPR = doc.createElement("n:findShortestPathResponse");
        Attr findattr = doc.createAttribute("xmlns:n");
        findattr.setValue("http://sigem.sitep.com/mrk");
        findSPR.setAttributeNode(findattr);
        body.appendChild(findSPR);

        // <headers>
        Element headers = doc.createElement("headers");
        findSPR.appendChild(headers);
        // <header> CREATED BY
        Element header1 = getHeader(doc, "CREATED BY", this.getClass().getName());
        headers.appendChild(header1);
        // <header> CREATION DATE
        Element header2 = getHeader(doc, "CREATION DATE", (new Date()).toString());
        headers.appendChild(header2);

        // <startPoint>
        Point startp = new Point(doc, "", "", "", "");
        Element startPoint = getPoint(doc, "start", startp);
        findSPR.appendChild(startPoint);
        // <targetPoint>
        Point targetp = new Point(doc, "", "", "", "");
        Element targetPoint = getPoint(doc, "target", targetp);
        findSPR.appendChild(targetPoint);

        // <path>
        Element path = doc.createElement("path");
        findSPR.appendChild(path);

        int i = 0;
        
        if(route == null) {
            return null;
        }
        
        PointList points_route = route.getPoints();
        //Instruction last = null;
        InstructionList inst_list = route.getInstructions();
        List<Map<String, Object>> json = inst_list.createJson();
        Iterator<Map<String, Object>> json_it = json.iterator();
        if (json.isEmpty()) {
            doc = null;
        } else {
            int size = json.size();
            while (json_it.hasNext() && i < json.size()) {
                Map<String, Object> json_map = json_it.next();
                Step step = new Step(doc);
                // order
                step.setOrder(Integer.toString(i));
                // text
                String text = (String) json_map.get("text");
                step.setName(text);
                // type
                if (i == 0) {
                    step.setType(Integer.toString(100));
                } else if (i == size - 1) {
                    step.setType(Integer.toString(101));
                } else {
                    step.setType(Integer.toString(7));
                }
                // startNode
                step.setStartNode(Integer.toString(i));
                // targetNode
                step.setTargetNode(Integer.toString(i + 1));
                // Time
                Long time = (Long) json_map.get("time");
                step.setTime(Long.toString(time));
                // Distance
                Double distance = (Double) json_map.get("distance");
                step.setDistance(Double.toString(distance));
                // Interval line
                List<Integer> interval = (List<Integer>) json_map.get("interval");
                GeometryFactory geometryFact = new GeometryFactory();
                List<EdgeStep> edges_list = step.getEdges();
                Integer first = interval.get(0);
                Integer last = interval.get(interval.size() - 1);
                Coordinate[] coordinate_list = new Coordinate[last - first + 1];
                int coordinates_index = 0;
                for (int index = first; index <= last; index++) {
                    Double lat = points_route.getLat(index);
                    Double lon = points_route.getLon(index);
                    Coordinate c = null;
                    if (srs.equalsIgnoreCase(this.SRS) || USE_LON_LAT) {
                        c = new Coordinate(lon, lat);
                    } else {
                        c = new Coordinate(lat, lon);
                    }
                    coordinate_list[coordinates_index] = c;
                    coordinates_index++;
                }
                Coordinate[] last_pos = new Coordinate[2];
                if (coordinate_list.length <= 1) {
                    last_pos[0] = coordinate_list[0];
                    last_pos[1] = coordinate_list[0];
                    coordinate_list = last_pos;
                }
                Geometry geom = this.getGeom(coordinate_list, geometryFact, srs);
                EdgeStep es = new EdgeStep(doc);
                es.setId(Integer.toString(i) + '0');
                es.setGeom(geom);
                edges_list.add(es);

                // Doc element to return 
                Element stepElement = doc.createElement("step");
                stepElement.setAttribute("order", step.getOrder());
                stepElement.appendChild(step.getName());
                stepElement.appendChild(step.getType());
                stepElement.appendChild(step.getStartNode());
                List<EdgeStep> edges = step.getEdges();
                for (int j = 0; j < edges.size(); j++) {
                    EdgeStep e = edges.get(j);
                    Element edge = doc.createElement("edge");
                    edge.appendChild(e.getId());
                    GMLWriter gmlwriter = new GMLWriter();
                    gmlwriter.setSrsName(srs);
                    gmlwriter.setNamespace(true);
                    String gml = gmlwriter.write(e.getGeo());
                    Element geometry = e.getGeometry();
                    geometry.setTextContent(gml);
                    edge.appendChild(geometry);
                    stepElement.appendChild(edge);
                }
                stepElement.appendChild(step.getTargetNode());
                stepElement.appendChild(step.getTime());
                stepElement.appendChild(step.getDistance());
                path.appendChild(stepElement);

                i++;
            }
        }

        return toString(doc);
    }

    private Geometry getGeom(Coordinate[] coordinate_list, GeometryFactory geometryFact, String srs) {
        CoordinateSequence seq = new CoordinateArraySequence(coordinate_list);
        Geometry geometryLatLng = new com.vividsolutions.jts.geom.LineString(seq, geometryFact);
        // Change CRS
        CoordinateReferenceSystem sourceCRS;
        CoordinateReferenceSystem targetCRS;
        MathTransform transform;
        Geometry geometryTransformed = null;
        try {
            sourceCRS = CRS.decode(this.SRS);
            targetCRS = CRS.decode(srs);
            transform = CRS.findMathTransform(sourceCRS, targetCRS);
            
            geometryTransformed = JTS.transform(geometryLatLng, transform);
            LOG.info(String.format("Source point: %s; target point: %s", geometryLatLng, geometryTransformed));
        } catch (FactoryException | MismatchedDimensionException | TransformException e1) {
            e1.printStackTrace();
        }
        return geometryTransformed;
    }

    private String[] getCoordinates(String tagName, Document document) {
        String[] value = new String[2];
        NodeList points = document.getElementsByTagName(tagName);
        for (int i = 0; i < points.getLength(); ++i) {
            Element sp = (Element) points.item(i);
            NodeList point = sp.getElementsByTagName("Point");
            for (int j = 0; j < point.getLength(); ++j) {
                Node p = (Node) point.item(j);
                value[0] = p.getAttributes().getNamedItem("srsName").getTextContent();
            }
            NodeList coordinates = sp.getElementsByTagName("coordinates");
            for (int j = 0; j < coordinates.getLength(); ++j) {
                Node coordinate = (Node) coordinates.item(j);
                value[1] = coordinate.getTextContent();
            }
        }
        return value;
    }

    private String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    private Element getHeader(Document doc, String name, String value) {
        Element header = doc.createElement("header");
        Element nameLabel = doc.createElement("name");
        nameLabel.setTextContent(name);
        Element valueLabel = doc.createElement("value");
        valueLabel.setTextContent(value);
        header.appendChild(nameLabel);
        header.appendChild(valueLabel);

        return header;
    }

    private Element getPoint(Document doc, String label, Point p) {
        Element point = doc.createElement(label + "Point");
        Element edge = doc.createElement("edge");
        edge.appendChild(p.getId());
        edge.appendChild(p.getDistFrom());
        edge.appendChild(p.getDistTo());
        edge.appendChild(p.getDirection());
        point.appendChild(edge);

        return point;
    }

    private class Point {

        private Element id;
        private Element distFrom;
        private Element distTo;
        private Element direction;

        public Point(Document doc, String id, String distFrom, String distTo, String direction) {
            this.id = doc.createElement("id");

            this.distFrom = doc.createElement("distFrom");

            this.distTo = doc.createElement("distTo");

            this.direction = doc.createElement("direction");
        }

        public Element getId() {
            return id;
        }

        public void setId(Element id) {
            this.id = id;
        }

        public Element getDistFrom() {
            return distFrom;
        }

        public void setDistFrom(Element distFrom) {
            this.distFrom = distFrom;
        }

        public Element getDistTo() {
            return distTo;
        }

        public void setDistTo(Element distTo) {
            this.distTo = distTo;
        }

        public Element getDirection() {
            return direction;
        }

        public void setDirection(Element direction) {
            this.direction = direction;
        }
    }

    private class Step {

        private String order;
        private Element name;
        private Element type;
        private NodePoint startNode;
        private Element startNodeElement;
        private List<EdgeStep> edges;
        private List<Element> edgesElements;
        private NodePoint targetNode;
        private Element targetNodeElement;
        private Element time;
        private Element distance;

        public Step(Document doc) {
            this.order = "";
            this.name = doc.createElement("name");
            this.type = doc.createElement("type");
            this.startNode = new NodePoint(doc);
            this.startNodeElement = doc.createElement("startNode");
            this.edges = new LinkedList<EdgeStep>();
            this.targetNode = new NodePoint(doc);
            this.targetNodeElement = doc.createElement("targetNode");
            this.time = doc.createElement("time");
            this.distance = doc.createElement("distance");
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public Element getName() {
            return name;
        }

        public void setName(String name) {
            this.name.setTextContent(HtmlUtils.htmlEscape(name));
        }

        public Element getType() {
            return type;
        }

        public void setType(String type) {
            this.type.setTextContent(type);
        }

        public Element getStartNode() {
            this.startNodeElement.appendChild(this.startNode.getId());
            return this.startNodeElement;
        }

        public void setStartNode(String startNode) {
            this.startNode.setId(startNode);
        }

        public List<EdgeStep> getEdges() {
            return this.edges;
        }

        public void setEdges(List<EdgeStep> edges) {
            this.edges = edges;
        }

        public Element getTargetNode() {
            this.targetNodeElement.appendChild(this.startNode.getId());
            return this.targetNodeElement;
        }

        public void setTargetNode(String targetNode) {
            this.targetNode.setId(targetNode);
        }

        public Element getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time.setTextContent(time);
        }

        public Element getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance.setTextContent(distance);
        }

    }

    private class EdgeStep {

        private Element id;
        private Element geometry;
        private Geometry geom;

        public EdgeStep(Document doc) {
            this.id = doc.createElement("id");
            this.geometry = doc.createElement("geometry");
            this.geom = null;
        }

        public Element getId() {
            return id;
        }

        public void setId(String id) {
            this.id.setTextContent(id);
        }

        public Element getGeometry() {
            return geometry;
        }

        public void setGeometry(String geometry) {
            this.geometry.setTextContent(geometry);
        }

        public Geometry getGeo() {
            return geom;
        }

        public void setGeom(Geometry geom) {
            this.geom = geom;
        }
    }

    private class NodePoint {

        private Element id;

        public NodePoint(Document doc) {
            this.id = doc.createElement("id");
        }

        public Element getId() {
            return id;
        }

        public void setId(String id) {
            this.id.setTextContent(id);
        }
    }

}
