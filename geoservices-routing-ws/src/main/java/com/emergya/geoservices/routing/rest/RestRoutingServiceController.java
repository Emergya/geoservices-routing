package com.emergya.geoservices.routing.rest;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.http.GraphHopperWeb;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.gml2.GMLWriter;

@RestController
@RequestMapping(value = "/mrk")
public class RestRoutingServiceController {
	
	@Value("${geoservices.routing.graphhopper.url}")
    private String GRAPHHOPPER_URL;
	@Value("${geoservices.routing.graphhopper.locale}")
	private String LOCALE;
	@Value("${geoservices.routing.graphhopper.srs}")
	private String SRS;

    private GraphHopperAPI gh;
    private Coordinate coordInicio;
    private Coordinate coordFinal;
    
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody String  test() {
    	return "Please try using POST ;)";
    }
	
	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody
	String restRouting(@RequestBody String payload){
		if (gh == null) {
            gh = new GraphHopperWeb();
            gh.load(GRAPHHOPPER_URL);
        }

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
			route = gh.route(createGHRequest(startPointValue, targetPointValue));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Response
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
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
		
		// Get Instructions
		Iterator<Instruction> instructions = route.getInstructions().iterator();
		int i = 0;
        double accTime = 0.0;
        double accDistance = 0.0;
        int size = route.getInstructions().getSize();

        Geometry startEdge = null;
        Geometry targetEdge = null;
        while (instructions.hasNext() && i < size - 1){
        	Instruction instruction = instructions.next();
        	Step step = new Step(doc);
        	// order
        	step.setOrder(Integer.toString(i));
        	// name
        	String name = instruction.getName();
        	step.setName(name);
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
        	// edges
        	PointList points = instruction.getPoints();
        	List<EdgeStep> edges_list = step.getEdges();
        	Geometry geom = null;
        	Geometry geomUnion;
        	GHPoint p1;
            GHPoint p2;
        	for(int j=0; j<points.getSize()-1; j++){
        		EdgeStep e = new EdgeStep(doc);
        		e.setId(Integer.toString(i) + Integer.toString(j) + '0');
        		p1 = points.toGHPoint(j);
                p2 = points.toGHPoint(j + 1);
        		try {
					geom = toLineString(p1, p2, srs);
				} catch (MismatchedDimensionException | FactoryException
						| TransformException e1) {
					e1.printStackTrace();
				}
        		e.setGeom(geom);
        		edges_list.add(e);
        	}
        	
        	// targetNode
            step.setTargetNode(Integer.toString(i+1));
            //time
            accTime += instruction.getTime() / 1000;
            step.setTime(Double.toString(accTime));
            //distance
            accDistance += instruction.getDistance();
            step.setDistance(Double.toString(accDistance));
        	
            // Doc element to return 
        	Element stepElement = doc.createElement("step");
        	stepElement.setAttribute("order", step.getOrder());
        	stepElement.appendChild(step.getName());
        	stepElement.appendChild(step.getType());
        	stepElement.appendChild(step.getStartNode());
        	List<EdgeStep> edges = step.getEdges();
        	for(int j=0; j<edges.size(); j++){
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
		
		return toString(doc);
	}
	
	private String[] getCoordinates(String tagName, Document document){
		String[] value = new String[2];
		NodeList points = document.getElementsByTagName(tagName);
		for (int i = 0; i < points.getLength(); ++i){
			Element sp = (Element) points.item(i);
			NodeList point = sp.getElementsByTagName("Point");
			for (int j = 0; j < point.getLength(); ++j){
				Node p = (Node) point.item(j);
				value[0] = p.getAttributes().getNamedItem("srsName").getTextContent();
			}
			NodeList coordinates = sp.getElementsByTagName("coordinates");
			for (int j = 0; j < coordinates.getLength(); ++j){
				Node coordinate = (Node) coordinates.item(j);
				value[1] = coordinate.getTextContent();
			}
		}
		return value;
	}
	
	private String[] getPoint(String pointValue){
		String[] point = new String[2];
		if(pointValue.indexOf(",") > -1){
			point = pointValue.split(",");
		}
		return point;
	}
	
	private GHRequest createGHRequest(String[] origin, String[] target) throws MismatchedDimensionException, FactoryException, TransformException {
        GHRequest request = new GHRequest(toGHPlace(origin), toGHPlace(target));
        request.setLocale(this.LOCALE);

        return request;
    }
	
	private GHPlace toGHPlace(String[] point) throws FactoryException, MismatchedDimensionException, TransformException {

		String srs = point[0];
		String coordinate = point[1];
		String[] pointValue = getPoint(coordinate);
        Coordinate c = new Coordinate(Double.valueOf(pointValue[1]), Double.valueOf(pointValue[0]));
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
        
        CoordinateReferenceSystem sourceCRS = CRS.decode(srs);
        CoordinateReferenceSystem targetCRS = CRS.decode(this.SRS);
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        Geometry targetGeometry = JTS.transform(geometry, transform);

        com.vividsolutions.jts.geom.Point jtsPoint = targetGeometry.getCentroid();
        GHPlace gHPlace = new GHPlace(jtsPoint.getX(), jtsPoint.getY());

        return gHPlace;
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
	
	private Element getHeader(Document doc, String name, String value){
		Element header = doc.createElement("header");
		Element nameLabel = doc.createElement("name");
		nameLabel.setTextContent(name);
		Element valueLabel = doc.createElement("value");
		valueLabel.setTextContent(value);
		header.appendChild(nameLabel);
		header.appendChild(valueLabel);
		
		return header;
	} 
	
	private Element getPoint(Document doc, String label, Point p){
		Element point = doc.createElement(label + "Point");
		Element edge = doc.createElement("edge");
		edge.appendChild(p.getId());
		edge.appendChild(p.getDistFrom());
		edge.appendChild(p.getDistTo());
		edge.appendChild(p.getDirection());
		point.appendChild(edge);
		
		return point;
	}
	
	private Geometry toLineString(GHPoint p1, GHPoint p2, String srs) throws FactoryException, MismatchedDimensionException, TransformException {
        Coordinate[] coord = new Coordinate[2];
        Coordinate c1 = new Coordinate(p1.getLon(), p1.getLat());
        Coordinate c2 = new Coordinate(p2.getLon(), p2.getLat());
        coord[0] = c1;
        coord[1] = c2;
        CoordinateSequence coorSeq = new CoordinateArraySequence(coord);

        GeometryFactory geometryFact = new GeometryFactory();
        geometryFact.createLineString(coorSeq);

        Geometry geometry = new com.vividsolutions.jts.geom.LineString(coorSeq, geometryFact);

        CoordinateReferenceSystem sourceCRS = CRS.decode(this.SRS);
        CoordinateReferenceSystem targetCRS = CRS.decode(srs);
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        Geometry edge = JTS.transform(geometry, transform);

        return edge;
    }
	
	private class Point {
		
		private Element id;
		private Element distFrom;
		private Element distTo;
		private Element direction;
		
		public Point(Document doc, String id, String distFrom, String distTo, String direction){
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
	
	private class Step{
		
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
		
		public Step(Document doc){
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
		
		public EdgeStep(Document doc){
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
		
		public NodePoint(Document doc){
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
